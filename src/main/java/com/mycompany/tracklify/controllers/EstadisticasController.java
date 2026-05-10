package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.EstadisticaDAO;
import com.mycompany.tracklify.dao.HabitoDAO;
import com.mycompany.tracklify.dao.RegistroHabitoDAO;
import com.mycompany.tracklify.models.Habito;
import com.mycompany.tracklify.models.ProgresoSemanal;
import com.mycompany.tracklify.models.ResumenUsuario;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.RachaService;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;

/**
 * Controlador de la vista de estadísticas: métricas por hábito o globales, gráficos semanales
 * e historial de cumplimientos en calendario.
 *
 * @author Tracklify
 */
public class EstadisticasController implements Initializable {

    private static final Comparator<ProgresoSemanal> ORDEN_SEMANA =
        Comparator.comparingInt(ProgresoSemanal::getAnioSemana)
            .thenComparingInt(ProgresoSemanal::getNumSemana);

    private static final String COLOR_BARRA_COMPLETADO = "-fx-bar-fill: #7A4578;";
    private static final String COLOR_BARRA_OBJETIVO = "-fx-bar-fill: #EDD6F0;";
    private static final String ESTILO_LINEA = "-fx-stroke: #93588F; -fx-stroke-width: 2px;";
    private static final String ESTILO_SIMBOLO = "-fx-background-color: #7A4578, white; -fx-background-insets: 0, 2; -fx-padding: 3px;";

    @FXML
    private ComboBox<Habito> comboHabito;

    @FXML
    private ToggleGroup grupoVistaEstadisticas;

    @FXML
    private ToggleButton btnVistaHabito;

    @FXML
    private ToggleButton btnVistaGlobal;

    @FXML
    private Label labelRachaActual;

    @FXML
    private Label labelMejorRacha;

    @FXML
    private Label labelTotalCumplimientos;

    @FXML
    private Label labelTasaExito;

    @FXML
    private BarChart<String, Number> graficaBarras;

    @FXML
    private LineChart<String, Number> graficaLineas;

    @FXML
    private NumberAxis ejeYLineas;

    @FXML
    private Label labelSinDatosBarras;

    @FXML
    private Label labelSinDatosLineas;

    @FXML
    private GridPane gridCalendario;

    @FXML
    private ScrollPane scrollEstadisticas;

    /** DAO de vistas de informe y resumen. */
    private final EstadisticaDAO estadisticaDAO = new EstadisticaDAO();

    /** Cálculo de racha actual desde días cumplidos. */
    private final RachaService rachaService = new RachaService();

    /** Acceso a hábitos. */
    private final HabitoDAO habitoDAO = new HabitoDAO();

    /** Registros de cumplimiento. */
    private final RegistroHabitoDAO registroHabitoDAO = new RegistroHabitoDAO();

    /** Hábitos activos del usuario actual. */
    private List<Habito> habitosActivos = new ArrayList<>();

    /** Hábito cuyos datos se muestran en modo «Por hábito». */
    private Habito habitoSeleccionado;

    /** Si es {@code true}, se muestran agregados de todos los hábitos activos. */
    private boolean vistaGlobal = false;

    /** Referencia al marco principal (opcional, para coherencia con otras subvistas). */
    @SuppressWarnings("unused")
    private MainViewController host;

    /** Evita disparar la lógica del ToggleGroup durante el primer {@link #initialize}. */
    private boolean inicializando = true;

    /**
     * Asocia el controlador del shell principal (marco con barra lateral).
     *
     * @param host controlador de {@code main_view.fxml}; puede ser {@code null}
     */
    public void setHost(MainViewController host) {
        this.host = host;
    }

