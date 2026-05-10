package com.mycompany.tracklify.models;

/**
 * Fila de la tabla de estadísticas por usuario en el panel de administración.
 *
 * <p>Los datos provienen de {@code v_resumen_usuario} y columnas relacionadas.</p>
 *
 * @author Tracklify
 */
public class AdminEstadisticaUsuarioFila {

    private final String nombreUsuario;
    private final String emailUsuario;
    private final int habitosActivos;
    private final int rachaMaxima;
    private final String tasaExitoTexto;

    /**
     * Construye una fila visible en la {@code TableView} de estadísticas admin.
     *
     * @param nombreUsuario  nombre o alias del perfil
     * @param emailUsuario   correo de la cuenta
     * @param habitosActivos cantidad de hábitos activos
     * @param rachaMaxima    racha actual (días) del hábito con más registros completados
     * @param tasaExitoTexto tasa de éxito ya formateada para mostrar (porcentaje)
     */
    public AdminEstadisticaUsuarioFila(String nombreUsuario, String emailUsuario, int habitosActivos,
                                       int rachaMaxima, String tasaExitoTexto) {
        this.nombreUsuario = nombreUsuario != null ? nombreUsuario : "";
        this.emailUsuario = emailUsuario != null ? emailUsuario : "";
        this.habitosActivos = habitosActivos;
        this.rachaMaxima = rachaMaxima;
        this.tasaExitoTexto = tasaExitoTexto != null ? tasaExitoTexto : "—";
    }

    /**
     * @return nombre mostrado del usuario
     */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /**
     * @return email de acceso
     */
    public String getEmailUsuario() {
        return emailUsuario;
    }

    /**
     * @return número de hábitos activos
     */
    public int getHabitosActivos() {
        return habitosActivos;
    }

    /**
     * @return racha en días según la regla del panel admin
     */
    public int getRachaMaxima() {
        return rachaMaxima;
    }

    /**
     * @return texto de tasa de éxito (porcentaje)
     */
    public String getTasaExitoTexto() {
        return tasaExitoTexto;
    }
}
