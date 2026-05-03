package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.HabitoDAO;
import com.mycompany.tracklify.dao.NotificacionDAO;
import com.mycompany.tracklify.dao.PeriodoDAO;
import com.mycompany.tracklify.models.Habito;
import com.mycompany.tracklify.models.Periodo;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.InvalidationListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.util.StringConverter;

/**
 * Controlador del asistente de cuatro pasos para crear o editar un hábito en Tracklify.
 *
 * <p>Gestiona la carga de periodos desde la base de datos, el modo duración indefinida,
 * la validación, la persistencia mediante {@link HabitoDAO} y el primer recordatorio
 * vía {@link NotificacionDAO}.</p>
 *
 * @author Tracklify
 */
public class CrearHabitoController implements Initializable {

    private static final List<String> NOMBRES_DURACION = Arrays.asList("HORA", "DIA", "SEMANA", "MES", "ANO");
    private static final List<String> NOMBRES_NOTIF = Arrays.asList("HORA", "DIA", "SEMANA", "MES");
    private static final List<String> NOMBRES_OBJETIVO = Arrays.asList("DIA", "SEMANA", "MES", "ANO");

    private static final Map<String, String> ETIQUETA_ES = new LinkedHashMap<>();

    private static final Map<String, String> ETIQUETA_SINGULAR = new LinkedHashMap<>();

    static {
        ETIQUETA_ES.put("HORA", "Horas");
        ETIQUETA_ES.put("DIA", "Días");
        ETIQUETA_ES.put("SEMANA", "Semanas");
        ETIQUETA_ES.put("MES", "Meses");
        ETIQUETA_ES.put("ANO", "Años");
        ETIQUETA_ES.put("INDEFINIDO", "Indefinido");
        ETIQUETA_SINGULAR.put("HORA", "Hora");
        ETIQUETA_SINGULAR.put("DIA", "Día");
        ETIQUETA_SINGULAR.put("SEMANA", "Semana");
        ETIQUETA_SINGULAR.put("MES", "Mes");
        ETIQUETA_SINGULAR.put("ANO", "Año");
    }

    @FXML
    private Button btnVolver;

    @FXML
    private TextField campoNombreHabito;

    @FXML
    private Spinner<Integer> spinnerDuracionValor;

    @FXML
    private ComboBox<Periodo> comboDuracionPeriodo;

    @FXML
    private Button btnIndefinido;

    @FXML
    private Label labelEjemploDuracion;

    @FXML
    private Spinner<Integer> spinnerNotifValor;

    @FXML
    private ComboBox<Periodo> comboNotifPeriodo;

    @FXML
    private Label labelEjemploNotif;

    @FXML
    private Spinner<Integer> spinnerObjetivoVeces;

    @FXML
    private ComboBox<Periodo> comboObjetivoPeriodo;

    @FXML
    private Label labelEjemploObjetivo;

    @FXML
    private TextFlow textFlowResumen;

    @FXML
    private Button btnCrearHabito;

    @FXML
    private Label labelError;

    /** Indica si se está editando un hábito existente. */
    private boolean modoEdicion = false;

    /** Hábito en edición; {@code null} en modo alta. */
    private Habito habitoAEditar = null;

    /** Si la duración es indefinida (sin fecha de fin concreta por periodo). */
    private boolean duracionIndefinida = false;

    /** Acceso a persistencia de hábitos. */
    private final HabitoDAO habitoDAO = new HabitoDAO();

    /** Acceso a periodos de tiempo. */
    private final PeriodoDAO periodoDAO = new PeriodoDAO();

    /** Acceso a notificaciones. */
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();

    /** Periodos cargados desde la base de datos. */
    private List<Periodo> periodos = new ArrayList<>();

    /** Periodo especial {@code INDEFINIDO} si existe en la tabla. */
    private Periodo periodoIndefinido;

    /** Evita precargar antes de que los controles FXML existan. */
    private boolean vistaLista = false;

