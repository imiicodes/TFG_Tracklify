package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.AuditoriaLoginDAO;
import com.mycompany.tracklify.models.AuditoriaLoginFila;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

/**
 * Sección de configuración del administrador ({@code admin/admin_config.fxml}): auditoría de login
 * y utilidades de mantenimiento.
 *
 * @author Tracklify
 */
public class AdminConfigController implements Initializable {

    private static final String FILTRO_TODOS = "Todos";
    private static final String FILTRO_EXITO = "EXITO";
    private static final String FILTRO_FALLO = "FALLO_PASSWORD";
    private static final String FILTRO_BLOQUEO = "CUENTA_BLOQUEADA";

    @FXML
    private ComboBox<String> comboFiltroAuditoria;

    @FXML
    private TableView<AuditoriaLoginFila> tablaAuditoria;

    @FXML
    private TableColumn<AuditoriaLoginFila, String> colAudEmail;

    @FXML
    private TableColumn<AuditoriaLoginFila, String> colAudIp;

    @FXML
    private TableColumn<AuditoriaLoginFila, String> colAudResultado;

    @FXML
    private TableColumn<AuditoriaLoginFila, String> colAudFecha;

    @FXML
    private Label labelVersionApp;

    @FXML
    private Label labelUltimoAccesoAdmin;

    private final AuditoriaLoginDAO auditoriaLoginDAO = new AuditoriaLoginDAO();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboFiltroAuditoria.getItems().setAll(
            FILTRO_TODOS,
            FILTRO_EXITO,
            FILTRO_FALLO,
            FILTRO_BLOQUEO
        );
        comboFiltroAuditoria.getSelectionModel().selectFirst();
        comboFiltroAuditoria.setOnAction(e -> cargarAuditoria());

        colAudEmail.setCellValueFactory(new PropertyValueFactory<>("emailIntento"));
        colAudIp.setCellValueFactory(new PropertyValueFactory<>("ipAddress"));
        colAudResultado.setCellValueFactory(new PropertyValueFactory<>("resultado"));
        colAudFecha.setCellValueFactory(new PropertyValueFactory<>("fechaIntentoTexto"));

        labelVersionApp.setText("Tracklify v1.0");
        actualizarUltimoAccesoAdmin();
        cargarAuditoria();
    }

    /**
     * Recarga la tabla de auditoría aplicando el filtro seleccionado en el combo.
     */
    private void cargarAuditoria() {
        String sel = comboFiltroAuditoria.getSelectionModel().getSelectedItem();
        String filtro = FILTRO_TODOS.equals(sel) ? null : sel;
        tablaAuditoria.setItems(FXCollections.observableArrayList(auditoriaLoginDAO.listarUltimos100(filtro)));
    }

    /**
     * Muestra la fecha del último login exitoso del administrador en sesión.
     */
    private void actualizarUltimoAccesoAdmin() {
        Usuario u = SessionManager.getInstancia().getUsuarioActual();
        if (u == null || u.getEmailUsuario() == null) {
            labelUltimoAccesoAdmin.setText("Último acceso admin: Sin datos");
            return;
        }
        LocalDateTime ultimo = auditoriaLoginDAO.obtenerUltimoExitoPorEmail(u.getEmailUsuario());
        if (ultimo == null) {
            labelUltimoAccesoAdmin.setText("Último acceso admin: Sin datos");
        } else {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            labelUltimoAccesoAdmin.setText("Último acceso admin: " + ultimo.format(fmt));
        }
    }

    /**
     * Elimina intentos de login anteriores a 30 días tras confirmación del usuario.
     *
     * @param event evento del botón
     */
    @FXML
    public void limpiarAuditoriaAntigua(ActionEvent event) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar limpieza");
        confirmacion.setHeaderText("Eliminar registros de auditoría de más de 30 días");
        confirmacion.setContentText("Esta acción no se puede deshacer.");

        if (confirmacion.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }

        int borradas = auditoriaLoginDAO.eliminarMasDe30Dias();
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Limpieza completada");
        info.setHeaderText(null);
        info.setContentText("Registros eliminados: " + borradas);
        info.showAndWait();

        cargarAuditoria();
        actualizarUltimoAccesoAdmin();
    }
}
