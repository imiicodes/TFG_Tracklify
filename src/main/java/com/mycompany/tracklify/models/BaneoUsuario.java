package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Fila de la tabla {@code baneos_usuario} para listados en el panel de administración.
 *
 * @author Tracklify
 */
public class BaneoUsuario {

    private int idBaneo;
    private int idUsuario;
    private int baneadoPor;
    private String motivo;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;
    private boolean activo;

    public BaneoUsuario() {
    }

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

    public int getIdBaneo() {
        return idBaneo;
    }

    public void setIdBaneo(int idBaneo) {
        this.idBaneo = idBaneo;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public int getBaneadoPor() {
        return baneadoPor;
    }

    public void setBaneadoPor(int baneadoPor) {
        this.baneadoPor = baneadoPor;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDateTime fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDateTime getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDateTime fechaFin) {
        this.fechaFin = fechaFin;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }
}
