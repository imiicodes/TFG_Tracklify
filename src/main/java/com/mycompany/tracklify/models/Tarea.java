/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;

/**
 *
 * @author imii
 */


public class Tarea {

    private int idTarea;
    private int usuarioId;
    private String nombreTarea;
    private String descripcionTarea;
    private String frecuenciaTarea;

    public Tarea() {}

    public Tarea(int idTarea, int usuarioId, String nombreTarea,
                 String descripcionTarea, String frecuenciaTarea) {
        this.idTarea = idTarea;
        this.usuarioId = usuarioId;
        this.nombreTarea = nombreTarea;
        this.descripcionTarea = descripcionTarea;
        this.frecuenciaTarea = frecuenciaTarea;
    }

    public int getIdTarea() { return idTarea; }
    public void setIdTarea(int idTarea) { this.idTarea = idTarea; }

    public int getUsuarioId() { return usuarioId; }
    public void setUsuarioId(int usuarioId) { this.usuarioId = usuarioId; }

    public String getNombreTarea() { return nombreTarea; }
    public void setNombreTarea(String nombreTarea) { this.nombreTarea = nombreTarea; }

    public String getDescripcionTarea() { return descripcionTarea; }
    public void setDescripcionTarea(String descripcionTarea) { this.descripcionTarea = descripcionTarea; }

    public String getFrecuenciaTarea() { return frecuenciaTarea; }
    public void setFrecuenciaTarea(String frecuenciaTarea) { this.frecuenciaTarea = frecuenciaTarea; }

    @Override
    public String toString() {
        return "Tarea{id=" + idTarea + ", nombre=" + nombreTarea + ", frecuencia=" + frecuenciaTarea + "}";
    }
}
