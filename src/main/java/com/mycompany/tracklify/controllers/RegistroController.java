package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.PasswordUtils;
import org.mindrot.jbcrypt.BCrypt;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controlador de la vista de registro ({@code registro_view.fxml}).
 *
 * <p>Gestiona el formulario de creación de cuenta de nuevos usuarios.
 * Tras un registro exitoso, muestra un mensaje de confirmación y
 * navega automáticamente al login usando {@link PauseTransition}
 * (hilo de JavaFX) en lugar de un {@code Thread} manual, evitando
 * que la JVM termine antes de completar la navegación.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see UsuarioDAO
 */
public class RegistroController {

    /** Campo para el nombre completo del nuevo usuario. */
    @FXML private TextField campoNombre;

    /** Campo para el email del nuevo usuario. */
    @FXML private TextField campoEmail;

    /** Campo para la contraseña. */
    @FXML private PasswordField campoPassword;
    
    /** Campo para confirmar la contraseña. */
    @FXML private PasswordField campoConfirmar;

    /** Etiqueta para mostrar mensajes de error o éxito. */
    @FXML private Label labelMensaje;

    /** DAO para persistir el nuevo usuario en la BD. */
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /**
     * Procesa el formulario de registro al pulsar "Crear cuenta".
     *
     * <p>Valida los campos, crea el usuario en BD y navega al login
     * usando {@link PauseTransition} para la pausa visual, que opera
     * en el hilo de JavaFX y no cierra la aplicación prematuramente.</p>
     *
     * @param event evento generado al pulsar el botón "Crear cuenta"
     */
    @FXML
    public void registrar(ActionEvent event) {

        String nombre    = campoNombre.getText().trim();
        String email     = campoEmail.getText().trim();
        String password  = campoPassword.getText();
        String confirmar = campoConfirmar.getText();

        // Validación: campos obligatorios
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Por favor rellena todos los campos.");
            return;
        }

        // Validación: longitud mínima de contraseña
        if (password.length() < 6) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // Validación: las contraseñas coinciden
        if (!password.equals(confirmar)) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Las contraseñas no coinciden.");
            return;
        }

        // Validación: formato básico de email
        if (!email.contains("@") || !email.contains(".")) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("El correo electrónico no es válido.");
            return;
        }

        // Construimos el usuario con rol estándar (rol_id = 1)
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre);
        usuario.setEmailUsuario(email);
        //usuario.setPasswordUsuario(password); sin hash
        usuario.setPasswordUsuario(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(10)));
        usuario.setRolId(1);

        boolean exito = usuarioDAO.registrar(usuario);

        if (exito) {
            labelMensaje.setStyle("-fx-text-fill: #3A8F5F;");
            labelMensaje.setText("¡Cuenta creada! Redirigiendo al login...");

            // Capturamos el Stage ANTES de la pausa
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            // PauseTransition opera en el hilo de JavaFX — nunca mata la app
            PauseTransition pausa = new PauseTransition(Duration.seconds(1.2));
            pausa.setOnFinished(e -> {
                try {
                    FXMLLoader loader = new FXMLLoader(
                        getClass().getResource("/fxml/login_view.fxml")
                    );
                    stage.setScene(new Scene(loader.load()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            pausa.play();

        } else {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Ese correo ya está registrado.");
        }
    }

    /**
     * Navega de vuelta al login al pulsar "¿Ya tienes cuenta? Inicia sesión".
     *
     * @param event evento generado al pulsar el botón
     * @throws Exception si el FXML no se puede cargar
     */
    @FXML
    public void irALogin(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/login_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }
}