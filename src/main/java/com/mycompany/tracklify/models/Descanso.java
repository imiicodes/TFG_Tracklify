/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 *
 * @author imii
 */
public class Descanso {

    private int idDescanso;
    private int idRegistro;
    private LocalDateTime marcaTiempoInicio;
    private LocalDateTime marcaTiempoFin;
    private Integer duracionSegundos;
    private String motivo;

    public Descanso() {
    }

    public Descanso(int idDescanso, int idRegistro, LocalDateTime marcaTiempoInicio,
                    LocalDateTime marcaTiempoFin, Integer duracionSegundos, String motivo) {
        this.idDescanso = idDescanso;
        this.idRegistro = idRegistro;
        this.marcaTiempoInicio = marcaTiempoInicio;
        this.marcaTiempoFin = marcaTiempoFin;
        this.duracionSegundos = duracionSegundos;
        this.motivo = motivo;
    }

    public int getIdDescanso() {
        return idDescanso;
    }

    public void setIdDescanso(int idDescanso) {
        this.idDescanso = idDescanso;
    }

    public int getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

    public LocalDateTime getMarcaTiempoInicio() {
        return marcaTiempoInicio;
    }

    public void setMarcaTiempoInicio(LocalDateTime marcaTiempoInicio) {
        this.marcaTiempoInicio = marcaTiempoInicio;
    }

    public LocalDateTime getMarcaTiempoFin() {
        return marcaTiempoFin;
    }

    public void setMarcaTiempoFin(LocalDateTime marcaTiempoFin) {
        this.marcaTiempoFin = marcaTiempoFin;
    }

    public Integer getDuracionSegundos() {
        return duracionSegundos;
    }

    public void setDuracionSegundos(Integer duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    @Override
    public String toString() {
        return "Descanso{idDescanso=" + idDescanso + ", idRegistro=" + idRegistro + "}";
    }
}
