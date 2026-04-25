package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.OnboardingDAO;
import com.mycompany.tracklify.models.RespuestaOnboarding;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * Controlador del onboarding de Tracklify ({@code onboarding_view.fxml}).
 *
 * <p>Gestiona la secuencia de 5 preguntas de personalización que se muestran
 * únicamente la primera vez que un usuario accede a la aplicación tras
 * registrarse. El flujo completo es:</p>
 *
 * <ol>
 *   <li>El usuario se registra → {@code onboarding_completado = 0}</li>
 *   <li>Al iniciar sesión, {@link LoginController} comprueba el flag</li>
 *   <li>Si es {@code 0}, navega a {@code onboarding_view.fxml}</li>
 *   <li>El usuario responde o salta cada pregunta</li>
 *   <li>Al finalizar, el flag se establece a {@code 1} y se redirige al dashboard</li>
 * </ol>
 *
 * <p>Reglas de selección:</p>
 * <ul>
 *   <li>Mínimo 1 opción, máximo 3 opciones por pregunta</li>
 *   <li>Al superar 3 selecciones se muestra un mensaje de aviso</li>
 *   <li>Skip omite la pregunta sin guardar respuestas</li>
 * </ul>
 *
 * @author Tracklify
 * @version 1.0
 * @see OnboardingDAO
 * @see RespuestaOnboarding
 * @see SessionManager
 */
public class OnboardingController implements Initializable {

    // ── FXML: estructura principal de la vista ─────────────────────────────

    /** Indicador visual del paso actual (ej: "Pregunta 2 de 5"). */
    @FXML private Label labelPaso;

    /** Barra de progreso visual construida dinámicamente con puntos. */
    @FXML private HBox barraProgreso;

    /** Etiqueta con el texto de la pregunta actual, centrado y grande. */
    @FXML private Label labelPregunta;

    /** Contenedor donde se renderizan los botones de opción. */
    @FXML private FlowPane contenedorOpciones;

    /** Mensaje de validación (ej: "Selecciona entre 1 y 3 opciones"). */
    @FXML private Label labelValidacion;

    /** Botón "Ver más" para mostrar las 4 opciones adicionales. */
    @FXML private Button btnVerMas;

    /** Botón para avanzar a la siguiente pregunta. */
    @FXML private Button btnSiguiente;

    /** Botón para saltar la pregunta actual sin responderla. */
    @FXML private Button btnSkip;

    // ── Datos de las preguntas ─────────────────────────────────────────────

    /**
     * Mapa ordenado que contiene todas las preguntas y sus opciones de respuesta.
     * La clave es el texto de la pregunta y el valor es el array de opciones.
     * Se usa {@link LinkedHashMap} para mantener el orden de inserción.
     */
    private final Map<String, String[]> preguntas = new LinkedHashMap<>();

    /**
     * Lista de preguntas para acceder por índice numérico.
     * Se inicializa a partir de las claves de {@link #preguntas}.
     */
    private List<String> listaPreguntas;

    /** Índice de la pregunta que se está mostrando actualmente (0-based). */
    private int preguntaActual = 0;

    /** Número máximo de opciones que el usuario puede seleccionar por pregunta. */
    private static final int MAX_SELECCIONES = 3;

    /** Número de opciones visibles inicialmente antes de pulsar "Ver más". */
    private static final int OPCIONES_INICIALES = 6;

    /**
     * Lista de {@link ToggleButton} que representan las opciones de la pregunta actual.
     * Se reconstruye en cada cambio de pregunta.
     */
    private List<ToggleButton> botonesOpciones = new ArrayList<>();

    /** DAO para persistir respuestas y actualizar el flag de onboarding. */
    private OnboardingDAO onboardingDAO = new OnboardingDAO();

