package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.PerfilDAO;
import com.mycompany.tracklify.models.Perfil;
import com.mycompany.tracklify.models.Usuario;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Marco principal del panel de administración ({@code admin_view.fxml}): barra superior,
 * barra lateral por secciones y un {@link StackPane} central donde se cargan los FXML
 * bajo {@code /fxml/admin/}.
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
    private StackPane contenidoAdmin;

    @FXML
    private Button btnAdminDashboard;

    @FXML
    private Button btnAdminUsuarios;

    @FXML
    private Button btnAdminEstadisticas;

    @FXML
    private Button btnAdminReportes;

    @FXML
    private Button btnAdminConfig;

    private final PerfilDAO perfilDAO = new PerfilDAO();

    private final List<Button> botonesNav = new ArrayList<>();

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

        botonesNav.clear();
        botonesNav.add(btnAdminDashboard);
        botonesNav.add(btnAdminUsuarios);
        botonesNav.add(btnAdminEstadisticas);
        botonesNav.add(btnAdminReportes);
        botonesNav.add(btnAdminConfig);

        marcarNavActivo(btnAdminDashboard);
        cargarSeccion("admin_dashboard.fxml");
    }

    /**
     * Carga un FXML de sección en el área central, sustituyendo el contenido anterior.
     *
     * @param fxml nombre del archivo dentro de {@code /fxml/admin/}
     */
    private void cargarSeccion(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/" + fxml));
            Node vista = loader.load();
            contenidoAdmin.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error al cargar sección");
            alert.setHeaderText(null);
            alert.setContentText("No se pudo cargar la vista: " + fxml + "\n" + e.getMessage());
            alert.showAndWait();
        }
    }

    /**
     * Marca un botón del menú lateral como activo y quita el estilo al resto.
     *
     * @param activo botón seleccionado o {@code null} para limpiar todos
     */
    private void marcarNavActivo(Button activo) {
        for (Button b : botonesNav) {
            b.getStyleClass().remove("nav-activo");
        }
        if (activo != null && !activo.getStyleClass().contains("nav-activo")) {
            activo.getStyleClass().add("nav-activo");
        }
    }

    /**
     * Muestra el dashboard (resumen y gestión de usuarios / IPs).
     *
     * @param event evento de navegación
     */
    @FXML
    public void mostrarDashboard(ActionEvent event) {
        marcarNavActivo(btnAdminDashboard);
        cargarSeccion("admin_dashboard.fxml");
    }

    /**
     * Muestra la misma vista de gestión de usuarios que el dashboard (tabla, baneos e IPs).
     *
     * @param event evento de navegación
     */
    @FXML
    public void mostrarUsuarios(ActionEvent event) {
        marcarNavActivo(btnAdminUsuarios);
        cargarSeccion("admin_dashboard.fxml");
    }

    /**
     * Muestra estadísticas globales y el detalle por usuario.
     *
     * @param event evento de navegación
     */
    @FXML
    public void mostrarEstadisticas(ActionEvent event) {
        marcarNavActivo(btnAdminEstadisticas);
        cargarSeccion("admin_estadisticas.fxml");
    }

    /**
     * Muestra el listado de informes por usuario.
     *
     * @param event evento de navegación
     */
    @FXML
    public void mostrarReportes(ActionEvent event) {
        marcarNavActivo(btnAdminReportes);
        cargarSeccion("admin_reportes.fxml");
    }

    /**
     * Muestra auditoría de accesos y utilidades de sistema.
     *
     * @param event evento de navegación
     */
    @FXML
    public void mostrarConfig(ActionEvent event) {
        marcarNavActivo(btnAdminConfig);
        cargarSeccion("admin_config.fxml");
    }

    /**
     * Cierra la sesión y vuelve a la pantalla de landing.
     *
     * @param event evento del botón «Cerrar sesión»
     * @throws Exception si no se puede cargar el FXML de landing
     */
    @FXML
    public void cerrarSesion(ActionEvent event) throws Exception {
        SessionManager.getInstancia().cerrarSesion();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/landing_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double w = stage.getWidth();
        double h = stage.getHeight();
        stage.setScene(new Scene(loader.load(), w, h));
    }
}
