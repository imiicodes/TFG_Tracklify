package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.AuditoriaLoginFila;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso JDBC a la tabla {@code auditoria_login}: listado para administración y mantenimiento.
 *
 * <p>Se asume la existencia de la columna {@code fecha_intento} (p. ej. {@code TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP}
 * en MySQL) para ordenar y filtrar.</p>
 *
 * @author Tracklify
 */
public class AuditoriaLoginDAO {

    /**
     * Lista los últimos intentos de login ordenados del más reciente al más antiguo.
     *
     * @param filtroResultado {@code null} o cadena vacía o {@code "Todos"} para no filtrar;
     *                        en caso contrario filtra por columna {@code resultado}
     * @return como máximo 100 filas
     */
    public List<AuditoriaLoginFila> listarUltimos100(String filtroResultado) {
        List<AuditoriaLoginFila> lista = new ArrayList<>();
        boolean filtrar = filtroResultado != null && !filtroResultado.isBlank() && !"Todos".equalsIgnoreCase(filtroResultado);

        String sql = "SELECT email_intento, ip_address, resultado, fecha_intento "
            + "FROM auditoria_login "
            + (filtrar ? "WHERE resultado = ? " : "")
            + "ORDER BY fecha_intento DESC LIMIT 100";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            if (filtrar) {
                ps.setString(1, filtroResultado.trim());
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_intento");
                    LocalDateTime fecha = ts != null ? ts.toLocalDateTime() : null;
                    lista.add(new AuditoriaLoginFila(
                        rs.getString("email_intento"),
                        rs.getString("ip_address"),
                        rs.getString("resultado"),
                        fecha
                    ));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene la fecha del último intento con resultado {@code EXITO} para un email concreto.
     *
     * @param email correo del administrador (o cualquier cuenta)
     * @return fecha del último éxito, o {@code null} si no hay filas
     */
    public LocalDateTime obtenerUltimoExitoPorEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        String sql = "SELECT fecha_intento FROM auditoria_login "
            + "WHERE email_intento = ? AND resultado = 'EXITO' "
            + "ORDER BY fecha_intento DESC LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp("fecha_intento");
                    return ts != null ? ts.toLocalDateTime() : null;
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Elimina registros de auditoría anteriores a 30 días respecto a {@code NOW()}.
     *
     * @return número de filas borradas, o {@code 0} si falla
     */
    public int eliminarMasDe30Dias() {
        String sql = "DELETE FROM auditoria_login WHERE fecha_intento < DATE_SUB(NOW(), INTERVAL 30 DAY)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            return ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
    }
}
