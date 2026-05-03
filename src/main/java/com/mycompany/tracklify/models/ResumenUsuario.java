/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;

/**
 * DTO de solo lectura mapeado desde la vista {@code v_resumen_usuario}.
 *
 * @author imii
 */
public class ResumenUsuario {

    private final int idUsuario;
    private final String nombreUsuario;
    private final int totalHabitosActivos;
    private final int totalCumplimientos;
    private final int habitosCompletadosHoy;
    private final double tasaExitoGlobal;

    public ResumenUsuario(int idUsuario, String nombreUsuario, int totalHabitosActivos,
                          int totalCumplimientos, int habitosCompletadosHoy, double tasaExitoGlobal) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.totalHabitosActivos = totalHabitosActivos;
        this.totalCumplimientos = totalCumplimientos;
        this.habitosCompletadosHoy = habitosCompletadosHoy;
        this.tasaExitoGlobal = tasaExitoGlobal;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public int getTotalHabitosActivos() {
        return totalHabitosActivos;
    }

    public int getTotalCumplimientos() {
        return totalCumplimientos;
    }

    public int getHabitosCompletadosHoy() {
        return habitosCompletadosHoy;
    }

    public double getTasaExitoGlobal() {
        return tasaExitoGlobal;
    }
}
