package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.EstadisticaDAO;
import com.mycompany.tracklify.dao.NotificacionDAO;
import com.mycompany.tracklify.dao.TareaDAO;
import com.mycompany.tracklify.models.Estadistica;
import com.mycompany.tracklify.models.Notificacion;
import com.mycompany.tracklify.models.Tarea;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.NotificacionScheduler;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Controlador del dashboard principal del usuario ({@code main_view.fxml}).
 *
 * <p>Además de gestionar hábitos y estadísticas, este controlador es el punto
 * de entrada del sistema de notificaciones: al inicializarse arranca el
 * {@link NotificacionScheduler} y al cerrar sesión lo detiene.</p>
 *
 * <p>Cuando el usuario guarda un nuevo hábito, este controlador calcula
 * automáticamente la primera fecha de notificación y la persiste en BD
 * mediante {@link NotificacionDAO}.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see NotificacionScheduler
 * @see NotificacionDAO
 * @see TareaDAO
 */
public class MainViewController implements Initializable {

    // ── Campos FXML del dashboard ──────────────────────────────────────────

    /** Etiqueta de saludo con el nombre del usuario. */
    @FXML private Label labelBienvenida;

    /** Contador de hábitos activos. */
    @FXML private Label labelTareasActivas;

    /** Porcentaje de completado del día. */
    @FXML private Label labelPorcentaje;

    /** Racha actual del usuario. */
    @FXML private Label labelRacha;

    /** Contenedor dinámico de filas de tareas. */
    @FXML private VBox contenedorTareas;

    /** Contenedor de la sección "Próximos recordatorios". */
    @FXML private VBox contenedorRecordatorios;

    // ── Campos FXML del formulario de nueva tarea ──────────────────────────

    /** Panel del formulario (oculto por defecto). */
    @FXML private VBox panelNuevaTarea;

    /** Campo de nombre de la nueva tarea. */
    @FXML private TextField campoNombreTarea;

    /** Campo de descripción de la nueva tarea. */
    @FXML private TextField campoDescripcionTarea;

    /** Selector de frecuencia (Diaria / Semanal / Mensual). */
    @FXML private ComboBox<String> comboFrecuencia;

    /** Spinner de hora de notificación (0-23). */
    @FXML private Spinner<Integer> spinnerHora;

    /** Spinner de minutos de notificación (0-59). */
    @FXML private Spinner<Integer> spinnerMinutos;

    /** Fila del selector de día (visible solo en Semanal/Mensual). */
    @FXML private HBox filaDia;

    /** Selector de día de la semana o del mes. */
    @FXML private ComboBox<String> comboDia;

    // ── DAOs ──────────────────────────────────────────────────────────────

    /** DAO para operaciones sobre hábitos/tareas. */
    private TareaDAO tareaDAO = new TareaDAO();

    /** DAO para operaciones sobre estadísticas. */
    private EstadisticaDAO estadisticaDAO = new EstadisticaDAO();

    /**
     * DAO para crear notificaciones al guardar un hábito y para
     * cargar los próximos recordatorios en el dashboard.
     */
    private NotificacionDAO notificacionDAO = new NotificacionDAO();

    // ── Inicialización ─────────────────────────────────────────────────────

