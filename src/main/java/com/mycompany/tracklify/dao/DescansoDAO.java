package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Descanso;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code descansos}.
 *
 * @author Tracklify
 */
public class DescansoDAO {

    private static Descanso mapFila(ResultSet rs) throws SQLException {
        Timestamp fin = rs.getTimestamp("marca_tiempo_fin");
        int dur = rs.getInt("duracion_segundos");
        Integer durObj = rs.wasNull() ? null : dur;
        return new Descanso(
            rs.getInt("id_descanso"),
            rs.getInt("id_registro"),
            rs.getTimestamp("marca_tiempo_inicio").toLocalDateTime(),
            fin != null ? fin.toLocalDateTime() : null,
            durObj,
            rs.getString("motivo")
        );
    }

    public boolean insertar(Descanso d) {
        String sql = "INSERT INTO descansos (id_registro, marca_tiempo_inicio) VALUES (?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, d.getIdRegistro());
            ps.setTimestamp(2, Timestamp.valueOf(d.getMarcaTiempoInicio()));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean cerrarDescanso(int idDescanso, LocalDateTime fin, int duracion) {
        String sql = "UPDATE descansos SET marca_tiempo_fin = ?, duracion_segundos = ? WHERE id_descanso = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(fin));
            ps.setInt(2, duracion);
            ps.setInt(3, idDescanso);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Descanso> obtenerPorRegistro(int idRegistro) {
        List<Descanso> lista = new ArrayList<>();
        String sql = "SELECT * FROM descansos WHERE id_registro = ? ORDER BY marca_tiempo_inicio";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idRegistro);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapFila(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
