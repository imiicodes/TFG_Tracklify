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
public class Usuario {

    private int idUsuario;
    private String emailUsuario;
    private String passwordUsuario;
    private int rolId;
    private boolean onboardingCompletado;
    private boolean emailConfirmado;
    private boolean cuentaActiva = true;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaConfirmacionEmail;
    private LocalDateTime fechaUltModificacion;
    private LocalDateTime fechaUltAcceso;

    public Usuario() {
    }

    public Usuario(int idUsuario, String emailUsuario, String passwordUsuario, int rolId,
                   boolean onboardingCompletado, boolean emailConfirmado, boolean cuentaActiva,
                   LocalDateTime fechaRegistro, LocalDateTime fechaConfirmacionEmail,
                   LocalDateTime fechaUltModificacion, LocalDateTime fechaUltAcceso) {
        this.idUsuario = idUsuario;
        this.emailUsuario = emailUsuario;
        this.passwordUsuario = passwordUsuario;
        this.rolId = rolId;
        this.onboardingCompletado = onboardingCompletado;
        this.emailConfirmado = emailConfirmado;
        this.cuentaActiva = cuentaActiva;
        this.fechaRegistro = fechaRegistro;
        this.fechaConfirmacionEmail = fechaConfirmacionEmail;
        this.fechaUltModificacion = fechaUltModificacion;
        this.fechaUltAcceso = fechaUltAcceso;
    }

    public int getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getEmailUsuario() {
        return emailUsuario;
    }

    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    public String getPasswordUsuario() {
        return passwordUsuario;
    }

    public void setPasswordUsuario(String passwordUsuario) {
        this.passwordUsuario = passwordUsuario;
    }

    public int getRolId() {
        return rolId;
    }

    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    public boolean isOnboardingCompletado() {
        return onboardingCompletado;
    }

    public void setOnboardingCompletado(boolean onboardingCompletado) {
        this.onboardingCompletado = onboardingCompletado;
    }

    public boolean isEmailConfirmado() {
        return emailConfirmado;
    }

    public void setEmailConfirmado(boolean emailConfirmado) {
        this.emailConfirmado = emailConfirmado;
    }

    public boolean isCuentaActiva() {
        return cuentaActiva;
    }

    public void setCuentaActiva(boolean cuentaActiva) {
        this.cuentaActiva = cuentaActiva;
    }

    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public LocalDateTime getFechaConfirmacionEmail() {
        return fechaConfirmacionEmail;
    }

    public void setFechaConfirmacionEmail(LocalDateTime fechaConfirmacionEmail) {
        this.fechaConfirmacionEmail = fechaConfirmacionEmail;
    }

    public LocalDateTime getFechaUltModificacion() {
        return fechaUltModificacion;
    }

    public void setFechaUltModificacion(LocalDateTime fechaUltModificacion) {
        this.fechaUltModificacion = fechaUltModificacion;
    }

    public LocalDateTime getFechaUltAcceso() {
        return fechaUltAcceso;
    }

    public void setFechaUltAcceso(LocalDateTime fechaUltAcceso) {
        this.fechaUltAcceso = fechaUltAcceso;
    }

    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario + ", email=" + emailUsuario + ", rolId=" + rolId + "}";
    }
}
