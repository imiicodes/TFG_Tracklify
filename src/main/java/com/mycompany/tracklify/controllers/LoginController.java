package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.OnboardingDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
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
 *
 * <ul>
 *   <li>Rol administrador ({@code rol_id = 2}) → {@code admin_view.fxml}</li>
 *   <li>Usuario con {@code onboarding_completado = 0} → {@code onboarding_view.fxml}</li>
 *   <li>Usuario con {@code onboarding_completado = 1} → {@code main_view.fxml}</li>
 * </ul>
 *
 * <p>El usuario autenticado se almacena en {@link SessionManager} para que
 * esté disponible globalmente en el resto de controladores sin necesidad
 * de pasarlo como parámetro entre vistas.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see UsuarioDAO
 * @see OnboardingDAO
 * @see SessionManager
 */
public class LoginController {

    /**
     * Campo de texto donde el usuario introduce su correo electrónico.
     * Vinculado al elemento {@code fx:id="campoEmail"} del FXML.
     */
    @FXML private TextField campoEmail;

    /**
     * Campo de contraseña donde el usuario introduce su clave de acceso.
     * El texto se enmascara automáticamente por ser {@link PasswordField}.
     * Vinculado al elemento {@code fx:id="campoPassword"} del FXML.
     */
    @FXML private PasswordField campoPassword;

    /**
     * Etiqueta informativa que muestra mensajes de error o estado al usuario
     * (campos vacíos, credenciales incorrectas, error al cargar vista).
     * Vinculada al elemento {@code fx:id="labelMensaje"} del FXML.
     */
    @FXML private Label labelMensaje;

    /**
     * DAO para realizar la consulta de autenticación contra la tabla
     * {@code usuarios} de la base de datos.
     */
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * DAO para comprobar si el usuario autenticado ya ha completado
     * el proceso de onboarding ({@code onboarding_completado}).
     */
    private OnboardingDAO onboardingDAO = new OnboardingDAO();

    /**
     * Gestiona el evento de inicio de sesión al pulsar el botón "Iniciar sesión".
     *
     * <p>El proceso sigue estos pasos en orden:</p>
     * <ol>
     *   <li>Valida que los campos de email y contraseña no estén vacíos.</li>
     *   <li>Consulta la BD mediante {@link UsuarioDAO#login(String, String)}.</li>
     *   <li>Si las credenciales son correctas, guarda el usuario en {@link SessionManager}.</li>
     *   <li>Determina la vista de destino según el rol y el estado del onboarding.</li>
     *   <li>Carga la vista destino y la muestra en el mismo {@link Stage}.</li>
     * </ol>
     *
     * <p>Si las credenciales son incorrectas o algún campo está vacío,
     * se muestra el mensaje correspondiente en {@code labelMensaje}
     * sin navegar a ninguna otra vista.</p>
     *
     * @param event el evento de acción generado al pulsar el botón "Iniciar sesión"
     */
    @FXML
    public void iniciarSesion(ActionEvent event) {

        String email    = campoEmail.getText().trim();
        String password = campoPassword.getText();

        // Validación básica: ningún campo puede estar vacío
        if (email.isEmpty() || password.isEmpty()) {
            labelMensaje.setText("Por favor rellena todos los campos.");
            return;
        }

        // Consultamos la BD con las credenciales introducidas
        Usuario usuario = usuarioDAO.login(email, password);

        if (usuario != null) {

            // Guardamos el usuario autenticado en la sesión global
            SessionManager.getInstancia().setUsuarioActual(usuario);

            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                String destino;

                if (SessionManager.getInstancia().esAdministrador()) {
                    // Rol administrador: accede al panel de gestión del sistema
                    destino = "/fxml/admin_view.fxml";

                } else if (!onboardingDAO.haCompletadoOnboarding(usuario.getIdUsuario())) {
                    // Usuario nuevo: onboarding_completado = 0, mostramos el onboarding
                    destino = "/fxml/onboarding_view.fxml";

                } else {
                    // Usuario existente: onboarding ya completado, accede al dashboard
                    destino = "/fxml/main_view.fxml";
                }

                // Cargamos y mostramos la vista de destino
                FXMLLoader loader = new FXMLLoader(getClass().getResource(destino));
                stage.setScene(new Scene(loader.load()));
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                labelMensaje.setText("Error al cargar la vista.");
            }

        } else {
            // Credenciales incorrectas: informamos al usuario sin revelar cuál es el error
            labelMensaje.setText("Credenciales incorrectas. Inténtalo de nuevo.");
        }
    }

    /**
     * Navega de vuelta a la pantalla de inicio (landing) al pulsar "← Volver".
     *
     * <p>No cierra la sesión porque en este punto el usuario aún
     * no ha iniciado sesión. Simplemente descarga el FXML de la landing
     * y lo establece como escena del {@link Stage} actual.</p>
     *
     * @param event el evento de acción generado al pulsar el botón "← Volver"
     * @throws Exception si el archivo {@code landing_view.fxml} no se encuentra
     *                   o no puede cargarse correctamente
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