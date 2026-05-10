package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.EstadisticaDAO;
import com.mycompany.tracklify.dao.HabitoDAO;
import com.mycompany.tracklify.models.Habito;
import com.mycompany.tracklify.models.ProgresoSemanal;
import com.mycompany.tracklify.utils.RachaService;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Controlador de la vista «Mis hábitos»: listado filtrable, tarjetas con progreso semanal y acciones sobre cada hábito.
 *
 * @author Tracklify
 */
public class MisHabitosController implements Initializable {

    private static final String FILTRO_TODOS = "TODOS";
    private static final String FILTRO_ACTIVO = "ACTIVO";
    private static final String FILTRO_PAUSADO = "PAUSADO";
    private static final String FILTRO_COMPLETADO = "COMPLETADO";

    /** Acceso a hábitos en base de datos. */
    private final HabitoDAO habitoDAO = new HabitoDAO();

    /** Consultas de progreso e informes. */
    private final EstadisticaDAO estadisticaDAO = new EstadisticaDAO();

    /** Cálculo de racha por hábito. */
    private final RachaService rachaService = new RachaService();

    /** Marco principal que incrusta esta vista y abre creación o edición. */
    private MainViewController host;

    /**
     * Enlaza el controlador del shell para delegar la navegación al panel central.
     *
     * @param host {@link MainViewController} de la escena principal
     */
    public void setHost(MainViewController host) {
        this.host = host;
    }

    /** Copia en memoria de todos los hábitos del usuario (sin filtrar). */
    private List<Habito> todosLosHabitos = new ArrayList<>();

    /** Filtro de estado aplicado a la lista. */
    private String filtroActual = FILTRO_ACTIVO;

    @FXML
    private Button btnNuevoHabito;

    @FXML
    private Button btnFiltroTodos;

    @FXML
    private Button btnFiltroActivos;

    @FXML
    private Button btnFiltrosPausados;

    @FXML
    private Button btnFiltroCompletados;

    @FXML
    private ScrollPane scrollLista;

    @FXML
    private VBox listaHabitos;

    @FXML
    private VBox estadoVacio;

