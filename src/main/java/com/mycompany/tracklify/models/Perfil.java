/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 *
 * @author imii
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

    public Perfil() {
    }

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

    public int getIdPerfil() {
        return idPerfil;
    }

    public void setIdPerfil(int idPerfil) {
        this.idPerfil = idPerfil;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }

    public void setNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public String getFotoPerfilUrl() {
        return fotoPerfilUrl;
    }

    public void setFotoPerfilUrl(String fotoPerfilUrl) {
        this.fotoPerfilUrl = fotoPerfilUrl;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public LocalDate getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDate fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getProfesion() {
        return profesion;
    }

    public void setProfesion(String profesion) {
        this.profesion = profesion;
    }

    public String getTema() {
        return tema;
    }

    public void setTema(String tema) {
        this.tema = tema;
    }

    public String getIdioma() {
        return idioma;
    }

    public void setIdioma(String idioma) {
        this.idioma = idioma;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaUltModificacion() {
        return fechaUltModificacion;
    }

    public void setFechaUltModificacion(LocalDateTime fechaUltModificacion) {
        this.fechaUltModificacion = fechaUltModificacion;
    }

    @Override
    public String toString() {
        return "Perfil{idPerfil=" + idPerfil + ", idUsuario=" + idUsuario + ", nombreUsuario=" + nombreUsuario + "}";
    }
}
