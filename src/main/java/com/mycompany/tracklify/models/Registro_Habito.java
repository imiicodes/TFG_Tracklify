/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.tracklify.models;
import java.time.LocalDate;
/**
 *
 * @author imii
 */

public class Registro_Habito {

    private int idRegistro;
    private int tareaId;
    private LocalDate fechaRegistro;
    private boolean estadoRegistro;
    private String comentario;

    public Registro_Habito() {}

    public Registro_Habito(int idRegistro, int tareaId, LocalDate fechaRegistro,
                           boolean estadoRegistro, String comentario) {
        this.idRegistro = idRegistro;
        this.tareaId = tareaId;
        this.fechaRegistro = fechaRegistro;
        this.estadoRegistro = estadoRegistro;
        this.comentario = comentario;
    }

    public int getIdRegistro() { return idRegistro; }
    public void setIdRegistro(int idRegistro) { this.idRegistro = idRegistro; }

    public int getTareaId() { return tareaId; }
    public void setTareaId(int tareaId) { this.tareaId = tareaId; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public boolean isEstadoRegistro() { return estadoRegistro; }
    public void setEstadoRegistro(boolean estadoRegistro) { this.estadoRegistro = estadoRegistro; }

    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }

    @Override
    public String toString() {
        return "Registro_Habito{id=" + idRegistro + ", tareaId=" + tareaId + ", fecha=" + fechaRegistro + "}";
    }
}
