package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.EstadisticaDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
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
 * <p>Accesible únicamente para usuarios con {@code rol_id = 2} (administrador).
 * Proporciona las siguientes funcionalidades:</p>
 * <ul>
 *   <li>Visualización de todos los usuarios registrados</li>
 *   <li>Cambio de rol de cualquier usuario</li>
 *   <li>Eliminación de usuarios del sistema</li>
 *   <li>Acceso a estadísticas globales del sistema</li>
 * </ul>
 *
 * <p>Si un usuario sin permisos de administrador intenta acceder a esta vista,
 * debe redirigirse al dashboard normal desde {@link LoginController}.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see UsuarioDAO
 * @see SessionManager
 */
public class AdminViewController implements Initializable {

    /** Etiqueta con el nombre del administrador autenticado. */
    @FXML
    private Label labelAdminNombre;

    /** Etiqueta que muestra el total de usuarios registrados en el sistema. */
    @FXML
    private Label labelTotalUsuarios;

    /** Etiqueta que muestra el número de administradores activos. */
    @FXML
    private Label labelTotalAdmins;

    /** Tabla que lista todos los usuarios del sistema. */
    @FXML
    private TableView<Usuario> tablaUsuarios;

    /** Columna de la tabla: identificador único del usuario. */
    @FXML
    private TableColumn<Usuario, Integer> colId;

    /** Columna de la tabla: nombre del usuario. */
    @FXML
    private TableColumn<Usuario, String> colNombre;

    /** Columna de la tabla: email del usuario. */
    @FXML
    private TableColumn<Usuario, String> colEmail;

    /** Columna de la tabla: rol actual del usuario (1=usuario, 2=admin). */
    @FXML
    private TableColumn<Usuario, Integer> colRol;

    /** DAO para realizar operaciones sobre la tabla de usuarios. */
    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** DAO para acceder a estadísticas globales del sistema. */
    private EstadisticaDAO estadisticaDAO = new EstadisticaDAO();
    /** Campo de texto para el nombre del nuevo usuario. */
    @FXML
    private TextField campoNuevoNombre;

    /** Campo de texto para el email del nuevo usuario. */
    @FXML
    private TextField campoNuevoEmail;

    /** Campo de contraseña para el nuevo usuario. */
    @FXML    private PasswordField campoNuevoPassword;

    /** Etiqueta para mostrar mensajes de éxito o error al admin. */
    @FXML
    private Label labelMensajeAdmin;
    /**
     * Inicializa el panel de administración al cargar la vista.
     *
     * <p>Configura las columnas de la tabla, muestra el nombre del administrador
     * y carga los datos de usuarios y estadísticas del sistema.</p>
     *
     * @param url            la URL del recurso FXML
     * @param resourceBundle el paquete de recursos de internacionalización
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Obtenemos el administrador actualmente autenticado
        Usuario admin = SessionManager.getInstancia().getUsuarioActual();
        if (admin != null) {
            labelAdminNombre.setText("Administrador: " + admin.getNombreUsuario());
        }

        // Configuramos las columnas de la TableView con los atributos del modelo
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailUsuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rolId"));

        // Cargamos los usuarios y las estadísticas globales
        cargarUsuarios();
        cargarEstadisticasGlobales();
    }

    /**
     * Carga todos los usuarios del sistema en la tabla.
     *
     * <p>Consulta {@link UsuarioDAO#obtenerTodos()} y actualiza
     * la {@link TableView} con los resultados.</p>
     */
    private void cargarUsuarios() {

        List<Usuario> usuarios = usuarioDAO.obtenerTodos();
        ObservableList<Usuario> lista = FXCollections.observableArrayList(usuarios);
        tablaUsuarios.setItems(lista);
    }

    /**
     * Actualiza las etiquetas de estadísticas globales del sistema.
     *
     * <p>Muestra el total de usuarios registrados y cuántos tienen rol de administrador.</p>
     */
    private void cargarEstadisticasGlobales() {

        // Total de usuarios en el sistema
        List<Usuario> todos = usuarioDAO.obtenerTodos();
        labelTotalUsuarios.setText(String.valueOf(todos.size()));

        // Total de administradores (rol_id = 2)
        List<Usuario> admins = usuarioDAO.obtenerPorRol(2);
        labelTotalAdmins.setText(String.valueOf(admins.size()));
    }

    /**
     * Asciende a administrador al usuario seleccionado en la tabla.
     *
     * <p>Cambia el {@code rol_id} del usuario seleccionado a {@code 2} (administrador).
     * Muestra un aviso si no hay ningún usuario seleccionado.</p>
     *
     * @param event el evento de acción generado al pulsar "Hacer admin"
     */
    @FXML
    public void hacerAdmin(ActionEvent event) {

        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor selecciona un usuario de la tabla.");
            return;
        }

        if (seleccionado.getRolId() == 2) {
            mostrarAlerta("Sin cambios", "Este usuario ya es administrador.");
            return;
        }

        // Cambiamos el rol a administrador (rol_id = 2)
        boolean exito = usuarioDAO.cambiarRol(seleccionado.getIdUsuario(), 2);