    /**
     * Inicializa combos, listeners, ejes de gráficas y la primera carga de datos.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle recursos; no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarScrollSuave(scrollEstadisticas);
        configurarEjePorcentajeLineas();
        configurarEstilosGraficasBase();

        if (grupoVistaEstadisticas == null) {
            grupoVistaEstadisticas = new ToggleGroup();
            btnVistaHabito.setToggleGroup(grupoVistaEstadisticas);
            btnVistaGlobal.setToggleGroup(grupoVistaEstadisticas);
        }

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) {
            inicializando = false;
            return;
        }

        habitosActivos = habitoDAO.obtenerActivosPorUsuario(usuario.getIdUsuario());
        comboHabito.setConverter(new StringConverter<>() {
            @Override
            public String toString(Habito h) {
                return h == null ? "" : h.getNombreHabito();
            }

            @Override
            public Habito fromString(String s) {
                return null;
            }
        });
        comboHabito.getItems().setAll(habitosActivos);

        if (!habitosActivos.isEmpty()) {
            comboHabito.getSelectionModel().select(0);
            habitoSeleccionado = habitosActivos.get(0);
        }

        comboHabito.valueProperty().addListener((obs, anterior, nuevo) -> {
            if (!vistaGlobal && nuevo != null) {
                cargarDatosHabito(nuevo);
            }
        });

        if (grupoVistaEstadisticas != null) {
            grupoVistaEstadisticas.selectedToggleProperty().addListener((ChangeListener<Toggle>) (obs, oldVal, newVal) -> {
                if (inicializando) {
                    return;
                }
                if (newVal == btnVistaGlobal) {
                    mostrarVistaGlobal();
                } else if (newVal == btnVistaHabito) {
                    mostrarVistaHabito();
                }
            });
        }

        if (!habitosActivos.isEmpty()) {
            cargarDatosHabito(habitosActivos.get(0));
        } else {
            limpiarMetricasVacias();
            mostrarGraficasSinDatos();
            gridCalendario.getChildren().clear();
        }

        inicializando = false;
    }

    /**
     * Ajusta el desplazamiento vertical del panel de estadísticas con la rueda del ratón de forma más suave.
     *
     * @param scrollPane raíz con scroll de la vista de estadísticas
     */
    private void configurarScrollSuave(ScrollPane scrollPane) {
        if (scrollPane == null) {
            return;
        }
        Node content = scrollPane.getContent();
        if (content == null) {
            return;
        }
        content.setOnScroll(event -> {
            double delta = event.getDeltaY() * -0.003;
            double nuevo = scrollPane.getVvalue() + delta;
            scrollPane.setVvalue(Math.max(0, Math.min(1, nuevo)));
            event.consume();
        });
    }

