package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.HabitoDAO;
import com.mycompany.tracklify.dao.NotificacionDAO;
import com.mycompany.tracklify.dao.RegistroHabitoDAO;
import com.mycompany.tracklify.models.Habito;
import com.mycompany.tracklify.models.RegistroHabito;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 * Controlador de la vista mensual de calendario: navegación por mes/año, rejilla de días
 * con marcas de cumplimiento y listado de hábitos del día seleccionado.
 *
 * @author Tracklify
 */
public class CalendarioController implements Initializable {

    /** Paleta cíclica para asociar un color visible a cada hábito activo. */
    private static final List<String> PALETA_COLORES = List.of(
        "#7A4578", "#93588F", "#B06EA8", "#5C3460",
        "#C48ABD", "#4A2850", "#D4A8CC", "#3D1F42",
        "#E8C8E0", "#6B3D6B"
    );

    private static final String[] CABECERAS_SEMANA = {"DOM", "LUN", "MAR", "MIÉ", "JUE", "VIE", "SAB"};
    private static final int CELDAS_CALENDARIO = 42;

    @FXML
    private Button btnAnioAnterior;

    @FXML
    private Button btnMesAnterior;

    @FXML
    private Label labelMesAnio;

    @FXML
    private Button btnMesSiguiente;

    @FXML
    private Button btnAnioSiguiente;

    @FXML
    private GridPane gridCalendario;

    @FXML
    private VBox panelHabitos;

    @FXML
    private Label labelFechaSeleccionada;

    @FXML
    private VBox listaHabitosDelDia;

    /** Acceso a hábitos del usuario. */
    private final HabitoDAO habitoDAO = new HabitoDAO();

    /** Registros de cumplimiento y sesiones. */
    private final RegistroHabitoDAO registroHabitoDAO = new RegistroHabitoDAO();

    /** Recordatorios programados. */
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();

    /** Mes mostrado en la rejilla. */
    private YearMonth mesActual = YearMonth.now();

    /** Día resaltado y origen del panel inferior. */
    private LocalDate diaSeleccionado = LocalDate.now();

    /** Hábitos en estado activo del usuario actual. */
    private List<Habito> habitosActivos = new ArrayList<>();

    /** Color de marca por identificador de hábito. */
    private final Map<Integer, Color> coloresPorHabito = new HashMap<>();

    /** Referencia al marco principal (opcional). */
    @SuppressWarnings("unused")
    private MainViewController host;

    /**
     * Enlaza el controlador del shell principal.
     *
     * @param host controlador de {@code main_view.fxml}; puede ser {@code null}
     */
    public void setHost(MainViewController host) {
        this.host = host;
    }

    /**
     * Carga hábitos activos, asigna colores, pinta el mes y rellena el panel del día actual.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btnAnioAnterior.setOnAction(e -> {
            mesActual = mesActual.minusYears(1);
            renderizarCalendario();
        });
        btnMesAnterior.setOnAction(e -> {
            mesActual = mesActual.minusMonths(1);
            renderizarCalendario();
        });
        btnMesSiguiente.setOnAction(e -> {
            mesActual = mesActual.plusMonths(1);
            renderizarCalendario();
        });
        btnAnioSiguiente.setOnAction(e -> {
            mesActual = mesActual.plusYears(1);
            renderizarCalendario();
        });

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) {
            habitosActivos = new ArrayList<>();
        } else {
            habitosActivos = habitoDAO.obtenerActivosPorUsuario(usuario.getIdUsuario());
            for (int i = 0; i < habitosActivos.size(); i++) {
                Habito h = habitosActivos.get(i);
                coloresPorHabito.put(h.getIdHabito(), Color.web(PALETA_COLORES.get(i % PALETA_COLORES.size())));
            }
        }

        renderizarCalendario();
    }

    /**
     * Ajusta el día seleccionado al mes visible si hace falta, actualiza el título del mes,
     * reconstruye la cabecera DOM–SAB y las 42 celdas, y refresca el panel de hábitos del día seleccionado.
     */
    private void renderizarCalendario() {
        if (!YearMonth.from(diaSeleccionado).equals(mesActual)) {
            int dia = Math.min(diaSeleccionado.getDayOfMonth(), mesActual.lengthOfMonth());
            diaSeleccionado = mesActual.atDay(dia);
        }

        String nombreMes = mesActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        labelMesAnio.setText(capitalizar(nombreMes) + " " + mesActual.getYear());

        gridCalendario.getChildren().clear();

        for (int c = 0; c < 7; c++) {
            Label cab = new Label(CABECERAS_SEMANA[c]);
            cab.getStyleClass().add("cabecera-calendario");
            cab.setMaxWidth(Double.MAX_VALUE);
            cab.setAlignment(Pos.CENTER);
            gridCalendario.add(cab, c, 0);
        }

        List<Integer> ids = habitosActivos.stream().map(Habito::getIdHabito).collect(Collectors.toList());
        Map<LocalDate, Integer> cumplidosPorDia = registroHabitoDAO.obtenerDiasCumplidosPorMes(mesActual, ids);

        LocalDate primerDiaMes = mesActual.atDay(1);
        int offsetDomingo = primerDiaMes.getDayOfWeek().getValue() % 7;
        LocalDate inicioRejilla = primerDiaMes.minusDays(offsetDomingo);

        for (int i = 0; i < CELDAS_CALENDARIO; i++) {
            LocalDate fecha = inicioRejilla.plusDays(i);
            int fila = 1 + i / 7;
            int col = i % 7;
            StackPane celda = crearCeldaDia(fecha, cumplidosPorDia);
            gridCalendario.add(celda, col, fila);
        }

        cargarHabitosDelDia(diaSeleccionado);
    }

