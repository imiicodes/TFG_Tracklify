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
import com.mycompany.tracklify.utils.RachaService;
import com.mycompany.tracklify.utils.SessionManager;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

/**
 * Controlador del panel central del dashboard ({@code dashboard_view.fxml}).
 *
 * <p>La barra lateral y el marco de la aplicación los gestiona {@link MainViewController};
 * este controlador solo rellena hábitos de hoy, estadísticas y recordatorios, y delega
 * la apertura del asistente de creación o edición en el host.</p>
 *
 * @author Tracklify
 */
public class DashboardViewController implements Initializable {

    @FXML
    private Label labelBienvenida;

    @FXML
    private Label labelTareasActivas;

    @FXML
    private Label labelPorcentaje;

    @FXML
    private Label labelRacha;

    @FXML
    private VBox contenedorTareas;

    @FXML
    private VBox contenedorRecordatorios;

    private final HabitoDAO habitoDAO = new HabitoDAO();
    private final EstadisticaDAO estadisticaDAO = new EstadisticaDAO();
    private final PerfilDAO perfilDAO = new PerfilDAO();
    private final RachaService rachaService = new RachaService();
    private final NotificacionDAO notificacionDAO = new NotificacionDAO();
    private final RegistroHabitoDAO registroHabitoDAO = new RegistroHabitoDAO();

    /** Contenedor principal que inyecta las dependencias de navegación. */
    private MainViewController host;

    /**
     * Asigna el controlador del marco principal para abrir creación o edición de hábitos.
     *
     * @param host controlador de {@code main_view.fxml}, o {@code null} si aún no está enlazado
     */
    public void setHost(MainViewController host) {
        this.host = host;
    }

    /**
     * Carga saludo, tareas del día, estadísticas y recordatorios del usuario en sesión.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle recursos de internacionalización; no usado
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
        }

        cargarProximosRecordatorios();
    }

    /**
     * Solicita al host abrir el asistente de alta de hábito (retorno al dashboard).
     *
     * @param event evento del botón "+ Crear nuevo hábito"
     */
    @FXML
    public void onCrearNuevoHabito(ActionEvent event) {
        if (host != null) {
            host.abrirCrearHabito(false);
        }
    }

    private void cargarTareas(int idUsuario) {
        List<Habito> habitos = habitoDAO.obtenerPorUsuario(idUsuario);
        contenedorTareas.getChildren().clear();

        if (habitos.isEmpty()) {
            contenedorTareas.getChildren().add(crearBloqueHabitosVacios());
        } else {
            for (Habito habito : habitos) {
                contenedorTareas.getChildren().add(crearFilaHabito(habito));
            }
        }
    }

    private void cargarProximosRecordatorios() {

        if (contenedorRecordatorios == null) {
            return;
        }
        contenedorRecordatorios.getChildren().clear();

        Usuario usuario = SessionManager.getInstancia().getUsuarioActual();
        if (usuario == null) {
            return;
        }

        List<Notificacion> todas = notificacionDAO.obtenerPorUsuario(usuario.getIdUsuario());

        todas.stream()
            .filter(n -> !n.isEstadoNotificacion())
            .filter(n -> n.getFechaProgramada() != null)
            .limit(5)
            .forEach(n -> {
                HBox fila = new HBox(12);
                fila.setAlignment(Pos.CENTER_LEFT);
                fila.setStyle("-fx-padding: 5 0 5 0;");

                String horaTexto = n.getFechaProgramada() != null
                    ? String.format("%02d:%02d",
                        n.getFechaProgramada().getHour(),
                        n.getFechaProgramada().getMinute())
                    : "--:--";

                Label hora = new Label(horaTexto);
                hora.setStyle("-fx-font-size: 11px; -fx-text-fill: #93588F; "
                    + "-fx-font-weight: bold; -fx-min-width: 48;");

                Label desc = new Label(n.getMensajeNotificacion());
                desc.setStyle("-fx-font-size: 11px; -fx-text-fill: #7E6A8A;");
                desc.setWrapText(true);

                fila.getChildren().addAll(hora, desc);
                contenedorRecordatorios.getChildren().add(fila);
            });

        if (contenedorRecordatorios.getChildren().isEmpty()) {
            Label sinRecordatorios = new Label("No hay recordatorios pendientes.");
            sinRecordatorios.setStyle("-fx-text-fill: #C4AADB; -fx-font-size: 11px;");
            contenedorRecordatorios.getChildren().add(sinRecordatorios);
        }
    }