    /**
     * Inicializa combos, spinners, conversores y escuchas para actualizar el resumen en vivo.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle recursos; no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        periodos = periodoDAO.obtenerTodos();
        periodoIndefinido = periodos.stream()
            .filter(p -> "INDEFINIDO".equalsIgnoreCase(p.getNombre()))
            .findFirst()
            .orElse(null);

        configurarCombo(comboDuracionPeriodo, filtrarPorOrden(NOMBRES_DURACION));
        configurarCombo(comboNotifPeriodo, filtrarPorOrden(NOMBRES_NOTIF));
        configurarCombo(comboObjetivoPeriodo, filtrarPorOrden(NOMBRES_OBJETIVO));

        configurarSpinnerConCommit(spinnerDuracionValor, 1, 999, 3);
        configurarSpinnerConCommit(spinnerNotifValor, 1, 99, 1);
        configurarSpinnerConCommit(spinnerObjetivoVeces, 1, 99, 4);

        seleccionarComboPorNombreBd(comboDuracionPeriodo, "MES");
        seleccionarComboPorNombreBd(comboNotifPeriodo, "DIA");
        seleccionarComboPorNombreBd(comboObjetivoPeriodo, "SEMANA");

        InvalidationListener alCambiar = obs -> actualizarResumen();
        campoNombreHabito.textProperty().addListener(alCambiar);
        spinnerDuracionValor.getEditor().textProperty().addListener(alCambiar);
        comboDuracionPeriodo.valueProperty().addListener(alCambiar);
        spinnerNotifValor.getEditor().textProperty().addListener(alCambiar);
        comboNotifPeriodo.valueProperty().addListener(alCambiar);
        spinnerObjetivoVeces.getEditor().textProperty().addListener(alCambiar);
        comboObjetivoPeriodo.valueProperty().addListener(alCambiar);

        vistaLista = true;
        actualizarResumen();
    }

    /**
     * Configura un {@link Spinner} entero editable: factoría, escucha de valor para el resumen
     * y confirmación del editor al pulsar Enter o al perder el foco (incluye cambios por flechas).
     *
     * @param spinner control
     * @param min     mínimo inclusive
     * @param max     máximo inclusive
     * @param inicial valor inicial
     */
    private void configurarSpinnerConCommit(Spinner<Integer> spinner, int min, int max, int inicial) {
        SpinnerValueFactory.IntegerSpinnerValueFactory factory =
            new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, inicial);
        spinner.setValueFactory(factory);
        spinner.setEditable(true);
        spinner.valueProperty().addListener((obs, oldVal, newVal) -> actualizarResumen());
        spinner.getEditor().setOnAction(e -> spinner.increment(0));
        spinner.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                spinner.increment(0);
            }
        });
    }

    /**
     * Activa el modo edición y asigna el hábito a modificar.
     *
     * <p>Si la vista ya está cargada, rellena de inmediato los campos.</p>
     *
     * @param habito hábito a editar, o {@code null} para cancelar el modo edición
     */
    public void setHabitoAEditar(Habito habito) {
        if (habito == null) {
            modoEdicion = false;
            habitoAEditar = null;
            return;
        }
        modoEdicion = true;
        habitoAEditar = habito;
        if (vistaLista) {
            precargarDatos();
        }
    }

    /**
     * Rellena el formulario con los datos de {@link #habitoAEditar}, activa el modo indefinido si aplica
     * y cambia la etiqueta del botón principal a «Guardar cambios».
     */
    public void precargarDatos() {
        if (habitoAEditar == null || !vistaLista) {
            return;
        }

        campoNombreHabito.setText(habitoAEditar.getNombreHabito() != null ? habitoAEditar.getNombreHabito() : "");

        Integer idIndef = periodoIndefinido != null ? periodoIndefinido.getIdPeriodo() : null;
        boolean indef = idIndef != null && idIndef.equals(habitoAEditar.getDuracionPeriodoId());
        duracionIndefinida = indef;
        aplicarEstiloIndefinido();

        if (indef) {
            if (habitoAEditar.getDuracionValor() != null) {
                spinnerDuracionValor.getValueFactory().setValue(habitoAEditar.getDuracionValor());
            }
        } else {
            if (habitoAEditar.getDuracionValor() != null) {
                spinnerDuracionValor.getValueFactory().setValue(habitoAEditar.getDuracionValor());
            }
            seleccionarComboPorId(comboDuracionPeriodo, habitoAEditar.getDuracionPeriodoId());
        }

        spinnerNotifValor.getValueFactory().setValue(Math.max(1, habitoAEditar.getNotifFrecuenciaValor()));
        seleccionarComboPorId(comboNotifPeriodo, habitoAEditar.getNotifFrecuenciaId());

        spinnerObjetivoVeces.getValueFactory().setValue(Math.max(1, habitoAEditar.getObjetivoVeces()));
        seleccionarComboPorId(comboObjetivoPeriodo, habitoAEditar.getObjetivoPeriodoId());

        btnCrearHabito.setText("Guardar cambios");
        ocultarError();
        actualizarResumen();
    }

    /**
     * Alterna la duración indefinida y actualiza el estado de los controles de duración.
     *
     * @param event evento del botón
     */
    @FXML
    public void btnIndefinidoClick(ActionEvent event) {
        duracionIndefinida = !duracionIndefinida;
        aplicarEstiloIndefinido();
        actualizarResumen();
    }

    /**
     * Valida los datos, inserta o actualiza el hábito y, si es un alta, programa el primer recordatorio.
     *
     * @param event evento del botón principal
     */
    @FXML
    public void crearHabito(ActionEvent event) {
        ocultarError();

        String nombre = campoNombreHabito.getText() != null ? campoNombreHabito.getText().trim() : "";
        if (nombre.isEmpty()) {
            mostrarError("El nombre del hábito no puede estar vacío.");
            return;
        }

        if (duracionIndefinida && periodoIndefinido == null) {
            mostrarError("No hay periodo INDEFINIDO en la base de datos; desactiva «Indefinido» o crea ese periodo.");
            return;
        }
        if (!duracionIndefinida && comboDuracionPeriodo.getValue() == null) {
            mostrarError("Selecciona un periodo de duración.");
            return;
        }
        if (comboNotifPeriodo.getValue() == null) {
            mostrarError("Selecciona la frecuencia de avisos.");
            return;
        }
        if (comboObjetivoPeriodo.getValue() == null) {
            mostrarError("Selecciona el periodo del objetivo.");
            return;
        }

        int idUsuario = SessionManager.getInstancia().getUsuarioActual().getIdUsuario();
        Integer excluir = modoEdicion && habitoAEditar != null ? habitoAEditar.getIdHabito() : null;
        if (habitoDAO.existeOtroHabitoMismoNombre(idUsuario, nombre, excluir)) {
            mostrarError("Ya tienes un hábito con ese nombre");
            return;
        }

        int durValor = leerSpinnerSeguro(spinnerDuracionValor, 1);
        int notifValor = leerSpinnerSeguro(spinnerNotifValor, 1);
        int objVeces = leerSpinnerSeguro(spinnerObjetivoVeces, 1);

        LocalDate hoy = LocalDate.now();
        Habito h = new Habito();
        if (modoEdicion && habitoAEditar != null) {
            h.setIdHabito(habitoAEditar.getIdHabito());
            h.setIdCategoria(habitoAEditar.getIdCategoria());
            h.setFechaInicio(habitoAEditar.getFechaInicio() != null ? habitoAEditar.getFechaInicio() : hoy);
        } else {
            h.setFechaInicio(hoy);
        }

        h.setIdUsuario(idUsuario);
        h.setNombreHabito(nombre);
        h.setDescripcionHabito(null);
        if (modoEdicion && habitoAEditar != null && habitoAEditar.getEstado() != null) {
            h.setEstado(habitoAEditar.getEstado());
        } else {
            h.setEstado("ACTIVO");
        }

        if (duracionIndefinida) {
            h.setDuracionValor(null);
            h.setDuracionPeriodoId(periodoIndefinido != null ? periodoIndefinido.getIdPeriodo() : null);
            h.setFechaFin(null);
        } else {
            Periodo pd = comboDuracionPeriodo.getValue();
            h.setDuracionValor(durValor);
            h.setDuracionPeriodoId(pd != null ? pd.getIdPeriodo() : null);
            h.setFechaFin(pd != null ? calcularFechaFin(hoy, durValor, pd) : null);
        }

        Periodo pn = comboNotifPeriodo.getValue();
        h.setNotifFrecuenciaValor(notifValor);
        h.setNotifFrecuenciaId(pn != null ? pn.getIdPeriodo() : null);

        Periodo po = comboObjetivoPeriodo.getValue();
        h.setObjetivoVeces(objVeces);
        h.setObjetivoPeriodoId(po != null ? po.getIdPeriodo() : null);

        if (modoEdicion && habitoAEditar != null) {
            if (!habitoDAO.actualizar(h)) {
                mostrarError("No se pudieron guardar los cambios.");
                return;
            }
        } else {
            int idNuevo = habitoDAO.insertar(h);
            if (idNuevo <= 0) {
                mostrarError("No se pudo crear el hábito.");
                return;
            }
            LocalDateTime primera = LocalDateTime.now().plus(obtenerDuracionNotificacion(notifValor, pn));
            String msg = "Recordatorio: " + nombre;
            notificacionDAO.insertarRecordatorioHabito(idUsuario, idNuevo, msg, primera);
        }
        try {
            irAMain(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Vuelve al panel principal sin guardar cambios.
     *
     * @param event evento de navegación
     */
    @FXML
    public void volver(ActionEvent event) {
        try {
            irAMain(event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reconstruye el resumen en el {@link TextFlow} y actualiza las etiquetas de ejemplo de duración,
     * notificación y objetivo.
     */
    public void actualizarResumen() {
        if (textFlowResumen == null) {
            return;
        }

        String nombre = campoNombreHabito.getText() != null ? campoNombreHabito.getText().trim() : "";
        if (nombre.isEmpty()) {
            nombre = "…";
        }

        int dv = leerSpinnerSeguro(spinnerDuracionValor, 3);
        Periodo pd = comboDuracionPeriodo.getValue();
        int nv = leerSpinnerSeguro(spinnerNotifValor, 1);
        Periodo pn = comboNotifPeriodo.getValue();
        int ov = leerSpinnerSeguro(spinnerObjetivoVeces, 4);
        Periodo po = comboObjetivoPeriodo.getValue();

        textFlowResumen.getChildren().clear();
        textFlowResumen.getChildren().add(new Text("Quiero "));
        textFlowResumen.getChildren().add(crearChip(nombre));
        textFlowResumen.getChildren().add(new Text(" durante "));
        if (duracionIndefinida) {
            textFlowResumen.getChildren().add(crearChip("tiempo indefinido"));
        } else {
            textFlowResumen.getChildren().add(crearChip(dv + " " + etiquetaPlural(pd, dv)));
        }
        textFlowResumen.getChildren().add(new Text(" y quiero que se me avise cada "));
        textFlowResumen.getChildren().add(crearChip(nv + " " + etiquetaPlural(pn, nv)));
        textFlowResumen.getChildren().add(new Text(" para completar mi objetivo "));
        textFlowResumen.getChildren().add(crearChip(ov + " veces"));
        textFlowResumen.getChildren().add(new Text(" a la "));
        textFlowResumen.getChildren().add(crearChip(po != null ? etiquetaEspanol(po.getNombre()) : "…"));
        textFlowResumen.getChildren().add(new Text("."));

        labelEjemploDuracion.setText(textoEjemploDuracion(dv, pd));
        labelEjemploNotif.setText(textoEjemploNotif(nv, pn));
        labelEjemploObjetivo.setText(textoEjemploObjetivo(ov, po));
    }

    /**
     * Sustituye la escena actual por el dashboard principal.
     *
     * @param event evento con origen en un nodo de la escena actual
     * @throws Exception si falla la carga de {@code main_view.fxml}
     */
    private void irAMain(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/main_view.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    /**
     * Habilita o deshabilita los controles de duración y alterna las clases CSS del botón indefinido.
     */
    private void aplicarEstiloIndefinido() {
        spinnerDuracionValor.setDisable(duracionIndefinida);
        comboDuracionPeriodo.setDisable(duracionIndefinida);
        btnIndefinido.getStyleClass().removeAll("btn-indefinido-activo");
        if (duracionIndefinida) {
            btnIndefinido.getStyleClass().add("btn-indefinido-activo");
        }
    }

    /**
     * Crea una etiqueta con estilo de chip para insertar en el {@link TextFlow} del resumen.
     *
     * @param texto contenido visible del chip
     * @return etiqueta estilizada
     */
    private Label crearChip(String texto) {
        Label l = new Label(texto);
        l.getStyleClass().add("chip-resumen");
        return l;
    }

    /**
     * Muestra un mensaje de error bajo el formulario.
     *
     * @param msg texto a mostrar
     */
    private void mostrarError(String msg) {
        labelError.setText(msg);
        labelError.setVisible(true);
        labelError.setManaged(true);
    }

    /** Oculta y vacía el mensaje de error. */
    private void ocultarError() {
        labelError.setText("");
        labelError.setVisible(false);
        labelError.setManaged(false);
    }

    /**
     * Obtiene un entero válido de un {@link Spinner}, confirmando el editor o parseando el texto.
     *
     * @param sp      spinner numérico
     * @param defecto valor si no hay entrada válida
     * @return entero dentro del rango gestionado por la factoría o el defecto
     */
    private int leerSpinnerSeguro(Spinner<Integer> sp, int defecto) {
        try {
            sp.commitValue();
        } catch (Exception ignored) {
            // valor no válido en el editor; se intentará parsear abajo
        }
        Integer v = sp.getValue();
        if (v != null) {
            return v;
        }
        try {
            String t = sp.getEditor().getText();
            if (t != null && !t.isEmpty()) {
                return Integer.parseInt(t.trim());
            }
        } catch (NumberFormatException ignored) {
            // ignorado
        }
        return defecto;
    }

    /**
     * Filtra y ordena los periodos cargados según la lista de nombres en base de datos.
     *
     * @param ordenNombresBd secuencia deseada (p. ej. HORA, DIA, …)
     * @return sublista ordenada
     */
    private List<Periodo> filtrarPorOrden(List<String> ordenNombresBd) {
        Map<String, Periodo> porNombre = periodos.stream()
            .collect(Collectors.toMap(p -> p.getNombre().toUpperCase(), p -> p, (a, b) -> a));
        List<Periodo> r = new ArrayList<>();
        for (String n : ordenNombresBd) {
            Periodo p = porNombre.get(n);
            if (p != null) {
                r.add(p);
            }
        }
        return r;
    }

    /**
     * Asigna ítems y un {@link StringConverter} que muestra las etiquetas en español.
     *
     * @param combo  destino
     * @param items  periodos permitidos
     */
    private void configurarCombo(ComboBox<Periodo> combo, List<Periodo> items) {
        combo.getItems().setAll(items);
        combo.setConverter(new StringConverter<>() {
            @Override
            public String toString(Periodo p) {
                return p == null ? "" : etiquetaEspanol(p.getNombre());
            }

            @Override
            public Periodo fromString(String s) {
                return null;
            }
        });
    }

    /**
     * Selecciona en el combo el periodo cuyo nombre en BD coincide (sin distinguir mayúsculas).
     *
     * @param combo    combo destino
     * @param nombreBd nombre exacto en tabla {@code periodos}
     */
    private void seleccionarComboPorNombreBd(ComboBox<Periodo> combo, String nombreBd) {
        for (Periodo p : combo.getItems()) {
            if (nombreBd.equalsIgnoreCase(p.getNombre())) {
                combo.setValue(p);
                return;
            }
        }
        if (!combo.getItems().isEmpty()) {
            combo.setValue(combo.getItems().get(0));
        }
    }

    /**
     * Selecciona el periodo por {@code id_periodo} dentro de los ítems ya cargados en el combo.
     *
     * @param combo combo destino
     * @param id    identificador de periodo o {@code null} para no hacer nada
     */
    private void seleccionarComboPorId(ComboBox<Periodo> combo, Integer id) {
        if (id == null) {
            return;
        }
        for (Periodo p : combo.getItems()) {
            if (p.getIdPeriodo() == id) {
                combo.setValue(p);
                return;
            }
        }
    }

    /**
     * Devuelve la etiqueta en español para un nombre de periodo de base de datos.
     *
     * @param nombreBd valor de columna {@code nombre}
     * @return etiqueta para la interfaz
     */
    private static String etiquetaEspanol(String nombreBd) {
        if (nombreBd == null) {
            return "";
        }
        return ETIQUETA_ES.getOrDefault(nombreBd.toUpperCase(), nombreBd);
    }

    /**
     * Devuelve singular o plural en español según el valor numérico mostrado.
     *
     * @param p     periodo
     * @param valor cantidad mostrada junto al nombre del periodo
     * @return forma lingüística adecuada
     */
    private static String etiquetaPlural(Periodo p, int valor) {
        if (p == null) {
            return "…";
        }
        String n = p.getNombre().toUpperCase();
        if (valor == 1) {
            return ETIQUETA_SINGULAR.getOrDefault(n, etiquetaEspanol(p.getNombre()));
        }
        return etiquetaEspanol(p.getNombre());
    }

    /**
     * Calcula la fecha de fin del hábito sumando la duración al inicio según el periodo.
     *
     * @param inicio fecha de inicio
     * @param valor  magnitud
     * @param p      unidad ({@code HORA}, {@code DIA}, etc.)
     * @return fecha de fin estimada
     */
    private LocalDate calcularFechaFin(LocalDate inicio, int valor, Periodo p) {
        if (p == null) {
            return inicio;
        }
        String n = p.getNombre().toUpperCase();
        LocalDateTime t0 = inicio.atStartOfDay();
        switch (n) {
            case "HORA":
                return t0.plusHours(valor).toLocalDate();
            case "DIA":
                return inicio.plusDays(valor);
            case "SEMANA":
                return inicio.plusWeeks(valor);
            case "MES":
                return inicio.plusMonths(valor);
            case "ANO":
                return inicio.plusYears(valor);
            default:
                return inicio;
        }
    }

    /**
     * Traduce la frecuencia de notificación a una {@link java.time.Duration} aproximada para la primera cita.
     *
     * @param valor cantidad
     * @param p     periodo de frecuencia
     * @return duración hasta el primer recordatorio
     */
    private java.time.Duration obtenerDuracionNotificacion(int valor, Periodo p) {
        if (p == null) {
            return java.time.Duration.ofDays(1);
        }
        String n = p.getNombre().toUpperCase();
        switch (n) {
            case "HORA":
                return java.time.Duration.ofHours(valor);
            case "DIA":
                return java.time.Duration.ofDays(valor);
            case "SEMANA":
                return java.time.Duration.ofDays(7L * valor);
            case "MES":
                return java.time.Duration.ofDays(30L * valor);
            case "ANO":
                return java.time.Duration.ofDays(365L * valor);
            default:
                return java.time.Duration.ofDays(1);
        }
    }

    /**
     * Genera el texto de ayuda bajo el paso de duración.
     *
     * @param v cantidad
     * @param p periodo seleccionado
     * @return descripción legible
     */
    private String textoEjemploDuracion(int v, Periodo p) {
        if (duracionIndefinida) {
            return "Sin fecha de fin: el hábito permanece activo de forma indefinida.";
        }
        if (p == null) {
            return "";
        }
        LocalDate fin = calcularFechaFin(LocalDate.now(), v, p);
        return "Ejemplo: desde hoy hasta el " + fin + " (" + v + " " + etiquetaPlural(p, v).toLowerCase() + ").";
    }

    /**
     * Genera el texto de ayuda bajo el paso de notificaciones.
     *
     * @param v cantidad
     * @param p periodo de aviso
     * @return descripción legible
     */
    private String textoEjemploNotif(int v, Periodo p) {
        if (p == null) {
            return "";
        }
        return "Ejemplo: te avisaremos cada " + v + " " + etiquetaPlural(p, v).toLowerCase() + ".";
    }

    /**
     * Genera el texto de ayuda bajo el paso de objetivo.
     *
     * @param v número de veces
     * @param p periodo del objetivo
     * @return descripción legible
     */
    private String textoEjemploObjetivo(int v, Periodo p) {
        if (p == null) {
            return "";
        }
        return "Ejemplo: objetivo de " + v + " veces por " + etiquetaEspanol(p.getNombre()).toLowerCase() + ".";
    }
}