        if (exito) {
            mostrarAlerta("Éxito", "El usuario ahora es administrador.");
            // Recargamos la tabla para reflejar el cambio
            cargarUsuarios();
            cargarEstadisticasGlobales();
        } else {
            mostrarAlerta("Error", "No se pudo cambiar el rol.");
        }
    }

    /**
     * Revoca los privilegios de administrador del usuario seleccionado.
     *
     * <p>Cambia el {@code rol_id} del usuario seleccionado a {@code 1} (usuario estándar).
     * Impide que un administrador se quite sus propios privilegios.</p>
     *
     * @param event el evento de acción generado al pulsar "Quitar admin"
     */
    @FXML
    public void quitarAdmin(ActionEvent event) {

        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor selecciona un usuario de la tabla.");
            return;
        }

        // Evitamos que el admin se quite sus propios privilegios
        if (seleccionado.getIdUsuario() == adminActual.getIdUsuario()) {
            mostrarAlerta("Acción no permitida", "No puedes quitarte tus propios privilegios.");
            return;
        }

        // Cambiamos el rol a usuario estándar (rol_id = 1)
        boolean exito = usuarioDAO.cambiarRol(seleccionado.getIdUsuario(), 1);

        if (exito) {
            mostrarAlerta("Éxito", "Se han retirado los privilegios de administrador.");
            cargarUsuarios();
            cargarEstadisticasGlobales();
        } else {
            mostrarAlerta("Error", "No se pudo cambiar el rol.");
        }
    }

    /**
     * Elimina del sistema al usuario seleccionado en la tabla.
     *
     * <p>Solicita confirmación antes de eliminar. Impide que un
     * administrador elimine su propia cuenta.</p>
     *
     * @param event el evento de acción generado al pulsar "Eliminar usuario"
     */
    @FXML
    public void eliminarUsuario(ActionEvent event) {

        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor selecciona un usuario de la tabla.");
            return;
        }

        // Impedimos que el admin se elimine a sí mismo
        if (seleccionado.getIdUsuario() == adminActual.getIdUsuario()) {
            mostrarAlerta("Acción no permitida", "No puedes eliminar tu propia cuenta.");
            return;
        }

        // Pedimos confirmación antes de eliminar
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar eliminación");
        confirmacion.setHeaderText("¿Eliminar a " + seleccionado.getNombreUsuario() + "?");
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
    /**
     * Crea un nuevo usuario desde el panel de administración.
     *
     * <p>El administrador puede crear usuarios con rol estándar (rol_id = 1)
     * o administrador (rol_id = 2) directamente desde este formulario.
     * Valida que todos los campos estén rellenos y que el email sea válido
     * antes de intentar el registro en la base de datos.</p>
     *  
     * @param event el evento de acción generado al pulsar "+ Crear usuario"
     */
    @FXML
    public void crearUsuario(ActionEvent event) {

        String nombre   = campoNuevoNombre.getText().trim();
        String email    = campoNuevoEmail.getText().trim();
        String password = campoNuevoPassword.getText();

        // Validación: todos los campos obligatorios
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty()) {
            labelMensajeAdmin.setStyle("-fx-text-fill: #B5368A;");
            labelMensajeAdmin.setText("Por favor rellena todos los campos.");
            return;
        }

        // Validación: formato básico de email
        if (!email.contains("@") || !email.contains(".")) {
            labelMensajeAdmin.setStyle("-fx-text-fill: #B5368A;");
            labelMensajeAdmin.setText("El correo electrónico no es válido.");
            return;
        }

        // Validación: contraseña mínima
        if (password.length() < 6) {
            labelMensajeAdmin.setStyle("-fx-text-fill: #B5368A;");
            labelMensajeAdmin.setText("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        // Construimos el nuevo usuario con rol estándar por defecto
        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreUsuario(nombre);
        nuevoUsuario.setEmailUsuario(email);
        nuevoUsuario.setPasswordUsuario(BCrypt.hashpw(password, BCrypt.gensalt(10)));
        nuevoUsuario.setRolId(1); // rol_id = 1 → usuario estándar

        boolean exito = usuarioDAO.registrar(nuevoUsuario);

        if (exito) {
            // Mostramos confirmación en verde
            labelMensajeAdmin.setStyle("-fx-text-fill: #3A8F5F;");
            labelMensajeAdmin.setText("✔ Usuario '" + nombre + "' creado correctamente.");

            // Limpiamos el formulario
            campoNuevoNombre.clear();
            campoNuevoEmail.clear();
            campoNuevoPassword.clear();

            // Recargamos la tabla y estadísticas
            cargarUsuarios();
            cargarEstadisticasGlobales();

        } else {
            labelMensajeAdmin.setStyle("-fx-text-fill: #B5368A;");
            labelMensajeAdmin.setText("❌ Ese email ya está registrado en el sistema.");
        }
    }
    /**
     * Cierra la sesión del administrador y regresa a la pantalla de inicio.
     *
     * @param event el evento de acción generado al pulsar "Cerrar sesión"
     * @throws Exception si el archivo FXML no se puede cargar
     */
    @FXML
    public void cerrarSesion(ActionEvent event) throws Exception {

        // Limpiamos la sesión activa del administrador
        SessionManager.getInstancia().cerrarSesion();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/landing_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }

    /**
     * Muestra una ventana de diálogo informativa al usuario.
     *
     * <p>Método auxiliar para evitar duplicar código de alertas en los distintos métodos.</p>
     *
     * @param titulo  el título de la ventana de alerta
     * @param mensaje el mensaje descriptivo que se muestra al usuario
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}