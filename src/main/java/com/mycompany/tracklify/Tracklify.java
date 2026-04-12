package com.mycompany.tracklify;

import com.mycompany.tracklify.database.ConexionBD;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Tracklify extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
        getClass().getResource("/fxml/landing_view.fxml")
        );

        Scene scene = new Scene(loader.load());

        stage.setTitle("Tracklify");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        ConexionBD.conectar();
        launch();
    }
}
