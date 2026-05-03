/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;

/**
 * DTO de solo lectura mapeado desde vistas SQL de progreso semanal.
 *
 * @author imii
 */
public class ProgresoSemanal {

    private final int idUsuario;
    private final int idHabito;
    private final String nombreHabito;
    private final int objetivoVeces;
    private final String periodoObjetivo;
    private final int anioSemana;
    private final int numSemana;
    private final int vecesCompletado;
    private final int vecesPendientes;
    private final double porcentajeCompletado;
    private final long segundosTotalesSemana;

    public ProgresoSemanal(int idUsuario, int idHabito, String nombreHabito, int objetivoVeces,
                           String periodoObjetivo, int anioSemana, int numSemana, int vecesCompletado,
                           int vecesPendientes, double porcentajeCompletado, long segundosTotalesSemana) {
        this.idUsuario = idUsuario;
        this.idHabito = idHabito;
        this.nombreHabito = nombreHabito;
        this.objetivoVeces = objetivoVeces;
        this.periodoObjetivo = periodoObjetivo;
        this.anioSemana = anioSemana;
        this.numSemana = numSemana;
        this.vecesCompletado = vecesCompletado;
        this.vecesPendientes = vecesPendientes;
        this.porcentajeCompletado = porcentajeCompletado;
        this.segundosTotalesSemana = segundosTotalesSemana;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public int getIdHabito() {
        return idHabito;
    }

    public String getNombreHabito() {
        return nombreHabito;
    }

    public int getObjetivoVeces() {
        return objetivoVeces;
    }

    public String getPeriodoObjetivo() {
        return periodoObjetivo;
    }

    public int getAnioSemana() {
        return anioSemana;
    }

    public int getNumSemana() {
        return numSemana;
    }

    public int getVecesCompletado() {
        return vecesCompletado;
    }

    public int getVecesPendientes() {
        return vecesPendientes;
    }

    public double getPorcentajeCompletado() {
        return porcentajeCompletado;
    }

    public long getSegundosTotalesSemana() {
        return segundosTotalesSemana;
    }
}
