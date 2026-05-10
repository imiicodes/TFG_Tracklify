package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.HabitoDAO;
import com.mycompany.tracklify.dao.PerfilDAO;
import com.mycompany.tracklify.models.Habito;
import com.mycompany.tracklify.models.Perfil;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.NotificacionScheduler;
import com.mycompany.tracklify.utils.RachaService;
import com.mycompany.tracklify.utils.SessionManager;
import com.mycompany.tracklify.utils.TemaService;
import com.mycompany.tracklify.utils.TokenService;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador del marco principal ({@code main_view.fxml}): barra superior, barra lateral
 * fija, un {@link AnchorPane} central donde se cargan las distintas vistas FXML sin
 * sustituir el {@link Stage} ni la {@link Scene} tras el inicio de sesión, y el asistente
 * de chat flotante enlazado al webhook configurado.
 *
 * @author Tracklify
 */
public class MainViewController implements Initializable {

    @FXML
    private AnchorPane contenidoPrincipal;

    @FXML
    private Button btnNavDashboard;

    @FXML
    private Button btnNavMisHabitos;

    @FXML
    private Button btnNavCalendario;

    @FXML
    private Button btnNavEstadisticas;

    @FXML
    private Button btnNavInformes;

    @FXML
    private Button btnNavConfiguracion;

    @FXML
    private Button btnChatbot;

    @FXML
    private VBox panelChatbot;

    @FXML
    private ScrollPane scrollChat;

    @FXML
    private VBox mensajesChat;

    @FXML
    private TextField campoChatInput;

    @FXML
    private Button btnEnviarChat;

    /** {@code true} tras mostrar una vez el mensaje de bienvenida del asistente. */
    private boolean bienvenidaChatMostrada;