    /**
     * Inicialización automática al cargar la vista.
     *
     * <p>Construye el mapa de preguntas y respuestas, inicializa
     * la lista de preguntas y carga la primera pregunta en pantalla.</p>
     *
     * @param url            URL del recurso FXML
     * @param resourceBundle paquete de recursos de internacionalización
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        construirPreguntas();
        listaPreguntas = new ArrayList<>(preguntas.keySet());
        mostrarPregunta(0);
    }

    /**
     * Define todas las preguntas y sus opciones de respuesta.
     *
     * <p>El orden de inserción se respeta gracias al uso de
     * {@link LinkedHashMap}. Cada entrada tiene como clave el texto
     * de la pregunta y como valor un array con todas las opciones posibles.</p>
     */
    private void construirPreguntas() {
        preguntas.put(
            "¿Qué te gustaría cambiar\nen tu vida ahora mismo?",
            new String[]{
                "Deporte", "Estudios", "Manejo del tiempo", "Meditación",
                "Beber agua", "Mentalidad", "Higiene personal", "Lectura",
                "Dieta y comida", "Medicación"
            }
        );
        preguntas.put(
            "¿Cuál es tu principal meta\nen este momento?",
            new String[]{
                "Mejorar mi productividad", "Reducir el estrés",
                "Organizar mejor mi tiempo", "Aprender algo nuevo",
                "Mejorar mi salud mental", "Crear rutinas sólidas",
                "Alcanzar objetivos personales", "Mejorar mis hábitos diarios",
                "Tener más equilibrio vida-trabajo", "Superar la procrastinación"
            }
        );
        preguntas.put(
            "¿Qué área sientes que necesitas\nmejorar más en tu día a día?",
            new String[]{
                "Constancia", "Energía diaria", "Organización personal",
                "Disciplina", "Motivación", "Descanso",
                "Gestión del tiempo", "Enfoque y concentración",
                "Control de distracciones", "Planificación semanal"
            }
        );
        preguntas.put(
            "¿Cómo te gustaría sentirte\ncada día?",
            new String[]{
                "Más enfocado/a", "Más tranquilo/a", "Más activo/a",
                "Más motivado/a", "Más equilibrado/a", "Más seguro de mí mismo/a",
                "Más productivo/a", "Más positivo/a",
                "Más relajado/a", "Más satisfecho conmigo mismo/a"
            }
        );
        preguntas.put(
            "¿Qué tipo de cambio\nestás buscando?",
            new String[]{
                "Físico", "Mental", "Profesional", "Académico",
                "Emocional", "Social", "Financiero",
                "Creativo", "Espiritual", "De estilo de vida"
            }
        );
    }

    /**
     * Renderiza en pantalla la pregunta correspondiente al índice dado.
     *
     * <p>Actualiza el indicador de paso, la barra de progreso, el texto
     * de la pregunta y genera los botones de opción. Las primeras 6 opciones
     * se muestran de inicio; las 4 restantes quedan ocultas tras "Ver más".</p>
     *
     * @param indice el índice (0-based) de la pregunta a mostrar
     */
    private void mostrarPregunta(int indice) {

        String textoPregunta = listaPreguntas.get(indice);
        String[] opciones = preguntas.get(textoPregunta);

        // ── Actualizar indicador de paso ──
        labelPaso.setText("Pregunta " + (indice + 1) + " de " + listaPreguntas.size());

        // ── Actualizar barra de progreso con puntos ──
        actualizarBarraProgreso(indice);

        // ── Mostrar texto de la pregunta ──
        labelPregunta.setText(textoPregunta);

        // ── Limpiar estado anterior ──
        contenedorOpciones.getChildren().clear();
        botonesOpciones.clear();
        labelValidacion.setText("");
        labelValidacion.setVisible(false);

        // ── Crear botones de opción ──
        for (int i = 0; i < opciones.length; i++) {
            ToggleButton btn = crearBotonOpcion(opciones[i]);
            botonesOpciones.add(btn);

            // Las primeras OPCIONES_INICIALES se muestran, el resto se oculta
            if (i >= OPCIONES_INICIALES) {
                btn.setVisible(false);
                btn.setManaged(false);
            }
            contenedorOpciones.getChildren().add(btn);
        }

        // ── Mostrar "Ver más" solo si hay más de OPCIONES_INICIALES opciones ──
        boolean hayMas = opciones.length > OPCIONES_INICIALES;
        btnVerMas.setVisible(hayMas);
        btnVerMas.setManaged(hayMas);

        // ── Actualizar texto del botón siguiente en la última pregunta ──
        if (indice == listaPreguntas.size() - 1) {
            btnSiguiente.setText("Finalizar");
        } else {
            btnSiguiente.setText("Siguiente →");
        }
    }

