package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Modelo que representa una notificación programada para un usuario en Tracklify.
 *
 * <p>Cada notificación está asociada a un hábito/tarea del usuario y
 * se dispara en el momento indicado por {@code fechaProgramada}.
 * El usuario puede completarla o posponerla, actualizando los campos
 * {@code estadoNotificacion} y {@code pospuestaHasta} respectivamente.</p>
 *
 * <p>Se persiste en la tabla {@code notificaciones} de la BD con la estructura:</p>
 * <ul>
 *   <li>{@code estado_notificacion = 0} → pendiente</li>
 *   <li>{@code estado_notificacion = 1} → completada</li>
 *   <li>{@code pospuesta_hasta != NULL} → pospuesta hasta esa fecha</li>
 * </ul>
 *
 * @author Tracklify
 * @version 1.0
 * @see com.mycompany.tracklify.dao.NotificacionDAO
 */
public class Notificacion {

    /** Identificador único de la notificación (auto-generado por la BD). */
    private int idNotificacion;

    /** Identificador del usuario al que pertenece esta notificación. */
    private int usuarioId;

    /**
     * Texto del mensaje que se mostrará en el pop-up y en la notificación
     * de escritorio (ej: "¡Hora de tu hábito: Meditación matutina!").
     */
    private String mensajeNotificacion;

    /**
     * Estado de la notificación.
     * {@code false} = pendiente, {@code true} = completada/leída.
     */
    private boolean estadoNotificacion;

    /**
     * Fecha y hora programada para disparar la notificación.
     * Se calcula al crear el hábito a partir de su frecuencia y hora configurada.
     */
    private LocalDateTime fechaProgramada;

    /**
     * Fecha y hora hasta la que el usuario ha pospuesto la notificación.
     * {@code null} si no ha sido pospuesta.
     * Cuando el scheduler detecta que {@code ahora >= pospuestaHasta},
     * vuelve a mostrar el pop-up.
     */
    private LocalDateTime pospuestaHasta;

    /**
     * Constructor vacío requerido para instanciación desde el DAO.
     */
    public Notificacion() {}

    /**
     * Constructor completo para crear una notificación con todos sus campos.
     *
     * @param idNotificacion     identificador único
     * @param usuarioId          identificador del usuario propietario
     * @param mensajeNotificacion texto del mensaje
     * @param estadoNotificacion  {@code false} pendiente, {@code true} completada
     * @param fechaProgramada    momento en que debe dispararse
     * @param pospuestaHasta     momento hasta el que está pospuesta, o {@code null}
     */
    public Notificacion(int idNotificacion, int usuarioId, String mensajeNotificacion,
                        boolean estadoNotificacion, LocalDateTime fechaProgramada,
                        LocalDateTime pospuestaHasta) {
        this.idNotificacion = idNotificacion;
        this.usuarioId = usuarioId;
        this.mensajeNotificacion = mensajeNotificacion;
        this.estadoNotificacion = estadoNotificacion;
        this.fechaProgramada = fechaProgramada;
        this.pospuestaHasta = pospuestaHasta;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    /** @return el identificador único de la notificación */
    public int getIdNotificacion() { return idNotificacion; }
    /** @param idNotificacion el identificador a establecer */
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }

    /** @return el identificador del usuario propietario */
    public int getUsuarioId() { return usuarioId; }
    /** @param usuarioId el identificador del usuario */
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    /** @return el texto del mensaje de la notificación */
    public String getMensajeNotificacion() { return mensajeNotificacion; }
    /** @param mensajeNotificacion el texto del mensaje */
    public void setMensajeNotificacion(String mensajeNotificacion) {
        this.mensajeNotificacion = mensajeNotificacion;
    }

    /** @return {@code true} si la notificación ya fue completada */
    public boolean isEstadoNotificacion() { return estadoNotificacion; }
    /** @param estadoNotificacion el nuevo estado */
    public void setEstadoNotificacion(boolean estadoNotificacion) {
        this.estadoNotificacion = estadoNotificacion;
    }

    /** @return la fecha y hora programada para disparar la notificación */
    public LocalDateTime getFechaProgramada() { return fechaProgramada; }
    /** @param fechaProgramada la fecha y hora de disparo */
    public void setFechaProgramada(LocalDateTime fechaProgramada) {
        this.fechaProgramada = fechaProgramada;
    }

    /** @return la fecha hasta la que está pospuesta, o {@code null} */
    public LocalDateTime getPospuestaHasta() { return pospuestaHasta; }
    /** @param pospuestaHasta la nueva fecha de posposición */
    public void setPospuestaHasta(LocalDateTime pospuestaHasta) {
        this.pospuestaHasta = pospuestaHasta;
    }

    /**
     * Indica si la notificación está actualmente pospuesta.
     *
     * @return {@code true} si tiene una fecha de posposición futura activa
     */
    public boolean estaPospuesta() {
        return pospuestaHasta != null && LocalDateTime.now().isBefore(pospuestaHasta);
    }

    /** @return representación legible para depuración */
    @Override
    public String toString() {
        return "Notificacion{id=" + idNotificacion
            + ", usuario=" + usuarioId
            + ", mensaje='" + mensajeNotificacion + "'"
            + ", estado=" + estadoNotificacion
            + ", programada=" + fechaProgramada + "}";
    }
}