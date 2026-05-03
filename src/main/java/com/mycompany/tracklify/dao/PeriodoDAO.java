package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Periodo;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para la tabla {@code periodos} (unidades de tiempo de hábitos y notificaciones).
 *
 * @author Tracklify
 */
public class PeriodoDAO {

    /**
     * Obtiene todos los periodos ordenados por identificador.
     *
     * @return lista de {@link Periodo}, posiblemente vacía
     */
    public List<Periodo> obtenerTodos() {

        List<Periodo> lista = new ArrayList<>();
        String sql = "SELECT * FROM periodos ORDER BY id_periodo";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapearFila(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Busca un periodo por su nombre exacto en base de datos (p. ej. {@code HORA}, {@code DIA}).
     *
     * @param nombre nombre del periodo
     * @return el periodo encontrado o {@code null}
     */
    public Periodo obtenerPorNombre(String nombre) {

        if (nombre == null) {
            return null;
        }

        String sql = "SELECT * FROM periodos WHERE nombre = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearFila(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Obtiene un periodo por su identificador.
     *
     * @param id identificador {@code id_periodo}
     * @return el periodo o {@code null}
     */
    public Periodo obtenerPorId(int id) {

        String sql = "SELECT * FROM periodos WHERE id_periodo = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapearFila(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Mapea una fila del {@link ResultSet} a un objeto {@link Periodo}.
     *
     * @param rs cursor posicionado en la fila
     * @return entidad periodo
     * @throws SQLException si falla la lectura de columnas
     */
    private static Periodo mapearFila(ResultSet rs) throws SQLException {
        return new Periodo(
            rs.getInt("id_periodo"),
            rs.getString("nombre"),
            rs.getString("descripcion")
        );
    }
}
