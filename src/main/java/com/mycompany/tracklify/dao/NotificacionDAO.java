package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Notificacion;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de acceso a datos (DAO) para la entidad {@link Notificacion}.
 *
 * <p>Gestiona todas las operaciones sobre la tabla {@code notificaciones},
 * incluyendo la creación automática al guardar un hábito, la consulta de
 * notificaciones pendientes para el scheduler, y las acciones del usuario
 * (completar y posponer).</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see Notificacion
 * @see ConexionBD
 */
public class NotificacionDAO {

    /**
     * Inserta una nueva notificación en la BD con su fecha programada.
     *
     * <p>Se llama automáticamente desde {@code MainViewController.guardarNuevaTarea()}
     * cada vez que el usuario crea un hábito, calculando la próxima fecha de disparo
     * a partir de la frecuencia y hora configuradas.</p>
     *
     * @param notificacion la {@link Notificacion} a persistir
     * @return {@code true} si el insert fue exitoso, {@code false} en caso contrario
     */
    public boolean insertar(Notificacion notificacion) {

        String sql = "INSERT INTO notificaciones "
                   + "(id_usuario, mensaje, estado, fecha_programada) "
                   + "VALUES (?, ?, 'PENDIENTE', ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, notificacion.getUsuarioId());
            ps.setString(2, notificacion.getMensajeNotificacion());

            // Convertimos LocalDateTime a Timestamp para JDBC
            if (notificacion.getFechaProgramada() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(notificacion.getFechaProgramada()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar notificación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Inserta el primer recordatorio asociado a un hábito recién creado.
     *
     * <p>Persiste {@code id_habito} en la columna correspondiente; el mensaje se guarda tal cual,
     * sin prefijos con el identificador.</p>
     *
     * @param idUsuario        destinatario
     * @param idHabito         hábito relacionado ({@code id_habito} en la tabla)
     * @param mensaje          texto del recordatorio
     * @param fechaProgramada  primera ejecución programada
     * @return {@code true} si el insert fue correcto
     */
    public boolean insertarRecordatorioHabito(int idUsuario, int idHabito, String mensaje,
                                                LocalDateTime fechaProgramada) {

        String sql = "INSERT INTO notificaciones "
            + "(id_usuario, id_habito, mensaje, estado, fecha_programada) "
            + "VALUES (?, ?, ?, 'PENDIENTE', ?)";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ps.setInt(2, idHabito);
            ps.setString(3, mensaje);
            ps.setTimestamp(4, Timestamp.valueOf(fechaProgramada));

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al insertar recordatorio de hábito: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene las notificaciones pendientes que deben dispararse ahora mismo.
     *
     * <p>El scheduler {@link com.mycompany.tracklify.utils.NotificacionScheduler}
     * llama a este método cada minuto. Devuelve notificaciones que cumplan:</p>
     * <ul>
     *   <li>Pertenecen al usuario indicado</li>
     *   <li>No están completadas ({@code estado = 0})</li>
     *   <li>Su {@code fecha_programada} ya ha llegado (es pasada o presente)</li>
     *   <li>No están actualmente pospuestas ({@code pospuesta_hasta} es NULL
     *       o ya ha pasado su tiempo de posposición)</li>
     * </ul>
     *
     * @param usuarioId identificador del usuario cuyas notificaciones se comprueban
     * @return lista de {@link Notificacion} que deben mostrarse ahora
     */
    public List<Notificacion> obtenerPendientesAhora(int usuarioId) {

        List<Notificacion> lista = new ArrayList<>();

        // Seleccionamos notificaciones cuya fecha programada ya pasó
        // y que no estén pospuestas activamente ni completadas
        String sql = "SELECT * FROM notificaciones "
                   + "WHERE id_usuario = ? "
                   + "AND estado = 'PENDIENTE' "
                   + "AND fecha_programada <= NOW() "
                   + "AND (pospuesta_hasta IS NULL OR pospuesta_hasta <= NOW())";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Notificacion n = mapearNotificacion(rs);
                lista.add(n);
            }

        } catch (SQLException e) {
            System.err.println("Error al obtener notificaciones pendientes: " + e.getMessage());
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Obtiene todas las notificaciones de un usuario para mostrar en el dashboard.
     *
     * @param usuarioId identificador del usuario
     * @return lista completa de {@link Notificacion} del usuario, ordenada por fecha
     */
    public List<Notificacion> obtenerPorUsuario(int usuarioId) {

        List<Notificacion> lista = new ArrayList<>();
        String sql = "SELECT * FROM notificaciones "
                   + "WHERE id_usuario = ? ORDER BY fecha_programada ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, usuarioId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(mapearNotificacion(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return lista;
    }

    /**
     * Marca una notificación como completada ({@code estado = 1}).
     *
     * <p>Se llama cuando el usuario pulsa "Completado" en el pop-up.
     * Una notificación completada no vuelve a aparecer.</p>
     *
     * @param idNotificacion identificador de la notificación a completar
     * @return {@code true} si la actualización fue exitosa
     */
    public boolean marcarComoCompletada(int idNotificacion) {

        String sql = "UPDATE notificaciones "
                   + "SET estado = 'LEIDA' "
                   + "WHERE id_notificacion = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idNotificacion);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al completar notificación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Pospone una notificación hasta la fecha indicada.
     *
     * <p>Se llama cuando el usuario pulsa "Posponer" en el pop-up y elige
     * un intervalo (15 minutos, 1 hora o mañana). El scheduler no volverá
     * a mostrarla hasta que {@code pospuesta_hasta} haya pasado.</p>
     *
     * @param idNotificacion identificador de la notificación a posponer
     * @param hasta          fecha y hora hasta la que se pospone
     * @return {@code true} si la actualización fue exitosa
     */
    public boolean posponer(int idNotificacion, LocalDateTime hasta) {

        String sql = "UPDATE notificaciones "
                   + "SET pospuesta_hasta = ? "
                   + "WHERE id_notificacion = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setTimestamp(1, Timestamp.valueOf(hasta));
            ps.setInt(2, idNotificacion);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("Error al posponer notificación: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina una notificación de la BD por su identificador.
     *
     * @param idNotificacion identificador de la notificación a eliminar
     * @return {@code true} si la eliminación fue exitosa
     */
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

    /**
     * Construye un objeto {@link Notificacion} a partir de una fila del {@link ResultSet}.
     *
     * <p>Método auxiliar privado para evitar duplicación de código de mapeo
     * entre los distintos métodos de consulta.</p>
     *
     * @param rs el {@link ResultSet} posicionado en la fila a mapear
     * @return la {@link Notificacion} construida con los datos de la fila
     * @throws SQLException si ocurre un error al leer columnas del ResultSet
     */
    private Notificacion mapearNotificacion(ResultSet rs) throws SQLException {

        Notificacion n = new Notificacion();
        n.setIdNotificacion(rs.getInt("id_notificacion"));
        n.setUsuarioId(rs.getInt("id_usuario"));
        n.setMensajeNotificacion(rs.getString("mensaje"));
        n.setEstadoNotificacion(rs.getString("estado").equals("LEIDA") || rs.getString("estado").equals("ENVIADA"));

        // Convertimos Timestamp a LocalDateTime (puede ser null)
        Timestamp fechaProg = rs.getTimestamp("fecha_programada");
        if (fechaProg != null) {
            n.setFechaProgramada(fechaProg.toLocalDateTime());
        }

        Timestamp pospuesta = rs.getTimestamp("pospuesta_hasta");
        if (pospuesta != null) {
            n.setPospuestaHasta(pospuesta.toLocalDateTime());
        }

        return n;
    }
}