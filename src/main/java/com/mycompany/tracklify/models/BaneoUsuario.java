package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Representa un baneo temporal o permanente de un usuario de la aplicación.
 * Se persiste en la tabla {@code baneos_usuario} y se usa en el panel de administración
 * para listar y gestionar restricciones de acceso.
 */
public class BaneoUsuario {

    private int idBaneo;
    private int idUsuario;
    private int baneadoPor;
    private String motivo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private boolean activo;

    /** Constructor vacío para formularios y mapeo desde JDBC. */
    public BaneoUsuario() {
    }

    /**
     * @param idBaneo      identificador del baneo
     * @param idUsuario    usuario sancionado
     * @param baneadoPor   administrador que aplica el baneo
     * @param motivo       texto del motivo
     * @param fechaInicio  inicio de la restricción
     * @param fechaFin     fin opcional; {@code null} si es indefinido
     * @param activo       si el baneo está vigente
     */
    public BaneoUsuario(int idBaneo, int idUsuario, int baneadoPor, String motivo,
                        LocalDateTime fechaInicio, LocalDateTime fechaFin, boolean activo) {
        this.idBaneo = idBaneo;
        this.idUsuario = idUsuario;
        this.baneadoPor = baneadoPor;
        this.motivo = motivo;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.activo = activo;
    }

    /** @return identificador del registro de baneo */
    public int getIdBaneo() {
        return idBaneo;
    }

    /** @param idBaneo identificador del registro de baneo */
    public void setIdBaneo(int idBaneo) {
        this.idBaneo = idBaneo;
    }

    /** @return identificador del usuario baneado */
    public int getIdUsuario() {
        return idUsuario;
    }

    /** @param idUsuario identificador del usuario baneado */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /** @return identificador del administrador que aplicó el baneo */
    public int getBaneadoPor() {
        return baneadoPor;
    }

    /** @param baneadoPor identificador del administrador que aplicó el baneo */
    public void setBaneadoPor(int baneadoPor) {
        this.baneadoPor = baneadoPor;
    }

    /** @return motivo o descripción del baneo */
    public String getMotivo() {
        return motivo;
    }

    /** @param motivo motivo o descripción del baneo */
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    /** @return fecha y hora de inicio del baneo */
    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    /** @param fechaInicio fecha y hora de inicio del baneo */
    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /** @return fecha y hora de fin del baneo, o {@code null} si es indefinido */
    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    /** @param fechaFin fecha y hora de fin del baneo */
    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    /** @return {@code true} si el baneo está vigente en la base de datos */
    public boolean isActivo() {
        return activo;
    }

    /** @param activo indica si el baneo está vigente */
    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
