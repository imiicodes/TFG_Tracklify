package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.models.Habito;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.NotificacionScheduler;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

/**
 * Controlador del marco principal ({@code main_view.fxml}): barra superior, barra lateral
 * fija y un {@link AnchorPane} central donde se cargan las distintas vistas FXML sin
 * sustituir el {@link Stage} ni la {@link Scene} tras el inicio de sesión.
 *
 * @author Tracklify
 */
public class MainViewController implements Initializable {

    @FXML
    private AnchorPane contenidoPrincipal;

    @FXML
    private Button btnNavDashboard;

    @FXML
    private Button btnNavMisHabitos;

    @FXML
    private Button btnNavCalendario;

    @FXML
    private Button btnNavEstadisticas;

    @FXML
    private Button btnNavInformes;

    @FXML
    private Button btnNavConfiguracion;

    /** Botones laterales para aplicar o quitar la clase de ítem activo. */
    private final List<Button> botonesSidebar = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        botonesSidebar.clear();
        botonesSidebar.add(btnNavDashboard);
        botonesSidebar.add(btnNavMisHabitos);
        botonesSidebar.add(btnNavCalendario);
        botonesSidebar.add(btnNavEstadisticas);
        botonesSidebar.add(btnNavInformes);
        botonesSidebar.add(btnNavConfiguracion);

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario != null) {
            NotificacionScheduler.getInstancia().iniciar(usuario.getIdUsuario());
        }

        cargarVista("dashboard_view.fxml");
        marcarItemActivo(btnNavDashboard);
    }

    /**
     * Carga un FXML en el panel central, anclándolo a los cuatro bordes del {@link AnchorPane}.
     *
     * @param fxml nombre del archivo bajo {@code /fxml/} (p. ej. {@code "dashboard_view.fxml"})
     */
    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
            Node vista = loader.load();
            if ("mis_habitos_view.fxml".equals(fxml)) {
                MisHabitosController c = loader.getController();
                c.setHost(this);
            } else if ("dashboard_view.fxml".equals(fxml)) {
                DashboardViewController d = loader.getController();
                d.setHost(this);
            }
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);
            contenidoPrincipal.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Marca un botón del sidebar como seleccionado y deja el resto en estado inactivo.
     *
     * @param btn botón pulsado o asociado a la vista mostrada
     */
    private void marcarItemActivo(Button btn) {
        for (Button b : botonesSidebar) {
            b.getStyleClass().remove("sidebar-item-activo");
        }
        if (btn != null) {
            if (!btn.getStyleClass().contains("sidebar-item-activo")) {
                btn.getStyleClass().add("sidebar-item-activo");
            }
        }
    }

    /**
     * Muestra el dashboard en el área central y actualiza el resaltado del menú.
     *
     * @param event evento del botón «Dashboard»
     */
    @FXML
    public void mostrarDashboard(ActionEvent event) {
        marcarItemActivo(btnNavDashboard);
        cargarVista("dashboard_view.fxml");
    }

    /**
     * Muestra el listado «Mis hábitos» en el área central.
     *
     * @param event evento del botón «Mis hábitos»
     */
    @FXML
    public void mostrarMisHabitos(ActionEvent event) {
        marcarItemActivo(btnNavMisHabitos);
        cargarVista("mis_habitos_view.fxml");
    }

    /**
     * Muestra la vista de calendario (placeholder si aún no hay lógica).
     *
     * @param event evento del botón «Calendario»
     */
    @FXML
    public void mostrarCalendario(ActionEvent event) {
        marcarItemActivo(btnNavCalendario);
        cargarVista("calendario_view.fxml");
    }

    /**
     * Muestra la vista de estadísticas.
     *
     * @param event evento del botón «Estadísticas»
     */
    @FXML
    public void mostrarEstadisticas(ActionEvent event) {
        marcarItemActivo(btnNavEstadisticas);
        cargarVista("estadisticas_view.fxml");
    }

    /**
     * Muestra la vista de informes.
     *
     * @param event evento del botón «Informes»
     */
    @FXML
    public void mostrarInformes(ActionEvent event) {
        marcarItemActivo(btnNavInformes);
        cargarVista("informes_view.fxml");
    }

    /**
     * Muestra la vista de configuración.
     *
     * @param event evento del botón «Configuración»
     */
    @FXML
    public void mostrarConfiguracion(ActionEvent event) {
        marcarItemActivo(btnNavConfiguracion);
        cargarVista("configuracion_view.fxml");
    }

    /**
     * Abre el asistente de creación de hábito dentro del panel central.
     *
     * @param desdeMisHabitos si es {@code true}, al volver o guardar se recarga «Mis hábitos»;
     *                        si es {@code false}, se vuelve al dashboard
     */
    public void abrirCrearHabito(boolean desdeMisHabitos) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear_habito_view.fxml"));
            Node vista = loader.load();
            CrearHabitoController ctrl = loader.getController();
            ctrl.setOnVolver(() -> {
                if (desdeMisHabitos) {
                    cargarVista("mis_habitos_view.fxml");
                    marcarItemActivo(btnNavMisHabitos);
                } else {
                    cargarVista("dashboard_view.fxml");
                    marcarItemActivo(btnNavDashboard);
                }
            });
            ctrl.setHabitoAEditar(null);
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);
            contenidoPrincipal.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre el asistente en modo edición para el hábito indicado.
     *
     * @param habito hábito a modificar
     * @param desdeMisHabitos misma semántica que {@link #abrirCrearHabito(boolean)}
     */
    public void abrirEditorHabito(Habito habito, boolean desdeMisHabitos) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear_habito_view.fxml"));
            Node vista = loader.load();
            CrearHabitoController ctrl = loader.getController();
            ctrl.setOnVolver(() -> {
                if (desdeMisHabitos) {
                    cargarVista("mis_habitos_view.fxml");
                    marcarItemActivo(btnNavMisHabitos);
                } else {
                    cargarVista("dashboard_view.fxml");
                    marcarItemActivo(btnNavDashboard);
                }
            });
            ctrl.setHabitoAEditar(habito);
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);
            contenidoPrincipal.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cierra la sesión, detiene el scheduler de notificaciones y vuelve a la pantalla de landing.
     *
     * @param event evento del botón «Cerrar sesión»
     * @throws Exception si el FXML de landing no se puede cargar
     */
    @FXML
    public void cerrarSesion(ActionEvent event) throws Exception {

        NotificacionScheduler.getInstancia().detener();
        SessionManager.getInstancia().cerrarSesion();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/landing_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }
}
