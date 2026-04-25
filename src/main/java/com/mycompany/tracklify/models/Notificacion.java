/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;

/**
 *
 * @author imii
 */

public class Notificacion {

    private int idNotificacion;
    private int usuarioId;
    private String mensajeNotificacion;
    private boolean estadoNotificacion;

    public Notificacion() {}

    public Notificacion(int idNotificacion, int usuarioId,
                        String mensajeNotificacion, boolean estadoNotificacion) {
        this.idNotificacion = idNotificacion;
        this.usuarioId = usuarioId;
        this.mensajeNotificacion = mensajeNotificacion;
        this.estadoNotificacion = estadoNotificacion;
    }

    public int getIdNotificacion() { return idNotificacion; }
    public void setIdNotificacion(int idNotificacion) { this.idNotificacion = idNotificacion; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getMensajeNotificacion() { return mensajeNotificacion; }
    public void setMensajeNotificacion(String mensajeNotificacion) { this.mensajeNotificacion = mensajeNotificacion; }

    public boolean isEstadoNotificacion() { return estadoNotificacion; }
    public void setEstadoNotificacion(boolean estadoNotificacion) { this.estadoNotificacion = estadoNotificacion; }

    @Override
    public String toString() {
        return "Notificacion{id=" + idNotificacion + ", mensaje=" + mensajeNotificacion + ", leida=" + estadoNotificacion + "}";
    }
}
