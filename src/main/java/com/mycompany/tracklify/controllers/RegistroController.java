/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 *
 * @author imii
 */

public class RegistroController {

    @FXML private TextField campoNombre;
    @FXML private TextField campoEmail;
    @FXML private PasswordField campoPassword;
    @FXML private PasswordField campoConfirmar;
    @FXML private Label labelMensaje;

    private UsuarioDAO usuarioDAO = new UsuarioDAO();

    @FXML
    public void registrar(ActionEvent event) {

        String nombre   = campoNombre.getText().trim();
        String email    = campoEmail.getText().trim();
        String password = campoPassword.getText();
        String confirmar = campoConfirmar.getText();

        // Validaciones
        if (nombre.isEmpty() || email.isEmpty() || password.isEmpty() || confirmar.isEmpty()) {
            labelMensaje.setText("Por favor rellena todos los campos.");
            return;
        }

        if (password.length() < 6) {
            labelMensaje.setText("La contraseña debe tener al menos 6 caracteres.");
            return;
        }

        if (!password.equals(confirmar)) {
            labelMensaje.setText("Las contraseñas no coinciden.");
            return;
        }

        if (!email.contains("@") || !email.contains(".")) {
            labelMensaje.setText("El correo electrónico no es válido.");
            return;
        }

        // Crear usuario con rol_id = 1 (usuario normal)
        Usuario usuario = new Usuario();
        usuario.setNombreUsuario(nombre);
        usuario.setEmailUsuario(email);
        usuario.setPasswordUsuario(password);
        usuario.setRolId(1);

        boolean exito = usuarioDAO.registrar(usuario);

        if (exito) {
            labelMensaje.setStyle("-fx-text-fill: #3A8F5F;");
            labelMensaje.setText("¡Cuenta creada correctamente! Redirigiendo...");

            // Pequeña pausa visual antes de navegar al login
            new Thread(() -> {
                try {
                    Thread.sleep(1200);
                    javafx.application.Platform.runLater(() -> {
                        try { irALogin(event); } catch (Exception e) { e.printStackTrace(); }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();

        } else {
            labelMensaje.setText("Ese correo ya está registrado.");
        }
    }

    @FXML
    public void irALogin(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/login_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }
}
