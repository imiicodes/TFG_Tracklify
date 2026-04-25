/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Registro_Habito;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author imii
 */


public class RegistroHabitoDAO {

    public List<Registro_Habito> obtenerPorTarea(int tareaId) {
        List<Registro_Habito> lista = new ArrayList<>();
        String sql = "SELECT * FROM registros_habitos WHERE tarea_id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tareaId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Registro_Habito r = new Registro_Habito(
                    rs.getInt("id_registro"),
                    rs.getInt("tarea_id"),
                    rs.getDate("fecha_registro").toLocalDate(),
                    rs.getBoolean("estado_registro"),
                    rs.getString("comentario")
                );
                lista.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<Registro_Habito> obtenerPorFecha(int tareaId, LocalDate fecha) {
        List<Registro_Habito> lista = new ArrayList<>();
        String sql = "SELECT * FROM registros_habitos WHERE tarea_id = ? AND fecha_registro = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, tareaId);
            ps.setDate(2, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Registro_Habito r = new Registro_Habito(
                    rs.getInt("id_registro"),
                    rs.getInt("tarea_id"),
                    rs.getDate("fecha_registro").toLocalDate(),
                    rs.getBoolean("estado_registro"),
                    rs.getString("comentario")
                );
                lista.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertar(Registro_Habito registro) {
        String sql = "INSERT INTO registros_habitos (tarea_id, fecha_registro, estado_registro, comentario) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, registro.getTareaId());
            ps.setDate(2, Date.valueOf(registro.getFechaRegistro()));
            ps.setBoolean(3, registro.isEstadoRegistro());
            ps.setString(4, registro.getComentario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean marcarCompletado(int idRegistro) {
        String sql = "UPDATE registros_habitos SET estado_registro = 1 WHERE id_registro = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRegistro);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
