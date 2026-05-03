/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.RegistroHabito;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author imii
 */
public class RegistroHabitoDAO {

    private static Integer getIntegerOrNull(ResultSet rs, String col) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? null : v;
    }

    private static LocalDateTime getTsOrNull(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    private static String getStringOrNull(ResultSet rs, String col) throws SQLException {
        String s = rs.getString(col);
        return rs.wasNull() ? null : s;
    }

    private static RegistroHabito mapFila(ResultSet rs) throws SQLException {
        return new RegistroHabito(
            rs.getInt("id_registro"),
            rs.getInt("id_habito"),
            rs.getTimestamp("marca_tiempo_inicio").toLocalDateTime(),
            getTsOrNull(rs, "marca_tiempo_fin"),
            getIntegerOrNull(rs, "duracion_segundos"),
            rs.getString("estado_registro"),
            rs.getInt("es_objetivo") != 0,
            getStringOrNull(rs, "comentario"),
            getIntegerOrNull(rs, "anio_semana"),
            getIntegerOrNull(rs, "num_semana")
        );
    }

    private static void setIntegerOrNull(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) {
            ps.setNull(idx, Types.INTEGER);
        } else {
            ps.setInt(idx, v);
        }
    }

    public List<RegistroHabito> obtenerPorHabito(int idHabito) {
        List<RegistroHabito> lista = new ArrayList<>();
        String sql = "SELECT * FROM registros_habitos WHERE id_habito = ? ORDER BY marca_tiempo_inicio DESC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idHabito);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapFila(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<RegistroHabito> obtenerPorFecha(int idHabito, LocalDate fecha) {
        List<RegistroHabito> lista = new ArrayList<>();
        String sql = "SELECT * FROM registros_habitos WHERE id_habito = ? AND DATE(marca_tiempo_inicio) = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idHabito);
            ps.setDate(2, Date.valueOf(fecha));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapFila(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertar(RegistroHabito registro) {
        String sql = "INSERT INTO registros_habitos (id_habito, marca_tiempo_inicio, marca_tiempo_fin, "
            + "duracion_segundos, estado_registro, es_objetivo, comentario, anio_semana, num_semana) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, registro.getIdHabito());
            ps.setTimestamp(2, Timestamp.valueOf(registro.getMarcaTiempoInicio()));
            if (registro.getMarcaTiempoFin() == null) {
                ps.setNull(3, Types.TIMESTAMP);
            } else {
                ps.setTimestamp(3, Timestamp.valueOf(registro.getMarcaTiempoFin()));
            }
            setIntegerOrNull(ps, 4, registro.getDuracionSegundos());
            ps.setString(5, registro.getEstadoRegistro());
            ps.setInt(6, registro.isEsObjetivo() ? 1 : 0);
            if (registro.getComentario() == null) {
                ps.setNull(7, Types.VARCHAR);
            } else {
                ps.setString(7, registro.getComentario());
            }
            setIntegerOrNull(ps, 8, registro.getAnioSemana());
            setIntegerOrNull(ps, 9, registro.getNumSemana());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserta un registro con marca de tiempo, semana ISO actual y estado PENDIENTE.
     */
    public boolean insertarConTimestamp(RegistroHabito r) {
        LocalDate hoy = LocalDate.now();
        int anioSemana = hoy.get(IsoFields.WEEK_BASED_YEAR);
        int numSemana = hoy.get(WeekFields.ISO.weekOfWeekBasedYear());

        String sql = "INSERT INTO registros_habitos (id_habito, marca_tiempo_inicio, anio_semana, num_semana, "
            + "estado_registro, es_objetivo, comentario) VALUES (?, ?, ?, ?, 'PENDIENTE', ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, r.getIdHabito());
            ps.setTimestamp(2, Timestamp.valueOf(r.getMarcaTiempoInicio()));
            ps.setInt(3, anioSemana);
            ps.setInt(4, numSemana);
            ps.setInt(5, r.isEsObjetivo() ? 1 : 0);
            if (r.getComentario() == null) {
                ps.setNull(6, Types.VARCHAR);
            } else {
                ps.setString(6, r.getComentario());
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cierra un registro de sesión de hábito marcándolo como completado.
     */
    public boolean cerrarSesion(int idRegistro, LocalDateTime fin, int duracionSegundos) {
        String sql = "UPDATE registros_habitos SET marca_tiempo_fin = ?, duracion_segundos = ?, estado_registro = 'COMPLETADO' "
            + "WHERE id_registro = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fin));
            ps.setInt(2, duracionSegundos);
            ps.setInt(3, idRegistro);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Días cumplidos según la vista {@code v_dias_cumplidos}.
     */
    public List<LocalDate> obtenerDiasCumplidos(int idHabito) {
        List<LocalDate> dias = new ArrayList<>();
        String sql = "SELECT dia_cumplido FROM v_dias_cumplidos WHERE id_habito = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idHabito);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Date d = rs.getDate("dia_cumplido");
                if (d != null) {
                    dias.add(d.toLocalDate());
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dias;
    }

    public boolean marcarCompletado(int idRegistro) {
        String sql = "UPDATE registros_habitos SET estado_registro = 'COMPLETADO' WHERE id_registro = ?";

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
