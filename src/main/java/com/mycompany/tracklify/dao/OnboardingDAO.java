package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.RespuestaOnboarding;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Clase de acceso a datos (DAO) para el sistema de onboarding de Tracklify.
 *
 * <p>Gestiona todas las operaciones de base de datos relacionadas con el
 * proceso de onboarding: persistencia de respuestas y gestión del flag
 * {@code onboarding_completado} en la tabla {@code usuarios}.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see RespuestaOnboarding
 * @see ConexionBD
 */
public class OnboardingDAO {

    /**
     * Guarda en la BD una lista de respuestas para una pregunta concreta.
     *
     * <p>Usa {@code addBatch} para insertar todas las respuestas en una
     * sola llamada a la BD. Si la lista está vacía (pregunta saltada),
     * no realiza ninguna operación.</p>
     *
     * @param respuestas lista de {@link RespuestaOnboarding} a persistir
     */
    public void guardarRespuestas(List<RespuestaOnboarding> respuestas) {

        if (respuestas == null || respuestas.isEmpty()) return;

        String sql = "INSERT INTO onboarding_respuestas "
                   + "(id_usuario, numero_pregunta, respuesta) VALUES (?, ?, ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            for (RespuestaOnboarding r : respuestas) {
                ps.setInt(1, r.getIdUsuario());
                ps.setInt(2, r.getNumeroPregunta());
                ps.setString(3, r.getRespuesta());
                ps.addBatch();
            }

            ps.executeBatch();

        } catch (SQLException e) {
            System.err.println("Error al guardar respuestas de onboarding: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Marca el onboarding como completado para un usuario dado.
     *
     * <p>Actualiza {@code onboarding_completado = 1} en la tabla {@code usuarios}.
     * Una vez marcado, el onboarding no volverá a mostrarse al usuario.</p>
     *
     * @param idUsuario el identificador del usuario que ha completado el onboarding
     * @return {@code true} si la actualización fue exitosa, {@code false} en caso contrario
     */
    public boolean marcarOnboardingCompletado(int idUsuario) {

        String sql = "UPDATE usuarios SET onboarding_completado = 1 WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al marcar onboarding como completado: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Comprueba si un usuario ya ha completado el onboarding.
     *
     * <p>CORRECCIÓN: comprueba también el caso {@code NULL} de forma explícita.
     * Si la columna es {@code NULL} o {@code 0}, el usuario NO ha completado
     * el onboarding. Solo devuelve {@code true} cuando el valor es exactamente
     * {@code 1}. Esto evita el bug donde usuarios registrados antes de añadir
     * la columna tenían {@code NULL} y la comprobación fallaba silenciosamente.</p>
     *
     * @param idUsuario el identificador del usuario a comprobar
     * @return {@code true} si el onboarding ya fue completado, {@code false} si es nuevo usuario
     */
    public boolean haCompletadoOnboarding(int idUsuario) {

        // Usamos COALESCE para tratar NULL como 0 directamente en SQL
        String sql = "SELECT COALESCE(onboarding_completado, 0) AS completado "
                   + "FROM usuarios WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                // Solo devuelve true si el valor es exactamente 1
                return rs.getInt("completado") == 1;
            }

        } catch (SQLException e) {
            System.err.println("Error al comprobar onboarding: " + e.getMessage());
            e.printStackTrace();
        }

        // Por seguridad, si falla la consulta asumimos que no lo ha completado
        return false;
    }
}