    /**
     * Configura el eje Y del gráfico de líneas entre 0 y 100 con sufijo de porcentaje en las etiquetas.
     */
    private void configurarEjePorcentajeLineas() {
        if (ejeYLineas == null) {
            return;
        }
        ejeYLineas.setAutoRanging(false);
        ejeYLineas.setLowerBound(0);
        ejeYLineas.setUpperBound(100);
        ejeYLineas.setLabel("%");
        ejeYLineas.setTickUnit(25);
        ejeYLineas.setMinorTickCount(0);
        ejeYLineas.setTickLabelFormatter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(Number n) {
                return n == null ? "" : n.intValue() + "%";
            }

            @Override
            public Number fromString(String string) {
                return 0;
            }
        });
    }

    /**
     * Aplica colores de la paleta a ejes, rejillas y leyendas de ambas gráficas.
     */
    private void configurarEstilosGraficasBase() {
        String estiloEje = "-fx-tick-label-fill: #6B4A6E; -fx-text-fill: #2D1B2E;";
        if (graficaBarras != null) {
            graficaBarras.setAnimated(false);
            graficaBarras.lookupAll(".axis").forEach(n -> n.setStyle(estiloEje));
            javafx.scene.chart.CategoryAxis xaB = (javafx.scene.chart.CategoryAxis) graficaBarras.getXAxis();
            xaB.setTickLabelFill(javafx.scene.paint.Color.web("#6B4A6E"));
        }
        if (graficaLineas != null) {
            graficaLineas.setAnimated(false);
            graficaLineas.lookupAll(".axis").forEach(n -> n.setStyle(estiloEje));
            javafx.scene.chart.CategoryAxis xaL = (javafx.scene.chart.CategoryAxis) graficaLineas.getXAxis();
            xaL.setTickLabelFill(javafx.scene.paint.Color.web("#6B4A6E"));
        }
    }

    /**
     * Actualiza el hábito seleccionado y refresca métricas, gráficas y calendario (modo por hábito).
     *
     * @param habito hábito del que se muestran los datos; no debe ser {@code null}
     */
    public void cargarDatosHabito(Habito habito) {
        if (habito == null) {
            return;
        }
        habitoSeleccionado = habito;
        actualizarMetricas(habito);
        actualizarGraficaBarras(habito);
        actualizarGraficaLineas(habito);
        actualizarCalendario(habito);
    }

    /**
     * Rellena las cuatro tarjetas de métricas para un hábito concreto.
     *
     * <p>Racha actual y mejor racha son específicas del hábito. Total de cumplimientos y tasa de éxito
     * se leen de la vista {@code v_resumen_usuario} del propietario del hábito, según el modelo de datos
     * agregado por usuario.</p>
     *
     * @param habito hábito analizado (para rachas y para resolver {@code id_usuario})
     */
    private void actualizarMetricas(Habito habito) {
        int racha = rachaService.calcularRachaActual(habito.getIdHabito());
        int mejorRacha = calcularMejorRacha(habito.getIdHabito());
        labelRachaActual.setText(formatearDias(racha));
        labelMejorRacha.setText(formatearDias(mejorRacha));

        ResumenUsuario resumen = estadisticaDAO.obtenerResumenUsuario(habito.getIdUsuario());
        if (resumen != null) {
            labelTotalCumplimientos.setText(String.valueOf(resumen.getTotalCumplimientos()));
            double tasa = resumen.getTasaExitoGlobal();
            if (tasa >= 0 && tasa <= 1.0) {
                tasa = tasa * 100.0;
            }
            labelTasaExito.setText(String.format("%.0f%%", tasa));
        } else {
            labelTotalCumplimientos.setText("0");
            labelTasaExito.setText("0%");
        }
    }

    /**
     * Formatea un número de días como «1 día» o «X días».
     *
     * @param dias cantidad de días
     * @return cadena legible
     */
    private String formatearDias(int dias) {
        if (dias == 1) {
            return "1 día";
        }
        return dias + " días";
    }

    /**
     * Calcula la racha máxima histórica de días consecutivos con cumplimiento.
     *
     * @param idHabito identificador del hábito
     * @return longitud máxima de segmentos consecutivos en el conjunto de días cumplidos
     */
    private int calcularMejorRacha(int idHabito) {
        List<LocalDate> dias = new ArrayList<>(registroHabitoDAO.obtenerDiasCumplidos(idHabito));
        if (dias.isEmpty()) {
            return 0;
        }
        Collections.sort(dias);
        int max = 0;
        int actual = 0;
        LocalDate anterior = null;
        for (LocalDate d : dias) {
            if (anterior != null && d.equals(anterior.plusDays(1))) {
                actual++;
            } else {
                actual = 1;
            }
            max = Math.max(max, actual);
            anterior = d;
        }
        return max;
    }

    /**
     * Actualiza el gráfico de barras con las últimas 8 semanas: series «Completado» y «Objetivo».
     *
     * @param habito hábito origen de {@link EstadisticaDAO#obtenerProgresoSemanal(int)}
     */
    private void actualizarGraficaBarras(Habito habito) {
        List<ProgresoSemanal> ultimas = ultimasSemanasOrdenadas(estadisticaDAO.obtenerProgresoSemanal(habito.getIdHabito()), 8);
        if (ultimas.isEmpty()) {
            graficaBarras.getData().clear();
            mostrarSinDatosBarras(true);
            return;
        }
        mostrarSinDatosBarras(false);

        XYChart.Series<String, Number> serieCompletado = new XYChart.Series<>();
        serieCompletado.setName("Completado");
        XYChart.Series<String, Number> serieObjetivo = new XYChart.Series<>();
        serieObjetivo.setName("Objetivo");

        for (ProgresoSemanal p : ultimas) {
            String etiqueta = etiquetaSemana(p);
            serieCompletado.getData().add(new XYChart.Data<>(etiqueta, p.getVecesCompletado()));
            serieObjetivo.getData().add(new XYChart.Data<>(etiqueta, p.getObjetivoVeces()));
        }

        graficaBarras.getData().setAll(serieCompletado, serieObjetivo);
        aplicarColoresBarrasDespuesDeRender();
    }

    /**
     * Etiqueta corta del eje X para una fila de progreso semanal.
     *
     * @param p fila de {@code v_progreso_semanal}
     * @return texto «Sem N» con el número de semana ISO
     */
    private String etiquetaSemana(ProgresoSemanal p) {
        return "Sem " + p.getNumSemana();
    }

    /**
     * Ordena por año/semana ISO y devuelve como máximo las últimas {@code max} filas.
     *
     * @param lista datos brutos
     * @param max   tamaño máximo del resultado
     * @return sublista ordenada cronológicamente
     */
    private List<ProgresoSemanal> ultimasSemanasOrdenadas(List<ProgresoSemanal> lista, int max) {
        if (lista == null || lista.isEmpty()) {
            return Collections.emptyList();
        }
        List<ProgresoSemanal> copia = new ArrayList<>(lista);
        copia.sort(ORDEN_SEMANA);
        if (copia.size() <= max) {
            return copia;
        }
        return new ArrayList<>(copia.subList(copia.size() - max, copia.size()));
    }

    /**
     * Aplica la paleta al {@link BarChart}: primera serie {@code #7A4578}, segunda {@code #EDD6F0},
     * usando selectores CSS tras el renderizado del nodo.
     */
    private void aplicarColoresBarrasDespuesDeRender() {
        Platform.runLater(() -> {
            for (Node n : graficaBarras.lookupAll(".default-color0.chart-bar")) {
                n.setStyle(COLOR_BARRA_COMPLETADO);
            }
            for (Node n : graficaBarras.lookupAll(".default-color1.chart-bar")) {
                n.setStyle(COLOR_BARRA_OBJETIVO);
            }
        });
    }

    /**
     * Actualiza el gráfico de líneas con el porcentaje de cumplimiento semanal.
     *
     * @param habito hábito analizado
     */
    private void actualizarGraficaLineas(Habito habito) {
        List<ProgresoSemanal> ultimas = ultimasSemanasOrdenadas(estadisticaDAO.obtenerProgresoSemanal(habito.getIdHabito()), 8);
        if (ultimas.isEmpty()) {
            graficaLineas.getData().clear();
            mostrarSinDatosLineas(true);
            return;
        }
        mostrarSinDatosLineas(false);

        XYChart.Series<String, Number> seriePct = new XYChart.Series<>();
        seriePct.setName("% Cumplimiento");

        for (ProgresoSemanal p : ultimas) {
            double v = p.getPorcentajeCompletado();
            if (v >= 0 && v <= 1.0) {
                v = v * 100.0;
            }
            seriePct.getData().add(new XYChart.Data<>(etiquetaSemana(p), v));
        }

        graficaLineas.getData().setAll(seriePct);
        aplicarEstiloLineaDespuesDeRender();
    }

    /**
     * Estiliza la línea ({@code #93588F}) y los puntos ({@code #7A4578}) del {@link LineChart} vía {@code lookup}.
     */
    private void aplicarEstiloLineaDespuesDeRender() {
        Platform.runLater(() -> {
            for (Node n : graficaLineas.lookupAll(".default-color0.chart-series-line")) {
                n.setStyle(ESTILO_LINEA);
            }
            for (Node n : graficaLineas.lookupAll(".default-color0.chart-line-symbol")) {
                n.setStyle(ESTILO_SIMBOLO);
            }
        });
    }

    /**
     * Pinta el calendario entre la fecha de inicio del hábito (si es más reciente que hace 3 meses)
     * o desde hace tres meses, y hoy. Cada día del rango aparece en la rejilla; los cumplidos se resaltan.
     *
     * @param habito hábito del que se leen días cumplidos
     */
    private void actualizarCalendario(Habito habito) {
        LocalDate fechaInicio = habito.getFechaInicio().isBefore(LocalDate.now().minusMonths(3))
            ? LocalDate.now().minusMonths(3)
            : habito.getFechaInicio();
        // Retroceder al lunes de la semana de fechaInicio
        fechaInicio = fechaInicio.with(DayOfWeek.MONDAY);
        LocalDate fechaFin = LocalDate.now();
        Set<LocalDate> cumplidos = new HashSet<>(registroHabitoDAO.obtenerDiasCumplidos(habito.getIdHabito()));

        gridCalendario.getChildren().clear();
        gridCalendario.getColumnConstraints().clear();
        for (int i = 0; i < 7; i++) {
            ColumnConstraints cc = new ColumnConstraints(34);
            cc.setHalignment(HPos.CENTER);
            gridCalendario.getColumnConstraints().add(cc);
        }

        // Cabecera días semana
        String[] dias = {"L","M","X","J","V","S","D"};
        for (int i = 0; i < 7; i++) {
            Label lDia = new Label(dias[i]);
            lDia.getStyleClass().add("cabecera-dia-semana");
            gridCalendario.add(lDia, i, 0);
        }

        int fila = 1;
        int mesActual = -1;
        LocalDate fecha = fechaInicio;

        while (!fecha.isAfter(fechaFin)) {
            // Cabecera de mes cuando cambia
            if (fecha.getMonthValue() != mesActual && mesActual != -1) {
                fila++; // fila extra de separación antes del nuevo mes
            }
            if (fecha.getMonthValue() != mesActual) {
                mesActual = fecha.getMonthValue();
                Label lMes = new Label(fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es")) 
                                      + " " + fecha.getYear());
                lMes.getStyleClass().add("cabecera-mes");
                GridPane.setColumnSpan(lMes, 7);
                gridCalendario.add(lMes, 0, fila);
                fila++;
            }
            int col = fecha.getDayOfWeek().getValue() - 1; // L=0, D=6
            Label lDiaNum = new Label(String.valueOf(fecha.getDayOfMonth()));
            lDiaNum.setMinSize(28, 28);
            lDiaNum.setMaxSize(28, 28);
            lDiaNum.setAlignment(Pos.CENTER);
            if (cumplidos.contains(fecha)) {
                lDiaNum.getStyleClass().add("dia-cumplido");
            } else if (!fecha.isAfter(LocalDate.now())) {
                lDiaNum.getStyleClass().add("dia-no-cumplido");
            } else {
                lDiaNum.getStyleClass().add("dia-fuera-rango");
            }
            gridCalendario.add(lDiaNum, col, fila);
            // Avanzar al lunes siguiente cuando llegamos al domingo
            if (fecha.getDayOfWeek() == DayOfWeek.SUNDAY) fila++;
            fecha = fecha.plusDays(1);
        }
    }

    /**
     * Construye la rejilla: fila de cabecera L–D, filas de nombre de mes al cambiar el mes,
     * y una celda por cada día del rango (columna = día de la semana ISO, fila incremental al cambiar de semana).
     *
     * @param fechaInicio   primer día inclusivo
     * @param fechaFin      último día inclusivo
     * @param diasCumplidos conjunto de fechas cumplidas (el resto del rango se muestra como no cumplido)
     */
    private void construirGridCalendario(LocalDate fechaInicio, LocalDate fechaFin, Set<LocalDate> diasCumplidos) {
        gridCalendario.getChildren().clear();
        String[] cabDias = {"L", "M", "X", "J", "V", "S", "D"};
        for (int c = 0; c < 7; c++) {
            Label h = new Label(cabDias[c]);
            h.getStyleClass().add("cabecera-dia-semana");
            gridCalendario.add(h, c, 0);
        }

        int fila = 1;
        YearMonth ultimoMesConCabecera = null;
        int columnaAnterior = -1;

        LocalDate fecha = fechaInicio;
        while (!fecha.isAfter(fechaFin)) {
            YearMonth ymActual = YearMonth.from(fecha);
            if (ultimoMesConCabecera == null || !ymActual.equals(ultimoMesConCabecera)) {
                String nombreMes = fecha.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                Label cabMes = new Label(capitalizar(nombreMes) + " " + fecha.getYear());
                cabMes.getStyleClass().add("cabecera-mes");
                GridPane.setColumnSpan(cabMes, 7);
                gridCalendario.add(cabMes, 0, fila);
                fila++;
                ultimoMesConCabecera = ymActual;
                columnaAnterior = -1;
            }

            int columna = fecha.getDayOfWeek().getValue() - 1;
            if (columnaAnterior != -1 && columna <= columnaAnterior) {
                fila++;
            }

            Label cel = new Label(String.valueOf(fecha.getDayOfMonth()));
            cel.setMinSize(28, 28);
            cel.setPrefSize(28, 28);
            cel.setMaxSize(28, 28);
            cel.setAlignment(javafx.geometry.Pos.CENTER);
            if (diasCumplidos.contains(fecha)) {
                cel.getStyleClass().add("dia-cumplido");
            } else {
                cel.getStyleClass().add("dia-no-cumplido");
            }
            gridCalendario.add(cel, columna, fila);

            columnaAnterior = columna;
            fecha = fecha.plusDays(1);
        }
    }

    /**
     * Pone en mayúscula la primera letra de una cadena.
     *
     * @param s texto
     * @return texto con primera letra en mayúscula
     */
    private String capitalizar(String s) {
        if (s == null || s.isEmpty()) {
            return s;
        }
        return s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }

    /**
     * Activa el modo global: oculta el combo, agrega métricas y gráficos de todos los hábitos activos
     * y un calendario con unión de días cumplidos.
     */
    private void mostrarVistaGlobal() {
        vistaGlobal = true;
        comboHabito.setVisible(false);
        comboHabito.setManaged(false);

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) {
            return;
        }

        if (habitosActivos.isEmpty()) {
            limpiarMetricasVacias();
            mostrarGraficasSinDatos();
            gridCalendario.getChildren().clear();
            return;
        }

        actualizarMetricasGlobales(usuario.getIdUsuario());
        actualizarGraficaBarrasGlobal();
        actualizarGraficaLineasGlobal();
        actualizarCalendarioGlobal();
    }

    /**
     * Activa el modo por hábito: muestra el combo y recarga el hábito seleccionado.
     */
    private void mostrarVistaHabito() {
        vistaGlobal = false;
        comboHabito.setVisible(true);
        comboHabito.setManaged(true);
        Habito h = comboHabito.getValue();
        if (h != null) {
            cargarDatosHabito(h);
        }
    }

    /**
     * Rellena métricas globales usando {@code v_resumen_usuario} y el máximo de rachas entre hábitos activos.
     *
     * @param idUsuario identificador del usuario
     */
    private void actualizarMetricasGlobales(int idUsuario) {
        int maxRachaActual = 0;
        int maxMejorRacha = 0;
        for (Habito h : habitosActivos) {
            maxRachaActual = Math.max(maxRachaActual, rachaService.calcularRachaActual(h.getIdHabito()));
            maxMejorRacha = Math.max(maxMejorRacha, calcularMejorRacha(h.getIdHabito()));
        }
        labelRachaActual.setText(formatearDias(maxRachaActual));
        labelMejorRacha.setText(formatearDias(maxMejorRacha));

        ResumenUsuario resumen = estadisticaDAO.obtenerResumenUsuario(idUsuario);
        if (resumen != null) {
            labelTotalCumplimientos.setText(String.valueOf(resumen.getTotalCumplimientos()));
            double tasa = resumen.getTasaExitoGlobal();
            if (tasa >= 0 && tasa <= 1.0) {
                tasa = tasa * 100.0;
            }
            labelTasaExito.setText(String.format("%.0f%%", tasa));
        } else {
            labelTotalCumplimientos.setText("0");
            labelTasaExito.setText("0%");
        }
    }

    /**
     * Agrega por semana ISO los completados y objetivos de todos los hábitos activos y actualiza el gráfico de barras.
     */
    private void actualizarGraficaBarrasGlobal() {
        Map<String, int[]> porSemana = new HashMap<>();
        for (Habito h : habitosActivos) {
            for (ProgresoSemanal p : estadisticaDAO.obtenerProgresoSemanal(h.getIdHabito())) {
                String key = p.getAnioSemana() + "-" + p.getNumSemana();
                int[] ac = porSemana.computeIfAbsent(key, k -> new int[2]);
                ac[0] += p.getVecesCompletado();
                ac[1] += p.getObjetivoVeces();
            }
        }
        List<String> clavesOrdenadas = new ArrayList<>(porSemana.keySet());
        clavesOrdenadas.sort(Comparator.comparing(EstadisticasController::parsearClaveSemana));
        if (clavesOrdenadas.size() > 8) {
            clavesOrdenadas = new ArrayList<>(clavesOrdenadas.subList(clavesOrdenadas.size() - 8, clavesOrdenadas.size()));
        }

        if (clavesOrdenadas.isEmpty()) {
            graficaBarras.getData().clear();
            mostrarSinDatosBarras(true);
            return;
        }
        mostrarSinDatosBarras(false);

        XYChart.Series<String, Number> serieCompletado = new XYChart.Series<>();
        serieCompletado.setName("Completado");
        XYChart.Series<String, Number> serieObjetivo = new XYChart.Series<>();
        serieObjetivo.setName("Objetivo");

        for (String key : clavesOrdenadas) {
            int[] v = porSemana.get(key);
            String etiqueta = etiquetaSemanaDesdeClave(key);
            serieCompletado.getData().add(new XYChart.Data<>(etiqueta, v[0]));
            serieObjetivo.getData().add(new XYChart.Data<>(etiqueta, v[1]));
        }

        graficaBarras.getData().setAll(serieCompletado, serieObjetivo);
        aplicarColoresBarrasDespuesDeRender();
    }

    /**
     * Parsea una clave {@code año-númeroSemana} para ordenación cronológica.
     *
     * @param key clave compuesta
     * @return valor comparable (año * 100 + semana)
     */
    private static int parsearClaveSemana(String key) {
        String[] p = key.split("-");
        if (p.length != 2) {
            return 0;
        }
        try {
            return Integer.parseInt(p[0]) * 100 + Integer.parseInt(p[1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Genera la etiqueta del eje X a partir de la clave año-semana.
     *
     * @param key clave {@code anio-numSemana}
     * @return texto «Sem N»
     */
    private String etiquetaSemanaDesdeClave(String key) {
        String[] p = key.split("-");
        if (p.length == 2) {
            return "Sem " + p[1];
        }
        return key;
    }

    /**
     * Construye el gráfico de líneas global con el porcentaje agregado por semana.
     */
    private void actualizarGraficaLineasGlobal() {
        Map<String, int[]> porSemana = new HashMap<>();
        for (Habito h : habitosActivos) {
            for (ProgresoSemanal p : estadisticaDAO.obtenerProgresoSemanal(h.getIdHabito())) {
                String key = p.getAnioSemana() + "-" + p.getNumSemana();
                int[] ac = porSemana.computeIfAbsent(key, k -> new int[2]);
                ac[0] += p.getVecesCompletado();
                ac[1] += p.getObjetivoVeces();
            }
        }
        List<String> clavesOrdenadas = new ArrayList<>(porSemana.keySet());
        clavesOrdenadas.sort(Comparator.comparing(EstadisticasController::parsearClaveSemana));
        if (clavesOrdenadas.size() > 8) {
            clavesOrdenadas = new ArrayList<>(clavesOrdenadas.subList(clavesOrdenadas.size() - 8, clavesOrdenadas.size()));
        }

        if (clavesOrdenadas.isEmpty()) {
            graficaLineas.getData().clear();
            mostrarSinDatosLineas(true);
            return;
        }
        mostrarSinDatosLineas(false);

        XYChart.Series<String, Number> seriePct = new XYChart.Series<>();
        seriePct.setName("% Cumplimiento");

        for (String key : clavesOrdenadas) {
            int[] v = porSemana.get(key);
            double pct = v[1] > 0 ? Math.min(100.0, 100.0 * v[0] / v[1]) : 0;
            seriePct.getData().add(new XYChart.Data<>(etiquetaSemanaDesdeClave(key), pct));
        }

        graficaLineas.getData().setAll(seriePct);
        aplicarEstiloLineaDespuesDeRender();
    }

    /**
     * Calendario global: unión de días cumplidos de todos los hábitos activos en el rango de tres meses.
     */
    private void actualizarCalendarioGlobal() {
        LocalDate hoy = LocalDate.now();
        LocalDate fechaInicio = hoy.minusMonths(3);
        LocalDate fechaFin = hoy;
        Set<LocalDate> union = new HashSet<>();
        for (Habito h : habitosActivos) {
            union.addAll(registroHabitoDAO.obtenerDiasCumplidos(h.getIdHabito()));
        }
        construirGridCalendario(fechaInicio, fechaFin, union);
    }

    /**
     * Deja las métricas en guiones cuando no hay datos de usuario o hábitos.
     */
    private void limpiarMetricasVacias() {
        labelRachaActual.setText("—");
        labelMejorRacha.setText("—");
        labelTotalCumplimientos.setText("—");
        labelTasaExito.setText("—");
    }

    /**
     * Muestra u oculta el mensaje de ausencia de datos sobre el gráfico de barras.
     *
     * @param sinDatos {@code true} para mostrar el aviso
     */
    private void mostrarSinDatosBarras(boolean sinDatos) {
        labelSinDatosBarras.setVisible(sinDatos);
        labelSinDatosBarras.setManaged(sinDatos);
        graficaBarras.setVisible(!sinDatos);
        graficaBarras.setManaged(!sinDatos);
    }

    /**
     * Muestra u oculta el mensaje de ausencia de datos sobre el gráfico de líneas.
     *
     * @param sinDatos {@code true} para mostrar el aviso
     */
    private void mostrarSinDatosLineas(boolean sinDatos) {
        labelSinDatosLineas.setVisible(sinDatos);
        labelSinDatosLineas.setManaged(sinDatos);
        graficaLineas.setVisible(!sinDatos);
        graficaLineas.setManaged(!sinDatos);
    }

    /**
     * Pone ambas gráficas en estado vacío con mensaje informativo.
     */
    private void mostrarGraficasSinDatos() {
        graficaBarras.getData().clear();
        graficaLineas.getData().clear();
        mostrarSinDatosBarras(true);
        mostrarSinDatosLineas(true);
    }
}