    private void cargarEstadisticas(int idUsuario) {
        refrescarEstadisticas(idUsuario);
    }

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
            labelRacha.setText(maxRacha + (maxRacha == 1 ? " día" : " días"));
        } else {
            List<Habito> hlist = habitoDAO.obtenerPorUsuario(idUsuario);
            labelTareasActivas.setText(String.valueOf(hlist.size()));
            labelPorcentaje.setText("0%");
            labelRacha.setText(maxRacha + (maxRacha == 1 ? " día" : " días"));
        }
    }

    /**
     * Construye el mensaje y la acción cuando el usuario aún no tiene hábitos en el dashboard.
     *
     * @return {@link VBox} centrado con texto y botón hacia el asistente de creación
     */
    private VBox crearBloqueHabitosVacios() {
        VBox caja = new VBox(14);
        caja.setAlignment(Pos.CENTER);
        caja.setPadding(new Insets(40, 16, 40, 16));
        caja.setMaxWidth(Double.MAX_VALUE);

        Label mensaje = new Label("Todavía no tienes hábitos. ¡Crea tu primero!");
        mensaje.setStyle("-fx-text-fill: #6B4A6E; -fx-font-size: 14px;");
        mensaje.setWrapText(true);
        mensaje.setTextAlignment(TextAlignment.CENTER);
        mensaje.setMaxWidth(420);

        Button btnPrimero = new Button("+ Crear mi primer hábito");
        btnPrimero.getStyleClass().add("btn-primary");
        btnPrimero.setOnAction(e -> {
            if (host != null) {
                host.abrirCrearHabito(false);
            }
        });

        caja.getChildren().addAll(mensaje, btnPrimero);
        return caja;
    }

    /**
     * Aplica un destello de opacidad breve a la fila del hábito al marcarlo como completado.
     *
     * @param fila contenedor visual de la fila del hábito
     */
    private void animarFilaCompletado(HBox fila) {
        FadeTransition ft = new FadeTransition(Duration.millis(300), fila);
        ft.setFromValue(1.0);
        ft.setToValue(0.7);
        ft.setCycleCount(2);
        ft.setAutoReverse(true);
        ft.play();
    }

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
            if (host != null) {
                host.abrirEditorHabito(habito, false);
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
            if (deseado) {
                animarFilaCompletado(fila);
            }
        });

        fila.getChildren().addAll(checkbox, nombre, badge, btnMenu);
        return fila;
    }

    private String construirFrecuencia(String frecuencia, String dia, int hora, int minutos) {
        String horaStr = String.format("%02d:%02d", hora, minutos);
        if (dia != null && !dia.isEmpty()) {
            return frecuencia + " · " + dia + " · " + horaStr;
        }
        return frecuencia + " · " + horaStr;
    }

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
        spH.setPrefWidth(72);
        spH.setEditable(true);
        Label sep = new Label(":");
        sep.setStyle("-fx-font-size: 14px; -fx-text-fill: #3A2B3F;");
        Spinner<Integer> spM = new Spinner<>(
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0)
        );
        spM.setPrefWidth(72);
        spM.setEditable(true);
        filaHora.getChildren().addAll(spH, sep, spM);
        Label lblD = estiloLabel("Día:");
        ComboBox<String> cbDia = new ComboBox<>();
        cbDia.setMaxWidth(Double.MAX_VALUE);
        lblD.setVisible(false);
        lblD.setManaged(false);
        cbDia.setVisible(false);
        cbDia.setManaged(false);

        cbFrec.setOnAction(e -> {
            String frec = cbFrec.getValue();
            boolean mostrar = frec.equals("Semanal") || frec.equals("Mensual");
            lblD.setVisible(mostrar);
            lblD.setManaged(mostrar);
            cbDia.setVisible(mostrar);
            cbDia.setManaged(mostrar);
            cbDia.getItems().clear();
            if (frec.equals("Semanal")) {
                cbDia.getItems().addAll("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo");
            } else if (frec.equals("Mensual")) {
                for (int i = 1; i <= 31; i++) {
                    cbDia.getItems().add("Día " + i);
                }
            }
            if (!cbDia.getItems().isEmpty()) {
                cbDia.setValue(cbDia.getItems().get(0));
            }
        });

        contenido.getChildren().addAll(lblN, tfNombre, lblF, cbFrec, lblH, filaHora, lblD, cbDia);
        dialog.getDialogPane().setContent(contenido);
        dialog.getDialogPane().setStyle("-fx-background-color: white;");

        Optional<ButtonType> res = dialog.showAndWait();
        if (res.isPresent() && res.get() == guardarBtn) {
            String nuevoNombre = tfNombre.getText().trim();
            if (nuevoNombre.isEmpty()) {
                return;
            }
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

    private void aplicarEstiloFilaHabitoCompletado(Label nombre, Label badge, boolean hecho) {
        if (hecho) {
            nombre.setStyle("-fx-strikethrough: true; -fx-text-fill: #C4AADB;");
            badge.setOpacity(0.4);
        } else {
            nombre.setStyle("");
            badge.setOpacity(1.0);
        }
    }

    private Label estiloLabel(String texto) {
        Label l = new Label(texto);
        l.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #93588F;");
        return l;
    }
}