    /**
     * Construye una celda de 60×60 px para un día concreto, con estilos de mes actual, hoy, selección y punto de cumplimiento.
     *
     * @param fecha           día representado
     * @param cumplidosPorDia mapa día → {@code id_habito} para el indicador inferior
     * @return nodo raíz clicable
     */
    private StackPane crearCeldaDia(LocalDate fecha, Map<LocalDate, Integer> cumplidosPorDia) {
        StackPane celda = new StackPane();
        celda.setPrefSize(60, 60);
        celda.setMinSize(60, 60);
        celda.setMaxSize(60, 60);
        celda.getStyleClass().add("celda-calendario");

        boolean enMesVisible = YearMonth.from(fecha).equals(mesActual);
        boolean esHoy = fecha.equals(LocalDate.now());
        boolean esSeleccionado = fecha.equals(diaSeleccionado);

        if (esSeleccionado) {
            celda.getStyleClass().add("dia-seleccionado");
        }

        if (esHoy) {
            Region fondoHoy = new Region();
            fondoHoy.setPrefSize(36, 36);
            fondoHoy.setMinSize(36, 36);
            fondoHoy.setMaxSize(36, 36);
            fondoHoy.getStyleClass().add("dia-actual");
            StackPane.setAlignment(fondoHoy, Pos.CENTER);
            celda.getChildren().add(fondoHoy);
        }

        Label num = new Label(String.valueOf(fecha.getDayOfMonth()));
        num.setMouseTransparent(true);
        if (esHoy) {
            num.setStyle("-fx-text-fill: white; -fx-font-size: 15px; -fx-font-weight: bold;");
        } else if (!enMesVisible) {
            num.getStyleClass().add("dia-otro-mes");
        } else {
            num.setStyle("-fx-text-fill: #2D1B2E; -fx-font-size: 15px; -fx-font-weight: bold;");
        }
        StackPane.setAlignment(num, Pos.CENTER);
        celda.getChildren().add(num);

        Integer idHabitoMarca = cumplidosPorDia.get(fecha);
        if (idHabitoMarca != null) {
            Color color = coloresPorHabito.getOrDefault(idHabitoMarca, Color.web("#7A4578"));
            Circle punto = new Circle(4);
            punto.setFill(color);
            punto.setMouseTransparent(true);
            StackPane.setAlignment(punto, Pos.BOTTOM_CENTER);
            StackPane.setMargin(punto, new Insets(0, 0, 6, 0));
            celda.getChildren().add(punto);
        }

        celda.setOnMouseClicked(ev -> {
            diaSeleccionado = fecha;
            if (!YearMonth.from(fecha).equals(mesActual)) {
                mesActual = YearMonth.from(fecha);
            }
            renderizarCalendario();
        });

        return celda;
    }