    /**
     * Crea un {@link ToggleButton} estilizado para representar una opción de respuesta.
     *
     * <p>El botón cambia de apariencia al seleccionarse (estilo {@code opcion-activa})
     * y aplica la lógica de límite máximo de selecciones al hacer clic.</p>
     *
     * @param texto el texto que mostrará el botón
     * @return el {@link ToggleButton} configurado con su estilo y lógica
     */
    private ToggleButton crearBotonOpcion(String texto) {

        ToggleButton btn = new ToggleButton(texto);
        btn.getStyleClass().add("opcion-btn");
        btn.setWrapText(true);

        // Lógica de selección con límite máximo de 3 opciones
        btn.setOnAction(e -> {
            long seleccionadas = botonesOpciones.stream()
                .filter(ToggleButton::isSelected)
                .count();

            if (btn.isSelected() && seleccionadas > MAX_SELECCIONES) {
                // Desmarcar automáticamente si se supera el límite
                btn.setSelected(false);
                mostrarValidacion("Puedes seleccionar un máximo de " + MAX_SELECCIONES + " opciones.");
            } else {
                // Actualizar el estilo visual del botón
                actualizarEstiloBoton(btn);
                labelValidacion.setVisible(false);
            }

            // Actualizamos estilos de todos los botones por si alguno fue desmarcado
            botonesOpciones.forEach(this::actualizarEstiloBoton);
        });

        return btn;
    }

    /**
     * Aplica o elimina el estilo {@code opcion-activa} en un botón según su estado.
     *
     * @param btn el {@link ToggleButton} cuyo estilo se va a actualizar
     */
    private void actualizarEstiloBoton(ToggleButton btn) {
        if (btn.isSelected()) {
            if (!btn.getStyleClass().contains("opcion-activa")) {
                btn.getStyleClass().add("opcion-activa");
            }
        } else {
            btn.getStyleClass().remove("opcion-activa");
        }
    }

    /**
     * Actualiza la barra de progreso visual con puntos/círculos.
     *
     * <p>El punto correspondiente a la pregunta actual se resalta
     * con el estilo {@code punto-activo}.</p>
     *
     * @param indiceActual el índice de la pregunta actualmente visible
     */
    private void actualizarBarraProgreso(int indiceActual) {
        barraProgreso.getChildren().clear();

        for (int i = 0; i < listaPreguntas.size(); i++) {
            Label punto = new Label("●");
            punto.getStyleClass().add("punto-progreso");
            if (i == indiceActual) {
                punto.getStyleClass().add("punto-activo");
            } else if (i < indiceActual) {
                punto.getStyleClass().add("punto-completado");
            }
            barraProgreso.getChildren().add(punto);
        }
    }

    /**
     * Muestra un mensaje de validación al usuario.
     *
     * @param mensaje el texto del mensaje a mostrar
     */
    private void mostrarValidacion(String mensaje) {
        labelValidacion.setText(mensaje);
        labelValidacion.setVisible(true);
    }

    /**
     * Avanza a la siguiente pregunta al pulsar "Siguiente".
     *
     * <p>Valida que el usuario haya seleccionado al menos 1 opción.
     * Si la validación pasa, guarda las respuestas en BD y avanza.
     * En la última pregunta, marca el onboarding como completado
     * y navega al dashboard.</p>
     *
     * @param event evento generado al pulsar "Siguiente" o "Finalizar"
     */
    @FXML
    public void siguiente(ActionEvent event) {

        // Obtenemos las opciones seleccionadas actualmente
        List<String> seleccionadas = obtenerSeleccionadas();

        // Validación: al menos 1 opción seleccionada
        if (seleccionadas.isEmpty()) {
            mostrarValidacion("Selecciona al menos una opción o pulsa 'Saltar'.");
            return;
        }

        // Guardamos las respuestas en la BD
        guardarRespuestasActuales(seleccionadas);

        // Avanzamos o finalizamos
        if (preguntaActual < listaPreguntas.size() - 1) {
            preguntaActual++;
            mostrarPregunta(preguntaActual);
        } else {
            finalizarOnboarding(event);
        }
    }

