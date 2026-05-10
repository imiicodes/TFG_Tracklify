package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.AdminReporteUsuarioFila;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Consultas JDBC para el listado de usuarios en la sección de informes del administrador.
 *
 * @author Tracklify
 */
public class AdminReportesDAO {

    /**
     * Lista usuarios con cuenta activa, resumen de hábitos y marca temporal del último registro de hábito.
     *
     * @return filas para la tabla de reportes; puede estar vacía
     */
    public List<AdminReporteUsuarioFila> listarUsuariosParaInformes() {
        List<AdminReporteUsuarioFila> lista = new ArrayList<>();
        String sql = "SELECT u.id_usuario, "
            + "COALESCE(NULLIF(TRIM(v.nombre_usuario), ''), u.email_usuario) AS nombre, "
            + "u.email_usuario, "
            + "COALESCE(v.total_habitos_activos, 0) AS habitos_activos, "
            + "(SELECT MAX(COALESCE(r.marca_tiempo_fin, r.marca_tiempo_inicio)) "
            + " FROM registros_habitos r "
            + " INNER JOIN habitos h2 ON h2.id_habito = r.id_habito "
            + " WHERE h2.id_usuario = u.id_usuario) AS ultimo_ts "
            + "FROM usuarios u "
            + "LEFT JOIN v_resumen_usuario v ON v.id_usuario = u.id_usuario "
            + "WHERE u.cuenta_activa = 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("ultimo_ts");
                LocalDateTime ultimo = ts != null ? ts.toLocalDateTime() : null;
                lista.add(new AdminReporteUsuarioFila(
                    rs.getInt("id_usuario"),
                    rs.getString("nombre"),
                    rs.getString("email_usuario"),
                    rs.getInt("habitos_activos"),
                    ultimo
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }
}
