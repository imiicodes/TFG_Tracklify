package com.mycompany.tracklify.controllers;

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
 * <p>Gestiona la autenticación del usuario y redirige a la vista
 * correspondiente según su rol:
 * <ul>
 *   <li>Rol 1 (usuario) → {@code main_view.fxml}</li>
 *   <li>Rol 2 (administrador) → {@code admin_view.fxml}</li>
 * </ul>
 * </p>
 *
 * <p>Una vez autenticado, el usuario se almacena en {@link SessionManager}
 * para que esté disponible en toda la aplicación.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see UsuarioDAO
 * @see SessionManager
 */
public class LoginController {

    /** Campo de texto para introducir el correo electrónico. */
    @FXML
    private TextField campoEmail;

    /** Campo de texto para introducir la contraseña (oculta). */
    @FXML
    private PasswordField campoPassword;

    /** Etiqueta para mostrar mensajes de error o estado al usuario. */
    @FXML
    private Label labelMensaje;

    /** DAO para realizar operaciones de autenticación sobre la BD. */
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Gestiona el evento de inicio de sesión.
     *
     * <p>Recoge las credenciales del formulario, las valida y,
     * si son correctas, guarda el usuario en {@link SessionManager}
     * y navega a la vista correspondiente según su rol.</p>
     *
     * @param event el evento de acción generado al pulsar el botón "Iniciar sesión"
     */
    @FXML
    public void iniciarSesion(ActionEvent event) {

        String email = campoEmail.getText().trim();
        String password = campoPassword.getText();

        // Validación básica: campos no vacíos
        if (email.isEmpty() || password.isEmpty()) {
            labelMensaje.setText("Por favor rellena todos los campos.");
            return;
        }

        // Intento de autenticación en la base de datos
        Usuario usuario = usuarioDAO.login(email, password);

        if (usuario != null) {
            // Guardamos el usuario autenticado en la sesión global
            SessionManager.getInstancia().setUsuarioActual(usuario);

            try {
                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                // Redirigimos según el rol del usuario autenticado
                if (SessionManager.getInstancia().esAdministrador()) {
                    // Rol 2: redirige al panel de administración
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/admin_view.fxml")
                    );
                    stage.setScene(new Scene(loader.load()));
                } else {
                    // Rol 1: redirige al dashboard de usuario
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/main_view.fxml")
                    );
                    stage.setScene(new Scene(loader.load()));
                }

                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
                labelMensaje.setText("Error al cargar la vista.");
            }

        } else {
            // Credenciales incorrectas: mostramos mensaje de error
            labelMensaje.setText("Credenciales incorrectas ❌");
        }
    }

    /**
     * Navega de vuelta a la pantalla de inicio (landing).
     *
     * @param event el evento de acción generado al pulsar "← Volver"
     * @throws Exception si el archivo FXML no se encuentra o no se puede cargar
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