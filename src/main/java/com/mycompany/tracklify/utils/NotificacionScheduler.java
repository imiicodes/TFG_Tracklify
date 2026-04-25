package com.mycompany.tracklify.utils;

import com.mycompany.tracklify.dao.NotificacionDAO;
import com.mycompany.tracklify.models.Notificacion;
import com.mycompany.tracklify.models.Usuario;
import java.awt.AWTException;
import java.awt.Image;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;

/**
 * Servicio planificador que comprueba y dispara las notificaciones de Tracklify.
 *
 * <p>Implementa el patrón Singleton para garantizar que solo existe un scheduler
 * activo durante toda la sesión de la aplicación. Utiliza un
 * {@link ScheduledExecutorService} que ejecuta una comprobación cada 60 segundos.</p>
 *
 * <p>Cuando detecta una notificación pendiente realiza dos acciones:</p>
 * <ol>
 *   <li>Muestra una notificación nativa del sistema operativo mediante
 *       {@link SystemTray} (si el SO lo soporta).</li>
 *   <li>Muestra un pop-up dentro de la aplicación JavaFX mediante
 *       {@link NotificacionPopup}, ejecutado en el hilo de JavaFX
 *       a través de {@link Platform#runLater}.</li>
 * </ol>
 *
 * <p>El scheduler debe iniciarse al hacer login ({@link #iniciar(int)})
 * y detenerse al cerrar sesión ({@link #detener()}).</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see NotificacionDAO
 * @see NotificacionPopup
 */
public class NotificacionScheduler {

    /** Instancia única del scheduler (patrón Singleton). */
    private static NotificacionScheduler instancia;

    /** Servicio de ejecución planificada con un único hilo daemon. */
    private ScheduledExecutorService executor;

    /** DAO para consultar notificaciones pendientes en la BD. */
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();

    /** Icono de la bandeja del sistema para notificaciones de escritorio. */
    private TrayIcon trayIcon;

    /**
     * Constructor privado — inicializa el icono de la bandeja del sistema
     * si el SO lo soporta.
     */
    private NotificacionScheduler() {
        inicializarTray();
    }

    /**
     * Devuelve la instancia única del scheduler.
     *
     * @return la instancia única de {@code NotificacionScheduler}
     */
    public static NotificacionScheduler getInstancia() {
        if (instancia == null) {
            instancia = new NotificacionScheduler();
        }
        return instancia;
    }

    /**
     * Inicia el scheduler para el usuario indicado.
     *
     * <p>Crea un hilo daemon que comprueba notificaciones pendientes
     * cada 60 segundos. Si ya había un scheduler activo, lo detiene
     * primero para evitar duplicados.</p>
     *
     * @param idUsuario identificador del usuario cuyas notificaciones se comprueban
     */
    public void iniciar(int idUsuario) {

        // Detenemos cualquier scheduler previo antes de crear uno nuevo
        detener();

        // Creamos un ejecutor con un hilo daemon (se cierra con la app)
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "tracklify-notif-scheduler");
            t.setDaemon(true); // El hilo no impide que la JVM se cierre
            return t;
        });

        // Comprobamos inmediatamente al iniciar y luego cada 60 segundos
        executor.scheduleAtFixedRate(
            () -> comprobarNotificaciones(idUsuario),
            0,       // Retardo inicial: 0 segundos
            60,      // Periodo de repetición: 60 segundos
            TimeUnit.SECONDS
        );

        System.out.println("NotificacionScheduler iniciado para usuario " + idUsuario);
    }

    /**
     * Detiene el scheduler y libera el hilo de ejecución.
     *
     * <p>Debe llamarse desde {@code MainViewController.cerrarSesion()}
     * para no dejar hilos huérfanos cuando el usuario cierra sesión.</p>
     */
    public void detener() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdownNow();
            System.out.println("NotificacionScheduler detenido.");
        }
    }

    /**
     * Comprueba en la BD si hay notificaciones pendientes para el usuario
     * y las dispara una a una.
     *
     * <p>Este método se ejecuta en el hilo del scheduler (no en el hilo de JavaFX).
     * Por eso, todo lo relacionado con la UI se delega a {@link Platform#runLater}.</p>
     *
     * @param idUsuario identificador del usuario a comprobar
     */
    private void comprobarNotificaciones(int idUsuario) {

        try {
            List<Notificacion> pendientes = notificacionDAO.obtenerPendientesAhora(idUsuario);

            for (Notificacion notif : pendientes) {
                // 1. Notificación de escritorio (hilo del scheduler)
                mostrarNotificacionEscritorio(notif.getMensajeNotificacion());

                // 2. Pop-up JavaFX (debe ejecutarse en el hilo de JavaFX)
                Platform.runLater(() ->
                    NotificacionPopup.mostrar(notif, notificacionDAO)
                );
            }

        } catch (Exception e) {
            // Capturamos cualquier excepción para que el scheduler no se detenga
            System.err.println("Error en scheduler de notificaciones: " + e.getMessage());
        }
    }

      /**
     * Inicializa el icono de la bandeja del sistema del SO.
     *
     * <p>Usa la API {@link SystemTray} de Java AWT. Si el SO no soporta
     * la bandeja del sistema (algunos entornos Linux), el método falla
     * silenciosamente y solo se mostrará el pop-up de JavaFX.</p>
     */
    private void inicializarTray() {

        // Comprobamos si el SO soporta la bandeja del sistema
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray no soportado en este SO — solo pop-up JavaFX.");
            return;
        }

        try {
            SystemTray tray = SystemTray.getSystemTray();

            // Intentamos cargar el icono personalizado de la app
            // Si no existe el recurso, usamos una imagen vacía como fallback
            java.net.URL iconUrl = getClass().getResource("/images/tracklify_icon.png");

            Image imagen;
            if (iconUrl != null) {
                // Icono personalizado encontrado en resources
                imagen = Toolkit.getDefaultToolkit().getImage(iconUrl);
            } else {
                // Fallback: imagen vacía de 1x1 pixel para evitar NullPointerException
                imagen = Toolkit.getDefaultToolkit().createImage(new byte[]{});
            }

            trayIcon = new TrayIcon(imagen, "Tracklify");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);

        } catch (AWTException e) {
            // AWTException ocurre si el sistema no permite añadir el TrayIcon
            System.out.println("No se pudo añadir el TrayIcon al sistema: " + e.getMessage());
            trayIcon = null;
        } catch (Exception e) {
            // Cualquier otro error inesperado (seguridad, permisos, etc.)
            System.out.println("Error inesperado al inicializar TrayIcon: " + e.getMessage());
            trayIcon = null;
        }
    }

    /**
     * Muestra una notificación nativa del escritorio mediante {@link SystemTray}.
     *
     * <p>Si {@link #trayIcon} no está disponible (SO sin soporte o error de
     * inicialización), el método no hace nada — el pop-up JavaFX se muestra
     * igualmente como alternativa.</p>
     *
     * @param mensaje el texto de la notificación a mostrar en el escritorio
     */
    private void mostrarNotificacionEscritorio(String mensaje) {

        if (trayIcon == null) return;

        // Mostramos la notificación nativa del SO
        trayIcon.displayMessage(
            "Tracklify — Recordatorio",  // Título
            mensaje,                      // Cuerpo
            MessageType.INFO              // Icono informativo
        );
    }
}