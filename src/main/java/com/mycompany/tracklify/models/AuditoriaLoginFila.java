package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Fila de auditoría de intentos de inicio de sesión.
 *
 * @author Tracklify
 */
public class AuditoriaLoginFila {

    private final String emailIntento;
    private final String ipAddress;
    private final String resultado;
    private final LocalDateTime fechaIntento;

    /**
     * Construye un registro de auditoría para mostrar en tabla.
     *
     * @param emailIntento  correo usado en el intento
     * @param ipAddress     dirección IP
     * @param resultado     código de resultado (éxito, fallo, etc.)
     * @param fechaIntento  momento del intento
     */
    public AuditoriaLoginFila(String emailIntento, String ipAddress, String resultado,
                              LocalDateTime fechaIntento) {
        this.emailIntento = emailIntento != null ? emailIntento : "";
        this.ipAddress = ipAddress != null ? ipAddress : "";
        this.resultado = resultado != null ? resultado : "";
        this.fechaIntento = fechaIntento;
    }

    /**
     * @return email del intento
     */
    public String getEmailIntento() {
        return emailIntento;
    }

    /**
     * @return IP del cliente
     */
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * @return resultado del intento
     */
    public String getResultado() {
        return resultado;
    }

    /**
     * @return fecha y hora del intento
     */
    public LocalDateTime getFechaIntento() {
        return fechaIntento;
    }

    /**
     * Texto para mostrar en tabla (sin prefijo {@code T} ISO).
     *
     * @return cadena legible o «—»
     */
    public String getFechaIntentoTexto() {
        return fechaIntento == null ? "—" : fechaIntento.toString().replace('T', ' ');
    }
}
