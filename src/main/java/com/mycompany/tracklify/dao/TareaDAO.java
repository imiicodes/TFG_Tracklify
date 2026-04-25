/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Tarea;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author imii
 */


public class TareaDAO {

    public List<Tarea> obtenerPorUsuario(int usuarioId) {
        List<Tarea> tareas = new ArrayList<>();
        String sql = "SELECT * FROM tareas WHERE usuario_id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Tarea t = new Tarea(
                    rs.getInt("id_tarea"),
                    rs.getInt("usuario_id"),
                    rs.getString("nombre_tarea"),
                    rs.getString("descripcion_tarea"),
                    rs.getString("frecuencia_tarea")
                );
                tareas.add(t);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tareas;
    }

    public boolean insertar(Tarea tarea) {
        String sql = "INSERT INTO tareas (usuario_id, nombre_tarea, descripcion_tarea, frecuencia_tarea) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tarea.getUsuarioId());
            ps.setString(2, tarea.getNombreTarea());
            ps.setString(3, tarea.getDescripcionTarea());
            ps.setString(4, tarea.getFrecuenciaTarea());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Tarea tarea) {
        String sql = "UPDATE tareas SET nombre_tarea=?, descripcion_tarea=?, frecuencia_tarea=? WHERE id_tarea=?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, tarea.getNombreTarea());
            ps.setString(2, tarea.getDescripcionTarea());
            ps.setString(3, tarea.getFrecuenciaTarea());
            ps.setInt(4, tarea.getIdTarea());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int idTarea) {
        String sql = "DELETE FROM tareas WHERE id_tarea = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idTarea);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
