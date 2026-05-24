package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Rol;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso JDBC a la tabla {@code roles}: listado y consulta por identificador.
 *
 * @author Tracklify
 */
public class RolDAO {

    /**
     * Obtiene todos los roles definidos en la base de datos.
     *
     * @return lista de roles; vacía si no hay filas o falla la consulta
     */
    public List<Rol> obtenerTodos() {
        List<Rol> roles = new ArrayList<>();
        String sql = "SELECT * FROM roles";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Rol r = new Rol(
                    rs.getInt("id_rol"),
                    rs.getString("nombre_rol"),
                    rs.getString("descripcion_rol")
                );
                roles.add(r);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return roles;
    }

    /**
     * Busca un rol por su clave primaria.
     *
     * @param idRol identificador {@code id_rol}
     * @return entidad encontrada, o {@code null} si no existe o falla la consulta
     */
    public Rol obtenerPorId(int idRol) {
        String sql = "SELECT * FROM roles WHERE id_rol = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRol);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Rol(
                    rs.getInt("id_rol"),
                    rs.getString("nombre_rol"),
                    rs.getString("descripcion_rol")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
