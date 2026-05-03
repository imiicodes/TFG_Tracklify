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
 * Acceso JDBC a la tabla {@code registros_habitos} (sesiones y cumplimientos de hábitos).
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
     * Inserta un registro con semana ISO calculada desde {@code marca_tiempo_inicio}.
     *
     * <p>Si {@link RegistroHabito#getEstadoRegistro()} es {@code COMPLETADO}, inserta inicio, fin,
     * duración y estado en una sola fila (cumplimiento inmediato). En caso contrario inserta
     * una sesión {@code PENDIENTE} como hasta ahora.</p>
     *
     * @param r datos del registro; para COMPLETADO deben venir {@code marca_tiempo_inicio} y
     *          {@code marca_tiempo_fin} (p. ej. ambos {@code now()})
     * @return {@code true} si se insertó correctamente
     */
    public boolean insertarConTimestamp(RegistroHabito r) {
        LocalDate diaRef = r.getMarcaTiempoInicio() != null
            ? r.getMarcaTiempoInicio().toLocalDate()
            : LocalDate.now();
        int anioSemana = diaRef.get(IsoFields.WEEK_BASED_YEAR);
        int numSemana = diaRef.get(WeekFields.ISO.weekOfWeekBasedYear());

        if (r.getEstadoRegistro() != null && "COMPLETADO".equalsIgnoreCase(r.getEstadoRegistro().trim())) {

            LocalDateTime inicio = r.getMarcaTiempoInicio();
            LocalDateTime fin = r.getMarcaTiempoFin() != null ? r.getMarcaTiempoFin() : inicio;
            int duracion = r.getDuracionSegundos() != null ? r.getDuracionSegundos() : 0;

            String sql = "INSERT INTO registros_habitos (id_habito, marca_tiempo_inicio, marca_tiempo_fin, "
                + "duracion_segundos, estado_registro, es_objetivo, comentario, anio_semana, num_semana) "
                + "VALUES (?, ?, ?, ?, 'COMPLETADO', ?, ?, ?, ?)";

            try (Connection con = ConexionBD.conectar();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setInt(1, r.getIdHabito());
                ps.setTimestamp(2, Timestamp.valueOf(inicio));
                ps.setTimestamp(3, Timestamp.valueOf(fin));
                ps.setInt(4, duracion);
                ps.setInt(5, r.isEsObjetivo() ? 1 : 0);
                if (r.getComentario() == null) {
                    ps.setNull(6, Types.VARCHAR);
                } else {
                    ps.setString(6, r.getComentario());
                }
                ps.setInt(7, anioSemana);
                ps.setInt(8, numSemana);

                return ps.executeUpdate() > 0;

            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }

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
     * Obtiene el registro de un hábito para hoy (fecha de {@code marca_tiempo_inicio}) y el estado indicado.
     *
     * @param idHabito identificador del hábito
     * @param estado   valor de {@code estado_registro} (p. ej. {@code PENDIENTE}, {@code COMPLETADO})
     * @return la fila encontrada o {@code null}
     */
    public RegistroHabito obtenerRegistroHoy(int idHabito, String estado) {

        String sql = "SELECT * FROM registros_habitos WHERE id_habito = ? AND estado_registro = ? "
            + "AND DATE(marca_tiempo_inicio) = CURDATE() LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idHabito);
            ps.setString(2, estado);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapFila(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene el registro {@code COMPLETADO} del hábito cuya marca de inicio es hoy.
     *
     * @param idHabito identificador del hábito
     * @return el registro o {@code null}
     */
    public RegistroHabito obtenerRegistroCompleadoHoy(int idHabito) {
        return obtenerRegistroHoy(idHabito, "COMPLETADO");
    }

    /**
     * Actualiza solo el campo {@code estado_registro} de un registro.
     *
     * @param idRegistro   clave primaria
     * @param nuevoEstado nuevo valor (p. ej. {@code PENDIENTE})
     * @return {@code true} si se actualizó al menos una fila
     */
    public boolean actualizarEstado(int idRegistro, String nuevoEstado) {

        String sql = "UPDATE registros_habitos SET estado_registro = ? WHERE id_registro = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idRegistro);
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