    /**
     * Salta la pregunta actual sin guardar ninguna respuesta.
     *
     * <p>Avanza a la siguiente pregunta o finaliza el onboarding si
     * ya estamos en la última. No persiste nada en la BD para esta pregunta.</p>
     *
     * @param event evento generado al pulsar "Saltar"
     */
    @FXML
    public void saltar(ActionEvent event) {

        // Avanzamos sin guardar nada para esta pregunta
        if (preguntaActual < listaPreguntas.size() - 1) {
            preguntaActual++;
            mostrarPregunta(preguntaActual);
        } else {
            finalizarOnboarding(event);
        }
    }

    /**
     * Muestra las opciones adicionales ocultas al pulsar "Ver más".
     *
     * <p>Hace visibles los botones que estaban ocultos a partir del
     * índice {@link #OPCIONES_INICIALES} y oculta el propio botón "Ver más".</p>
     *
     * @param event evento generado al pulsar "Ver más"
     */
    @FXML
    public void verMas(ActionEvent event) {

        // Hacemos visibles los botones ocultos (índices >= OPCIONES_INICIALES)
        for (int i = OPCIONES_INICIALES; i < botonesOpciones.size(); i++) {
            botonesOpciones.get(i).setVisible(true);
            botonesOpciones.get(i).setManaged(true);
        }

        // Ocultamos el botón "Ver más" ya que se han mostrado todas las opciones
        btnVerMas.setVisible(false);
        btnVerMas.setManaged(false);
    }

    /**
     * Obtiene la lista de opciones seleccionadas en la pregunta actual.
     *
     * @return lista de textos de las opciones marcadas por el usuario
     */
    private List<String> obtenerSeleccionadas() {
        List<String> seleccionadas = new ArrayList<>();
        for (ToggleButton btn : botonesOpciones) {
            if (btn.isSelected()) {
                seleccionadas.add(btn.getText());
            }
        }
        return seleccionadas;
    }

    /**
     * Persiste en la BD las respuestas seleccionadas para la pregunta actual.
     *
     * <p>Crea un objeto {@link RespuestaOnboarding} por cada opción
     * seleccionada y los envía al DAO en bloque.</p>
     *
     * @param seleccionadas lista de textos de las opciones seleccionadas
     */
    private void guardarRespuestasActuales(List<String> seleccionadas) {

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) return;

        List<RespuestaOnboarding> respuestas = new ArrayList<>();

        for (String opcion : seleccionadas) {
            // Número de pregunta es 1-based (preguntaActual es 0-based)
            RespuestaOnboarding r = new RespuestaOnboarding(
                usuario.getIdUsuario(),
                preguntaActual + 1,
                opcion
            );
            respuestas.add(r);
        }

        onboardingDAO.guardarRespuestas(respuestas);
    }

    /**
     * Finaliza el onboarding, actualiza el flag en BD y navega al dashboard.
     *
     * <p>Este método se ejecuta tanto al completar todas las preguntas
     * como al saltar la última. Marca {@code onboarding_completado = 1}
     * para que el onboarding no vuelva a mostrarse.</p>
     *
     * @param event el evento de acción para obtener la referencia al {@link Stage}
     */
    private void finalizarOnboarding(ActionEvent event) {

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();

        // Marcamos el onboarding como completado en la BD
        if (usuario != null) {
            onboardingDAO.marcarOnboardingCompletado(usuario.getIdUsuario());
        }

        try {
            // Navegamos al dashboard principal
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/main_view.fxml")
            );
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loader.load()));
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}