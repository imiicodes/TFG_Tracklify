package com.mycompany.tracklify.utils;

import com.mycompany.tracklify.dao.NotificacionDAO;
import com.mycompany.tracklify.models.Notificacion;
import java.time.LocalDateTime;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Pop-up de notificación de Tracklify.
 *
 * <p>Muestra una ventana flotante con la paleta de colores de Tracklify
 * cuando el {@link NotificacionScheduler} detecta una notificación pendiente.
 * El usuario puede:</p>
 * <ul>
 *   <li><strong>Completar</strong> — marca la notificación como {@code estado = 1}
 *       en la BD y cierra el pop-up definitivamente.</li>
 *   <li><strong>Posponer 15 min</strong> — actualiza {@code pospuesta_hasta}
 *       a {@code ahora + 15 minutos} en la BD.</li>
 *   <li><strong>Posponer 1 hora</strong> — actualiza {@code pospuesta_hasta}
 *       a {@code ahora + 1 hora}.</li>
 *   <li><strong>Posponer a mañana</strong> — actualiza {@code pospuesta_hasta}
 *       a las 09:00 del día siguiente.</li>
 * </ul>
 *
 * <p>El pop-up es modal ({@link Modality#APPLICATION_MODAL}) pero no bloquea
 * el hilo de JavaFX gracias a {@link Stage#show()} en lugar de
 * {@link Stage#showAndWait()}.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see NotificacionScheduler
 * @see NotificacionDAO
 */
public class NotificacionPopup {

    /**
     * Crea y muestra el pop-up de notificación.
     *
     * <p>Método estático de conveniencia que construye toda la UI del pop-up,
     * asigna los manejadores de los botones y muestra la ventana.
     * Debe llamarse siempre desde el hilo de JavaFX.</p>
     *
     * @param notificacion la {@link Notificacion} que se va a mostrar
     * @param dao          el {@link NotificacionDAO} para persistir las acciones del usuario
     */
    public static void mostrar(Notificacion notificacion, NotificacionDAO dao) {

        // ── Creación del Stage del pop-up ──────────────────────────────────
        Stage popup = new Stage();
        popup.initModality(Modality.NONE);       // No bloqueante — la app sigue funcionando
        popup.initStyle(StageStyle.UNDECORATED); // Sin barra de título del SO
        popup.setAlwaysOnTop(true);              // Siempre visible sobre otras ventanas
        popup.setTitle("Tracklify — Recordatorio");

        // ── Icono de campana ───────────────────────────────────────────────
        Label iconoCampana = new Label("🔔");
        iconoCampana.setStyle("-fx-font-size: 28px;");

        // ── Título del pop-up ──────────────────────────────────────────────
        Label labelTitulo = new Label("¡Tienes un recordatorio!");
        labelTitulo.setStyle(
            "-fx-font-size: 15px;" +
            "-fx-font-weight: bold;" +
            "-fx-text-fill: #93588F;" +
            "-fx-font-family: Georgia;"
        );

        // ── Mensaje de la notificación ─────────────────────────────────────
        Label labelMensaje = new Label(notificacion.getMensajeNotificacion());
        labelMensaje.setWrapText(true);
        labelMensaje.setMaxWidth(300);
        labelMensaje.setStyle(
            "-fx-font-size: 13px;" +
            "-fx-text-fill: #3A2B3F;" +
            "-fx-text-alignment: center;" +
            "-fx-alignment: center;"
        );

        // ── Botón COMPLETAR ────────────────────────────────────────────────
        Button btnCompletar = new Button("✔  Completar");
        btnCompletar.setStyle(
            "-fx-background-color: #93588F;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 13px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 8 20 8 20;"
        );
        btnCompletar.setOnAction(e -> {
            // Marcamos como completada en la BD y cerramos
            dao.marcarComoCompletada(notificacion.getIdNotificacion());
            popup.close();
        });

        // ── Separador de texto ─────────────────────────────────────────────
        Label labelPosponer = new Label("Posponer:");
        labelPosponer.setStyle(
            "-fx-font-size: 11px;" +
            "-fx-text-fill: #7E6A8A;" +
            "-fx-padding: 8 0 4 0;"
        );

        // ── Botones de POSPONER ────────────────────────────────────────────
        Button btn15min = crearBotonPosponer("15 min");
        Button btn1hora = crearBotonPosponer("1 hora");
        Button btnManana = crearBotonPosponer("Mañana");

        // Manejador: posponer 15 minutos
        btn15min.setOnAction(e -> {
            LocalDateTime hasta = LocalDateTime.now().plusMinutes(15);
            dao.posponer(notificacion.getIdNotificacion(), hasta);
            popup.close();
        });

        // Manejador: posponer 1 hora
        btn1hora.setOnAction(e -> {
            LocalDateTime hasta = LocalDateTime.now().plusHours(1);
            dao.posponer(notificacion.getIdNotificacion(), hasta);
            popup.close();
        });

        // Manejador: posponer a mañana a las 09:00
        btnManana.setOnAction(e -> {
            LocalDateTime hasta = LocalDateTime.now()
                .plusDays(1)
                .withHour(9)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
            dao.posponer(notificacion.getIdNotificacion(), hasta);
            popup.close();
        });

        // ── Fila de botones de posponer ────────────────────────────────────
        HBox filaPosponer = new HBox(8, btn15min, btn1hora, btnManana);
        filaPosponer.setAlignment(Pos.CENTER);

        // ── Layout principal del pop-up ────────────────────────────────────
        VBox contenido = new VBox(10,
            iconoCampana,
            labelTitulo,
            labelMensaje,
            btnCompletar,
            labelPosponer,
            filaPosponer
        );
        contenido.setAlignment(Pos.CENTER);
        contenido.setPadding(new Insets(24, 28, 24, 28));
        contenido.setStyle(
            "-fx-background-color: white;" +
            "-fx-border-color: #DEA9FF;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 14;" +
            "-fx-background-radius: 14;" +
            "-fx-effect: dropshadow(gaussian, rgba(147,88,143,0.25), 20, 0.1, 0, 6);"
        );

        // Fondo semitransparente alrededor de la tarjeta
        VBox raiz = new VBox(contenido);
        raiz.setAlignment(Pos.CENTER);
        raiz.setStyle("-fx-background-color: rgba(246,238,255,0.95);");

        // ── Escena y posición del pop-up ───────────────────────────────────
        Scene escena = new Scene(raiz, 360, 280);

        // Posicionamos en la esquina inferior derecha de la pantalla
        javafx.geometry.Rectangle2D pantalla =
            javafx.stage.Screen.getPrimary().getVisualBounds();
        popup.setX(pantalla.getMaxX() - 380);
        popup.setY(pantalla.getMaxY() - 310);

        popup.setScene(escena);
        popup.show();
    }

    /**
     * Crea un botón de posponer con el estilo outline de Tracklify.
     *
     * <p>Método auxiliar privado para evitar duplicación del estilo
     * entre los tres botones de posposición.</p>
     *
     * @param texto el texto que mostrará el botón
     * @return el {@link Button} estilizado listo para usar
     */
    private static Button crearBotonPosponer(String texto) {

        Button btn = new Button(texto);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: #93588F;" +
            "-fx-font-size: 12px;" +
            "-fx-font-weight: bold;" +
            "-fx-background-radius: 8;" +
            "-fx-border-color: #DEA9FF;" +
            "-fx-border-radius: 8;" +
            "-fx-border-width: 1.5;" +
            "-fx-cursor: hand;" +
            "-fx-padding: 6 12 6 12;"
        );

        // Efecto hover inline (sin CSS externo)
        btn.setOnMouseEntered(e ->
            btn.setStyle(
                "-fx-background-color: #F6EEFF;" +
                "-fx-text-fill: #93588F;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #93588F;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1.5;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 6 12 6 12;"
            )
        );
        btn.setOnMouseExited(e ->
            btn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #93588F;" +
                "-fx-font-size: 12px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 8;" +
                "-fx-border-color: #DEA9FF;" +
                "-fx-border-radius: 8;" +
                "-fx-border-width: 1.5;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 6 12 6 12;"
            )
        );

        return btn;
    }
}