    /**
     * Rellena el panel inferior con una fila por hábito activo y la hora de la próxima notificación si existe.
     *
     * @param fecha día seleccionado
     */
    private void cargarHabitosDelDia(LocalDate fecha) {
        labelFechaSeleccionada.setText("Hábitos del " + formatearFechaLarga(fecha));

        listaHabitosDelDia.getChildren().clear();

        if (habitosActivos.isEmpty()) {
            Label vacio = new Label("No hay hábitos para este día");
            vacio.setStyle("-fx-text-fill: #6B4A6E; -fx-font-size: 13px;");
            vacio.setMaxWidth(Double.MAX_VALUE);
            vacio.setAlignment(Pos.CENTER);
            listaHabitosDelDia.getChildren().add(vacio);
            return;
        }

        for (Habito habito : habitosActivos) {
            listaHabitosDelDia.getChildren().add(crearFilaHabitoDelDia(habito, fecha));
        }
    }

    /**
     * Construye la fila visual de un hábito: franja de color, nombre, estado, hora de notificación e icono de registro.
     *
     * @param habito hábito listado
     * @param fecha  día consultado
     * @return {@link HBox} listo para insertar en {@link #listaHabitosDelDia}
     */
    private HBox crearFilaHabitoDelDia(Habito habito, LocalDate fecha) {
        HBox fila = new HBox(10);
        fila.setAlignment(Pos.CENTER_LEFT);
        fila.getStyleClass().add("fila-habito-calendario");
        fila.setPadding(new Insets(4, 8, 4, 8));

        Color c = coloresPorHabito.getOrDefault(habito.getIdHabito(), Color.web("#7A4578"));
        Rectangle franja = new Rectangle(4, 40);
        franja.setFill(c);

        VBox textos = new VBox(2);
        Label nombre = new Label(habito.getNombreHabito() != null ? habito.getNombreHabito() : "—");
        nombre.setStyle("-fx-text-fill: #2D1B2E; -fx-font-size: 14px; -fx-font-weight: bold;");

        RegistroHabito reg = registroHabitoDAO.obtenerRegistroHoy(habito.getIdHabito(), fecha);
        String estadoTxt = reg == null ? "Sin registro" : reg.getEstadoRegistro();
        Label estado = new Label(estadoTxt != null ? estadoTxt : "—");
        estado.setStyle("-fx-text-fill: #6B4A6E; -fx-font-size: 11px;");
        textos.getChildren().addAll(nombre, estado);

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);

        Label horaNotif = new Label();
        horaNotif.setStyle("-fx-text-fill: #93588F; -fx-font-size: 13px; -fx-font-weight: bold;");
        LocalDateTime prox = notificacionDAO.obtenerProximaNotificacion(habito.getIdHabito());
        if (prox != null) {
            horaNotif.setText(String.format("%02d:%02d", prox.getHour(), prox.getMinute()));
            horaNotif.setVisible(true);
            horaNotif.setManaged(true);
        } else {
            horaNotif.setVisible(false);
            horaNotif.setManaged(false);
        }

        Circle iconoEstado = new Circle(6);
        iconoEstado.setVisible(false);
        iconoEstado.setManaged(false);
        if (reg != null) {
            String er = reg.getEstadoRegistro();
            if (er != null && "COMPLETADO".equalsIgnoreCase(er.trim())) {
                iconoEstado.setFill(Color.web("#2E7D32"));
                iconoEstado.setVisible(true);
                iconoEstado.setManaged(true);
            } else if (er != null && "PENDIENTE".equalsIgnoreCase(er.trim())) {
                iconoEstado.setFill(Color.web("#C8B8CA"));
                iconoEstado.setVisible(true);
                iconoEstado.setManaged(true);
            }
        }

        fila.getChildren().addAll(franja, textos, espacio, horaNotif, iconoEstado);
        return fila;
    }

    /**
     * Formatea la fecha como {@code dd} + nombre de mes en español + año, con el mes capitalizado.
     *
     * @param fecha día a mostrar
     * @return cadena tipo {@code 03 Mayo 2026}
     */
    private String formatearFechaLarga(LocalDate fecha) {
        String mes = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        return String.format("%02d", fecha.getDayOfMonth()) + " "
            + capitalizar(mes) + " " + fecha.getYear();
    }

    /**
     * Pone en mayúscula la primera letra de la cadena (útil para nombres de mes y títulos de fecha).
     *
     * @param s texto
     * @return cadena capitalizada o {@code null} / vacío sin cambios
     */
    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}
