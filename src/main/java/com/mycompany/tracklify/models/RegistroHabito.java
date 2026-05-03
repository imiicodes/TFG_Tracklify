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
public class RegistroHabito {

    private int idRegistro;
    private int idHabito;
    private LocalDateTime marcaTiempoInicio;
    private LocalDateTime marcaTiempoFin;
    private Integer duracionSegundos;
    private String estadoRegistro;
    private boolean esObjetivo = true;
    private String comentario;
    private Integer anioSemana;
    private Integer numSemana;

    public RegistroHabito() {
    }

    public RegistroHabito(int idRegistro, int idHabito, LocalDateTime marcaTiempoInicio,
                          LocalDateTime marcaTiempoFin, Integer duracionSegundos, String estadoRegistro,
                          boolean esObjetivo, String comentario, Integer anioSemana, Integer numSemana) {
        this.idRegistro = idRegistro;
        this.idHabito = idHabito;
        this.marcaTiempoInicio = marcaTiempoInicio;
        this.marcaTiempoFin = marcaTiempoFin;
        this.duracionSegundos = duracionSegundos;
        this.estadoRegistro = estadoRegistro;
        this.esObjetivo = esObjetivo;
        this.comentario = comentario;
        this.anioSemana = anioSemana;
        this.numSemana = numSemana;
    }

    public int getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

    public int getIdHabito() {
        return idHabito;
    }

    public void setIdHabito(int idHabito) {
        this.idHabito = idHabito;
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

    public String getEstadoRegistro() {
        return estadoRegistro;
    }

    public void setEstadoRegistro(String estadoRegistro) {
        this.estadoRegistro = estadoRegistro;
    }

    public boolean isEsObjetivo() {
        return esObjetivo;
    }

    public void setEsObjetivo(boolean esObjetivo) {
        this.esObjetivo = esObjetivo;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public Integer getAnioSemana() {
        return anioSemana;
    }

    public void setAnioSemana(Integer anioSemana) {
        this.anioSemana = anioSemana;
    }

    public Integer getNumSemana() {
        return numSemana;
    }

    public void setNumSemana(Integer numSemana) {
        this.numSemana = numSemana;
    }

    @Override
    public String toString() {
        return "RegistroHabito{idRegistro=" + idRegistro + ", idHabito=" + idHabito
            + ", marcaTiempoInicio=" + marcaTiempoInicio + ", estadoRegistro=" + estadoRegistro + "}";
    }
}
