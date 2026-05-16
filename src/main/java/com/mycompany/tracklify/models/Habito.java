/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;

import java.time.LocalDate;

/**
 *
 * @author imii
 */
public class Habito {

    private int idHabito;
    private int idUsuario;
    private Integer idCategoria;
    private String nombreHabito;
    private String descripcionHabito;
    private Integer duracionValor;
    private Integer duracionPeriodoId;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private int notifFrecuenciaValor = 1;
    private Integer notifFrecuenciaId;
    private int objetivoVeces = 1;
    private Integer objetivoPeriodoId;
    /** Días de la semana en que aplica el objetivo semanal (p. ej. {@code LUNES,MIERCOLES}). */
    private String diasSemana;
    private String estado = "ACTIVO";

    public Habito() {
    }

    public Habito(int idHabito, int idUsuario, Integer idCategoria, String nombreHabito,
                  String descripcionHabito, Integer duracionValor, Integer duracionPeriodoId,
                  LocalDate fechaInicio, LocalDate fechaFin, int notifFrecuenciaValor,
                  Integer notifFrecuenciaId, int objetivoVeces, Integer objetivoPeriodoId,
                  String estado) {
        this.idHabito = idHabito;
        this.idUsuario = idUsuario;
        this.idCategoria = idCategoria;
        this.nombreHabito = nombreHabito;
        this.descripcionHabito = descripcionHabito;
        this.duracionValor = duracionValor;
        this.duracionPeriodoId = duracionPeriodoId;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.notifFrecuenciaValor = notifFrecuenciaValor;
        this.notifFrecuenciaId = notifFrecuenciaId;
        this.objetivoVeces = objetivoVeces;
        this.objetivoPeriodoId = objetivoPeriodoId;
        this.estado = estado;
    }

    public int getIdHabito() {
        return idHabito;
    }

    public void setIdHabito(int idHabito) {
        this.idHabito = idHabito;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public Integer getIdCategoria() {
        return idCategoria;
    }

    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    public String getNombreHabito() {
        return nombreHabito;
    }

    public void setNombreHabito(String nombreHabito) {
        this.nombreHabito = nombreHabito;
    }

    public String getDescripcionHabito() {
        return descripcionHabito;
    }

    public void setDescripcionHabito(String descripcionHabito) {
        this.descripcionHabito = descripcionHabito;
    }

    public Integer getDuracionValor() {
        return duracionValor;
    }

    public void setDuracionValor(Integer duracionValor) {
        this.duracionValor = duracionValor;
    }

    public Integer getDuracionPeriodoId() {
        return duracionPeriodoId;
    }

    public void setDuracionPeriodoId(Integer duracionPeriodoId) {
        this.duracionPeriodoId = duracionPeriodoId;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    public int getNotifFrecuenciaValor() {
        return notifFrecuenciaValor;
    }

    public void setNotifFrecuenciaValor(int notifFrecuenciaValor) {
        this.notifFrecuenciaValor = notifFrecuenciaValor;
    }

    public Integer getNotifFrecuenciaId() {
        return notifFrecuenciaId;
    }

    public void setNotifFrecuenciaId(Integer notifFrecuenciaId) {
        this.notifFrecuenciaId = notifFrecuenciaId;
    }

    public int getObjetivoVeces() {
        return objetivoVeces;
    }

    public void setObjetivoVeces(int objetivoVeces) {
        this.objetivoVeces = objetivoVeces;
    }

    public Integer getObjetivoPeriodoId() {
        return objetivoPeriodoId;
    }

    public void setObjetivoPeriodoId(Integer objetivoPeriodoId) {
        this.objetivoPeriodoId = objetivoPeriodoId;
    }

    public String getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(String diasSemana) {
        this.diasSemana = diasSemana;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Habito{idHabito=" + idHabito + ", idUsuario=" + idUsuario + ", nombreHabito="
            + nombreHabito + ", fechaInicio=" + fechaInicio + ", estado=" + estado + "}";
    }
}
