package com.mycompany.tracklify.controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

public class MainViewController implements Initializable {

    @FXML
    private TextField campoUsuario;

    @FXML
    private PasswordField campoContrasena;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Este método se ejecuta automáticamente al cargar la vista
        System.out.println("Vista cargada correctamente");
    }

    @FXML
    private void iniciarSesion() {

        String usuario = campoUsuario.getText();
        String contrasena = campoContrasena.getText();

        System.out.println("Usuario: " + usuario);
        System.out.println("Contraseña: " + contrasena);

    }

}
