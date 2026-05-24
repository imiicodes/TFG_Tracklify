package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.InformeService;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * Controlador de la vista de informes ({@code informes_view.fxml}): permite generar y guardar
 * el informe semanal en PDF mediante {@link InformeService}.
 *
 * <p>Recibe una referencia a {@link MainViewController} para alinearse con el resto de vistas
 * embebidas en el marco principal.</p>
 *
 * @author Tracklify
 */
public class InformesController implements Initializable {

    @FXML
    private Button btnDescargarInforme;

    @FXML
    private Label labelMensajeInforme;

    /** Marco principal que carga esta vista en el {@code AnchorPane} central. */
    private MainViewController host;

    /**
     * Punto de inicialización de la vista; reservado para configuración futura según sesión.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle recursos de internacionalización; no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Reservado para futuras inicializaciones (p. ej. estado según sesión).
    }

    /**
     * Inyecta el controlador del marco principal para integración con la navegación.
     *
     * @param host controlador de {@code main_view.fxml}
     */
    public void setHost(MainViewController host) {
        this.host = host;
    }

    /**
     * @return el host de navegación o {@code null} si aún no se ha inyectado
     */
    public MainViewController getHost() {
        return host;
    }

    /**
     * Deshabilita el botón, genera el informe PDF en segundo plano y restaura la UI al terminar.
     * El {@link InformeService} muestra el FileChooser en el hilo de JavaFX de forma segura.
     *
     * @param event evento del botón «Descargar informe PDF»
     */
    @FXML
    void descargarInforme(ActionEvent event) {
        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) {
            labelMensajeInforme.setText("No hay sesión activa.");
            return;
        }

        btnDescargarInforme.setDisable(true);
        labelMensajeInforme.setText("Generando informe...");

        Thread hilo = new Thread(() -> {
            new InformeService().generarInformeSemanal(
                usuario,
                (Stage) btnDescargarInforme.getScene().getWindow()
            );
            Platform.runLater(() -> {
                btnDescargarInforme.setDisable(false);
                labelMensajeInforme.setText("");
            });
        });
        hilo.setDaemon(true);
        hilo.start();
    }
}
