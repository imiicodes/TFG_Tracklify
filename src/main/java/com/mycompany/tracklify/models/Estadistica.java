/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;
import java.math.BigDecimal;
import java.time.LocalDate;
/**
 *
 * @author imii
 */

public class Estadistica {

    private int idEstadistica;
    private int idUsuario;
    private LocalDate fechaGeneracion;
    private BigDecimal porcentajeCompletado;
    private int rachaActual;
    private int tareasCompletadas;
    private int tareasTotales;

    public Estadistica() {}

    public Estadistica(int idEstadistica, int idUsuario, LocalDate fechaGeneracion,
                       BigDecimal porcentajeCompletado, int rachaActual,
                       int tareasCompletadas, int tareasTotales) {
        this.idEstadistica = idEstadistica;
        this.idUsuario = idUsuario;
        this.fechaGeneracion = fechaGeneracion;
        this.porcentajeCompletado = porcentajeCompletado;
        this.rachaActual = rachaActual;
        this.tareasCompletadas = tareasCompletadas;
        this.tareasTotales = tareasTotales;
    }

    public int getIdEstadistica() { return idEstadistica; }
    public void setIdEstadistica(int idEstadistica) { this.idEstadistica = idEstadistica; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public LocalDate getFechaGeneracion() { return fechaGeneracion; }
    public void setFechaGeneracion(LocalDate fechaGeneracion) { this.fechaGeneracion = fechaGeneracion; }

    public BigDecimal getPorcentajeCompletado() { return porcentajeCompletado; }
    public void setPorcentajeCompletado(BigDecimal porcentajeCompletado) { this.porcentajeCompletado = porcentajeCompletado; }

    public int getRachaActual() { return rachaActual; }
    public void setRachaActual(int rachaActual) { this.rachaActual = rachaActual; }

    public int getTareasCompletadas() { return tareasCompletadas; }
    public void setTareasCompletadas(int tareasCompletadas) { this.tareasCompletadas = tareasCompletadas; }

    public int getTareasTotales() { return tareasTotales; }
    public void setTareasTotales(int tareasTotales) { this.tareasTotales = tareasTotales; }

    @Override
    public String toString() {
        return "Estadistica{id=" + idEstadistica + ", usuario=" + idUsuario +
               ", completado=" + porcentajeCompletado + "%, racha=" + rachaActual + "}";
    }
}
