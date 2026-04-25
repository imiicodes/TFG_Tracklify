package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.EstadisticaDAO;
import com.mycompany.tracklify.dao.TareaDAO;
import com.mycompany.tracklify.models.Estadistica;
import com.mycompany.tracklify.models.Tarea;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
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
 * <p>Gestiona todas las interacciones del usuario con sus hábitos/tareas:</p>
 * <ul>
 *   <li>Carga y muestra las tareas del usuario desde la BD</li>
 *   <li>Creación de nuevas tareas mediante formulario en línea</li>
 *   <li>Checkbox por tarea: al completarla se tacha y desaparece tras 3 minutos</li>
 *   <li>Menú contextual (···) por tarea: renombrar y borrar</li>
 *   <li>Diálogo de modificación con frecuencia, hora y día de notificación</li>
 *   <li>Sección de próximos recordatorios (placeholder)</li>
 * </ul>
 *
 * @author Tracklify
 * @version 1.0
 * @see TareaDAO
 * @see SessionManager
 */
public class MainViewController implements Initializable {

    /** Etiqueta que muestra el saludo con el nombre del usuario. */
    @FXML private Label labelBienvenida;

    /** Etiqueta que muestra el número de tareas activas del usuario. */
    @FXML private Label labelTareasActivas;

    /** Etiqueta que muestra el porcentaje de completado del día. */
    @FXML private Label labelPorcentaje;

    /** Etiqueta que muestra la racha actual del usuario. */
    @FXML private Label labelRacha;

    /** Contenedor donde se renderizan dinámicamente las filas de tareas. */
    @FXML private VBox contenedorTareas;

    /** Contenedor de la sección de próximos recordatorios. */
    @FXML private VBox contenedorRecordatorios;

    /** Panel del formulario de nueva tarea (oculto por defecto). */
    @FXML private VBox panelNuevaTarea;

    /** Campo de texto para el nombre de la nueva tarea. */
    @FXML private TextField campoNombreTarea;

    /** Campo de texto para la descripción de la nueva tarea. */
    @FXML private TextField campoDescripcionTarea;

    /** ComboBox para seleccionar la frecuencia de notificación. */
    @FXML private ComboBox<String> comboFrecuencia;

    /** Spinner para la hora de notificación (0-23). */
    @FXML private Spinner<Integer> spinnerHora;

    /** Spinner para los minutos de notificación (0-59). */
    @FXML private Spinner<Integer> spinnerMinutos;

    /** Fila adicional que aparece cuando la frecuencia es semanal o mensual. */
    @FXML private HBox filaDia;

    /** ComboBox para seleccionar el día de la semana o del mes. */
    @FXML private ComboBox<String> comboDia;

    /** DAO para acceder a las tareas del usuario. */
    private TareaDAO tareaDAO = new TareaDAO();

    /** DAO para acceder a las estadísticas del usuario. */
    private EstadisticaDAO estadisticaDAO = new EstadisticaDAO();

