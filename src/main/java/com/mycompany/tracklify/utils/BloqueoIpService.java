package com.mycompany.tracklify.utils;

import com.mycompany.tracklify.dao.BloqueoIpDAO;

/**
 * Lógica de negocio para bloqueo temporal por IP tras varios intentos de login fallidos.
 *
 * @author Tracklify
 */
public class BloqueoIpService {

    private final BloqueoIpDAO bloqueoIpDAO;

    /**
     * Construye el servicio con un {@link BloqueoIpDAO} nuevo.
     */
    public BloqueoIpService() {
        this.bloqueoIpDAO = new BloqueoIpDAO();
    }

    /**
     * Construye el servicio con un DAO inyectado (útil para pruebas).
     *
     * @param bloqueoIpDAO implementación de acceso a {@code bloqueos_ip}
     */
    public BloqueoIpService(BloqueoIpDAO bloqueoIpDAO) {
        this.bloqueoIpDAO = bloqueoIpDAO;
    }

    /**
     * Indica si la IP está bloqueada por superar el umbral de intentos fallidos.
     *
     * @param ip dirección IP a comprobar
     * @return {@code true} si existe bloqueo vigente ({@code bloqueada_hasta > NOW()})
     */
    public boolean estaBlockeada(String ip) {
        return bloqueoIpDAO.estaIpBloqueada(ip);
    }

    /**
     * Registra un intento fallido: crea o actualiza el contador y, si llega a 5 o más,
     * establece el bloqueo de un minuto.
     *
     * @param ip dirección IP desde la que se intentó el acceso
     */
    public void registrarFallo(String ip) {
        bloqueoIpDAO.upsertIncrementarFallo(ip);
    }

    /**
     * Limpia el estado de fallos y bloqueo para la IP tras un inicio de sesión correcto.
     *
     * @param ip dirección IP del cliente
     */
    public void registrarExito(String ip) {
        bloqueoIpDAO.resetearTrasExito(ip);
    }
}
