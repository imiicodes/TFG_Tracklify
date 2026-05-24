package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Representa un bloqueo temporal de una dirección IP tras intentos fallidos de inicio de sesión.
 * Se mapea desde la tabla {@code bloqueos_ip} cuando el bloqueo sigue vigente.
 */
public class BloqueoIpVigente {

    private String ipAddress;
    private int intentosFallidos;
    private LocalDateTime fechaUltIntento;
    private LocalDateTime bloqueadaHasta;

    /** Constructor vacío para mapeo desde JDBC. */
    public BloqueoIpVigente() {
    }

    /**
     * @param ipAddress         dirección bloqueada
     * @param intentosFallidos  intentos fallidos acumulados
     * @param fechaUltIntento   último intento registrado
     * @param bloqueadaHasta    fin del bloqueo temporal
     */
    public BloqueoIpVigente(String ipAddress, int intentosFallidos,
                            LocalDateTime fechaUltIntento, LocalDateTime bloqueadaHasta) {
        this.ipAddress = ipAddress;
        this.intentosFallidos = intentosFallidos;
        this.fechaUltIntento = fechaUltIntento;
        this.bloqueadaHasta = bloqueadaHasta;
    }

    /** @return dirección IP bloqueada */
    public String getIpAddress() {
        return ipAddress;
    }

    /** @param ipAddress dirección IP bloqueada */
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    /** @return número de intentos fallidos acumulados */
    public int getIntentosFallidos() {
        return intentosFallidos;
    }

    /** @param intentosFallidos número de intentos fallidos acumulados */
    public void setIntentosFallidos(int intentosFallidos) {
        this.intentosFallidos = intentosFallidos;
    }

    /** @return fecha y hora del último intento fallido */
    public LocalDateTime getFechaUltIntento() {
        return fechaUltIntento;
    }

    /** @param fechaUltIntento fecha y hora del último intento fallido */
    public void setFechaUltIntento(LocalDateTime fechaUltIntento) {
        this.fechaUltIntento = fechaUltIntento;
    }

    /** @return fecha y hora hasta la que permanece bloqueada la IP */
    public LocalDateTime getBloqueadaHasta() {
        return bloqueadaHasta;
    }

    /** @param bloqueadaHasta fecha y hora hasta la que permanece bloqueada la IP */
    public void setBloqueadaHasta(LocalDateTime bloqueadaHasta) {
        this.bloqueadaHasta = bloqueadaHasta;
    }
}
