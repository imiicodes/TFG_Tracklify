/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Estadistica;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author imii
 */

public class EstadisticaDAO {

    public List<Estadistica> obtenerPorUsuario(int idUsuario) {
        List<Estadistica> lista = new ArrayList<>();
        String sql = "SELECT * FROM estadisticas WHERE id_usuario = ? ORDER BY fecha_generacion DESC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Estadistica e = new Estadistica(
                    rs.getInt("id_estadistica"),
                    rs.getInt("id_usuario"),
                    rs.getDate("fecha_generacion").toLocalDate(),
                    rs.getBigDecimal("porcentaje_completado"),
                    rs.getInt("racha_actual"),
                    rs.getInt("tareas_completadas"),
                    rs.getInt("tareas_totales")
                );
                lista.add(e);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public Estadistica obtenerUltima(int idUsuario) {
        String sql = "SELECT * FROM estadisticas WHERE id_usuario = ? ORDER BY fecha_generacion DESC LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Estadistica(
                    rs.getInt("id_estadistica"),
                    rs.getInt("id_usuario"),
                    rs.getDate("fecha_generacion").toLocalDate(),
                    rs.getBigDecimal("porcentaje_completado"),
                    rs.getInt("racha_actual"),
                    rs.getInt("tareas_completadas"),
                    rs.getInt("tareas_totales")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertar(Estadistica estadistica) {
        String sql = "INSERT INTO estadisticas (id_usuario, fecha_generacion, porcentaje_completado, racha_actual, tareas_completadas, tareas_totales) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, estadistica.getIdUsuario());
            ps.setDate(2, Date.valueOf(estadistica.getFechaGeneracion()));
            ps.setBigDecimal(3, estadistica.getPorcentajeCompletado());
            ps.setInt(4, estadistica.getRachaActual());
            ps.setInt(5, estadistica.getTareasCompletadas());
            ps.setInt(6, estadistica.getTareasTotales());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean generarEstadisticaHoy(int idUsuario) {
        String sql = """
            INSERT INTO estadisticas (id_usuario, fecha_generacion, porcentaje_completado, racha_actual, tareas_completadas, tareas_totales)
            SELECT
                t.usuario_id,
                CURDATE(),
                ROUND(SUM(rh.estado_registro) * 100.0 / COUNT(rh.id_registro), 2),
                0,
                SUM(rh.estado_registro),
                COUNT(rh.id_registro)
            FROM tareas t
            JOIN registros_habitos rh ON rh.tarea_id = t.id_tarea
            WHERE t.usuario_id = ? AND rh.fecha_registro = CURDATE()
            GROUP BY t.usuario_id
        """;

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
