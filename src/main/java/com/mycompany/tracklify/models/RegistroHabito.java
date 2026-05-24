package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Representa un registro de cumplimiento o sesión de un hábito.
 * Se persiste en la tabla {@code registros_habitos} con marcas de tiempo, duración y estado.
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

    /** Constructor vacío para formularios y mapeo desde JDBC. */
    public RegistroHabito() {
    }

    /**
     * @param idRegistro         identificador del registro
     * @param idHabito           hábito asociado
     * @param marcaTiempoInicio  inicio de la sesión o cumplimiento
     * @param marcaTiempoFin     fin opcional
     * @param duracionSegundos   duración en segundos
     * @param estadoRegistro     p. ej. {@code PENDIENTE}, {@code COMPLETADO}
     * @param esObjetivo         si cuenta para el objetivo del hábito
     * @param comentario         nota opcional del usuario
     * @param anioSemana         año ISO de la semana
     * @param numSemana          número de semana ISO
     */
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

    /** @return identificador del registro */
    public int getIdRegistro() {
        return idRegistro;
    }

    /** @param idRegistro identificador del registro */
    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

    /** @return identificador del hábito asociado */
    public int getIdHabito() {
        return idHabito;
    }

    /** @param idHabito identificador del hábito asociado */
    public void setIdHabito(int idHabito) {
        this.idHabito = idHabito;
    }

    /** @return marca de tiempo de inicio de la sesión */
    public LocalDateTime getMarcaTiempoInicio() {
        return marcaTiempoInicio;
    }

    /** @param marcaTiempoInicio marca de tiempo de inicio de la sesión */
    public void setMarcaTiempoInicio(LocalDateTime marcaTiempoInicio) {
        this.marcaTiempoInicio = marcaTiempoInicio;
    }

    /** @return marca de tiempo de fin de la sesión */
    public LocalDateTime getMarcaTiempoFin() {
        return marcaTiempoFin;
    }

    /** @param marcaTiempoFin marca de tiempo de fin de la sesión */
    public void setMarcaTiempoFin(LocalDateTime marcaTiempoFin) {
        this.marcaTiempoFin = marcaTiempoFin;
    }

    /** @return duración en segundos, o {@code null} si no se calculó */
    public Integer getDuracionSegundos() {
        return duracionSegundos;
    }

    /** @param duracionSegundos duración en segundos */
    public void setDuracionSegundos(Integer duracionSegundos) {
        this.duracionSegundos = duracionSegundos;
    }

    /** @return estado del registro (p. ej. COMPLETADO, EN_CURSO) */
    public String getEstadoRegistro() {
        return estadoRegistro;
    }

    /** @param estadoRegistro estado del registro */
    public void setEstadoRegistro(String estadoRegistro) {
        this.estadoRegistro = estadoRegistro;
    }

    /** @return {@code true} si el registro cuenta para el objetivo del hábito */
    public boolean isEsObjetivo() {
        return esObjetivo;
    }

    /** @param esObjetivo indica si cuenta para el objetivo */
    public void setEsObjetivo(boolean esObjetivo) {
        this.esObjetivo = esObjetivo;
    }

    /** @return comentario opcional del usuario */
    public String getComentario() {
        return comentario;
    }

    /** @param comentario comentario opcional del usuario */
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    /** @return año ISO de la semana del registro */
    public Integer getAnioSemana() {
        return anioSemana;
    }

    /** @param anioSemana año ISO de la semana del registro */
    public void setAnioSemana(Integer anioSemana) {
        this.anioSemana = anioSemana;
    }

    /** @return número de semana ISO del registro */
    public Integer getNumSemana() {
        return numSemana;
    }

    /** @param numSemana número de semana ISO del registro */
    public void setNumSemana(Integer numSemana) {
        this.numSemana = numSemana;
    }

    /** @return representación para depuración */
    @Override
    public String toString() {
        return "RegistroHabito{idRegistro=" + idRegistro + ", idHabito=" + idHabito
            + ", marcaTiempoInicio=" + marcaTiempoInicio + ", estadoRegistro=" + estadoRegistro + "}";
    }
}
