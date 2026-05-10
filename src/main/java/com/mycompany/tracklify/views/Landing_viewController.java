/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXMLController.java to edit this template
 */
package com.mycompany.tracklify.views;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Landing_viewController {

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
