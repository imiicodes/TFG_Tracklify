package com.mycompany.tracklify.models;

/**
 * Representa un rol de acceso en la aplicación (usuario, administrador, etc.).
 * Se persiste en la tabla {@code roles} y se asocia a cada cuenta en {@code usuarios}.
 */
public class Rol {

    private int idRol;
    private String nombreRol;
    private String descripcionRol;

    /** Constructor vacío para mapeo desde JDBC. */
    public Rol() {}

    /**
     * @param idRol          identificador del rol
     * @param nombreRol      nombre en catálogo
     * @param descripcionRol descripción del rol
     */
    public Rol(int idRol, String nombreRol, String descripcionRol) {
        this.idRol = idRol;
        this.nombreRol = nombreRol;
        this.descripcionRol = descripcionRol;
    }

    /** @return identificador del rol */
    public int getIdRol() { return idRol; }

    /** @param idRol identificador del rol */
    public void setIdRol(int idRol) { this.idRol = idRol; }

    /** @return nombre del rol */
    public String getNombreRol() { return nombreRol; }

    /** @param nombreRol nombre del rol */
    public void setNombreRol(String nombreRol) { this.nombreRol = nombreRol; }

    /** @return descripción del rol y sus permisos */
    public String getDescripcionRol() { return descripcionRol; }

    /** @param descripcionRol descripción del rol y sus permisos */
    public void setDescripcionRol(String descripcionRol) { this.descripcionRol = descripcionRol; }

    /** @return representación para depuración */
    @Override
    public String toString() {
        return "Rol{id=" + idRol + ", nombre=" + nombreRol + "}";
    }
}
