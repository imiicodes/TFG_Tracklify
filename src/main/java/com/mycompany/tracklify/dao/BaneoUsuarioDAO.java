package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.BaneoUsuario;
import java.time.LocalDateTime;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Acceso a datos para baneos de usuarios y actualización del estado de cuenta.
 *
 * @author Tracklify
 */
public class BaneoUsuarioDAO {

    private static LocalDateTime getTsOrNull(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    private static BaneoUsuario mapFila(ResultSet rs) throws SQLException {
        return new BaneoUsuario(
            rs.getInt("id_baneo"),
            rs.getInt("id_usuario"),
            rs.getInt("baneado_por"),
            rs.getString("motivo"),
            getTsOrNull(rs, "fecha_inicio"),
            getTsOrNull(rs, "fecha_fin"),
            rs.getInt("activo") != 0
        );
    }

    /**
     * Registra un baneo permanente y desactiva la cuenta del usuario.
     *
     * @param idUsuario usuario baneado
     * @param idAdmin   administrador que ejecuta el baneo
     * @param motivo    texto descriptivo del motivo
     * @return {@code true} si el insert y la actualización de cuenta tuvieron éxito
     */
    public boolean banear(int idUsuario, int idAdmin, String motivo) {

        String insertSql = "INSERT INTO baneos_usuario (id_usuario, baneado_por, motivo, fecha_inicio, fecha_fin, activo) "
            + "VALUES (?, ?, ?, NOW(), NULL, 1)";
        String updateSql = "UPDATE usuarios SET cuenta_activa = 0 WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try (PreparedStatement psIns = con.prepareStatement(insertSql);
                 PreparedStatement psUpd = con.prepareStatement(updateSql)) {

                psIns.setInt(1, idUsuario);
                psIns.setInt(2, idAdmin);
                psIns.setString(3, motivo);
                if (psIns.executeUpdate() <= 0) {
                    con.rollback();
                    return false;
                }

                psUpd.setInt(1, idUsuario);
                if (psUpd.executeUpdate() <= 0) {
                    con.rollback();
                    return false;
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cierra un baneo activo y reactiva la cuenta del usuario asociado.
     *
     * @param idBaneo identificador del registro de baneo
     * @param idAdmin administrador que desbanea
     * @return {@code true} si se actualizó el baneo y la cuenta del usuario
     */
    public boolean desbanear(int idBaneo, int idAdmin) {

        String selectSql = "SELECT id_usuario FROM baneos_usuario WHERE id_baneo = ? AND activo = 1";
        String updateBaneoSql = "UPDATE baneos_usuario SET activo = 0, desbaneado_por = ?, fecha_desbaneo = NOW() "
            + "WHERE id_baneo = ? AND activo = 1";
        String updateUsuarioSql = "UPDATE usuarios SET cuenta_activa = 1 WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                int idUsuario;
                try (PreparedStatement psSel = con.prepareStatement(selectSql)) {
                    psSel.setInt(1, idBaneo);
                    ResultSet rs = psSel.executeQuery();
                    if (!rs.next()) {
                        con.rollback();
                        return false;
                    }
                    idUsuario = rs.getInt("id_usuario");
                }

                try (PreparedStatement psBan = con.prepareStatement(updateBaneoSql)) {
                    psBan.setInt(1, idAdmin);
                    psBan.setInt(2, idBaneo);
                    if (psBan.executeUpdate() <= 0) {
                        con.rollback();
                        return false;
                    }
                }

                try (PreparedStatement psUsr = con.prepareStatement(updateUsuarioSql)) {
                    psUsr.setInt(1, idUsuario);
                    if (psUsr.executeUpdate() <= 0) {
                        con.rollback();
                        return false;
                    }
                }

                con.commit();
                return true;

            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Lista todos los baneos marcados como activos.
     *
     * @return lista de {@link BaneoUsuario}; puede estar vacía
     */
    public List<BaneoUsuario> obtenerBaneosActivos() {

        List<BaneoUsuario> lista = new ArrayList<>();
        String sql = "SELECT * FROM baneos_usuario WHERE activo = 1 ORDER BY fecha_inicio DESC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(mapFila(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    /**
     * Indica si el usuario tiene al menos un baneo activo.
     *
     * @param idUsuario identificador del usuario
     * @return {@code true} si existe baneo con {@code activo = 1}
     */
    public boolean estaActualmenteBaneado(int idUsuario) {

        String sql = "SELECT 1 FROM baneos_usuario WHERE id_usuario = ? AND activo = 1 LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
