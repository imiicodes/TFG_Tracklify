package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.OnboardingDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.BloqueoIpService;
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
 * <ul>
 *   <li>Rol administrador ({@code rol_id = 1}) → {@code admin_view.fxml}</li>
 *   <li>Usuario con {@code onboarding_completado = 0} → {@code onboarding_view.fxml}</li>
 *   <li>Usuario con {@code onboarding_completado = 1} → {@code main_view.fxml}</li>
 * </ul>
 *
 * @author Tracklify
 * @version 1.0
 * @see UsuarioDAO
 * @see OnboardingDAO
 * @see SessionManager
 * @see BloqueoIpService
 */
public class LoginController {

    /** IP del cliente; temporalmente fija hasta disponer de la IP real de la petición. */
    private static final String IP_LOGIN = "127.0.0.1";

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

    /** DAO para autenticación contra la tabla {@code usuarios}. */
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** DAO para comprobar el estado del onboarding del usuario. */
    private OnboardingDAO onboardingDAO = new OnboardingDAO();

    /** Control de bloqueo por intentos fallidos desde una misma IP. */
    private BloqueoIpService bloqueoIpService = new BloqueoIpService();

    /**
     * Gestiona el inicio de sesión al pulsar "Iniciar sesión".
     *
     * <p>Valida los campos, autentica al usuario y navega a la vista correspondiente
     * según rol y estado del onboarding.</p>
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

        if (bloqueoIpService.estaBlockeada(IP_LOGIN)) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Demasiados intentos fallidos. Espera 1 minuto.");
            usuarioDAO.registrarIntento(email, IP_LOGIN, "CUENTA_BLOQUEADA");
            return;
        }

        Usuario usuario = usuarioDAO.login(email, password);

        if (usuario == null) {
            bloqueoIpService.registrarFallo(IP_LOGIN);
            usuarioDAO.registrarIntento(email, IP_LOGIN, "FALLO_PASSWORD");
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Credenciales incorrectas. Inténtalo de nuevo.");
            return;
        }

        usuarioDAO.registrarIntento(email, IP_LOGIN, "EXITO");
        bloqueoIpService.registrarExito(IP_LOGIN);

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
            double w = stage.getWidth();
            double h = stage.getHeight();
            stage.setScene(new Scene(loader.load(), w, h));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Error al cargar la vista.");
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
        double w = stage.getWidth();
        double h = stage.getHeight();
        stage.setScene(new Scene(loader.load(), w, h));
    }
    @FXML
    void abrirSoporte() {
        try {
            java.awt.Desktop.getDesktop().mail(
                new java.net.URI("mailto:tumail@gmail.com?subject=Soporte Tracklify")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
