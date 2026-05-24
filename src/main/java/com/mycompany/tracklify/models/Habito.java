package com.mycompany.tracklify.models;

import java.time.LocalDate;

/**
 * Representa un hábito o rutina que el usuario desea seguir en Tracklify.
 * Se persiste en la tabla {@code habitos} con objetivos, notificaciones, fechas y estado.
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

    /** Constructor vacío para formularios y mapeo desde JDBC. */
    public Habito() {
    }

    /**
     * @param idHabito            identificador del hábito
     * @param idUsuario           propietario
     * @param idCategoria         categoría opcional
     * @param nombreHabito        nombre visible
     * @param descripcionHabito   descripción opcional
     * @param duracionValor       valor de duración de sesión
     * @param duracionPeriodoId   periodo de la duración
     * @param fechaInicio         inicio de vigencia
     * @param fechaFin            fin opcional
     * @param notifFrecuenciaValor valor numérico de notificación
     * @param notifFrecuenciaId   periodo de notificación
     * @param objetivoVeces       repeticiones del objetivo
     * @param objetivoPeriodoId   periodo del objetivo
     * @param estado              {@code ACTIVO}, {@code PAUSADO}, etc.
     */
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

    /** @return identificador del hábito */
    public int getIdHabito() {
        return idHabito;
    }

    /** @param idHabito identificador del hábito */
    public void setIdHabito(int idHabito) {
        this.idHabito = idHabito;
    }

    /** @return identificador del usuario propietario */
    public int getIdUsuario() {
        return idUsuario;
    }

    /** @param idUsuario identificador del usuario propietario */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /** @return identificador de categoría, o {@code null} si no tiene */
    public Integer getIdCategoria() {
        return idCategoria;
    }

    /** @param idCategoria identificador de categoría */
    public void setIdCategoria(Integer idCategoria) {
        this.idCategoria = idCategoria;
    }

    /** @return nombre visible del hábito */
    public String getNombreHabito() {
        return nombreHabito;
    }

    /** @param nombreHabito nombre visible del hábito */
    public void setNombreHabito(String nombreHabito) {
        this.nombreHabito = nombreHabito;
    }

    /** @return descripción opcional del hábito */
    public String getDescripcionHabito() {
        return descripcionHabito;
    }

    /** @param descripcionHabito descripción opcional del hábito */
    public void setDescripcionHabito(String descripcionHabito) {
        this.descripcionHabito = descripcionHabito;
    }

    /** @return valor numérico de la duración planificada */
    public Integer getDuracionValor() {
        return duracionValor;
    }

    /** @param duracionValor valor numérico de la duración planificada */
    public void setDuracionValor(Integer duracionValor) {
        this.duracionValor = duracionValor;
    }

    /** @return identificador del periodo de duración en {@code periodos} */
    public Integer getDuracionPeriodoId() {
        return duracionPeriodoId;
    }

    /** @param duracionPeriodoId identificador del periodo de duración */
    public void setDuracionPeriodoId(Integer duracionPeriodoId) {
        this.duracionPeriodoId = duracionPeriodoId;
    }

    /** @return fecha de inicio del seguimiento */
    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    /** @param fechaInicio fecha de inicio del seguimiento */
    public void setFechaInicio(LocalDate fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    /** @return fecha de fin planificada, o {@code null} si no tiene */
    public LocalDate getFechaFin() {
        return fechaFin;
    }

    /** @param fechaFin fecha de fin planificada */
    public void setFechaFin(LocalDate fechaFin) {
        this.fechaFin = fechaFin;
    }

    /** @return valor numérico de la frecuencia de notificación */
    public int getNotifFrecuenciaValor() {
        return notifFrecuenciaValor;
    }

    /** @param notifFrecuenciaValor valor numérico de la frecuencia de notificación */
    public void setNotifFrecuenciaValor(int notifFrecuenciaValor) {
        this.notifFrecuenciaValor = notifFrecuenciaValor;
    }

    /** @return identificador del periodo de notificación en {@code periodos} */
    public Integer getNotifFrecuenciaId() {
        return notifFrecuenciaId;
    }

    /** @param notifFrecuenciaId identificador del periodo de notificación */
    public void setNotifFrecuenciaId(Integer notifFrecuenciaId) {
        this.notifFrecuenciaId = notifFrecuenciaId;
    }

    /** @return veces que debe cumplirse el objetivo en el periodo indicado */
    public int getObjetivoVeces() {
        return objetivoVeces;
    }

    /** @param objetivoVeces veces que debe cumplirse el objetivo */
    public void setObjetivoVeces(int objetivoVeces) {
        this.objetivoVeces = objetivoVeces;
    }

    /** @return identificador del periodo del objetivo en {@code periodos} */
    public Integer getObjetivoPeriodoId() {
        return objetivoPeriodoId;
    }

    /** @param objetivoPeriodoId identificador del periodo del objetivo */
    public void setObjetivoPeriodoId(Integer objetivoPeriodoId) {
        this.objetivoPeriodoId = objetivoPeriodoId;
    }

    /** @return días de la semana en que aplica el objetivo (cadena separada por comas) */
    public String getDiasSemana() {
        return diasSemana;
    }

    /** @param diasSemana días de la semana en que aplica el objetivo */
    public void setDiasSemana(String diasSemana) {
        this.diasSemana = diasSemana;
    }

    /** @return estado del hábito (p. ej. ACTIVO, PAUSADO) */
    public String getEstado() {
        return estado;
    }

    /** @param estado estado del hábito */
    public void setEstado(String estado) {
        this.estado = estado;
    }

    /** @return representación para depuración */
    @Override
    public String toString() {
        return "Habito{idHabito=" + idHabito + ", idUsuario=" + idUsuario + ", nombreHabito="
            + nombreHabito + ", fechaInicio=" + fechaInicio + ", estado=" + estado + "}";
    }
}
