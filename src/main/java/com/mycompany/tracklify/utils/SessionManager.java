package com.mycompany.tracklify.utils;

import com.mycompany.tracklify.models.Usuario;

/**
 * Gestiona la sesión activa del usuario en la aplicación.
 *
 * <p>Implementa el patrón Singleton para garantizar que solo existe
 * una sesión activa en toda la aplicación en todo momento.</p>
 *
 * <p>Se utiliza para compartir el usuario autenticado entre
 * los distintos controladores sin necesidad de pasarlo por parámetros.</p>
 *
 * @author Tracklify
 * @version 1.0
 */
public class SessionManager {

    /** Instancia única de la clase (patrón Singleton). */
    private static SessionManager instancia;

    /** Usuario que ha iniciado sesión actualmente. */
    private Usuario usuarioActual;

    /**
     * Constructor privado para evitar instanciación externa.
     * Solo se puede obtener la instancia a través de {@link #getInstancia()}.
     */
    private SessionManager() {}

    /**
     * Devuelve la instancia única de {@code SessionManager}.
     *
     * <p>Si no existe una instancia previa, la crea. Este método
     * garantiza que solo haya un {@code SessionManager} activo.</p>
     *
     * @return la instancia única de {@code SessionManager}
     */
    public static SessionManager getInstancia() {
        if (instancia == null) {
            instancia = new SessionManager();
        }
        return instancia;
    }

    /**
     * Establece el usuario que ha iniciado sesión.
     *
     * @param usuario el {@link Usuario} autenticado que se desea guardar en sesión
     */
    public void setUsuarioActual(Usuario usuario) {
        this.usuarioActual = usuario;
    }

    /**
     * Devuelve el usuario actualmente autenticado.
     *
     * @return el {@link Usuario} en sesión, o {@code null} si no hay sesión activa
     */
    public Usuario getUsuarioActual() {
        return usuarioActual;
    }

    /**
     * Comprueba si hay un usuario con sesión iniciada.
     *
     * @return {@code true} si hay un usuario autenticado, {@code false} en caso contrario
     */
    public boolean haySesionActiva() {
        return usuarioActual != null;
    }

    /**
     * Comprueba si el usuario actual tiene rol de administrador.
     *
     * <p>El rol de administrador tiene {@code rol_id = 2} en la base de datos.</p>
     *
     * @return {@code true} si el usuario es administrador, {@code false} en caso contrario
     */
    public boolean esAdministrador() {
        return usuarioActual != null && usuarioActual.getRolId() == 2;
    }

    /**
     * Cierra la sesión actual eliminando el usuario almacenado.
     *
     * <p>Tras llamar a este método, {@link #getUsuarioActual()} devolverá {@code null}.</p>
     */
    public void cerrarSesion() {
        this.usuarioActual = null;
    }
}