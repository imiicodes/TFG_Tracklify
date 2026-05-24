package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Representa la cuenta de acceso de un usuario en Tracklify.
 * Se persiste en la tabla {@code usuarios} con credenciales, rol, estado de cuenta y fechas de actividad.
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

    /** Constructor vacío para formularios y mapeo desde JDBC. */
    public Usuario() {
    }

    /**
     * @param idUsuario                identificador del usuario
     * @param emailUsuario             correo de acceso
     * @param passwordUsuario          hash BCrypt de la contraseña
     * @param rolId                    rol asignado
     * @param onboardingCompletado     si terminó el asistente inicial
     * @param emailConfirmado          si verificó el correo
     * @param cuentaActiva             si la cuenta puede iniciar sesión
     * @param fechaRegistro            alta en el sistema
     * @param fechaConfirmacionEmail   confirmación de email
     * @param fechaUltModificacion     última modificación de datos
     * @param fechaUltAcceso           último inicio de sesión
     */
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

    /** @return identificador del usuario */
    public int getIdUsuario() {
        return idUsuario;
    }

    /** @param idUsuario identificador del usuario */
    public void setIdUsuario(int idUsuario) {
        this.idUsuario = idUsuario;
    }

    /** @return correo electrónico de la cuenta */
    public String getEmailUsuario() {
        return emailUsuario;
    }

    /** @param emailUsuario correo electrónico de la cuenta */
    public void setEmailUsuario(String emailUsuario) {
        this.emailUsuario = emailUsuario;
    }

    /** @return contraseña almacenada (hash) */
    public String getPasswordUsuario() {
        return passwordUsuario;
    }

    /** @param passwordUsuario contraseña almacenada (hash) */
    public void setPasswordUsuario(String passwordUsuario) {
        this.passwordUsuario = passwordUsuario;
    }

    /** @return identificador del rol en {@code roles} */
    public int getRolId() {
        return rolId;
    }

    /** @param rolId identificador del rol */
    public void setRolId(int rolId) {
        this.rolId = rolId;
    }

    /** @return {@code true} si el usuario completó el onboarding */
    public boolean isOnboardingCompletado() {
        return onboardingCompletado;
    }

    /** @param onboardingCompletado indica si completó el onboarding */
    public void setOnboardingCompletado(boolean onboardingCompletado) {
        this.onboardingCompletado = onboardingCompletado;
    }

    /** @return {@code true} si el correo fue confirmado */
    public boolean isEmailConfirmado() {
        return emailConfirmado;
    }

    /** @param emailConfirmado indica si el correo fue confirmado */
    public void setEmailConfirmado(boolean emailConfirmado) {
        this.emailConfirmado = emailConfirmado;
    }

    /** @return {@code true} si la cuenta está activa y puede iniciar sesión */
    public boolean isCuentaActiva() {
        return cuentaActiva;
    }

    /** @param cuentaActiva indica si la cuenta está activa */
    public void setCuentaActiva(boolean cuentaActiva) {
        this.cuentaActiva = cuentaActiva;
    }

    /** @return fecha y hora de registro en la aplicación */
    public LocalDateTime getFechaRegistro() {
        return fechaRegistro;
    }

    /** @param fechaRegistro fecha y hora de registro */
    public void setFechaRegistro(LocalDateTime fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    /** @return fecha y hora de confirmación del correo */
    public LocalDateTime getFechaConfirmacionEmail() {
        return fechaConfirmacionEmail;
    }

    /** @param fechaConfirmacionEmail fecha y hora de confirmación del correo */
    public void setFechaConfirmacionEmail(LocalDateTime fechaConfirmacionEmail) {
        this.fechaConfirmacionEmail = fechaConfirmacionEmail;
    }

    /** @return fecha y hora de la última modificación de la cuenta */
    public LocalDateTime getFechaUltModificacion() {
        return fechaUltModificacion;
    }

    /** @param fechaUltModificacion fecha y hora de la última modificación */
    public void setFechaUltModificacion(LocalDateTime fechaUltModificacion) {
        this.fechaUltModificacion = fechaUltModificacion;
    }

    /** @return fecha y hora del último acceso */
    public LocalDateTime getFechaUltAcceso() {
        return fechaUltAcceso;
    }

    /** @param fechaUltAcceso fecha y hora del último acceso */
    public void setFechaUltAcceso(LocalDateTime fechaUltAcceso) {
        this.fechaUltAcceso = fechaUltAcceso;
    }

    /** @return representación para depuración */
    @Override
    public String toString() {
        return "Usuario{id=" + idUsuario + ", email=" + emailUsuario + ", rolId=" + rolId + "}";
    }
}
