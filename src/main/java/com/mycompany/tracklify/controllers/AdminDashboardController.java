package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.BaneoUsuarioDAO;
import com.mycompany.tracklify.dao.BloqueoIpDAO;
import com.mycompany.tracklify.dao.PerfilDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.BaneoUsuario;
import com.mycompany.tracklify.models.BloqueoIpVigente;
import com.mycompany.tracklify.models.Perfil;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.BloqueoIpService;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Button;
import javafx.scene.control.cell.PropertyValueFactory;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Sección «Dashboard / Usuarios» del panel de administración ({@code admin/admin_dashboard.fxml}):
 * estadísticas rápidas, tabla de cuentas, creación de usuario, roles, baneos e IPs bloqueadas.
 *
 * @author Tracklify
 */
public class AdminDashboardController implements Initializable {

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

    @FXML
    private TableColumn<Usuario, Void> colAcciones;

    @FXML
    private TableView<BloqueoIpVigente> tablaIpsBloqueadas;

    @FXML
    private TableColumn<BloqueoIpVigente, String> colIpAddr;

    @FXML
    private TableColumn<BloqueoIpVigente, Integer> colIpIntentos;

    @FXML
    private TableColumn<BloqueoIpVigente, String> colIpUltIntento;

    @FXML
    private TableColumn<BloqueoIpVigente, String> colIpBloqueadaHasta;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    private final PerfilDAO perfilDAO = new PerfilDAO();

    private final BaneoUsuarioDAO baneoUsuarioDAO = new BaneoUsuarioDAO();

    private final BloqueoIpDAO bloqueoIpDAO = new BloqueoIpDAO();

    private final BloqueoIpService bloqueoIpService = new BloqueoIpService();

    @FXML
    private TextField campoNuevoNombre;

    @FXML
    private TextField campoNuevoEmail;

    @FXML
    private PasswordField campoNuevoPassword;

    @FXML
    private Label labelMensajeAdmin;

    /**
     * Configura tablas de usuarios e IPs bloqueadas y carga datos iniciales del panel.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle recursos de internacionalización; no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colId.setCellValueFactory(new PropertyValueFactory<>("idUsuario"));
        colNombre.setCellValueFactory(cellData -> {
            Perfil p = perfilDAO.obtenerPorUsuario(cellData.getValue().getIdUsuario());
            String texto = p != null && p.getNombreUsuario() != null ? p.getNombreUsuario() : "";
            return new ReadOnlyStringWrapper(texto);
        });
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailUsuario"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rolId"));
        configurarColumnaAcciones();

        colIpAddr.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colIpIntentos.setCellValueFactory(new PropertyValueFactory<>("intentosFallidos"));
        colIpUltIntento.setCellValueFactory(cd -> {
            var f = cd.getValue().getFechaUltIntento();
            return new ReadOnlyStringWrapper(f != null ? f.toString() : "—");
        });
        colIpBloqueadaHasta.setCellValueFactory(cd -> {
            var h = cd.getValue().getBloqueadaHasta();
            return new ReadOnlyStringWrapper(h != null ? h.toString() : "—");
        });

        cargarUsuarios();
        cargarEstadisticasGlobales();
        cargarIpsBloqueadas();
    }

    /**
     * Recarga la tabla de IPs con bloqueo temporal vigente.
     */
    private void cargarIpsBloqueadas() {
        List<BloqueoIpVigente> ips = bloqueoIpDAO.obtenerBloqueosVigentes();
        tablaIpsBloqueadas.setItems(FXCollections.observableArrayList(ips));
    }

    /**
     * Abre un diálogo para el motivo y registra un baneo permanente del usuario seleccionado,
     * desactivando su cuenta.
     *
     * @param event evento del botón
     */
    @FXML
    public void banearUsuario(ActionEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Selecciona un usuario en la tabla.");
            return;
        }
        if (adminActual == null) {
            return;
        }
        if (seleccionado.getIdUsuario() == adminActual.getIdUsuario()) {
            mostrarAlerta("Acción no permitida", "No puedes banear tu propia cuenta.");
            return;
        }
        if (baneoUsuarioDAO.estaActualmenteBaneado(seleccionado.getIdUsuario())) {
            mostrarAlerta("Sin cambios", "Este usuario ya tiene un baneo activo.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Banear usuario");
        dialog.setHeaderText("Indica el motivo del baneo (obligatorio).");
        dialog.setContentText("Motivo:");

        Optional<String> resultado = dialog.showAndWait();
        if (resultado.isEmpty()) {
            return;
        }
        String motivo = resultado.get().trim();
        if (motivo.isEmpty()) {
            mostrarAlerta("Motivo requerido", "Debes escribir un motivo para el baneo.");
            return;
        }

        boolean ok = baneoUsuarioDAO.banear(seleccionado.getIdUsuario(), adminActual.getIdUsuario(), motivo);
        if (ok) {
            mostrarAlerta("Éxito", "Usuario baneado y cuenta desactivada.");
            cargarUsuarios();
            cargarEstadisticasGlobales();
        } else {
            mostrarAlerta("Error", "No se pudo registrar el baneo.");
        }
    }

    /**
     * Localiza el baneo activo del usuario seleccionado y lo cierra, reactivando la cuenta.
     *
     * @param event evento del botón
     */
    @FXML
    public void desbanearUsuario(ActionEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Selecciona un usuario en la tabla.");
            return;
        }
        if (adminActual == null) {
            return;
        }

