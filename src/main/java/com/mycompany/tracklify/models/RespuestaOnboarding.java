package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Modelo que representa una respuesta individual del usuario
 * durante el proceso de onboarding.
 *
 * <p>Cada instancia corresponde a una opción seleccionada por el usuario
 * en una pregunta concreta del onboarding. Como se permiten hasta 3
 * selecciones por pregunta, puede haber múltiples registros del mismo
 * {@code numeroPregunta} para un mismo {@code idUsuario}.</p>
 *
 * <p>Se persiste en la tabla {@code onboarding_respuestas} de la BD.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see com.mycompany.tracklify.dao.OnboardingDAO
 */
public class RespuestaOnboarding {

    /** Identificador único de la respuesta (auto-generado por la BD). */
    private int idRespuesta;

    /** Identificador del usuario que responde el onboarding. */
    private int idUsuario;

    /**
     * Número de la pregunta a la que corresponde esta respuesta (1 al 5).
     * Permite agrupar las respuestas por pregunta al consultarlas.
     */
    private int numeroPregunta;

    /** Texto exacto de la opción seleccionada por el usuario. */
    private String respuesta;

    /** Fecha y hora en que se registró la respuesta. */
    private LocalDateTime fechaRespuesta;

    /**
     * Constructor vacío requerido para instanciación desde el DAO.
     */
    public RespuestaOnboarding() {}

    /**
     * Constructor principal para crear una respuesta antes de persistirla.
     *
     * @param idUsuario      identificador del usuario que responde
     * @param numeroPregunta número de la pregunta (1-5)
     * @param respuesta      texto de la opción seleccionada
     */
    public RespuestaOnboarding(int idUsuario, int numeroPregunta, String respuesta) {
        this.idUsuario = idUsuario;
        this.numeroPregunta = numeroPregunta;
        this.respuesta = respuesta;
    }

    // ── Getters y Setters ──────────────────────────────────────────────────

    /**
     * @return el identificador único de la respuesta
     */
    public int getIdRespuesta() { return idRespuesta; }

    /**
     * @param idRespuesta el identificador a establecer
     */
    public void setIdRespuesta(int idRespuesta) { this.idRespuesta = idRespuesta; }

    /**
     * @return el identificador del usuario
     */
    public int getIdUsuario() { return idUsuario; }

    /**
     * @param idUsuario el identificador del usuario a establecer
     */
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    /**
     * @return el número de pregunta al que corresponde esta respuesta
     */
    public int getNumeroPregunta() { return numeroPregunta; }

    /**
     * @param numeroPregunta el número de pregunta a establecer
     */
    public void setNumeroPregunta(int numeroPregunta) { this.numeroPregunta = numeroPregunta; }

    /**
     * @return el texto de la opción seleccionada
     */
    public String getRespuesta() { return respuesta; }

    /**
     * @param respuesta el texto de la respuesta a establecer
     */
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }

    /**
     * @return la fecha y hora en que se registró la respuesta
     */
    public LocalDateTime getFechaRespuesta() { return fechaRespuesta; }

    /**
     * @param fechaRespuesta la fecha y hora a establecer
     */
    public void setFechaRespuesta(LocalDateTime fechaRespuesta) {
        this.fechaRespuesta = fechaRespuesta;
    }

    /**
     * Representación legible del objeto para depuración.
     *
     * @return cadena con los campos principales del objeto
     */
    @Override
    public String toString() {
        return "RespuestaOnboarding{usuario=" + idUsuario
            + ", pregunta=" + numeroPregunta
            + ", respuesta='" + respuesta + "'}";
    }
}