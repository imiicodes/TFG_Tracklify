package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.EstadisticaDAO;
import com.mycompany.tracklify.dao.HabitoDAO;
import com.mycompany.tracklify.dao.NotificacionDAO;
import com.mycompany.tracklify.dao.PerfilDAO;
import com.mycompany.tracklify.dao.RegistroHabitoDAO;
import com.mycompany.tracklify.models.Habito;
import com.mycompany.tracklify.models.Notificacion;
import com.mycompany.tracklify.models.Perfil;
import com.mycompany.tracklify.models.RegistroHabito;
import com.mycompany.tracklify.models.ResumenUsuario;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.NotificacionScheduler;
import com.mycompany.tracklify.utils.RachaService;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador del dashboard principal del usuario ({@code main_view.fxml}).
 *
 * <p>Además de gestionar hábitos y estadísticas, este controlador es el punto
 * de entrada del sistema de notificaciones: al inicializarse arranca el
 * {@link NotificacionScheduler} y al cerrar sesión lo detiene.</p>
 *
 * <p>La creación guiada de hábitos se realiza en {@link CrearHabitoController}
 * ({@code crear_habito_view.fxml}); al volver a esta vista se recargan listas y estadísticas.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see NotificacionScheduler
 * @see NotificacionDAO
 * @see HabitoDAO
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

    // ── DAOs ──────────────────────────────────────────────────────────────

    /** DAO para operaciones sobre hábitos. */
    private HabitoDAO habitoDAO = new HabitoDAO();

    /** DAO para resúmenes e informes (vistas SQL). */
    private EstadisticaDAO estadisticaDAO = new EstadisticaDAO();

    private PerfilDAO perfilDAO = new PerfilDAO();

    private RachaService rachaService = new RachaService();

    /**
     * DAO para crear notificaciones al guardar un hábito y para
     * cargar los próximos recordatorios en el dashboard.
     */
    private NotificacionDAO notificacionDAO = new NotificacionDAO();

    /** DAO para registros diarios de cumplimiento de hábitos. */
    private final RegistroHabitoDAO registroHabitoDAO = new RegistroHabitoDAO();

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
            Perfil perfil = perfilDAO.obtenerPorUsuario(usuario.getIdUsuario());
            String nombreSaludo = perfil != null && perfil.getNombreUsuario() != null && !perfil.getNombreUsuario().isEmpty()
                ? perfil.getNombreUsuario()
                : usuario.getEmailUsuario();
            labelBienvenida.setText("Bienvenido, " + nombreSaludo + " ✦");
            cargarTareas(usuario.getIdUsuario());
            cargarEstadisticas(usuario.getIdUsuario());

            // Arrancamos el scheduler de notificaciones para este usuario
            NotificacionScheduler.getInstancia().iniciar(usuario.getIdUsuario());
        }

        cargarProximosRecordatorios();
    }

    /**
     * Navega al asistente de creación de hábito en cuatro pasos.
     *
     * @param event evento del botón "+ Crear nuevo hábito"
     * @throws Exception si falla la carga del FXML
     */
    @FXML
    public void navegarCrearHabito(ActionEvent event) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear_habito_view.fxml"));
        Scene scene = new Scene(loader.load());
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    /**
     * Abre el asistente en modo edición tras asignar el hábito en el controlador destino.
     *
     * @param habito hábito a modificar
     * @param anchor nodo con escena activa (por ejemplo la fila del listado)
     * @throws Exception si falla la carga del FXML
     */
    private void abrirEditorHabito(Habito habito, Node anchor) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/crear_habito_view.fxml"));
        Scene scene = new Scene(loader.load());
        CrearHabitoController ctrl = loader.getController();
        ctrl.setHabitoAEditar(habito);
        Stage stage = (Stage) anchor.getScene().getWindow();
        stage.setScene(scene);
    }

    // ── Carga de datos ─────────────────────────────────────────────────────

    /**
     * Carga las tareas del usuario y las muestra en el contenedor.
     *
     * @param idUsuario identificador del usuario
     */
    private void cargarTareas(int idUsuario) {
        List<Habito> habitos = habitoDAO.obtenerPorUsuario(idUsuario);
        contenedorTareas.getChildren().clear();

        if (habitos.isEmpty()) {
            Label sinTareas = new Label("No tienes hábitos aún. ¡Pulsa + Crear nuevo hábito!");
            sinTareas.setStyle("-fx-text-fill: #C4AADB; -fx-font-size: 12px;");
            contenedorTareas.getChildren().add(sinTareas);
        } else {
            for (Habito habito : habitos) {
                contenedorTareas.getChildren().add(crearFilaHabito(habito));
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
        refrescarEstadisticas(idUsuario);
    }

    /**
     * Actualiza las tres tarjetas del dashboard: hábitos activos y agregados desde
     * {@code v_resumen_usuario}, tasa de éxito y racha máxima entre hábitos activos.
     *
     * @param idUsuario identificador del usuario
     */
    private void refrescarEstadisticas(int idUsuario) {
        ResumenUsuario resumen = estadisticaDAO.obtenerResumenUsuario(idUsuario);
        int maxRacha = 0;
        for (Habito h : habitoDAO.obtenerPorUsuario(idUsuario)) {
            maxRacha = Math.max(maxRacha, rachaService.calcularRachaActual(h.getIdHabito()));
        }
        if (resumen != null) {
            labelTareasActivas.setText(String.valueOf(resumen.getTotalHabitosActivos()));
            double tasa = resumen.getTasaExitoGlobal();
            double porcentaje = (tasa >= 0 && tasa <= 1.0) ? tasa * 100.0 : tasa;
            labelPorcentaje.setText(String.format("%.0f%%", porcentaje));
            labelRacha.setText(maxRacha + " días");
        } else {
            List<Habito> hlist = habitoDAO.obtenerPorUsuario(idUsuario);
            labelTareasActivas.setText(String.valueOf(hlist.size()));
            labelPorcentaje.setText("0%");
            labelRacha.setText(maxRacha + " días");
        }
    }

    // ── Construcción de filas de tarea ─────────────────────────────────────

    /**
     * Construye el nodo visual de una tarea con checkbox, nombre, badge y menú.
     *
     * @param habito el {@link Habito} a representar
     * @return {@link HBox} con todos los elementos de la fila
     */
    private HBox crearFilaHabito(Habito habito) {
        HBox fila = new HBox(10);
        fila.getStyleClass().add("habito-fila");
        fila.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkbox = new CheckBox();
        checkbox.getStyleClass().add("habito-checkbox");

        Label nombre = new Label(habito.getNombreHabito());
        nombre.getStyleClass().add("habito-nombre");
        HBox.setHgrow(nombre, Priority.ALWAYS);

        Label badge = new Label(textoBadgeHorario(habito));
        badge.getStyleClass().add("badge-pendiente");

        Button btnMenu = new Button("···");
        btnMenu.getStyleClass().add("btn-dots");

        ContextMenu menu = new ContextMenu();

        MenuItem editar = new MenuItem("Editar");
        editar.setOnAction(e -> {
            try {
                abrirEditorHabito(habito, fila);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        MenuItem renombrar = new MenuItem("Renombrar");
        renombrar.setOnAction(e -> mostrarDialogoRenombrar(habito, nombre, badge));

        MenuItem borrar = new MenuItem("Borrar hábito");
        borrar.setStyle("-fx-text-fill: #C0392B;");
        borrar.setOnAction(e -> {
            habitoDAO.eliminar(habito.getIdHabito());
            contenedorTareas.getChildren().remove(fila);
            Usuario u = SessionManager.getInstancia().getUsuarioActual();
            if (u != null) {
                refrescarEstadisticas(u.getIdUsuario());
            }
        });

        menu.getItems().addAll(editar, renombrar, new SeparatorMenuItem(), borrar);
        btnMenu.setOnAction(e -> menu.show(btnMenu, javafx.geometry.Side.BOTTOM, 0, 0));

        boolean completadoHoy = registroHabitoDAO.obtenerRegistroCompleadoHoy(habito.getIdHabito()) != null;
        checkbox.setSelected(completadoHoy);
        aplicarEstiloFilaHabitoCompletado(nombre, badge, completadoHoy);

        final int idHabito = habito.getIdHabito();
        checkbox.setOnAction(e -> {
            Usuario usuarioAct = SessionManager.getInstancia().getUsuarioActual();
            if (usuarioAct == null) {
                checkbox.setSelected(!checkbox.isSelected());
                return;
            }
            int idUsuario = usuarioAct.getIdUsuario();
            boolean deseado = checkbox.isSelected();
            boolean ok;
            if (deseado) {
                RegistroHabito pendiente = registroHabitoDAO.obtenerRegistroHoy(idHabito, "PENDIENTE");
                if (pendiente != null) {
                    ok = registroHabitoDAO.cerrarSesion(pendiente.getIdRegistro(), LocalDateTime.now(), 0);
                } else {
                    LocalDateTime ahora = LocalDateTime.now();
                    RegistroHabito nuevo = new RegistroHabito(
                        0, idHabito, ahora, ahora, 0, "COMPLETADO", true, null, null, null);
                    ok = registroHabitoDAO.insertarConTimestamp(nuevo);
                }
            } else {
                RegistroHabito hecho = registroHabitoDAO.obtenerRegistroCompleadoHoy(idHabito);
                ok = hecho != null && registroHabitoDAO.actualizarEstado(hecho.getIdRegistro(), "PENDIENTE");
            }
            if (!ok) {
                checkbox.setSelected(!deseado);
                return;
            }
            aplicarEstiloFilaHabitoCompletado(nombre, badge, checkbox.isSelected());
            refrescarEstadisticas(idUsuario);
        });

        fila.getChildren().addAll(checkbox, nombre, badge, btnMenu);
        return fila;
    }

    // ── Utilidades ─────────────────────────────────────────────────────────

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
    private String textoBadgeHorario(Habito habito) {
        String d = habito.getDescripcionHabito();
        if (d == null || d.isEmpty()) {
            return "—";
        }
        int nl = d.indexOf('\n');
        if (nl >= 0 && nl < d.length() - 1) {
            return d.substring(nl + 1).trim();
        }
        return d.trim();
    }

    private void mostrarDialogoRenombrar(Habito habito, Label labelNombre, Label labelBadge) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Modificar hábito");
        dialog.setHeaderText(null);
        ButtonType guardarBtn = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(guardarBtn, ButtonType.CANCEL);

        VBox contenido = new VBox(10);
        contenido.setStyle("-fx-padding: 20; -fx-min-width: 360; -fx-background-color: white;");

        Label lblN = estiloLabel("Nombre:");
        TextField tfNombre = new TextField(habito.getNombreHabito());
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
            String fullDesc = habito.getDescripcionHabito();
            String userPart = "";
            if (fullDesc != null) {
                int nl = fullDesc.indexOf('\n');
                if (nl >= 0) {
                    userPart = fullDesc.substring(0, nl).trim();
                }
            }
            habito.setNombreHabito(nuevoNombre);
            habito.setDescripcionHabito(userPart.isEmpty() ? nuevaFrecuencia : userPart + "\n" + nuevaFrecuencia);
            habitoDAO.actualizar(habito);
            labelNombre.setText(nuevoNombre);
            labelBadge.setText(nuevaFrecuencia);
        }
    }

    /**
     * Aplica tachado y opacidad al nombre y badge cuando el hábito figura completado hoy.
     *
     * @param nombre  etiqueta del nombre del hábito
     * @param badge   etiqueta secundaria (horario / frecuencia)
     * @param hecho   {@code true} si está completado
     */
    private void aplicarEstiloFilaHabitoCompletado(Label nombre, Label badge, boolean hecho) {
        if (hecho) {
            nombre.setStyle("-fx-strikethrough: true; -fx-text-fill: #C4AADB;");
            badge.setOpacity(0.4);
        } else {
            nombre.setStyle("");
            badge.setOpacity(1.0);
        }
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