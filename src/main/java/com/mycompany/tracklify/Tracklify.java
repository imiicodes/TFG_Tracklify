package com.mycompany.tracklify;

import com.mycompany.tracklify.database.ConexionBD;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX Tracklify.
 *
 * <p>Carga la pantalla de bienvenida ({@code landing_view.fxml}) y comprueba la conectividad
 * JDBC al arrancar.</p>
 *
 * @author Tracklify
 */
public class Tracklify extends Application {

    /**
     * Inicializa la ventana principal con la vista de landing.
     *
     * @param stage escenario principal de la aplicación
     * @throws Exception si falla la carga del FXML
     */
    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/fxml/landing_view.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("Tracklify");
        stage.setScene(scene);
        stage.show();
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setResizable(true);
        stage.centerOnScreen();
    }

    /**
     * Arranca JavaFX tras intentar una conexión de prueba a la base de datos.
     *
     * @param args argumentos de línea de comandos (no usados)
     */
    public static void main(String[] args) {
        ConexionBD.conectar();
        launch();
    }
}
