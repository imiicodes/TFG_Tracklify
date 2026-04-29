package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.OnboardingDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.SessionManager;
import java.util.prefs.Preferences;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * Controlador de la vista de inicio de sesión ({@code login_view.fxml}).
 *
 * <p>Gestiona la autenticación del usuario contra la base de datos y,
 * una vez verificadas las credenciales, decide a qué vista navegar
 * en función del rol y del estado del onboarding:</p>
 * <ul>
 *   <li>Rol administrador ({@code rol_id = 2}) → {@code admin_view.fxml}</li>
 *   <li>Usuario con {@code onboarding_completado = 0} → {@code onboarding_view.fxml}</li>
 *   <li>Usuario con {@code onboarding_completado = 1} → {@code main_view.fxml}</li>
 * </ul>
 *
 * <p>Si el usuario marca "Recordar sesión", las credenciales se persisten
 * en las preferencias del SO mediante {@link Preferences} y se rellenan
 * automáticamente en el siguiente arranque.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see UsuarioDAO
 * @see OnboardingDAO
 * @see SessionManager
 */
public class LoginController {

    /**
     * Campo de texto para el correo electrónico.
     * Vinculado a {@code fx:id="campoEmail"} del FXML.
     */
    @FXML private TextField campoEmail;

    /**
     * Campo de contraseña enmascarado.
     * Vinculado a {@code fx:id="campoPassword"} del FXML.
     */
    @FXML private PasswordField campoPassword;

    /**
     * Etiqueta para mostrar mensajes de error o estado.
     * Vinculada a {@code fx:id="labelMensaje"} del FXML.
     */
    @FXML private Label labelMensaje;

    /**
     * Checkbox "Recordar sesión".
     * Vinculado a {@code fx:id="checkRecordar"} del FXML.
     */
    @FXML private CheckBox checkRecordar;

    /** Nodo de preferencias del SO para persistir las credenciales recordadas. */
    private static final Preferences PREFS =
        Preferences.userNodeForPackage(LoginController.class);

    /** Clave para guardar el email en preferencias. */
    private static final String PREF_EMAIL    = "recordar_email";

    /** Clave para guardar la contraseña en preferencias. */
    private static final String PREF_PASSWORD = "recordar_password";

    /** Clave para guardar el estado del checkbox en preferencias. */
    private static final String PREF_RECORDAR = "recordar_sesion";

    /** DAO para autenticación contra la tabla {@code usuarios}. */
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** DAO para comprobar el estado del onboarding del usuario. */
    private OnboardingDAO onboardingDAO = new OnboardingDAO();

    /**
     * Se ejecuta automáticamente al cargar la vista.
     *
     * <p>Si el usuario marcó "Recordar sesión" en un login anterior,
     * rellena los campos con sus credenciales guardadas y marca el checkbox.</p>
     */
    @FXML
    public void initialize() {
        boolean recordar = PREFS.getBoolean(PREF_RECORDAR, false);
        if (recordar) {
            campoEmail.setText(PREFS.get(PREF_EMAIL, ""));
            campoPassword.setText(PREFS.get(PREF_PASSWORD, ""));
            checkRecordar.setSelected(true);
        }
    }

    /**
     * Gestiona el inicio de sesión al pulsar "Iniciar sesión".
     *
     * <p>Valida los campos, gestiona las preferencias de recordar sesión,
     * autentica al usuario y navega a la vista correspondiente según rol
     * y estado del onboarding.</p>
     *
     * @param event evento generado al pulsar el botón
     */
    @FXML
    public void iniciarSesion(ActionEvent event) {

        String email    = campoEmail.getText().trim();
        String password = campoPassword.getText();

        // Validación: ningún campo puede estar vacío
        if (email.isEmpty() || password.isEmpty()) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Debe introducir su email y contraseña completos.");
            return;
        }

        // Gestionamos la preferencia de recordar sesión
        if (checkRecordar.isSelected()) {
            // Guardamos las credenciales en las preferencias del SO
            PREFS.put(PREF_EMAIL, email);
            PREFS.put(PREF_PASSWORD, password);
            PREFS.putBoolean(PREF_RECORDAR, true);
        } else {
            // Si desmarcó el checkbox, borramos los datos guardados
            PREFS.remove(PREF_EMAIL);
            PREFS.remove(PREF_PASSWORD);
            PREFS.putBoolean(PREF_RECORDAR, false);
        }

        // Autenticación contra la BD
        Usuario usuario = usuarioDAO.login(email, password);

        if (usuario != null) {

            // Guardamos el usuario en la sesión global
            SessionManager.getInstancia().setUsuarioActual(usuario);

            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                String destino;

                if (SessionManager.getInstancia().esAdministrador()) {
                    destino = "/fxml/admin_view.fxml";
                } else if (!onboardingDAO.haCompletadoOnboarding(usuario.getIdUsuario())) {
                    destino = "/fxml/onboarding_view.fxml";
                } else {
                    destino = "/fxml/main_view.fxml";
                }

                FXMLLoader loader = new FXMLLoader(getClass().getResource(destino));
                stage.setScene(new Scene(loader.load()));
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                labelMensaje.setStyle("-fx-text-fill: #B5368A;");
                labelMensaje.setText("Error al cargar la vista.");
            }

        } else {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Credenciales incorrectas. Inténtalo de nuevo.");
        }
    }

    /**
     * Navega de vuelta a la landing al pulsar "← Volver".
     *
     * @param event evento generado al pulsar el botón
     * @throws Exception si el FXML no se puede cargar
     */
    @FXML
    public void volverALanding(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/landing_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }
}