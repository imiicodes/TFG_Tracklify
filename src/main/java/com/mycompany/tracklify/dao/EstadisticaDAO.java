/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.ProgresoSemanal;
import com.mycompany.tracklify.models.ResumenUsuario;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas de informes contra vistas SQL.
 *
 * @author imii
 */
public class EstadisticaDAO {

    private static ProgresoSemanal mapProgresoSemanal(ResultSet rs) throws SQLException {
        return new ProgresoSemanal(
            rs.getInt("id_usuario"),
            rs.getInt("id_habito"),
            rs.getString("nombre_habito"),
            rs.getInt("objetivo_veces"),
            rs.getString("periodo_objetivo"),
            rs.getInt("anio_semana"),
            rs.getInt("num_semana"),
            rs.getInt("veces_completado"),
            rs.getInt("veces_pendientes"),
            rs.getDouble("porcentaje_completado"),
            rs.getLong("segundos_totales_semana")
        );
    }

    public List<ProgresoSemanal> obtenerProgresoSemanal(int idHabito) {
        List<ProgresoSemanal> lista = new ArrayList<>();
        String sql = "SELECT * FROM v_progreso_semanal WHERE id_habito = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idHabito);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapProgresoSemanal(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public ResumenUsuario obtenerResumenUsuario(int idUsuario) {
        String sql = "SELECT * FROM v_resumen_usuario WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new ResumenUsuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_usuario"),
                    rs.getInt("total_habitos_activos"),
                    rs.getInt("total_cumplimientos"),
                    rs.getInt("habitos_completados_hoy"),
                    rs.getDouble("tasa_exito_global")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ProgresoSemanal> obtenerInformeSemanal(int idUsuario) {
        List<ProgresoSemanal> lista = new ArrayList<>();
        String sql = "SELECT * FROM v_informe_semanal WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapProgresoSemanal(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
