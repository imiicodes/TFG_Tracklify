package com.mycompany.tracklify.models;

/**
 * Representa una unidad de tiempo de referencia (día, semana, mes, etc.) del catálogo.
 * Se persiste en la tabla {@code periodos} y se usa para duración, notificaciones y objetivos de hábitos.
 */
public class Periodo {

    private int idPeriodo;
    private String nombre;
    private String descripcion;

    /** Constructor vacío para mapeo desde JDBC. */
    public Periodo() {
    }

    /**
     * @param idPeriodo   identificador del periodo
     * @param nombre      nombre en catálogo (p. ej. {@code SEMANA})
     * @param descripcion descripción legible
     */
    public Periodo(int idPeriodo, String nombre, String descripcion) {
        this.idPeriodo = idPeriodo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    /** @return identificador del periodo */
    public int getIdPeriodo() {
        return idPeriodo;
    }

    /** @param idPeriodo identificador del periodo */
    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    /** @return nombre corto del periodo (p. ej. DIA, SEMANA) */
    public String getNombre() {
        return nombre;
    }

    /** @param nombre nombre corto del periodo */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /** @return descripción legible del periodo */
    public String getDescripcion() {
        return descripcion;
    }

    /** @param descripcion descripción legible del periodo */
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    /** @return representación para depuración */
    @Override
    public String toString() {
        return "Periodo{idPeriodo=" + idPeriodo + ", nombre=" + nombre + "}";
    }
}
