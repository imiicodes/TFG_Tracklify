/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;

/**
 *
 * @author imii
 */
public class Periodo {

    private int idPeriodo;
    private String nombre;
    private String descripcion;

    public Periodo() {
    }

    public Periodo(int idPeriodo, String nombre, String descripcion) {
        this.idPeriodo = idPeriodo;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public int getIdPeriodo() {
        return idPeriodo;
    }

    public void setIdPeriodo(int idPeriodo) {
        this.idPeriodo = idPeriodo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @Override
    public String toString() {
        return "Periodo{idPeriodo=" + idPeriodo + ", nombre=" + nombre + "}";
    }
}
