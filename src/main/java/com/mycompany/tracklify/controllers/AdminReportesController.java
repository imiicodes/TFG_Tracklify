package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.AdminReportesDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.AdminReporteUsuarioFila;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.InformeService;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

/**
 * Sección de informes por usuario del administrador ({@code admin/admin_reportes.fxml}).
 *
 * <p>«Ver informe» genera el PDF semanal del usuario con {@link InformeService}.</p>
 *
 * @author Tracklify
 */
public class AdminReportesController implements Initializable {

    private static final String ORDEN_RECIENTES = "Más recientes";
    private static final String ORDEN_NOMBRE = "Por nombre";
    private static final String ORDEN_HABITOS = "Con más hábitos";

    @FXML
    private ComboBox<String> comboFiltroReportes;

    @FXML
    private Button btnExportarTodo;

    @FXML
    private TableView<AdminReporteUsuarioFila> tablaReportes;

    @FXML
    private TableColumn<AdminReporteUsuarioFila, String> colRepUsuario;

    @FXML
    private TableColumn<AdminReporteUsuarioFila, String> colRepEmail;

    @FXML
    private TableColumn<AdminReporteUsuarioFila, Integer> colRepHabitos;

    @FXML
    private TableColumn<AdminReporteUsuarioFila, String> colRepUltimo;

    @FXML
    private TableColumn<AdminReporteUsuarioFila, Void> colRepAccion;

    private final AdminReportesDAO adminReportesDAO = new AdminReportesDAO();

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    /** Copia base recargada desde JDBC antes de ordenar en memoria. */
    private List<AdminReporteUsuarioFila> datosBase = new ArrayList<>();

    /**
     * Configura filtros, columnas de la tabla y acciones de informe por usuario.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle recursos de internacionalización; no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        comboFiltroReportes.getItems().setAll(ORDEN_RECIENTES, ORDEN_NOMBRE, ORDEN_HABITOS);
        comboFiltroReportes.getSelectionModel().selectFirst();
        comboFiltroReportes.setOnAction(e -> aplicarOrden());

        btnExportarTodo.setDisable(true);
        btnExportarTodo.setTooltip(new Tooltip("Próximamente"));

        colRepUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colRepEmail.setCellValueFactory(new PropertyValueFactory<>("emailUsuario"));
        colRepHabitos.setCellValueFactory(new PropertyValueFactory<>("habitosActivos"));
        colRepUltimo.setCellValueFactory(new PropertyValueFactory<>("ultimoRegistroTexto"));

        colRepAccion.setCellFactory(columna -> new TableCell<>() {
            private final Button btn = new Button("Ver informe");

            {
                btn.setOnAction(e -> {
                    int idx = getIndex();
                    if (idx < 0 || idx >= getTableView().getItems().size()) {
                        return;
                    }
                    AdminReporteUsuarioFila fila = getTableView().getItems().get(idx);
                    Usuario usuarioDeLaFila = usuarioDAO.obtenerPorId(fila.getIdUsuario());
                    if (usuarioDeLaFila == null) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Usuario no encontrado");
                        alert.setHeaderText(null);
                        alert.setContentText("No existe el usuario con id " + fila.getIdUsuario() + ".");
                        alert.showAndWait();
                        return;
                    }
                    new InformeService().generarInformeSemanal(
                        usuarioDeLaFila,
                        (Stage) btn.getScene().getWindow()
                    );
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        recargarDatos();
    }

    /**
     * Lee usuarios desde la base de datos y aplica el criterio de ordenación seleccionado.
     */
    private void recargarDatos() {
        datosBase = new ArrayList<>(adminReportesDAO.listarUsuariosParaInformes());
        aplicarOrden();
    }

    /**
     * Ordena {@link #datosBase} según el valor del {@code ComboBox} y actualiza la tabla.
     */
    private void aplicarOrden() {
        String criterio = comboFiltroReportes.getSelectionModel().getSelectedItem();
        if (criterio == null) {
            criterio = ORDEN_RECIENTES;
        }

        List<AdminReporteUsuarioFila> copia = new ArrayList<>(datosBase);
        Comparator<AdminReporteUsuarioFila> cmp;
        switch (criterio) {
            case ORDEN_NOMBRE:
                cmp = Comparator.comparing(AdminReporteUsuarioFila::getNombreUsuario,
                    String.CASE_INSENSITIVE_ORDER);
                break;
            case ORDEN_HABITOS:
                cmp = Comparator.comparingInt(AdminReporteUsuarioFila::getHabitosActivos).reversed()
                    .thenComparing(AdminReporteUsuarioFila::getNombreUsuario, String.CASE_INSENSITIVE_ORDER);
                break;
            case ORDEN_RECIENTES:
            default:
                cmp = Comparator.comparing(AdminReporteUsuarioFila::getUltimoRegistro,
                    Comparator.nullsLast(Comparator.reverseOrder()));
                break;
        }
        copia.sort(cmp);
        tablaReportes.setItems(FXCollections.observableArrayList(copia));
    }
}
