package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Habito;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso JDBC a la tabla {@code habitos}: consultas por usuario o por id, alta con clave generada,
 * actualización, borrado y listados filtrados (p. ej. solo activos).
 *
 * @author Tracklify
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

    /**
     * Obtiene un hábito por su identificador.
     *
     * @param idHabito clave primaria
     * @return el hábito o {@code null}
     */
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

    /**
     * Lista todos los hábitos de un usuario.
     *
     * @param idUsuario propietario
     * @return lista, posiblemente vacía
     */
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

    /**
     * Lista los hábitos del usuario cuyo estado es {@code ACTIVO}.
     *
     * @param idUsuario propietario
     * @return lista ordenada por la consulta (posiblemente vacía)
     */
    public List<Habito> obtenerActivosPorUsuario(int idUsuario) {
        List<Habito> lista = new ArrayList<>();
        String sql = "SELECT * FROM habitos WHERE id_usuario = ? AND UPPER(TRIM(estado)) = 'ACTIVO'";

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

    /**
     * Inserta un hábito y devuelve el identificador generado.
     *
     * @param habito datos a persistir
     * @return {@code id_habito} generado, o {@code 0} si falla
     */
    public int insertar(Habito habito) {
        String sql = "INSERT INTO habitos (id_usuario, id_categoria, nombre_habito, descripcion_habito, "
            + "duracion_valor, duracion_periodo_id, fecha_inicio, fecha_fin, "
            + "notif_frecuencia_valor, notif_frecuencia_id, objetivo_veces, objetivo_periodo_id, estado) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

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

            if (ps.executeUpdate() <= 0) {
                return 0;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Indica si ya existe otro hábito con el mismo nombre para el usuario (comparación sin mayúsculas).
     *
     * @param idUsuario      propietario
     * @param nombre         nombre a comprobar
     * @param excluirIdHabito si no es {@code null}, se ignora ese id (modo edición)
     * @return {@code true} si hay duplicado
     */
    public boolean existeOtroHabitoMismoNombre(int idUsuario, String nombre, Integer excluirIdHabito) {

        String sql = "SELECT 1 FROM habitos WHERE id_usuario = ? AND LOWER(TRIM(nombre_habito)) = LOWER(TRIM(?)) ";
        if (excluirIdHabito != null) {
            sql += "AND id_habito <> ? ";
        }
        sql += "LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, nombre);
            if (excluirIdHabito != null) {
                ps.setInt(3, excluirIdHabito);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza una fila de hábito existente.
     *
     * @param habito datos a persistir (incluye {@code id_habito})
     * @return {@code true} si se modificó al menos una fila
     */
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

    /**
     * Elimina un hábito por identificador.
     *
     * @param idHabito clave primaria
     * @return {@code true} si se borró una fila
     */
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