    /**
     * Inicializa el dashboard al cargar la vista.
     *
     * <p>Además de cargar datos del usuario, arranca el
     * {@link NotificacionScheduler} para que empiece a comprobar
     * notificaciones pendientes cada 60 segundos.</p>
     *
     * @param url            URL del FXML
     * @param resourceBundle recursos de internacionalización
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();

        if (usuario != null) {
            labelBienvenida.setText("Bienvenido, " + usuario.getNombreUsuario() + " ✦");
            cargarTareas(usuario.getIdUsuario());
            cargarEstadisticas(usuario.getIdUsuario());

            // Arrancamos el scheduler de notificaciones para este usuario
            NotificacionScheduler.getInstancia().iniciar(usuario.getIdUsuario());
        }

        // Configuramos el formulario de nueva tarea
        configurarFormulario();

        // Cargamos los próximos recordatorios desde la BD
        cargarProximosRecordatorios();
    }

    /**
     * Configura los controles del formulario de nueva tarea:
     * opciones del ComboBox de frecuencia, valores por defecto de los
     * spinners y listener para mostrar/ocultar el selector de día.
     */
    private void configurarFormulario() {

        comboFrecuencia.getItems().addAll("Diaria", "Semanal", "Mensual");
        comboFrecuencia.setValue("Diaria");

        spinnerHora.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8)
        );
        spinnerMinutos.setValueFactory(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0)
        );

        filaDia.setVisible(false);
        filaDia.setManaged(false);

        comboFrecuencia.setOnAction(e ->
            actualizarSelectorDia(comboFrecuencia, comboDia, filaDia)
        );

        panelNuevaTarea.setVisible(false);
        panelNuevaTarea.setManaged(false);
    }

    // ── Gestión del formulario de nueva tarea ──────────────────────────────

    /**
     * Muestra u oculta el formulario de nueva tarea.
     *
     * @param event evento del botón "+ Crear nuevo hábito"
     */
    @FXML
    public void mostrarFormularioNuevaTarea(ActionEvent event) {
        boolean visible = panelNuevaTarea.isVisible();
        panelNuevaTarea.setVisible(!visible);
        panelNuevaTarea.setManaged(!visible);
        if (!visible) {
            campoNombreTarea.clear();
            campoDescripcionTarea.clear();
            comboFrecuencia.setValue("Diaria");
            spinnerHora.getValueFactory().setValue(8);
            spinnerMinutos.getValueFactory().setValue(0);
            filaDia.setVisible(false);
            filaDia.setManaged(false);
        }
    }

    /**
     * Guarda la nueva tarea en BD y crea automáticamente su primera notificación.
     *
     * <p>Tras validar el nombre, calcula la próxima fecha de disparo de la
     * notificación a partir de la frecuencia y hora configuradas, inserta
     * la tarea y la notificación en la BD, y recarga la lista visible.</p>
     *
     * @param event evento del botón "Guardar"
     */
    @FXML
    public void guardarNuevaTarea(ActionEvent event) {

        String nombre = campoNombreTarea.getText().trim();
        if (nombre.isEmpty()) {
            campoNombreTarea.setStyle("-fx-border-color: #C0392B; -fx-border-radius: 8;");
            return;
        }
        campoNombreTarea.setStyle("");

        String frecuencia = construirFrecuencia(
            comboFrecuencia.getValue(),
            comboDia.isVisible() ? comboDia.getValue() : null,
            spinnerHora.getValue(),
            spinnerMinutos.getValue()
        );

        // Construimos y persistimos la tarea
        Tarea nueva = new Tarea();
        nueva.setUsuarioId(SessionManager.getInstancia().getUsuarioActual().getIdUsuario());
        nueva.setNombreTarea(nombre);
        nueva.setDescripcionTarea(campoDescripcionTarea.getText().trim());
        nueva.setFrecuenciaTarea(frecuencia);

        boolean tareaGuardada = tareaDAO.insertar(nueva);

        if (tareaGuardada) {

            // Calculamos la primera fecha de notificación y la creamos en BD
            LocalDateTime fechaNotificacion = calcularProximaFecha(
                comboFrecuencia.getValue(),
                comboDia.isVisible() ? comboDia.getValue() : null,
                spinnerHora.getValue(),
                spinnerMinutos.getValue()
            );

            Notificacion notif = new Notificacion();
            notif.setUsuarioId(SessionManager.getInstancia().getUsuarioActual().getIdUsuario());
            notif.setMensajeNotificacion("¡Hora de tu hábito: " + nombre + "!");
            notif.setFechaProgramada(fechaNotificacion);
            notificacionDAO.insertar(notif);

            // Recargamos la vista
            cargarTareas(SessionManager.getInstancia().getUsuarioActual().getIdUsuario());
            cargarProximosRecordatorios();
            panelNuevaTarea.setVisible(false);
            panelNuevaTarea.setManaged(false);
        }
    }

    /**
     * Cancela la creación y oculta el formulario.
     *
     * @param event evento del botón "Cancelar"
     */
    @FXML
    public void cancelarNuevaTarea(ActionEvent event) {
        panelNuevaTarea.setVisible(false);
        panelNuevaTarea.setManaged(false);
    }

    // ── Carga de datos ─────────────────────────────────────────────────────

    /**
     * Carga las tareas del usuario y las muestra en el contenedor.
     *
     * @param idUsuario identificador del usuario
     */
    private void cargarTareas(int idUsuario) {
        List<Tarea> tareas = tareaDAO.obtenerPorUsuario(idUsuario);
        labelTareasActivas.setText(String.valueOf(tareas.size()));
        contenedorTareas.getChildren().clear();

        if (tareas.isEmpty()) {
            Label sinTareas = new Label("No tienes hábitos aún. ¡Pulsa + Crear nuevo hábito!");
            sinTareas.setStyle("-fx-text-fill: #C4AADB; -fx-font-size: 12px;");
            contenedorTareas.getChildren().add(sinTareas);
        } else {
            for (Tarea tarea : tareas) {
                contenedorTareas.getChildren().add(crearFilaTarea(tarea));
            }
        }
    }

    /**
     * Carga las próximas notificaciones pendientes del usuario y las muestra
     * en la sección "Próximos recordatorios" del dashboard.
     *
     * <p>Solo muestra las 5 más próximas para no sobrecargar la UI.</p>
     */
    private void cargarProximosRecordatorios() {

        if (contenedorRecordatorios == null) return;
        contenedorRecordatorios.getChildren().clear();

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) return;

        // Obtenemos todas las notificaciones pendientes del usuario
        List<Notificacion> todas = notificacionDAO.obtenerPorUsuario(usuario.getIdUsuario());

        // Filtramos solo las pendientes (no completadas) y tomamos las 5 primeras
        todas.stream()
            .filter(n -> !n.isEstadoNotificacion())
            .filter(n -> n.getFechaProgramada() != null)
            .limit(5)
            .forEach(n -> {
                HBox fila = new HBox(12);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setStyle("-fx-padding: 5 0 5 0;");

                // Formateamos la hora de la notificación
                String horaTexto = n.getFechaProgramada() != null
                    ? String.format("%02d:%02d",
                        n.getFechaProgramada().getHour(),
                        n.getFechaProgramada().getMinute())
                    : "--:--";

                Label hora = new Label(horaTexto);
                hora.setStyle("-fx-font-size: 11px; -fx-text-fill: #93588F; " +
                              "-fx-font-weight: bold; -fx-min-width: 48;");

                Label desc = new Label(n.getMensajeNotificacion());
                desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #7E6A8A;");
                desc.setWrapText(true);

                fila.getChildren().addAll(hora, desc);
                contenedorRecordatorios.getChildren().add(fila);
            });

        // Si no hay recordatorios pendientes mostramos un mensaje
        if (contenedorRecordatorios.getChildren().isEmpty()) {
            Label sinRecordatorios = new Label("No hay recordatorios pendientes.");
            sinRecordatorios.setStyle("-fx-text-fill: #C4AADB; -fx-font-size: 11px;");
            contenedorRecordatorios.getChildren().add(sinRecordatorios);
        }
    }

    /**
     * Carga las estadísticas más recientes del usuario.
     *
     * @param idUsuario identificador del usuario
     */
    private void cargarEstadisticas(int idUsuario) {
        Estadistica ultima = estadisticaDAO.obtenerUltima(idUsuario);
        if (ultima != null) {
            labelPorcentaje.setText(ultima.getPorcentajeCompletado() + "%");
            labelRacha.setText(ultima.getRachaActual() + " días");
        } else {
            labelPorcentaje.setText("0%");
            labelRacha.setText("0 días");
        }
    }

    // ── Construcción de filas de tarea ─────────────────────────────────────

    /**
     * Construye el nodo visual de una tarea con checkbox, nombre, badge y menú.
     *
     * @param tarea la {@link Tarea} a representar
     * @return {@link HBox} con todos los elementos de la fila
     */
    private HBox crearFilaTarea(Tarea tarea) {
        HBox fila = new HBox(10);
        fila.getStyleClass().add("habito-fila");
        fila.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkbox = new CheckBox();
        checkbox.getStyleClass().add("habito-checkbox");

        Label nombre = new Label(tarea.getNombreTarea());
        nombre.getStyleClass().add("habito-nombre");
        HBox.setHgrow(nombre, Priority.ALWAYS);

        Label badge = new Label(
            tarea.getFrecuenciaTarea() != null ? tarea.getFrecuenciaTarea() : "—"
        );
        badge.getStyleClass().add("badge-pendiente");

        Button btnMenu = new Button("···");
        btnMenu.getStyleClass().add("btn-dots");

        ContextMenu menu = new ContextMenu();

        MenuItem renombrar = new MenuItem("✎  Renombrar");
        renombrar.setOnAction(e -> mostrarDialogoRenombrar(tarea, nombre, badge));

        MenuItem borrar = new MenuItem("✕  Borrar hábito");
        borrar.setStyle("-fx-text-fill: #C0392B;");
        borrar.setOnAction(e -> {
            tareaDAO.eliminar(tarea.getIdTarea());
            contenedorTareas.getChildren().remove(fila);
            actualizarContadorTareas();
        });

        menu.getItems().addAll(renombrar, new SeparatorMenuItem(), borrar);
        btnMenu.setOnAction(e -> menu.show(btnMenu, javafx.geometry.Side.BOTTOM, 0, 0));

        // Al completar: tachar y desaparecer en 3 minutos
        checkbox.setOnAction(e -> {
            if (checkbox.isSelected()) {
                nombre.setStyle("-fx-strikethrough: true; -fx-text-fill: #C4AADB;");
                badge.setOpacity(0.4);
                btnMenu.setVisible(false);
                PauseTransition pausa = new PauseTransition(Duration.seconds(180));
                pausa.setOnFinished(ev -> {
                    contenedorTareas.getChildren().remove(fila);
                    actualizarContadorTareas();
                });
                pausa.play();
            } else {
                nombre.setStyle("");
                badge.setOpacity(1.0);
                btnMenu.setVisible(true);
            }
        });

        fila.getChildren().addAll(checkbox, nombre, badge, btnMenu);
        return fila;
    }

    // ── Utilidades ─────────────────────────────────────────────────────────

    /**
     * Calcula la próxima fecha de disparo de una notificación a partir
     * de la frecuencia, día y hora configurados por el usuario.
     *
     * <p>Lógica de cálculo:</p>
     * <ul>
     *   <li><strong>Diaria</strong>: hoy a la hora indicada. Si ya pasó, mañana.</li>
     *   <li><strong>Semanal</strong>: el próximo día de la semana indicado a esa hora.</li>
     *   <li><strong>Mensual</strong>: el próximo día del mes indicado a esa hora.</li>
     * </ul>
     *
     * @param frecuencia tipo de frecuencia ("Diaria", "Semanal", "Mensual")
     * @param dia        día de la semana o del mes, o {@code null} si es diaria
     * @param hora       hora de notificación (0-23)
     * @param minutos    minutos de notificación (0-59)
     * @return la próxima {@link LocalDateTime} en la que debe dispararse
     */
    private LocalDateTime calcularProximaFecha(String frecuencia, String dia,
                                                int hora, int minutos) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalTime horaNotif = LocalTime.of(hora, minutos);

        switch (frecuencia) {
            case "Diaria": {
                // Hoy a la hora indicada; si ya pasó, mañana
                LocalDateTime candidata = LocalDate.now().atTime(horaNotif);
                return candidata.isAfter(ahora) ? candidata : candidata.plusDays(1);
            }
            case "Semanal": {
                // Próximo día de la semana indicado
                java.time.DayOfWeek diaSemana = parsearDiaSemana(dia);
                LocalDate hoy = LocalDate.now();
                LocalDate proximoDia = hoy.with(
                    java.time.temporal.TemporalAdjusters.nextOrSame(diaSemana)
                );
                LocalDateTime candidata = proximoDia.atTime(horaNotif);
                // Si es hoy mismo y ya pasó la hora, ir al siguiente ciclo
                if (!candidata.isAfter(ahora)) {
                    candidata = candidata.plusWeeks(1);
                }
                return candidata;
            }
            case "Mensual": {
                // Próximo día del mes indicado
                int numeroDia = parsearNumeroDia(dia);
                LocalDate hoy = LocalDate.now();
                LocalDate candidataFecha;
                try {
                    candidataFecha = LocalDate.of(hoy.getYear(), hoy.getMonth(), numeroDia);
                } catch (Exception e) {
                    // Si el día no existe en el mes (ej: 31 de febrero), usamos el último día
                    candidataFecha = hoy.withDayOfMonth(hoy.lengthOfMonth());
                }
                LocalDateTime candidata = candidataFecha.atTime(horaNotif);
                if (!candidata.isAfter(ahora)) {
                    candidata = candidata.plusMonths(1);
                }
                return candidata;
            }
            default:
                return ahora.plusHours(1);
        }
    }

    /**
     * Convierte el nombre de un día de la semana en castellano a {@link java.time.DayOfWeek}.
     *
     * @param dia nombre del día en castellano (ej: "Lunes", "Martes")
     * @return el {@link java.time.DayOfWeek} correspondiente, o MONDAY por defecto
     */
    private java.time.DayOfWeek parsearDiaSemana(String dia) {
        if (dia == null) return java.time.DayOfWeek.MONDAY;
        switch (dia) {
            case "Lunes":     return java.time.DayOfWeek.MONDAY;
            case "Martes":    return java.time.DayOfWeek.TUESDAY;
            case "Miércoles": return java.time.DayOfWeek.WEDNESDAY;
            case "Jueves":    return java.time.DayOfWeek.THURSDAY;
            case "Viernes":   return java.time.DayOfWeek.FRIDAY;
            case "Sábado":    return java.time.DayOfWeek.SATURDAY;
            case "Domingo":   return java.time.DayOfWeek.SUNDAY;
            default:          return java.time.DayOfWeek.MONDAY;
        }
    }

    /**
     * Extrae el número de día de una cadena con formato "Día X".
     *
     * @param dia cadena con formato "Día 15" o similar
     * @return el número de día (1-31), o 1 si no se puede parsear
     */
    private int parsearNumeroDia(String dia) {
        if (dia == null) return 1;
        try {
            return Integer.parseInt(dia.replace("Día ", "").trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Construye el texto completo de frecuencia para guardar en la tarea.
     *
     * @param frecuencia tipo ("Diaria", "Semanal", "Mensual")
     * @param dia        día seleccionado o {@code null}
     * @param hora       hora (0-23)
     * @param minutos    minutos (0-59)
     * @return cadena formateada (ej: "Semanal · Lunes · 08:00")
     */
    private String construirFrecuencia(String frecuencia, String dia, int hora, int minutos) {
        String horaStr = String.format("%02d:%02d", hora, minutos);
        if (dia != null && !dia.isEmpty()) {
            return frecuencia + " · " + dia + " · " + horaStr;
        }
        return frecuencia + " · " + horaStr;
    }

    /**
     * Muestra el diálogo para renombrar una tarea y modificar su frecuencia.
     *
     * @param tarea       la tarea a modificar
     * @param labelNombre la etiqueta visual del nombre
     * @param labelBadge  la etiqueta visual del badge
     */
    private void mostrarDialogoRenombrar(Tarea tarea, Label labelNombre, Label labelBadge) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modificar hábito");
        dialog.setHeaderText(null);
        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 20; -fx-min-width: 360; -fx-background-color: white;");

        Label lblN = estiloLabel("Nombre:");
        TextField tfNombre = new TextField(tarea.getNombreTarea());
        Label lblF = estiloLabel("Frecuencia:");
        ComboBox<String> cbFrec = new ComboBox<>();
        cbFrec.getItems().addAll("Diaria", "Semanal", "Mensual");
        cbFrec.setValue("Diaria");
        cbFrec.setMaxWidth(Double.MAX_VALUE);
        Label lblH = estiloLabel("Hora:");
        HBox filaHora = new HBox(8);
        filaHora.setAlignment(Pos.CENTER_LEFT);
        Spinner<Integer> spH = new Spinner<>(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8)
        );
        spH.setPrefWidth(72); spH.setEditable(true);
        Label sep = new Label(":"); sep.setStyle("-fx-font-size: 14px; -fx-text-fill: #3A2B3F;");
        Spinner<Integer> spM = new Spinner<>(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0)
        );
        spM.setPrefWidth(72); spM.setEditable(true);
        filaHora.getChildren().addAll(spH, sep, spM);
        Label lblD = estiloLabel("Día:");
        ComboBox<String> cbDia = new ComboBox<>();
        cbDia.setMaxWidth(Double.MAX_VALUE);
        lblD.setVisible(false); lblD.setManaged(false);
        cbDia.setVisible(false); cbDia.setManaged(false);

        cbFrec.setOnAction(e -> {
            String frec = cbFrec.getValue();
            boolean mostrar = frec.equals("Semanal") || frec.equals("Mensual");
            lblD.setVisible(mostrar); lblD.setManaged(mostrar);
            cbDia.setVisible(mostrar); cbDia.setManaged(mostrar);
            cbDia.getItems().clear();
            if (frec.equals("Semanal")) {
                cbDia.getItems().addAll("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo");
            } else if (frec.equals("Mensual")) {
                for (int i = 1; i <= 31; i++) cbDia.getItems().add("Día " + i);
            }
            if (!cbDia.getItems().isEmpty()) cbDia.setValue(cbDia.getItems().get(0));
        });

        contenido.getChildren().addAll(lblN, tfNombre, lblF, cbFrec, lblH, filaHora, lblD, cbDia);
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == guardarBtn) {
            String nuevoNombre = tfNombre.getText().trim();
            if (nuevoNombre.isEmpty()) return;
            String nuevaFrecuencia = construirFrecuencia(
                cbFrec.getValue(),
                cbDia.isVisible() ? cbDia.getValue() : null,
                spH.getValue(), spM.getValue()
            );
            tarea.setNombreTarea(nuevoNombre);
            tarea.setFrecuenciaTarea(nuevaFrecuencia);
            tareaDAO.actualizar(tarea);
            labelNombre.setText(nuevoNombre);
            labelBadge.setText(nuevaFrecuencia);
        }
    }

    /**
     * Actualiza el selector de día según la frecuencia elegida.
     *
     * @param cbFrecuencia  ComboBox de frecuencia
     * @param cbDia         ComboBox de día
     * @param contenedorDia nodo contenedor del día para controlar visibilidad
     */
    private void actualizarSelectorDia(ComboBox<String> cbFrecuencia,
                                        ComboBox<String> cbDia,
                                        Region contenedorDia) {
        String frec = cbFrecuencia.getValue();
        boolean mostrar = frec.equals("Semanal") || frec.equals("Mensual");
        contenedorDia.setVisible(mostrar);
        contenedorDia.setManaged(mostrar);
        cbDia.getItems().clear();
        if (frec.equals("Semanal")) {
            cbDia.getItems().addAll("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo");
        } else if (frec.equals("Mensual")) {
            for (int i = 1; i <= 31; i++) cbDia.getItems().add("Día " + i);
        }
        if (!cbDia.getItems().isEmpty()) cbDia.setValue(cbDia.getItems().get(0));
    }

    /**
     * Actualiza el contador de hábitos activos visibles en las tarjetas.
     */
    private void actualizarContadorTareas() {
        long count = contenedorTareas.getChildren().stream()
            .filter(n -> n instanceof HBox).count();
        labelTareasActivas.setText(String.valueOf(count));
    }

    /**
     * Crea una etiqueta con el estilo de campo del formulario de Tracklify.
     *
     * @param texto el texto de la etiqueta
     * @return {@link Label} estilizada
     */
    private Label estiloLabel(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #93588F;");
        return l;
    }

    /**
     * Cierra la sesión, detiene el scheduler de notificaciones y navega a la landing.
     *
     * <p>Es fundamental detener el scheduler aquí para liberar el hilo daemon
     * y evitar que siga comprobando notificaciones de un usuario que ya cerró sesión.</p>
     *
     * @param event evento del botón "Cerrar sesión"
     * @throws Exception si el FXML no se puede cargar
     */
    @FXML
    public void cerrarSesion(ActionEvent event) throws Exception {

        // Detenemos el scheduler antes de cerrar sesión
        NotificacionScheduler.getInstancia().detener();
        SessionManager.getInstancia().cerrarSesion();

        FXMLLoader loader = new FXMLLoader(
            getClass().getResource("/fxml/landing_view.fxml")
        );
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }
}