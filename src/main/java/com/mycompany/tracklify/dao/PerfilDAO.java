package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Perfil;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Acceso a datos para la tabla {@code perfiles}.
 *
 * @author Tracklify
 */
public class PerfilDAO {

    private static LocalDateTime getDateTimeOrNull(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return ts == null ? null : ts.toLocalDateTime();
    }

    private static LocalDate getDateOrNull(ResultSet rs, String column) throws SQLException {
        Date d = rs.getDate(column);
        return d == null ? null : d.toLocalDate();
    }

    private static String getStringOrNull(ResultSet rs, String column) throws SQLException {
        String s = rs.getString(column);
        return rs.wasNull() ? null : s;
    }

    private static Perfil mapFila(ResultSet rs) throws SQLException {
        return new Perfil(
            rs.getInt("id_perfil"),
            rs.getInt("id_usuario"),
            rs.getString("nombre_usuario"),
            getStringOrNull(rs, "foto_perfil_url"),
            getStringOrNull(rs, "genero"),
            getDateOrNull(rs, "fecha_nacimiento"),
            getStringOrNull(rs, "profesion"),
            Optional.ofNullable(rs.getString("tema")).orElse("SISTEMA"),
            Optional.ofNullable(rs.getString("idioma")).orElse("es"),
            getDateTimeOrNull(rs, "fecha_creacion"),
            getDateTimeOrNull(rs, "fecha_ult_modificacion")
        );
    }

    private static void setStringOrNull(PreparedStatement ps, int index, String value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.VARCHAR);
        } else {
            ps.setString(index, value);
        }
    }

    private static void setDateOrNull(PreparedStatement ps, int index, LocalDate value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.DATE);
        } else {
            ps.setDate(index, Date.valueOf(value));
        }
    }

    /**
     * Obtiene el perfil asociado a un usuario.
     *
     * @param idUsuario identificador del usuario
     * @return perfil encontrado o {@code null}
     */
    public Perfil obtenerPorUsuario(int idUsuario) {
        String sql = "SELECT * FROM perfiles WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
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
     * Inserta un perfil mínimo tras el alta de usuario.
     */
    public boolean insertar(Perfil perfil) {
        String sql = "INSERT INTO perfiles (id_usuario, nombre_usuario, tema, idioma) VALUES (?, ?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, perfil.getIdUsuario());
            ps.setString(2, perfil.getNombreUsuario());
            ps.setString(3, perfil.getTema() != null ? perfil.getTema() : "SISTEMA");
            ps.setString(4, perfil.getIdioma() != null ? perfil.getIdioma() : "es");

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Actualiza todos los campos editables del perfil.
     */
    public boolean actualizar(Perfil perfil) {
        String sql = "UPDATE perfiles SET nombre_usuario=?, foto_perfil_url=?, genero=?, fecha_nacimiento=?, "
            + "profesion=?, tema=?, idioma=?, fecha_ult_modificacion=? WHERE id_perfil=?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, perfil.getNombreUsuario());
            setStringOrNull(ps, 2, perfil.getFotoPerfilUrl());
            setStringOrNull(ps, 3, perfil.getGenero());
            setDateOrNull(ps, 4, perfil.getFechaNacimiento());
            setStringOrNull(ps, 5, perfil.getProfesion());
            ps.setString(6, perfil.getTema() != null ? perfil.getTema() : "SISTEMA");
            ps.setString(7, perfil.getIdioma() != null ? perfil.getIdioma() : "es");
            ps.setTimestamp(8, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(9, perfil.getIdPerfil());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
