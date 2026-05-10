package com.mycompany.tracklify.models;

import java.time.LocalDateTime;

/**
 * Fila del listado de informes por usuario en administración.
 *
 * @author Tracklify
 */
public class AdminReporteUsuarioFila {

    private final int idUsuario;
    private final String nombreUsuario;
    private final String emailUsuario;
    private final int habitosActivos;
    private final LocalDateTime ultimoRegistro;

    /**
     * Crea una fila para la tabla de reportes admin.
     *
     * @param idUsuario        identificador del usuario
     * @param nombreUsuario    nombre visible
     * @param emailUsuario     correo
     * @param habitosActivos   hábitos activos (desde resumen o 0)
     * @param ultimoRegistro   última marca de registro de hábito, o {@code null}
     */
    public AdminReporteUsuarioFila(int idUsuario, String nombreUsuario, String emailUsuario,
                                   int habitosActivos, LocalDateTime ultimoRegistro) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario != null ? nombreUsuario : "";
        this.emailUsuario = emailUsuario != null ? emailUsuario : "";
        this.habitosActivos = habitosActivos;
        this.ultimoRegistro = ultimoRegistro;
    }

    /**
     * @return id de usuario (para cargar informe)
     */
    public int getIdUsuario() {
        return idUsuario;
    }

    /**
     * @return nombre para la columna Usuario
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * @return email
     */
    public String getEmailUsuario() {
        return emailUsuario;
    }

    /**
     * @return hábitos activos
     */
    public int getHabitosActivos() {
        return habitosActivos;
    }

    /**
     * @return fecha/hora del último registro de hábito
     */
    public LocalDateTime getUltimoRegistro() {
        return ultimoRegistro;
    }

    /**
     * Texto para la columna «Último registro» en la tabla.
     *
     * @return representación legible o «—» si no hay datos
     */
    public String getUltimoRegistroTexto() {
        return ultimoRegistro == null ? "—" : ultimoRegistro.toString().replace('T', ' ');
    }
}
