package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.ProgresoSemanal;
import com.mycompany.tracklify.models.ResumenUsuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas JDBC de solo lectura contra vistas SQL de informes y agregados de usuario.
 *
 * @author Tracklify
 */
public class EstadisticaDAO {

    /**
     * Mapea una fila de {@code ResultSet} a {@link ProgresoSemanal}.
     *
     * @param rs cursor posicionado en la fila
     * @return DTO construido
     * @throws SQLException si falla la lectura de columnas
     */
    private static ProgresoSemanal mapProgresoSemanal(ResultSet rs) throws SQLException {
        return new ProgresoSemanal(
            rs.getInt("id_usuario"),
            rs.getInt("id_habito"),
            rs.getString("nombre_habito"),
            rs.getInt("objetivo_semana"),
            "SEMANA",
            rs.getInt("anio"),
            rs.getInt("semana_actual"),
            rs.getInt("completados_esta_semana"),
            Math.max(rs.getInt("objetivo_semana") - rs.getInt("completados_esta_semana"), 0),
            rs.getInt("objetivo_semana") > 0
                ? Math.round(rs.getInt("completados_esta_semana") * 100.0 / rs.getInt("objetivo_semana") * 10) / 10.0
                : 0.0,
            rs.getLong("segundos_semana")
        );
    }

    /**
     * Lista el progreso semanal de un hábito desde la vista {@code v_progreso_semanal}.
     *
     * @param idHabito identificador del hábito
     * @return filas de la vista, posiblemente vacía
     */
    public List<ProgresoSemanal> obtenerProgresoSemanal(int idHabito) {
        List<ProgresoSemanal> lista = new ArrayList<>();
        String sql = "SELECT * FROM v_progreso_semanal WHERE id_habito = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idHabito);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapProgresoSemanal(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Obtiene la fila de agregados del usuario desde la vista {@code v_resumen_usuario}.
     *
     * <p>Columnas esperadas y mapeo a {@link ResumenUsuario}:</p>
     * <ul>
     *   <li>{@code id_usuario} → identificador</li>
     *   <li>{@code nombre_usuario} → nombre para saludo o informes</li>
     *   <li>{@code total_habitos_activos} → hábitos activos del usuario</li>
     *   <li>{@code total_cumplimientos} → total de cumplimientos registrados</li>
     *   <li>{@code habitos_completados_hoy} → hábitos distintos completados en el día actual</li>
     *   <li>{@code tasa_exito_global} → ratio o porcentaje de éxito (0–1 o 0–100 según la vista)</li>
     * </ul>
     *
     * @param idUsuario propietario de los datos
     * @return DTO con los agregados o {@code null} si no hay fila para ese usuario
     */
    public ResumenUsuario obtenerResumenUsuario(int idUsuario) {
        String sql = "SELECT * FROM v_resumen_usuario WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new ResumenUsuario(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre_usuario"),
                    rs.getInt("total_habitos_activos"),
                    rs.getInt("total_cumplimientos"),
                    rs.getInt("habitos_completados_hoy"),
                    rs.getDouble("tasa_exito_global")
                );
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Lista el informe semanal del usuario desde {@code v_informe_semanal}.
     *
     * @param idUsuario identificador del usuario
     * @return filas de la vista, posiblemente vacía
     */
    public List<ProgresoSemanal> obtenerInformeSemanal(int idUsuario) {
        List<ProgresoSemanal> lista = new ArrayList<>();
        String sql = "SELECT * FROM v_informe_semanal WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapProgresoSemanal(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
