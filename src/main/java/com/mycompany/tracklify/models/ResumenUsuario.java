package com.mycompany.tracklify.models;

/**
 * DTO de solo lectura con métricas agregadas de actividad de un usuario.
 * Se mapea desde la vista {@code v_resumen_usuario} para el panel y el dashboard.
 */
public class ResumenUsuario {

    private final int idUsuario;
    private final String nombreUsuario;
    private final int totalHabitosActivos;
    private final int totalCumplimientos;
    private final int habitosCompletadosHoy;
    private final double tasaExitoGlobal;

    /**
     * @param idUsuario               identificador del usuario
     * @param nombreUsuario           nombre para saludo o listados
     * @param totalHabitosActivos     hábitos en estado activo
     * @param totalCumplimientos      cumplimientos históricos
     * @param habitosCompletadosHoy   hábitos distintos completados hoy
     * @param tasaExitoGlobal         ratio o porcentaje de éxito agregado
     */
    public ResumenUsuario(int idUsuario, String nombreUsuario, int totalHabitosActivos,
                          int totalCumplimientos, int habitosCompletadosHoy, double tasaExitoGlobal) {
        this.idUsuario = idUsuario;
        this.nombreUsuario = nombreUsuario;
        this.totalHabitosActivos = totalHabitosActivos;
        this.totalCumplimientos = totalCumplimientos;
        this.habitosCompletadosHoy = habitosCompletadosHoy;
        this.tasaExitoGlobal = tasaExitoGlobal;
    }

    /** @return identificador del usuario */
    public int getIdUsuario() {
        return idUsuario;
    }

    /** @return nombre mostrado del usuario */
    public String getNombreUsuario() {
        return nombreUsuario;
    }

    /** @return cantidad de hábitos en estado activo */
    public int getTotalHabitosActivos() {
        return totalHabitosActivos;
    }

    /** @return total histórico de cumplimientos registrados */
    public int getTotalCumplimientos() {
        return totalCumplimientos;
    }

    /** @return hábitos con objetivo cumplido en el día actual */
    public int getHabitosCompletadosHoy() {
        return habitosCompletadosHoy;
    }

    /** @return tasa de éxito global en porcentaje o proporción */
    public double getTasaExitoGlobal() {
        return tasaExitoGlobal;
    }
}
