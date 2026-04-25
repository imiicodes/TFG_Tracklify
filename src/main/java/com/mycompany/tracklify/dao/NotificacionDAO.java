/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Notificacion;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author imii
 */


public class NotificacionDAO {

    public List<Notificacion> obtenerPorUsuario(int usuarioId) {
        List<Notificacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones WHERE usuario_id = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Notificacion n = new Notificacion(
                    rs.getInt("id_notificacion"),
                    rs.getInt("usuario_id"),
                    rs.getString("mensaje_notificacion"),
                    rs.getBoolean("estado_notificacion")
                );
                lista.add(n);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean insertar(Notificacion notificacion) {
        String sql = "INSERT INTO notificaciones (usuario_id, mensaje_notificacion, estado_notificacion) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, notificacion.getUsuarioId());
            ps.setString(2, notificacion.getMensajeNotificacion());
            ps.setBoolean(3, notificacion.isEstadoNotificacion());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean marcarComoLeida(int idNotificacion) {
        String sql = "UPDATE notificaciones SET estado_notificacion = 1 WHERE id_notificacion = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idNotificacion);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean eliminar(int idNotificacion) {
        String sql = "DELETE FROM notificaciones WHERE id_notificacion = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idNotificacion);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
