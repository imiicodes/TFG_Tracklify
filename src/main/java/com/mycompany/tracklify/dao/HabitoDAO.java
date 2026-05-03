/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Habito;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author imii
 */
public class HabitoDAO {

    private static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int v = rs.getInt(column);
        return rs.wasNull() ? null : v;
    }

    private static LocalDate getLocalDateOrNull(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d == null ? null : d.toLocalDate();
    }

    private static Habito mapFila(ResultSet rs) throws SQLException {
        return new Habito(
            rs.getInt("id_habito"),
            rs.getInt("id_usuario"),
            getInteger(rs, "id_categoria"),
            rs.getString("nombre_habito"),
            rs.getString("descripcion_habito"),
            getInteger(rs, "duracion_valor"),
            getInteger(rs, "duracion_periodo_id"),
            rs.getDate("fecha_inicio").toLocalDate(),
            getLocalDateOrNull(rs, "fecha_fin"),
            rs.getInt("notif_frecuencia_valor"),
            getInteger(rs, "notif_frecuencia_id"),
            rs.getInt("objetivo_veces"),
            getInteger(rs, "objetivo_periodo_id"),
            rs.getString("estado")
        );
    }

    private static void setIntegerOrNull(PreparedStatement ps, int index, Integer value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.INTEGER);
        } else {
            ps.setInt(index, value);
        }
    }

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate value)
            throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    public Habito obtenerPorId(int idHabito) {
        String sql = "SELECT * FROM habitos WHERE id_habito = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idHabito);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapFila(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Habito> obtenerPorUsuario(int idUsuario) {
        List<Habito> lista = new ArrayList<>();
        String sql = "SELECT * FROM habitos WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapFila(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertar(Habito habito) {
        String sql = "INSERT INTO habitos (id_usuario, id_categoria, nombre_habito, descripcion_habito, "
            + "duracion_valor, duracion_periodo_id, fecha_inicio, fecha_fin, "
            + "notif_frecuencia_valor, notif_frecuencia_id, objetivo_veces, objetivo_periodo_id, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, habito.getIdUsuario());
            setIntegerOrNull(ps, 2, habito.getIdCategoria());
            ps.setString(3, habito.getNombreHabito());
            ps.setString(4, habito.getDescripcionHabito());
            setIntegerOrNull(ps, 5, habito.getDuracionValor());
            setIntegerOrNull(ps, 6, habito.getDuracionPeriodoId());
            ps.setDate(7, Date.valueOf(habito.getFechaInicio()));
            setDateOrNull(ps, 8, habito.getFechaFin());
            ps.setInt(9, habito.getNotifFrecuenciaValor());
            setIntegerOrNull(ps, 10, habito.getNotifFrecuenciaId());
            ps.setInt(11, habito.getObjetivoVeces());
            setIntegerOrNull(ps, 12, habito.getObjetivoPeriodoId());
            ps.setString(13, habito.getEstado() != null ? habito.getEstado() : "ACTIVO");

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Habito habito) {
        String sql = "UPDATE habitos SET id_usuario=?, id_categoria=?, nombre_habito=?, descripcion_habito=?, "
            + "duracion_valor=?, duracion_periodo_id=?, fecha_inicio=?, fecha_fin=?, "
            + "notif_frecuencia_valor=?, notif_frecuencia_id=?, objetivo_veces=?, objetivo_periodo_id=?, estado=? "
            + "WHERE id_habito=?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, habito.getIdUsuario());
            setIntegerOrNull(ps, 2, habito.getIdCategoria());
            ps.setString(3, habito.getNombreHabito());
            ps.setString(4, habito.getDescripcionHabito());
            setIntegerOrNull(ps, 5, habito.getDuracionValor());
            setIntegerOrNull(ps, 6, habito.getDuracionPeriodoId());
            ps.setDate(7, Date.valueOf(habito.getFechaInicio()));
            setDateOrNull(ps, 8, habito.getFechaFin());
            ps.setInt(9, habito.getNotifFrecuenciaValor());
            setIntegerOrNull(ps, 10, habito.getNotifFrecuenciaId());
            ps.setInt(11, habito.getObjetivoVeces());
            setIntegerOrNull(ps, 12, habito.getObjetivoPeriodoId());
            ps.setString(13, habito.getEstado() != null ? habito.getEstado() : "ACTIVO");
            ps.setInt(14, habito.getIdHabito());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int idHabito) {
        String sql = "DELETE FROM habitos WHERE id_habito = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idHabito);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
