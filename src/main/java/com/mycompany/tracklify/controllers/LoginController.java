/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;
/**
 *
 * @author imii
 */
public class LoginController {
    @FXML
    private TextField campoEmail;

    @FXML
    private PasswordField campoPassword;

    @FXML
    private Label labelMensaje;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void iniciarSesion(ActionEvent event) {

        String email = campoEmail.getText();
        String password = campoPassword.getText();

        boolean loginCorrecto = usuarioDAO.login(email, password);

        if (loginCorrecto) {
            try {
                FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/main_view.fxml")
                );

                Scene scene = new Scene(loader.load());

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                stage.setScene(scene);
                stage.show();

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            labelMensaje.setText("Credenciales incorrectas ❌");
        }
    }
    
    @FXML
    public void volverALanding(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/landing_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }
    
}
