package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

/**
 * Acceso a datos para tokens de verificación y reset de contraseña
 * en la tabla {@code configuracion_email}.
 *
 * @author Tracklify
 */
public class ConfiguracionEmailDAO {

    /**
     * Inserta un registro de token con {@code usado = 0}.
     *
     * @return identificador generado, o {@code 0} si falla
     */
    public int insertar(int idUsuario, String codigo, String tipo, LocalDateTime fechaExpiracion) {

        String sql = "INSERT INTO configuracion_email (id_usuario, codigo, tipo, fecha_expiracion, usado) "
            + "VALUES (?, ?, ?, ?, 0)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, codigo);
            ps.setString(3, tipo);
            ps.setTimestamp(4, Timestamp.valueOf(fechaExpiracion));

            int filas = ps.executeUpdate();
            if (filas <= 0) {
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
     * Marca como usado el token que coincida y siga siendo válido (no expirado, {@code usado = 0}).
     *
     * @return {@code true} si se actualizó exactamente una fila
     */
    public boolean marcarTokenValidoComoUsado(int idUsuario, String codigo, String tipo) {

        String sql = "UPDATE configuracion_email SET usado = 1 "
            + "WHERE id_usuario = ? AND codigo = ? AND tipo = ? AND usado = 0 AND fecha_expiracion > NOW()";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setString(2, codigo);
            ps.setString(3, tipo);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
