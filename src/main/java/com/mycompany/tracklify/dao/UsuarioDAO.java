/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
/**
 *
 * @author imii
 */
public class UsuarioDAO {
    public boolean login(String email, String password) {

        String sql = "SELECT * FROM usuarios WHERE email_usuario = ? AND password_usuario = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            return rs.next(); // si encuentra usuario → true

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean registrar(Usuario usuario) {
        String sql = "INSERT INTO usuarios (nombre_usuario, email_usuario, password_usuario, rol_id) VALUES (?, ?, ?, ?)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getEmailUsuario());
            ps.setString(3, usuario.getPasswordUsuario());
            ps.setInt(4, usuario.getRolId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // Si el email ya existe (UNIQUE KEY) devuelve false
            return false;
        }
    }
}
