package com.mycompany.tracklify.models;

/**
 * DTO de solo lectura con el progreso semanal de un hábito para un usuario.
 * Se obtiene desde vistas SQL de progreso semanal, no se persiste como entidad independiente.
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

    /**
     * @param idUsuario              usuario propietario
     * @param idHabito               hábito evaluado
     * @param nombreHabito           nombre para mostrar
     * @param objetivoVeces          repeticiones objetivo en el periodo
     * @param periodoObjetivo        etiqueta del periodo (p. ej. semana)
     * @param anioSemana             año ISO de la semana
     * @param numSemana              número de semana ISO
     * @param vecesCompletado        cumplimientos en la semana
     * @param vecesPendientes        repeticiones restantes
     * @param porcentajeCompletado   porcentaje 0–100
     * @param segundosTotalesSemana  tiempo registrado en segundos
     */
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

    /** @return identificador del usuario */
    public int getIdUsuario() {
        return idUsuario;
    }

    /** @return identificador del hábito */
    public int getIdHabito() {
        return idHabito;
    }

    /** @return nombre del hábito */
    public String getNombreHabito() {
        return nombreHabito;
    }

    /** @return veces objetivo en el periodo configurado */
    public int getObjetivoVeces() {
        return objetivoVeces;
    }

    /** @return nombre del periodo del objetivo (p. ej. SEMANA) */
    public String getPeriodoObjetivo() {
        return periodoObjetivo;
    }

    /** @return año ISO de la semana consultada */
    public int getAnioSemana() {
        return anioSemana;
    }

    /** @return número de semana ISO */
    public int getNumSemana() {
        return numSemana;
    }

    /** @return cumplimientos registrados en la semana */
    public int getVecesCompletado() {
        return vecesCompletado;
    }

    /** @return cumplimientos que faltan para alcanzar el objetivo */
    public int getVecesPendientes() {
        return vecesPendientes;
    }

    /** @return porcentaje de objetivo cumplido (0–100) */
    public double getPorcentajeCompletado() {
        return porcentajeCompletado;
    }

    /** @return segundos totales dedicados al hábito en la semana */
    public long getSegundosTotalesSemana() {
        return segundosTotalesSemana;
    }
}
