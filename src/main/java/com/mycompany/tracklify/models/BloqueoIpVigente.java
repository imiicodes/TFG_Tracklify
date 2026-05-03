package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Representa una fila de {@code bloqueos_ip} con bloqueo temporal aún vigente.
 *
 * @author Tracklify
 */
public class BloqueoIpVigente {

    private String ipAddress;
    private int intentosFallidos;
    private LocalDateTime fechaUltIntento;
    private LocalDateTime bloqueadaHasta;

    public BloqueoIpVigente() {
    }

    public BloqueoIpVigente(String ipAddress, int intentosFallidos,
                            LocalDateTime fechaUltIntento, LocalDateTime bloqueadaHasta) {
        this.ipAddress = ipAddress;
        this.intentosFallidos = intentosFallidos;
        this.fechaUltIntento = fechaUltIntento;
        this.bloqueadaHasta = bloqueadaHasta;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    public LocalDateTime getFechaUltIntento() {
        return fechaUltIntento;
    }

    public void setFechaUltIntento(LocalDateTime fechaUltIntento) {
        this.fechaUltIntento = fechaUltIntento;
    }

    public LocalDateTime getBloqueadaHasta() {
        return bloqueadaHasta;
    }

    public void setBloqueadaHasta(LocalDateTime bloqueadaHasta) {
        this.bloqueadaHasta = bloqueadaHasta;
    }
}
