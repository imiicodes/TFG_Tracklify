package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Acceso a datos para la tabla {@code bloqueos_ip} (intentos fallidos y ventana de bloqueo).
 *
 * @author Tracklify
 */
public class BloqueoIpDAO {

    /**
     * Comprueba si la IP tiene un bloqueo activo (fecha de fin de bloqueo posterior a ahora).
     *
     * @param ip dirección IP
     * @return {@code true} si existe fila con {@code bloqueada_hasta > NOW()}
     */
    public boolean estaIpBloqueada(String ip) {

        String sql = "SELECT 1 FROM bloqueos_ip WHERE ip_address = ? AND bloqueada_hasta > NOW() LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ip);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserta la IP con un fallo o incrementa intentos; si tras incrementar hay 5 o más fallos,
     * fija {@code bloqueada_hasta} a un minuto a partir de ahora.
     *
     * @param ip dirección IP
     */
    public void upsertIncrementarFallo(String ip) {

        String sql = "INSERT INTO bloqueos_ip (ip_address, intentos_fallidos, fecha_ult_intento, bloqueada_hasta) "
            + "VALUES (?, 1, NOW(), NULL) "
            + "ON DUPLICATE KEY UPDATE "
            + "intentos_fallidos = intentos_fallidos + 1, "
            + "fecha_ult_intento = NOW(), "
            + "bloqueada_hasta = IF(intentos_fallidos + 1 >= 5, DATE_ADD(NOW(), INTERVAL 1 MINUTE), bloqueada_hasta)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ip);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Pone a cero los intentos fallidos y elimina la fecha de bloqueo para la IP indicada.
     *
     * @param ip dirección IP
     */
    public void resetearTrasExito(String ip) {

        String sql = "UPDATE bloqueos_ip SET intentos_fallidos = 0, bloqueada_hasta = NULL WHERE ip_address = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, ip);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