        Optional<Integer> idBaneo = baneoUsuarioDAO.obtenerBaneosActivos().stream()
            .filter(b -> b.getIdUsuario() == seleccionado.getIdUsuario())
            .map(BaneoUsuario::getIdBaneo)
            .findFirst();

        if (idBaneo.isEmpty()) {
            mostrarAlerta("Sin baneo activo", "El usuario seleccionado no tiene un baneo activo.");
            return;
        }

        boolean ok = baneoUsuarioDAO.desbanear(idBaneo.get(), adminActual.getIdUsuario());
        if (ok) {
            mostrarAlerta("Éxito", "Baneo revocado y cuenta reactivada.");
            cargarUsuarios();
            cargarEstadisticasGlobales();
        } else {
            mostrarAlerta("Error", "No se pudo desbanear al usuario.");
        }
    }

    /**
     * Invoca {@link BloqueoIpService#registrarExito(String)} sobre la IP seleccionada para quitar el bloqueo temporal.
     *
     * @param event evento del botón
     */
    @FXML
    public void desbloquearIp(ActionEvent event) {
        BloqueoIpVigente fila = tablaIpsBloqueadas.getSelectionModel().getSelectedItem();
        if (fila == null || fila.getIpAddress() == null || fila.getIpAddress().isBlank()) {
            mostrarAlerta("Selección requerida", "Selecciona una IP en la tabla de IPs bloqueadas.");
            return;
        }

        bloqueoIpService.registrarExito(fila.getIpAddress());
        mostrarAlerta("Éxito", "Bloqueo de IP eliminado.");
        cargarIpsBloqueadas();
    }

    /**
     * Recarga la tabla de usuarios desde la base de datos.
     */
    private void cargarUsuarios() {
        List<Usuario> usuarios = usuarioDAO.obtenerTodosUsuarios();
        tablaUsuarios.setItems(FXCollections.observableArrayList(usuarios));
    }

    /**
     * Añade un botón «Eliminar» por fila en la tabla de usuarios.
     */
    private void configurarColumnaAcciones() {
        colAcciones.setCellFactory(columna -> new TableCell<>() {
            private final Button btnEliminar = new Button("Eliminar");

            {
                btnEliminar.getStyleClass().addAll("btn-tabla-accion", "btn-danger");
                btnEliminar.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    if (usuario != null) {
                        confirmarYEliminarUsuario(usuario);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                Usuario usuario = getTableRow() != null ? getTableRow().getItem() : null;
                if (usuario == null) {
                    setGraphic(null);
                    return;
                }
                Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();
                boolean esYo = adminActual != null && usuario.getIdUsuario() == adminActual.getIdUsuario();
                setGraphic(esYo ? null : btnEliminar);
            }
        });
    }

    /**
     * Pide confirmación y elimina físicamente al usuario de la base de datos.
     *
     * @param usuario fila seleccionada
     */
    private void confirmarYEliminarUsuario(Usuario usuario) {
        Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();
        if (adminActual != null && usuario.getIdUsuario() == adminActual.getIdUsuario()) {
            mostrarAlerta("Acción no permitida", "No puedes eliminar tu propia cuenta.");
            return;
        }

        String nombre = nombreVisibleUsuario(usuario);
        String email = usuario.getEmailUsuario() != null ? usuario.getEmailUsuario() : "";

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar cuenta");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(
            "¿Eliminar la cuenta de " + nombre + " (" + email + ")? Esta acción no se puede deshacer.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        if (usuarioDAO.eliminarUsuario(usuario.getIdUsuario())) {
            tablaUsuarios.getItems().remove(usuario);
            cargarEstadisticasGlobales();
            mostrarAlerta("Éxito", "Usuario eliminado correctamente.");
        } else {
            mostrarAlerta("Error", "No se pudo eliminar el usuario.");
        }
    }

    private String nombreVisibleUsuario(Usuario usuario) {
        Perfil perfil = perfilDAO.obtenerPorUsuario(usuario.getIdUsuario());
        if (perfil != null && perfil.getNombreUsuario() != null && !perfil.getNombreUsuario().isBlank()) {
            return perfil.getNombreUsuario();
        }
        return usuario.getEmailUsuario() != null ? usuario.getEmailUsuario() : "Usuario";
    }

    /**
     * Actualiza las tarjetas de totales (usuarios y administradores).
     */
    private void cargarEstadisticasGlobales() {
        List<Usuario> todos = usuarioDAO.obtenerTodosUsuarios();
        labelTotalUsuarios.setText(String.valueOf(todos.size()));

        List<Usuario> admins = usuarioDAO.obtenerPorRol(1);
        labelTotalAdmins.setText(String.valueOf(admins.size()));
    }

    /**
     * Asigna rol administrador al usuario seleccionado en la tabla.
     *
     * @param event evento del botón
     */
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

    /**
     * Retira el rol administrador del usuario seleccionado (excepto a uno mismo).
     *
     * @param event evento del botón
     */
    @FXML
    public void quitarAdmin(ActionEvent event) {
        Usuario seleccionado = tablaUsuarios.getSelectionModel().getSelectedItem();
        Usuario adminActual = SessionManager.getInstancia().getUsuarioActual();

        if (seleccionado == null) {
            mostrarAlerta("Selección requerida", "Por favor selecciona un usuario de la tabla.");
            return;
        }

        if (adminActual != null && seleccionado.getIdUsuario() == adminActual.getIdUsuario()) {
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

    /**
     * Crea un usuario estándar (rol usuario) con perfil inicial.
     *
     * @param event evento del botón
     */
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

    /**
     * Muestra un cuadro de diálogo informativo.
     *
     * @param titulo  título de la ventana
     * @param mensaje texto principal
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
