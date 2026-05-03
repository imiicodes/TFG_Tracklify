package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.PerfilDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Perfil;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Controlador del panel de administración ({@code admin_view.fxml}).
 *
 * <p>Accesible únicamente para usuarios con {@code rol_id = 1} (administrador).</p>
 *
 * @author Tracklify
 * @version 1.0
 */
public class AdminViewController implements Initializable {

    @FXML
    private Label labelAdminNombre;

    @FXML
    private Label labelTotalUsuarios;

    @FXML
    private Label labelTotalAdmins;

    @FXML
    private TableView<Usuario> tablaUsuarios;

    @FXML
    private TableColumn<Usuario, Integer> colId;

    @FXML
    private TableColumn<Usuario, String> colNombre;

    @FXML
    private TableColumn<Usuario, String> colEmail;

    @FXML
    private TableColumn<Usuario, Integer> colRol;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    private PerfilDAO perfilDAO = new PerfilDAO();

    @FXML
    private TextField campoNuevoNombre;

    @FXML
    private TextField campoNuevoEmail;

    @FXML
    private PasswordField campoNuevoPassword;

    @FXML
    private Label labelMensajeAdmin;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Usuario admin = SessionManager.getInstancia().getUsuarioActual();
        if (admin != null) {
            Perfil perfil = perfilDAO.obtenerPorUsuario(admin.getIdUsuario());
            String nombre = perfil != null && perfil.getNombreUsuario() != null && !perfil.getNombreUsuario().isEmpty()
                ? perfil.getNombreUsuario()
                : admin.getEmailUsuario();
            labelAdminNombre.setText("Administrador: " + nombre);
        }

        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(cellData -> {
            Perfil p = perfilDAO.obtenerPorUsuario(cellData.getValue().getIdUsuario());
            String texto = p != null && p.getNombreUsuario() != null ? p.getNombreUsuario() : "";
            return new ReadOnlyStringWrapper(texto);
        });
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailUsuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rolId"));

        cargarUsuarios();
        cargarEstadisticasGlobales();
    }

    private void cargarUsuarios() {

        List<Usuario> usuarios = usuarioDAO.obtenerTodos();
        ObservableList<Usuario> lista = FXCollections.observableArrayList(usuarios);
        tablaUsuarios.setItems(lista);
    }

    private void cargarEstadisticasGlobales() {

        List<Usuario> todos = usuarioDAO.obtenerTodos();
        labelTotalUsuarios.setText(String.valueOf(todos.size()));

        List<Usuario> admins = usuarioDAO.obtenerPorRol(1);
        labelTotalAdmins.setText(String.valueOf(admins.size()));
    }

    @FXML
    public void hacerAdmin(ActionEvent event) {

        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor selecciona un usuario de la tabla.");
            return;
        }

        if (seleccionado.getRolId() == 1) {
            mostrarAlerta("Sin cambios", "Este usuario ya es administrador.");
            return;
        }

        boolean exito = usuarioDAO.cambiarRol(seleccionado.getIdUsuario(), 1);

        if (exito) {
            mostrarAlerta("Éxito", "El usuario ahora es administrador.");
            cargarUsuarios();
            cargarEstadisticasGlobales();
        } else {
            mostrarAlerta("Error", "No se pudo cambiar el rol.");
        }
    }

    @FXML
    public void quitarAdmin(ActionEvent event) {

        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor selecciona un usuario de la tabla.");
            return;
        }

        if (seleccionado.getIdUsuario() == adminActual.getIdUsuario()) {
            mostrarAlerta("Acción no permitida", "No puedes quitarte tus propios privilegios.");
            return;
        }

        boolean exito = usuarioDAO.cambiarRol(seleccionado.getIdUsuario(), 2);

        if (exito) {
            mostrarAlerta("Éxito", "Se han retirado los privilegios de administrador.");
            cargarUsuarios();
            cargarEstadisticasGlobales();
        } else {
            mostrarAlerta("Error", "No se pudo cambiar el rol.");
        }
    }

    @FXML
    public void eliminarUsuario(ActionEvent event) {

        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor selecciona un usuario de la tabla.");
            return;
        }

        if (seleccionado.getIdUsuario() == adminActual.getIdUsuario()) {
            mostrarAlerta("Acción no permitida", "No puedes eliminar tu propia cuenta.");
            return;
        }

        Perfil perfilSel = perfilDAO.obtenerPorUsuario(seleccionado.getIdUsuario());
        String nombreMostrar = perfilSel != null && perfilSel.getNombreUsuario() != null && !perfilSel.getNombreUsuario().isEmpty()
            ? perfilSel.getNombreUsuario()
            : seleccionado.getEmailUsuario();

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar a " + nombreMostrar + "?");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
            boolean exito = usuarioDAO.eliminar(seleccionado.getIdUsuario());

            if (exito) {
                mostrarAlerta("Éxito", "Usuario eliminado correctamente.");
                cargarUsuarios();
                cargarEstadisticasGlobales();
            } else {
                mostrarAlerta("Error", "No se pudo eliminar el usuario.");
            }
        }
    }

    @FXML
    public void crearUsuario(ActionEvent event) {

        String nombre = campoNuevoNombre.getText().trim();
        String email = campoNuevoEmail.getText().trim();
        String password = campoNuevoPassword.getText();

        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            labelMensajeAdmin.setStyle("-fx-text-fill: #B5368A;");
            labelMensajeAdmin.setText("Por favor rellena todos los campos.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            labelMensajeAdmin.setStyle("-fx-text-fill: #B5368A;");
            labelMensajeAdmin.setText("El correo electrónico no es válido.");
            return;
        }

        if (password.length() < 6) {
            labelMensajeAdmin.setStyle("-fx-text-fill: #B5368A;");
            labelMensajeAdmin.setText("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setEmailUsuario(email);
        nuevoUsuario.setPasswordUsuario(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        nuevoUsuario.setRolId(2);

        int id = usuarioDAO.registrar(nuevoUsuario, nombre);

        if (id > 0) {
            labelMensajeAdmin.setStyle("-fx-text-fill: #3A8F5F;");
            labelMensajeAdmin.setText("✔ Usuario '" + nombre + "' creado correctamente.");

            campoNuevoNombre.clear();
            campoNuevoEmail.clear();
            campoNuevoPassword.clear();

            cargarUsuarios();
            cargarEstadisticasGlobales();

        } else {
            labelMensajeAdmin.setStyle("-fx-text-fill: #B5368A;");
            labelMensajeAdmin.setText("❌ Ese email ya está registrado en el sistema.");
        }
    }

    @FXML
    public void cerrarSesion(ActionEvent event) throws Exception {

        SessionManager.getInstancia().cerrarSesion();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/landing_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