    /**
     * Carga los hábitos del usuario, aplica el filtro por defecto (Activos) y pinta la interfaz.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        recargarHabitosDesdeBd();
        aplicarFiltro(FILTRO_ACTIVO);
        configurarScrollSuave(scrollLista);
    }

    /**
     * Ajusta el desplazamiento vertical del listado con la rueda del ratón de forma más suave.
     *
     * @param scrollPane panel con scroll de la lista de hábitos
     */
    private void configurarScrollSuave(ScrollPane scrollPane) {
        if (scrollPane == null) {
            return;
        }
        javafx.scene.Node content = scrollPane.getContent();
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
     * Sustituye {@link #todosLosHabitos} por el resultado de {@link HabitoDAO#obtenerPorUsuario(int)}.
     */
    private void recargarHabitosDesdeBd() {
        if (SessionManager.getInstancia().getUsuarioActual() == null) {
            todosLosHabitos = new ArrayList<>();
            return;
        }
        int idUsuario = SessionManager.getInstancia().getUsuarioActual().getIdUsuario();
        todosLosHabitos = habitoDAO.obtenerPorUsuario(idUsuario);
    }

    /**
     * Filtra por estado, renderiza la lista y actualiza el aspecto de las pastillas de filtro.
     *
     * @param estado {@link #FILTRO_TODOS} o un valor de {@link Habito#getEstado()} (p. ej. ACTIVO)
     */
    private void aplicarFiltro(String estado) {
        filtroActual = estado;
        List<Habito> filtrada;
        if (FILTRO_TODOS.equals(estado)) {
            filtrada = new ArrayList<>(todosLosHabitos);
        } else {
            filtrada = todosLosHabitos.stream()
                .filter(h -> estado.equalsIgnoreCase(estadoNormalizado(h)))
                .collect(Collectors.toList());
        }
        renderizarHabitos(filtrada);
        Button activo = pillParaEstado(estado);
        actualizarEstiloPills(activo);
    }

    /**
     * Devuelve el estado del hábito nunca nulo (por defecto ACTIVO).
     *
     * @param h hábito
     * @return cadena de estado en mayúsculas coherente con la BD
     */
    private static String estadoNormalizado(Habito h) {
        String e = h.getEstado();
        return e != null && !e.isEmpty() ? e.trim() : FILTRO_ACTIVO;
    }

    /**
     * Resuelve qué botón de filtro corresponde al estado lógico seleccionado.
     *
     * @param estado estado o TODOS
     * @return botón a marcar como activo
     */
    private Button pillParaEstado(String estado) {
        if (FILTRO_TODOS.equals(estado)) {
            return btnFiltroTodos;
        }
        if (FILTRO_ACTIVO.equals(estado)) {
            return btnFiltroActivos;
        }
        if (FILTRO_PAUSADO.equals(estado)) {
            return btnFiltrosPausados;
        }
        if (FILTRO_COMPLETADO.equals(estado)) {
            return btnFiltroCompletados;
        }
        return btnFiltroTodos;
    }

    /**
     * Quita la clase activa de todas las pastillas y la aplica solo al botón seleccionado.
     *
     * @param seleccionado pastilla actual
     */
    private void actualizarEstiloPills(Button seleccionado) {
        for (Button b : new Button[]{
            btnFiltroTodos, btnFiltroActivos, btnFiltrosPausados, btnFiltroCompletados}) {
            b.getStyleClass().removeAll("filtro-pill-activo");
            if (!b.getStyleClass().contains("filtro-pill")) {
                b.getStyleClass().add("filtro-pill");
            }
        }
        if (seleccionado != null) {
            seleccionado.getStyleClass().add("filtro-pill-activo");
        }
    }

    /**
     * Vuelca la lista de hábitos en la interfaz o muestra el estado vacío.
     *
     * @param habitos hábitos ya filtrados
     */
    private void renderizarHabitos(List<Habito> habitos) {
        listaHabitos.getChildren().clear();
        if (habitos.isEmpty()) {
            estadoVacio.setVisible(true);
            estadoVacio.setManaged(true);
            scrollLista.setVisible(false);
            scrollLista.setManaged(false);
        } else {
            estadoVacio.setVisible(false);
            estadoVacio.setManaged(false);
            scrollLista.setVisible(true);
            scrollLista.setManaged(true);
            for (Habito h : habitos) {
                Node tarjeta = crearTarjetaHabito(h);
                VBox.setMargin(tarjeta, new Insets(0, 0, 10, 0));
                listaHabitos.getChildren().add(tarjeta);
            }
        }
    }

    /**
     * Construye la tarjeta visual de un hábito con progreso semanal, racha, estado y menú de acciones.
     *
     * @param habito entidad
     * @return contenedor raíz de la tarjeta
     */
    private HBox crearTarjetaHabito(Habito habito) {
        HBox tarjeta = new HBox(20);
        tarjeta.setAlignment(Pos.TOP_LEFT);
        tarjeta.getStyleClass().add("tarjeta-mis-habitos");

        VBox colIzq = new VBox(8);
        colIzq.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(colIzq, Priority.ALWAYS);

        Label lblNombre = new Label(habito.getNombreHabito() != null ? habito.getNombreHabito() : "");
        lblNombre.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #2D1B2E;");

        Label chipCat = new Label(textoCategoria(habito));
        chipCat.setStyle("-fx-background-color: #EDD6F0; -fx-text-fill: #7A4578; -fx-border-color: #93588F; "
            + "-fx-border-width: 1px; -fx-border-radius: 10px; -fx-background-radius: 10px; "
            + "-fx-padding: 2px 8px; -fx-font-size: 11px;");

        List<ProgresoSemanal> progresos = estadisticaDAO.obtenerProgresoSemanal(habito.getIdHabito());
        ProgresoSemanal semanaActual = buscarProgresoSemanaActual(progresos, habito.getIdHabito());
        int objetivo = habito.getObjetivoVeces() > 0 ? habito.getObjetivoVeces() : 1;
        int hecho = 0;
        if (semanaActual != null) {
            objetivo = Math.max(1, semanaActual.getObjetivoVeces());
            hecho = Math.max(0, semanaActual.getVecesCompletado());
        }

        ProgressBar barra = new ProgressBar();
        barra.setMaxWidth(Double.MAX_VALUE);
        double ratio = objetivo > 0 ? Math.min(1.0, (double) hecho / (double) objetivo) : 0;
        barra.setProgress(ratio);
        barra.setStyle("-fx-accent: #7A4578;");

        Label lblProgreso = new Label(hecho + "/" + objetivo + " veces esta semana");
        lblProgreso.setStyle("-fx-text-fill: #6B4A6E; -fx-font-size: 12px;");

        colIzq.getChildren().addAll(lblNombre, chipCat, barra, lblProgreso);

        VBox colDer = new VBox(10);
        colDer.setAlignment(Pos.TOP_RIGHT);

        int racha = rachaService.calcularRachaActual(habito.getIdHabito());
        Label lblRacha = new Label(racha == 1 ? "1 día" : racha + " días");
        lblRacha.setStyle("-fx-text-fill: #7A4578; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label badgeEstado = new Label(estadoNormalizado(habito));
        aplicarEstiloBadgeEstado(badgeEstado, estadoNormalizado(habito));

        MenuButton menu = new MenuButton("···");
        menu.getStyleClass().add("menu-tres-puntos");
        menu.setFocusTraversable(false);

        MenuItem editar = new MenuItem("Editar");
        editar.setOnAction(ev -> abrirEditorHabito(habito));

        String est = estadoNormalizado(habito);
        MenuItem pausa = new MenuItem(FILTRO_PAUSADO.equalsIgnoreCase(est) ? "Reanudar" : "Pausar");
        pausa.setOnAction(ev -> alternarPausa(habito));

        MenuItem abandonar = new MenuItem("Abandonar");
        abandonar.setOnAction(ev -> confirmarAbandonar(habito));

        MenuItem eliminar = new MenuItem("Eliminar");
        eliminar.setStyle("-fx-text-fill: #C62828;");
        eliminar.setOnAction(ev -> confirmarEliminar(habito));

        menu.getItems().addAll(
            editar,
            new SeparatorMenuItem(),
            pausa,
            abandonar,
            new SeparatorMenuItem(),
            eliminar
        );

        colDer.getChildren().addAll(lblRacha, badgeEstado, menu);
        tarjeta.getChildren().addAll(colIzq, colDer);
        return tarjeta;
    }

    /**
     * Busca en la lista el registro de progreso de la semana ISO actual para el hábito.
     *
     * @param lista   resultados de {@link EstadisticaDAO#obtenerProgresoSemanal(int)}
     * @param idHabito identificador del hábito
     * @return fila coincidente o {@code null}
     */
    private ProgresoSemanal buscarProgresoSemanaActual(List<ProgresoSemanal> lista, int idHabito) {
        LocalDate hoy = LocalDate.now();
        int anio = hoy.get(IsoFields.WEEK_BASED_YEAR);
        int sem = hoy.get(WeekFields.ISO.weekOfWeekBasedYear());
        for (ProgresoSemanal p : lista) {
            if (p.getIdHabito() == idHabito && p.getAnioSemana() == anio && p.getNumSemana() == sem) {
                return p;
            }
        }
        return null;
    }

    /**
     * Texto mostrado en el chip de categoría.
     *
     * @param habito hábito
     * @return etiqueta legible
     */
    private String textoCategoria(Habito habito) {
        if (habito.getIdCategoria() == null) {
            return "Sin categoría";
        }
        return "Categoría #" + habito.getIdCategoria();
    }

    /**
     * Aplica clases CSS al badge según el estado del hábito.
     *
     * @param badge etiqueta visual
     * @param estado estado normalizado
     */
    private void aplicarEstiloBadgeEstado(Label badge, String estado) {
        badge.getStyleClass().clear();
        badge.setStyle(null);
        String s = estado != null ? estado.toUpperCase() : FILTRO_ACTIVO;
        switch (s) {
            case "ACTIVO":
                badge.getStyleClass().add("badge-activo");
                break;
            case "PAUSADO":
                badge.getStyleClass().add("badge-pausado");
                break;
            case "COMPLETADO":
                badge.getStyleClass().add("badge-completado");
                break;
            case "ABANDONADO":
                badge.getStyleClass().add("badge-abandonado");
                break;
            default:
                badge.setStyle("-fx-background-color: #EDE7F6; -fx-text-fill: #4527A0; -fx-background-radius: 8px; "
                    + "-fx-padding: 2px 8px; -fx-font-size: 11px;");
        }
    }

    /**
     * Abre el formulario de edición en el panel central; el retorno queda configurado en el host.
     *
     * @param habito hábito a editar
     */
    private void abrirEditorHabito(Habito habito) {
        if (host != null) {
            host.abrirEditorHabito(habito, true);
        }
    }

    /**
     * Pausa o reanuda el hábito actualizando solo el estado en base de datos.
     *
     * @param habito hábito afectado
     */
    private void alternarPausa(Habito habito) {
        String est = estadoNormalizado(habito);
        String nuevo = FILTRO_PAUSADO.equalsIgnoreCase(est) ? FILTRO_ACTIVO : FILTRO_PAUSADO;
        Habito copia = copiarHabitoParaActualizar(habito, nuevo);
        if (habitoDAO.actualizar(copia)) {
            recargarHabitosDesdeBd();
            aplicarFiltro(filtroActual);
        }
    }

    /**
     * Solicita confirmación y marca el hábito como abandonado.
     *
     * @param habito hábito
     */
    private void confirmarAbandonar(Habito habito) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Abandonar hábito");
        alert.setHeaderText(null);
        alert.setContentText("¿Seguro que quieres abandonar este hábito? Quedará marcado como abandonado.");
        aplicarEstiloAlerta(alert);
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                Habito copia = copiarHabitoParaActualizar(habito, "ABANDONADO");
                if (habitoDAO.actualizar(copia)) {
                    recargarHabitosDesdeBd();
                    aplicarFiltro(filtroActual);
                }
            }
        });
    }

    /**
     * Solicita confirmación y elimina el hábito de forma permanente.
     *
     * @param habito hábito a borrar
     */
    private void confirmarEliminar(Habito habito) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Eliminar hábito");
        alert.setHeaderText(null);
        alert.setContentText("Esta acción no se puede deshacer. ¿Eliminar este hábito?");
        aplicarEstiloAlerta(alert);
        alert.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                if (habitoDAO.eliminar(habito.getIdHabito())) {
                    recargarHabitosDesdeBd();
                    aplicarFiltro(filtroActual);
                }
            }
        });
    }

    /**
     * Copia los campos necesarios para un UPDATE completo del hábito con un estado distinto.
     *
     * @param origen hábito original
     * @param nuevoEstado estado a persistir
     * @return copia lista para {@link HabitoDAO#actualizar(Habito)}
     */
    private Habito copiarHabitoParaActualizar(Habito origen, String nuevoEstado) {
        return new Habito(
            origen.getIdHabito(),
            origen.getIdUsuario(),
            origen.getIdCategoria(),
            origen.getNombreHabito(),
            origen.getDescripcionHabito(),
            origen.getDuracionValor(),
            origen.getDuracionPeriodoId(),
            origen.getFechaInicio(),
            origen.getFechaFin(),
            origen.getNotifFrecuenciaValor(),
            origen.getNotifFrecuenciaId(),
            origen.getObjetivoVeces(),
            origen.getObjetivoPeriodoId(),
            nuevoEstado
        );
    }

    /**
     * Aplica hoja de estilos y fondo al panel de un diálogo de confirmación.
     *
     * @param alert diálogo a estilizar
     */
    private void aplicarEstiloAlerta(Alert alert) {
        URL css = getClass().getResource("/styles/main_view.css");
        if (css != null) {
            alert.getDialogPane().getStylesheets().add(css.toExternalForm());
        }
        alert.getDialogPane().setStyle("-fx-background-color: #F6EEFF;");
    }

    /**
     * Navega al asistente de creación de hábito indicando retorno a esta pantalla.
     *
     * @param event evento de botón
     */
    @FXML
    public void irACrearHabito(ActionEvent event) {
        if (host != null) {
            host.abrirCrearHabito(true);
        }
    }

    /**
     * Muestra todos los hábitos del usuario.
     *
     * @param event evento de filtro
     */
    @FXML
    public void filtrarTodos(ActionEvent event) {
        aplicarFiltro(FILTRO_TODOS);
    }

    /**
     * Filtra hábitos en estado ACTIVO.
     *
     * @param event evento de filtro
     */
    @FXML
    public void filtrarActivos(ActionEvent event) {
        aplicarFiltro(FILTRO_ACTIVO);
    }

    /**
     * Filtra hábitos en estado PAUSADO.
     *
     * @param event evento de filtro
     */
    @FXML
    public void filtrarPausados(ActionEvent event) {
        aplicarFiltro(FILTRO_PAUSADO);
    }

    /**
     * Filtra hábitos en estado COMPLETADO.
     *
     * @param event evento de filtro
     */
    @FXML
    public void filtrarCompletados(ActionEvent event) {
        aplicarFiltro(FILTRO_COMPLETADO);
    }
}
