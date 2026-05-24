package com.mycompany.tracklify.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Controlador de la pantalla de bienvenida ({@code landing_view.fxml}).
 *
 * <p>Ofrece navegación hacia login o registro conservando el tamaño de la ventana.</p>
 *
 * @author Tracklify
 */
public class Landing_viewController {

    /**
     * Cambia la escena actual a la vista de inicio de sesión.
     *
     * @param event evento del botón que disparó la acción
     * @throws Exception si no se puede cargar {@code login_view.fxml}
     */
    @FXML
    public void irALogin(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/login_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();
        stage.setScene(new Scene(loader.load(), width, height));
    }

    /**
     * Cambia la escena actual a la vista de registro de usuario.
     *
     * @param event evento del botón que disparó la acción
     * @throws Exception si no se puede cargar {@code registro_view.fxml}
     */
    @FXML
    public void irARegistro(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/registro_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        double width = stage.getWidth();
        double height = stage.getHeight();
        stage.setScene(new Scene(loader.load(), width, height));
    }
}