    /** Botones laterales para aplicar o quitar la clase de ítem activo. */
    private final List<Button> botonesSidebar = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        botonesSidebar.clear();
        botonesSidebar.add(btnNavDashboard);
        botonesSidebar.add(btnNavMisHabitos);
        botonesSidebar.add(btnNavCalendario);
        botonesSidebar.add(btnNavEstadisticas);
        botonesSidebar.add(btnNavInformes);
        botonesSidebar.add(btnNavConfiguracion);

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario != null) {
            NotificacionScheduler.getInstancia().iniciar(usuario.getIdUsuario());
        }

        cargarVista("dashboard_view.fxml");
        marcarItemActivo(btnNavDashboard);

        Usuario usuarioTema = SessionManager.getInstancia().getUsuarioActual();
        if (usuarioTema != null) {
            Perfil perfil = new PerfilDAO().obtenerPorUsuario(usuarioTema.getIdUsuario());
            if (perfil != null) {
                Platform.runLater(() -> {
                    Scene escena = btnNavDashboard.getScene();
                    if (escena != null) {
                        TemaService.aplicar(perfil.getTema(), escena);
                    }
                });
            }
        }

        if (campoChatInput != null) {
            campoChatInput.setOnAction(e -> enviarMensajeChat());
        }
    }

    /**
     * Carga un FXML en el panel central, anclándolo a los cuatro bordes del {@link AnchorPane}.
     *
     * @param fxml nombre del archivo bajo {@code /fxml/} (p. ej. {@code "dashboard_view.fxml"})
     */
    private void cargarVista(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/" + fxml));
            Node vista = loader.load();
            if ("mis_habitos_view.fxml".equals(fxml)) {
                MisHabitosController c = loader.getController();
                c.setHost(this);
            } else if ("dashboard_view.fxml".equals(fxml)) {
                DashboardViewController d = loader.getController();
                d.setHost(this);
            } else if ("estadisticas_view.fxml".equals(fxml)) {
                EstadisticasController estadisticas = loader.getController();
                estadisticas.setHost(this);
            } else if ("calendario_view.fxml".equals(fxml)) {
                CalendarioController calendario = loader.getController();
                calendario.setHost(this);
            } else if ("configuracion_view.fxml".equals(fxml)) {
                ConfiguracionController configuracion = loader.getController();
                configuracion.setHost(this);
            }
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);
            contenidoPrincipal.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Marca un botón del sidebar como seleccionado y deja el resto en estado inactivo.
     *
     * @param btn botón pulsado o asociado a la vista mostrada
     */
    private void marcarItemActivo(Button btn) {
        for (Button b : botonesSidebar) {
            b.getStyleClass().remove("sidebar-item-activo");
        }
        if (btn != null) {
            if (!btn.getStyleClass().contains("sidebar-item-activo")) {
                btn.getStyleClass().add("sidebar-item-activo");
            }
        }
    }

    /**
     * Muestra el dashboard en el área central y actualiza el resaltado del menú.
     *
     * @param event evento del botón «Dashboard»
     */
    @FXML
    public void mostrarDashboard(ActionEvent event) {
        marcarItemActivo(btnNavDashboard);
        cargarVista("dashboard_view.fxml");
    }

    /**
     * Muestra el listado «Mis hábitos» en el área central.
     *
     * @param event evento del botón «Mis hábitos»
     */
    @FXML
    public void mostrarMisHabitos(ActionEvent event) {
        marcarItemActivo(btnNavMisHabitos);
        cargarVista("mis_habitos_view.fxml");
    }

    /**
     * Muestra la vista de calendario (placeholder si aún no hay lógica).
     *
     * @param event evento del botón «Calendario»
     */
    @FXML
    public void mostrarCalendario(ActionEvent event) {
        marcarItemActivo(btnNavCalendario);
        cargarVista("calendario_view.fxml");
    }

    /**
     * Muestra la vista de estadísticas.
     *
     * @param event evento del botón «Estadísticas»
     */
    @FXML
    public void mostrarEstadisticas(ActionEvent event) {
        marcarItemActivo(btnNavEstadisticas);
        cargarVista("estadisticas_view.fxml");
    }

    /**
     * Muestra la vista de informes.
     *
     * @param event evento del botón «Informes»
     */
    @FXML
    public void mostrarInformes(ActionEvent event) {
        marcarItemActivo(btnNavInformes);
        cargarVista("informes_view.fxml");
    }

    /**
     * Muestra la vista de configuración.
     *
     * @param event evento del botón «Configuración»
     */
    @FXML
    public void mostrarConfiguracion(ActionEvent event) {
        marcarItemActivo(btnNavConfiguracion);
        cargarVista("configuracion_view.fxml");
    }

    /**
     * Muestra u oculta el panel del asistente y, al abrirlo, lo sitúa por encima del resto de nodos.
     */
    @FXML
    void toggleChatbot() {
        boolean abierto = !panelChatbot.isVisible();
        panelChatbot.setVisible(abierto);
        panelChatbot.setManaged(abierto);
        if (abierto) {
            panelChatbot.toFront();
            btnChatbot.toFront();
            if (!bienvenidaChatMostrada) {
                agregarBurbuja(
                    "¡Hola! Soy tu asistente de Tracklify. ¿En qué puedo ayudarte hoy?",
                    false
                );
                bienvenidaChatMostrada = true;
            }
            campoChatInput.requestFocus();
        }
    }

    /**
     * Envía el texto del campo al webhook del asistente y muestra la respuesta en el hilo de JavaFX.
     */
    @FXML
    void enviarMensajeChat() {
        String mensaje = campoChatInput.getText().trim();
        if (mensaje.isEmpty()) {
            return;
        }

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) {
            agregarBurbuja("Debes iniciar sesión para usar el asistente.", false);
            campoChatInput.clear();
            return;
        }

        agregarBurbuja(mensaje, true);
        campoChatInput.clear();

        Thread hiloChat = new Thread(() -> {
            try {
                Perfil perfil = new PerfilDAO().obtenerPorUsuario(usuario.getIdUsuario());
                List<Habito> habitos = new HabitoDAO().obtenerActivosPorUsuario(usuario.getIdUsuario());
                String nombresHabitos = habitos.stream()
                    .map(Habito::getNombreHabito)
                    .map(n -> n != null ? n : "")
                    .collect(Collectors.joining(", "));
                int racha = habitos.isEmpty()
                    ? 0
                    : new RachaService().calcularRachaActual(habitos.get(0).getIdHabito());

                String nombreCtx = "Usuario";
                if (perfil != null && perfil.getNombreUsuario() != null && !perfil.getNombreUsuario().isBlank()) {
                    nombreCtx = perfil.getNombreUsuario();
                }

                Map<String, String> datos = Map.of(
                    "nombre", nombreCtx,
                    "habitos", nombresHabitos,
                    "racha", String.valueOf(racha),
                    "mensaje", mensaje
                );
                String respuesta = TokenService.llamarWebhookConRespuesta(
                    "http://localhost:5678/webhook/chatbot",
                    datos
                );

                Platform.runLater(() -> agregarBurbuja(respuesta, false));

            } catch (Exception e) {
                Platform.runLater(() -> agregarBurbuja("Error al conectar con el asistente.", false));
            }
        });
        hiloChat.setDaemon(true);
        hiloChat.start();
    }

    /**
     * Añade una burbuja de mensaje al listado del chat (usuario a la derecha, asistente a la izquierda).
     *
     * @param texto   contenido del mensaje
     * @param esUsuario {@code true} si lo envió el usuario; {@code false} si es respuesta del asistente
     */
    private void agregarBurbuja(String texto, boolean esUsuario) {
        Label burbuja = new Label(texto);
        burbuja.setWrapText(true);
        burbuja.setMaxWidth(240);
        burbuja.setPadding(new Insets(8, 12, 8, 12));
        if (esUsuario) {
            burbuja.setStyle(
                "-fx-background-color: #7A4578; -fx-text-fill: white; "
                    + "-fx-background-radius: 12 12 0 12;"
            );
            HBox fila = new HBox(burbuja);
            fila.setAlignment(Pos.CENTER_RIGHT);
            fila.setPadding(new Insets(4, 8, 4, 8));
            mensajesChat.getChildren().add(fila);
        } else {
            burbuja.setStyle(
                "-fx-background-color: #F3D9F5; -fx-text-fill: #2D1B2E; "
                    + "-fx-background-radius: 12 12 12 0;"
            );
            HBox fila = new HBox(burbuja);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setPadding(new Insets(4, 8, 4, 8));
            mensajesChat.getChildren().add(fila);
        }
        scrollChat.layout();
        Platform.runLater(() -> scrollChat.setVvalue(1.0));
    }

    /**
     * Abre el asistente de creación de hábito dentro del panel central.
     *
     * @param desdeMisHabitos si es {@code true}, al volver o guardar se recarga «Mis hábitos»;
     *                        si es {@code false}, se vuelve al dashboard
     */
    public void abrirCrearHabito(boolean desdeMisHabitos) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear_habito_view.fxml"));
            Node vista = loader.load();
            CrearHabitoController ctrl = loader.getController();
            ctrl.setOnVolver(() -> {
                if (desdeMisHabitos) {
                    cargarVista("mis_habitos_view.fxml");
                    marcarItemActivo(btnNavMisHabitos);
                } else {
                    cargarVista("dashboard_view.fxml");
                    marcarItemActivo(btnNavDashboard);
                }
            });
            ctrl.setHabitoAEditar(null);
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);
            contenidoPrincipal.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Abre el asistente en modo edición para el hábito indicado.
     *
     * @param habito hábito a modificar
     * @param desdeMisHabitos misma semántica que {@link #abrirCrearHabito(boolean)}
     */
    public void abrirEditorHabito(Habito habito, boolean desdeMisHabitos) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear_habito_view.fxml"));
            Node vista = loader.load();
            CrearHabitoController ctrl = loader.getController();
            ctrl.setOnVolver(() -> {
                if (desdeMisHabitos) {
                    cargarVista("mis_habitos_view.fxml");
                    marcarItemActivo(btnNavMisHabitos);
                } else {
                    cargarVista("dashboard_view.fxml");
                    marcarItemActivo(btnNavDashboard);
                }
            });
            ctrl.setHabitoAEditar(habito);
            AnchorPane.setTopAnchor(vista, 0.0);
            AnchorPane.setBottomAnchor(vista, 0.0);
            AnchorPane.setLeftAnchor(vista, 0.0);
            AnchorPane.setRightAnchor(vista, 0.0);
            contenidoPrincipal.getChildren().setAll(vista);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Cierra la sesión, detiene el scheduler de notificaciones y vuelve a la pantalla de landing.
     *
     * @param event evento del botón «Cerrar sesión»
     * @throws Exception si el FXML de landing no se puede cargar
     */
    @FXML
    public void cerrarSesion(ActionEvent event) throws Exception {

        NotificacionScheduler.getInstancia().detener();
        SessionManager.getInstancia().cerrarSesion();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/landing_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }
}
