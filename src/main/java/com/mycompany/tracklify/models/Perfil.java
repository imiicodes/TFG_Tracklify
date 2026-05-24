package com.mycompany.tracklify.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Representa los datos de perfil y preferencias de un usuario.
 * Se persiste en la tabla {@code perfiles}, vinculada uno a uno con {@code usuarios}.
 */
public class Perfil {

    private int idPerfil;
    private int idUsuario;
    private String nombreUsuario;
    private String fotoPerfilUrl;
    private String genero;
    private LocalDate fechaNacimiento;
    private String profesion;
    private String tema = "SISTEMA";
    private String idioma = "es";
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaUltModificacion;

    /** Constructor vacío para formularios y mapeo desde JDBC. */
    public Perfil() {
    }

    /**
     * @param idPerfil               identificador del perfil
     * @param idUsuario              usuario propietario
     * @param nombreUsuario          nombre visible
     * @param fotoPerfilUrl          ruta o URL de avatar
     * @param genero                 código de género en BD
     * @param fechaNacimiento        fecha de nacimiento
     * @param profesion              profesión opcional
     * @param tema                   tema de interfaz
     * @param idioma                 código de idioma
     * @param fechaCreacion          alta del perfil
     * @param fechaUltModificacion   última actualización
     */
    public Perfil(int idPerfil, int idUsuario, String nombreUsuario, String fotoPerfilUrl,
                  String genero, LocalDate fechaNacimiento, String profesion, String tema, String idioma,
                  LocalDateTime fechaCreacion, LocalDateTime fechaUltModificacion) {
        this.idPerfil = idPerfil;
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.fotoPerfilUrl = fotoPerfilUrl;
        this.genero = genero;
        this.fechaNacimiento = fechaNacimiento;
        this.profesion = profesion;
        this.tema = tema != null ? tema : "SISTEMA";
        this.idioma = idioma != null ? idioma : "es";
        this.fechaCreacion = fechaCreacion;
        this.fechaUltModificacion = fechaUltModificacion;
    }

    /** @return identificador del perfil */
    public int getIdPerfil() {
        return idPerfil;
    }

    /** @param idPerfil identificador del perfil */
    public void setIdPerfil(int idPerfil) {
        this.idPerfil = idPerfil;
    }

    /** @return identificador del usuario asociado */
    public int getIdUsuario() {
        return idUsuario;
    }

    /** @param idUsuario identificador del usuario asociado */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /** @return nombre mostrado en la interfaz */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /** @param nombreUsuario nombre mostrado en la interfaz */
    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    /** @return URL de la foto de perfil */
    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    /** @param fotoPerfilUrl URL de la foto de perfil */
    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    /** @return género indicado por el usuario */
    public String getGenero() {
        return genero;
    }

    /** @param genero género indicado por el usuario */
    public void setGenero(String genero) {
        this.genero = genero;
    }

    /** @return fecha de nacimiento */
    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    /** @param fechaNacimiento fecha de nacimiento */
    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    /** @return profesión u ocupación */
    public String getProfesion() {
        return profesion;
    }

    /** @param profesion profesión u ocupación */
    public void setProfesion(String profesion) {
        this.profesion = profesion;
    }

    /** @return tema de interfaz (p. ej. SISTEMA, CLARO, OSCURO) */
    public String getTema() {
        return tema;
    }

    /** @param tema tema de interfaz */
    public void setTema(String tema) {
        this.tema = tema;
    }

    /** @return código de idioma de la interfaz */
    public String getIdioma() {
        return idioma;
    }

    /** @param idioma código de idioma de la interfaz */
    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    /** @return fecha y hora de creación del perfil */
    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    /** @param fechaCreacion fecha y hora de creación del perfil */
    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /** @return fecha y hora de la última modificación */
    public LocalDateTime getFechaUltModificacion() {
        return fechaUltModificacion;
    }

    /** @param fechaUltModificacion fecha y hora de la última modificación */
    public void setFechaUltModificacion(LocalDateTime fechaUltModificacion) {
        this.fechaUltModificacion = fechaUltModificacion;
    }

    /** @return representación para depuración */
    @Override
    public String toString() {
        return "Perfil{idPerfil=" + idPerfil + ", idUsuario=" + idUsuario + ", nombreUsuario=" + nombreUsuario + "}";
    }
}