    /**
     * Inicialización automática al cargar la vista.
     *
     * <p>Personaliza el saludo, carga tareas y estadísticas del usuario
     * en sesión, configura el ComboBox de frecuencia y oculta el formulario.</p>
     *
     * @param url            URL del recurso FXML
     * @param resourceBundle paquete de recursos de internacionalización
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        // Cargamos datos del usuario en sesión
        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario != null) {
            labelBienvenida.setText("Bienvenido, " + usuario.getNombreUsuario() + " ✦");
            cargarTareas(usuario.getIdUsuario());
            cargarEstadisticas(usuario.getIdUsuario());
        }

        // Configuramos opciones de frecuencia
        comboFrecuencia.getItems().addAll("Diaria", "Semanal", "Mensual");
        comboFrecuencia.setValue("Diaria");

        // Configuramos spinners de hora y minutos
        spinnerHora.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        spinnerMinutos.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));

        // Ocultamos la fila del día por defecto
        filaDia.setVisible(false);
        filaDia.setManaged(false);

        // Listener: muestra/oculta selector de día según frecuencia elegida
        comboFrecuencia.setOnAction(e -> actualizarSelectorDia(comboFrecuencia, comboDia, filaDia));

        // El formulario empieza oculto
        panelNuevaTarea.setVisible(false);
        panelNuevaTarea.setManaged(false);

        // Cargamos recordatorios placeholder
        cargarRecordatoriosPlaceholder();
    }

    /**
     * Muestra u oculta el formulario de nueva tarea al pulsar el botón.
     *
     * <p>El formulario aparece dentro del mismo dashboard sin cambiar de pantalla.
     * Al abrirlo, limpia los campos del formulario anterior.</p>
     *
     * @param event evento generado al pulsar "+ Crear nuevo hábito"
     */
    @FXML
    public void mostrarFormularioNuevaTarea(ActionEvent event) {
        boolean visible = panelNuevaTarea.isVisible();
        panelNuevaTarea.setVisible(!visible);
        panelNuevaTarea.setManaged(!visible);

        // Limpiamos los campos al abrir
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
     * Guarda la nueva tarea al pulsar "Guardar" en el formulario.
     *
     * <p>Valida el nombre, construye la frecuencia completa con hora y día,
     * persiste la tarea en BD y actualiza la lista visible sin recargar todo.</p>
     *
     * @param event evento generado al pulsar "Guardar"
     */
    @FXML
    public void guardarNuevaTarea(ActionEvent event) {

        String nombre = campoNombreTarea.getText().trim();

        // Nombre obligatorio: marcamos el campo en rojo si está vacío
        if (nombre.isEmpty()) {
            campoNombreTarea.setStyle("-fx-border-color: #C0392B; -fx-border-radius: 8;");
            return;
        }
        campoNombreTarea.setStyle("");

        // Construimos la frecuencia completa
        String frecuencia = construirFrecuencia(
            comboFrecuencia.getValue(),
            comboDia.isVisible() ? comboDia.getValue() : null,
            spinnerHora.getValue(),
            spinnerMinutos.getValue()
        );

        // Construimos el objeto Tarea
        Tarea nueva = new Tarea();
        nueva.setUsuarioId(SessionManager.getInstancia().getUsuarioActual().getIdUsuario());
        nueva.setNombreTarea(nombre);
        nueva.setDescripcionTarea(campoDescripcionTarea.getText().trim());
        nueva.setFrecuenciaTarea(frecuencia);

        // Guardamos en BD y recargamos la lista
        boolean exito = tareaDAO.insertar(nueva);
        if (exito) {
            cargarTareas(SessionManager.getInstancia().getUsuarioActual().getIdUsuario());
            panelNuevaTarea.setVisible(false);
            panelNuevaTarea.setManaged(false);
        }
    }

    /**
     * Cancela la creación y oculta el formulario.
     *
     * @param event evento generado al pulsar "Cancelar"
     */
    @FXML
    public void cancelarNuevaTarea(ActionEvent event) {
        panelNuevaTarea.setVisible(false);
        panelNuevaTarea.setManaged(false);
    }

    /**
     * Carga y muestra las tareas del usuario en el dashboard.
     *
     * @param idUsuario identificador del usuario cuyas tareas se cargan
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
     * Construye el nodo visual completo de una tarea.
     *
     * <p>La fila contiene checkbox, nombre, badge de frecuencia y botón ···.
     * El checkbox tacha la tarea y la elimina de la pantalla a los 3 minutos.
     * El botón ··· abre un menú con "Renombrar" y "Borrar".</p>
     *
     * @param tarea la {@link Tarea} a representar
     * @return {@link HBox} con todos los elementos de la fila
     */
    private HBox crearFilaTarea(Tarea tarea) {
        HBox fila = new HBox(10);
        fila.getStyleClass().add("habito-fila");
        fila.setAlignment(Pos.CENTER_LEFT);

        // Checkbox cuadrado para completar la tarea
        CheckBox checkbox = new CheckBox();
        checkbox.getStyleClass().add("habito-checkbox");

        // Nombre de la tarea
        Label nombre = new Label(tarea.getNombreTarea());
        nombre.getStyleClass().add("habito-nombre");
        HBox.setHgrow(nombre, Priority.ALWAYS);

        // Badge de frecuencia
        Label badge = new Label(
            tarea.getFrecuenciaTarea() != null ? tarea.getFrecuenciaTarea() : "—"
        );
        badge.getStyleClass().add("badge-pendiente");

        // Botón ··· para el menú contextual
        Button btnMenu = new Button("···");
        btnMenu.getStyleClass().add("btn-dots");

        // Menú contextual
        ContextMenu menu = new ContextMenu();

        MenuItem renombrar = new MenuItem("✎  Renombrar");
        renombrar.setOnAction(e -> mostrarDialogoRenombrar(tarea, nombre, badge));

        MenuItem borrar = new MenuItem("✕  Borrar hábito");
        borrar.setStyle("-fx-text-fill: #C0392B;");
        borrar.setOnAction(e -> {
            // Borrar elimina de la BD y de la lista — NO es completar
            tareaDAO.eliminar(tarea.getIdTarea());
            contenedorTareas.getChildren().remove(fila);
            actualizarContadorTareas();
        });

        menu.getItems().addAll(renombrar, new SeparatorMenuItem(), borrar);
        btnMenu.setOnAction(e -> menu.show(btnMenu, javafx.geometry.Side.BOTTOM, 0, 0));

        // Lógica de completar: tachar + desaparecer a los 3 minutos
        checkbox.setOnAction(e -> {
            if (checkbox.isSelected()) {
                nombre.setStyle("-fx-strikethrough: true; -fx-text-fill: #C4AADB;");
                badge.setOpacity(0.4);
                btnMenu.setVisible(false);

                // Eliminación automática tras 3 minutos (180 segundos)
                PauseTransition pausa = new PauseTransition(Duration.seconds(180));
                pausa.setOnFinished(ev -> {
                    contenedorTareas.getChildren().remove(fila);
                    actualizarContadorTareas();
                });
                pausa.play();
            } else {
                // Si el usuario desmarca, restauramos el aspecto
                nombre.setStyle("");
                badge.setOpacity(1.0);
                btnMenu.setVisible(true);
            }
        });

        fila.getChildren().addAll(checkbox, nombre, badge, btnMenu);
        return fila;
    }

    /**
     * Muestra un diálogo para renombrar la tarea y modificar su configuración
     * de notificación (frecuencia, hora y día).
     *
     * @param tarea       la {@link Tarea} a modificar
     * @param labelNombre la {@link Label} visual del nombre en el dashboard
     * @param labelBadge  la {@link Label} visual del badge de frecuencia
     */
    private void mostrarDialogoRenombrar(Tarea tarea, Label labelNombre, Label labelBadge) {

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modificar hábito");
        dialog.setHeaderText(null);

        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        // Layout del formulario
        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 20; -fx-min-width: 360; -fx-background-color: white;");

        // Campo: nombre
        Label lblN = estiloLabel("Nombre:");
        TextField tfNombre = new TextField(tarea.getNombreTarea());
        tfNombre.setStyle("-fx-background-radius: 8; -fx-border-color: #DEA9FF; -fx-border-radius: 8; -fx-border-width: 1.5; -fx-padding: 7 10;");

        // Campo: frecuencia
        Label lblF = estiloLabel("Frecuencia:");
        ComboBox<String> cbFrec = new ComboBox<>();
        cbFrec.getItems().addAll("Diaria", "Semanal", "Mensual");
        cbFrec.setValue("Diaria");
        cbFrec.setMaxWidth(Double.MAX_VALUE);

        // Campo: hora
        Label lblH = estiloLabel("Hora de notificación:");
        HBox filaHora = new HBox(8);
        filaHora.setAlignment(Pos.CENTER_LEFT);
        Spinner<Integer> spH = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 23, 8));
        spH.setPrefWidth(72);
        spH.setEditable(true);
        Label sep = new Label(":");
        sep.setStyle("-fx-font-size: 14px; -fx-text-fill: #3A2B3F;");
        Spinner<Integer> spM = new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0));
        spM.setPrefWidth(72);
        spM.setEditable(true);
        filaHora.getChildren().addAll(spH, sep, spM);

        // Campo: día (oculto por defecto)
        Label lblD = estiloLabel("Día:");
        ComboBox<String> cbDia = new ComboBox<>();
        cbDia.setMaxWidth(Double.MAX_VALUE);
        lblD.setVisible(false); lblD.setManaged(false);
        cbDia.setVisible(false); cbDia.setManaged(false);

        // Listener frecuencia → mostrar/ocultar día
        cbFrec.setOnAction(e -> actualizarSelectorDia(cbFrec, cbDia, new HBox() {{
            // Usamos los labels directamente en lugar de un HBox contenedor
            lblD.setVisible(cbFrec.getValue().equals("Semanal") || cbFrec.getValue().equals("Mensual"));
            lblD.setManaged(lblD.isVisible());
            cbDia.setVisible(lblD.isVisible());
            cbDia.setManaged(lblD.isVisible());
            cbDia.getItems().clear();
            if (cbFrec.getValue().equals("Semanal")) {
                cbDia.getItems().addAll("Lunes","Martes","Miércoles","Jueves","Viernes","Sábado","Domingo");
            } else if (cbFrec.getValue().equals("Mensual")) {
                for (int i = 1; i <= 31; i++) cbDia.getItems().add("Día " + i);
            }
            if (!cbDia.getItems().isEmpty()) cbDia.setValue(cbDia.getItems().get(0));
        }}));

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

            // Actualizamos la vista directamente sin recargar toda la lista
            labelNombre.setText(nuevoNombre);
            labelBadge.setText(nuevaFrecuencia);
        }
    }

    /**
     * Actualiza el selector de día en función de la frecuencia seleccionada.
     *
     * <p>Muestra el combo de días si la frecuencia es semanal o mensual,
     * y lo oculta si es diaria. Rellena las opciones correspondientes.</p>
     *
     * @param cbFrecuencia el ComboBox de frecuencia
     * @param cbDia        el ComboBox de día a actualizar
     * @param contenedorDia el nodo contenedor del selector de día para su visibilidad
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
     * Construye el texto completo de la frecuencia para guardar en BD y mostrar en badge.
     *
     * <p>Ejemplos de resultado: {@code "Diaria · 08:00"},
     * {@code "Semanal · Lunes · 18:30"}, {@code "Mensual · Día 15 · 09:00"}.</p>
     *
     * @param frecuencia tipo de frecuencia (Diaria, Semanal, Mensual)
     * @param dia        día de la semana o del mes, o {@code null} si es diaria
     * @param hora       hora de la notificación (0-23)
     * @param minutos    minutos de la notificación (0-59)
     * @return cadena de texto con la frecuencia completa formateada
     */
    private String construirFrecuencia(String frecuencia, String dia, int hora, int minutos) {
        String horaStr = String.format("%02d:%02d", hora, minutos);
        if (dia != null && !dia.isEmpty()) {
            return frecuencia + " · " + dia + " · " + horaStr;
        }
        return frecuencia + " · " + horaStr;
    }

    /**
     * Crea una etiqueta con el estilo de campo del formulario.
     *
     * @param texto el texto de la etiqueta
     * @return {@link Label} con el estilo de Tracklify aplicado
     */
    private Label estiloLabel(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #93588F;");
        return l;
    }

    /**
     * Actualiza el contador de tareas activas en el dashboard.
     *
     * <p>Se llama tras eliminar o completar una tarea para mantener
     * el número sincronizado con lo que el usuario ve en pantalla.</p>
     */
    private void actualizarContadorTareas() {
        long count = contenedorTareas.getChildren().stream()
            .filter(n -> n instanceof HBox)
            .count();
        labelTareasActivas.setText(String.valueOf(count));
    }

    /**
     * Carga las estadísticas más recientes del usuario y las muestra.
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

    /**
     * Rellena la sección "Próximos recordatorios" con datos de ejemplo.
     *
     * <p>Se conectará a datos reales cuando se implemente el sistema
     * de notificaciones en fases posteriores del proyecto.</p>
     */
    private void cargarRecordatoriosPlaceholder() {
        if (contenedorRecordatorios == null) return;
        contenedorRecordatorios.getChildren().clear();

        String[][] recordatorios = {
            {"09:00", "Meditación matutina — hoy"},
            {"18:00", "Ejercicio diario — próximo lunes"},
            {"22:00", "Lectura — mañana"}
        };

        for (String[] r : recordatorios) {
            HBox fila = new HBox(12);
            fila.setAlignment(Pos.CENTER_LEFT);
            fila.setStyle("-fx-padding: 5 0 5 0; -fx-border-color: transparent transparent #F6EEFF transparent; -fx-border-width: 1;");

            Label hora = new Label(r[0]);
            hora.setStyle("-fx-font-size: 11px; -fx-text-fill: #93588F; -fx-font-weight: bold; -fx-min-width: 48;");

            Label desc = new Label(r[1]);
            desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #7E6A8A;");

            fila.getChildren().addAll(hora, desc);
            contenedorRecordatorios.getChildren().add(fila);
        }
    }

    /**
     * Cierra la sesión del usuario y navega de vuelta a la landing.
     *
     * @param event evento generado al pulsar "Cerrar sesión"
     * @throws Exception si el FXML no se puede cargar
     */
    @FXML
    public void cerrarSesion(ActionEvent event) throws Exception {
        SessionManager.getInstancia().cerrarSesion();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/landing_view.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(loader.load()));
    }
}