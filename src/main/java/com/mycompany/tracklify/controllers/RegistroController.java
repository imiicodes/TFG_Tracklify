package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import java.util.List;
import java.util.Locale;
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

    private static final List<String> DOMINIOS_VALIDOS = List.of(
        "gmail.com",
        "outlook.com",
        "hotmail.com",
        "hotmail.es",
        "yahoo.com",
        "yahoo.es",
        "icloud.com",
        "live.com",
        "live.es",
        "msn.com",
        "protonmail.com",
        "tutanota.com",
        "ufv.es"
    );

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

        int arroba = email.lastIndexOf('@');
        String dominio = arroba >= 0 && arroba < email.length() - 1
            ? email.substring(arroba + 1).trim().toLowerCase(Locale.ROOT)
            : "";
        if (dominio.isEmpty()) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("El correo electrónico no es válido.");
            return;
        }
        if (!esDominioValido(email)) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Por favor usa un correo de un proveedor conocido (Gmail, Outlook, etc.).");
            return;
        }

        if (usuarioDAO.existeEmail(email)) {
            labelMensaje.setStyle("-fx-text-fill: #B5368A;");
            labelMensaje.setText("Ese correo ya está registrado.");
            return;
        }

        Usuario usuario = new Usuario();
        usuario.setEmailUsuario(email);
        usuario.setPasswordUsuario(org.mindrot.jbcrypt.BCrypt.hashpw(password, org.mindrot.jbcrypt.BCrypt.gensalt(10)));
        usuario.setRolId(2);

        int idGenerado = usuarioDAO.registrar(usuario, nombre);

        if (idGenerado > 0) {
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
            labelMensaje.setText("No se pudo completar el registro. Inténtalo de nuevo o contacta con soporte.");
        }
    }

    /**
     * Comprueba que la parte dominio del correo esté en la lista de proveedores aceptados.
     *
     * @param email dirección completa (se usa la parte tras la última {@code @})
     * @return {@code true} si el dominio está permitido
     */
    private boolean esDominioValido(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at >= email.length() - 1) {
            return false;
        }
        String dominio = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return !dominio.isEmpty() && DOMINIOS_VALIDOS.contains(dominio);
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