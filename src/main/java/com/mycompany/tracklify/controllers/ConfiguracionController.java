package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.dao.PerfilDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Perfil;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.NotificacionScheduler;
import com.mycompany.tracklify.utils.SessionManager;
import com.mycompany.tracklify.utils.TemaService;
import com.mycompany.tracklify.utils.TokenService;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

/**
 * Controlador de la vista de configuración del usuario: perfil, apariencia (tema e idioma),
 * seguridad (contraseña) y acciones sensibles sobre la cuenta.
 *
 * @author Tracklify
 */
public class ConfiguracionController implements Initializable {

    private static final String COLOR_EXITO = "#2E7D32";
    private static final String COLOR_ERROR = "#C62828";
    private static final String COLOR_MEDIA = "#F57F17";

    private final PerfilDAO perfilDAO = new PerfilDAO();
    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final TokenService tokenService = new TokenService();

    @SuppressWarnings("unused")
    private MainViewController host;
    private Perfil perfilActual;
    private Usuario usuarioActual;
    private String temaSeleccionado = "SISTEMA";

    @FXML
    private StackPane circuloFoto;
    @FXML
    private Label labelInicialFoto;
    @FXML
    private ImageView imagePerfil;
    @FXML
    private Button btnCambiarFoto;
    @FXML
    private TextField campoNombre;
    @FXML
    private TextField campoEmail;
    @FXML
    private ComboBox<String> comboGenero;
    @FXML
    private DatePicker dateNacimiento;
    @FXML
    private TextField campoProfesion;
    @FXML
    private Button btnGuardarPerfil;
    @FXML
    private Label labelMensajePerfil;
    @FXML
    private ToggleGroup grupoTema;
    @FXML
    private ToggleButton btnTemaClaro;
    @FXML
    private ToggleButton btnTemaOscuro;
    @FXML
    private ToggleButton btnTemaSistema;
    @FXML
    private ComboBox<String> comboIdioma;
    @FXML
    private Button btnAplicarApariencia;
    @FXML
    private PasswordField campoPasswordActual;
    @FXML
    private PasswordField campoPasswordNueva;
    @FXML
    private PasswordField campoPasswordConfirmar;
    @FXML
    private ProgressBar barraFortaleza;
    @FXML
    private Label labelFortaleza;
    @FXML
    private Button btnCambiarPassword;
    @FXML
    private Label labelMensajePassword;
    @FXML
    private Button btnCerrarSesiones;
    @FXML
    private Button btnEliminarCuenta;

    private final Map<String, String> etiquetaGeneroABd = new LinkedHashMap<>();

    /**
     * Asigna el controlador del marco principal para integraciones futuras (navegación, host).
     *
     * @param host controlador de {@code main_view.fxml}
     */
    public void setHost(MainViewController host) {
        this.host = host;
    }

    /**
     * Inicializa combos, tema, fortaleza de contraseña y carga perfil del usuario en sesión.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle recursos de internacionalización; no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        etiquetaGeneroABd.put("Masculino", "MASCULINO");
        etiquetaGeneroABd.put("Femenino", "FEMENINO");
        etiquetaGeneroABd.put("No binario", "NO_BINARIO");
        etiquetaGeneroABd.put("Prefiero no decir", "PREFIERO_NO_DECIR");

        comboGenero.getItems().setAll(etiquetaGeneroABd.keySet());
        comboIdioma.getItems().setAll("Español", "English");

        btnTemaClaro.setUserData("CLARO");
        btnTemaOscuro.setUserData("OSCURO");
        btnTemaSistema.setUserData("SISTEMA");

        campoPasswordNueva.textProperty().addListener((obs, anterior, nuevo) -> calcularFortaleza(nuevo));

        prepararClipFotoPerfil();

        usuarioActual = SessionManager.getInstancia().getUsuarioActual();
        if (usuarioActual == null) {
            return;
        }

        perfilActual = perfilDAO.obtenerPorUsuario(usuarioActual.getIdUsuario());
        if (perfilActual == null) {
            perfilActual = new Perfil();
            perfilActual.setIdUsuario(usuarioActual.getIdUsuario());
            perfilActual.setNombreUsuario(nombrePorDefectoDesdeEmail(usuarioActual.getEmailUsuario()));
            perfilActual.setTema("SISTEMA");
            perfilActual.setIdioma("es");
            if (!perfilDAO.insertar(perfilActual)) {
                return;
            }
            perfilActual = perfilDAO.obtenerPorUsuario(usuarioActual.getIdUsuario());
        }

        if (perfilActual == null) {
            return;
        }

        rellenarFormularioDesdePerfil();
        temaSeleccionado = perfilActual.getTema() != null ? perfilActual.getTema() : "SISTEMA";
        seleccionarToggleTema(temaSeleccionado);

        Platform.runLater(() -> {
            Scene escena = circuloFoto.getScene();
            if (escena != null) {
                TemaService.aplicar(temaSeleccionado, escena);
            }
        });
    }

    /**
     * Recorta la foto de perfil en forma circular.
     */
    private void prepararClipFotoPerfil() {
        imagePerfil.setFitWidth(80);
        imagePerfil.setFitHeight(80);
        imagePerfil.setPreserveRatio(false);
        Circle clip = new Circle(40);
        clip.setCenterX(40);
        clip.setCenterY(40);
        imagePerfil.setClip(clip);
    }

    /**
     * Rellena los controles con {@link #perfilActual} y el email del usuario (solo lectura).
     */
    private void rellenarFormularioDesdePerfil() {
        campoNombre.setText(Optional.ofNullable(perfilActual.getNombreUsuario()).orElse(""));
        campoEmail.setText(Optional.ofNullable(usuarioActual.getEmailUsuario()).orElse(""));
        campoEmail.setDisable(true);

        String etiquetaGen = generoBdAEtiqueta(perfilActual.getGenero());
        if (etiquetaGen != null && comboGenero.getItems().contains(etiquetaGen)) {
            comboGenero.setValue(etiquetaGen);
        } else {
            comboGenero.getSelectionModel().clearSelection();
        }

        dateNacimiento.setValue(perfilActual.getFechaNacimiento());
        campoProfesion.setText(Optional.ofNullable(perfilActual.getProfesion()).orElse(""));

        String idi = perfilActual.getIdioma() != null ? perfilActual.getIdioma() : "es";
        comboIdioma.setValue("en".equalsIgnoreCase(idi) ? "English" : "Español");

        actualizarInicialFoto();
        cargarImagenPerfilSiExiste();
    }

    private static String nombrePorDefectoDesdeEmail(String email) {
        if (email == null || email.isBlank()) {
            return "Usuario";
        }
        int at = email.indexOf('@');
        String base = at > 0 ? email.substring(0, at) : email;
        return base.trim().isEmpty() ? "Usuario" : base.trim();
    }

    private static String generoBdAEtiqueta(String valorBd) {
        if (valorBd == null || valorBd.isBlank()) {
            return null;
        }
        return switch (valorBd.trim().toUpperCase()) {
            case "MASCULINO" -> "Masculino";
            case "FEMENINO" -> "Femenino";
            case "NO_BINARIO" -> "No binario";
            case "PREFIERO_NO_DECIR" -> "Prefiero no decir";
            default -> null;
        };
    }

    /**
     * Marca el botón de tema según el valor persistido ({@code CLARO}, {@code OSCURO}, {@code SISTEMA}).
     *
     * @param tema código de tema en base de datos
     */
    private void seleccionarToggleTema(String tema) {
        String t = tema != null ? tema.toUpperCase() : "SISTEMA";
        ToggleButton elegido = switch (t) {
            case "CLARO" -> btnTemaClaro;
            case "OSCURO" -> btnTemaOscuro;
            default -> btnTemaSistema;
        };
        grupoTema.selectToggle(elegido);
    }

    /**
     * Actualiza la letra mostrada en el círculo de avatar a partir del nombre actual.
     */
    private void actualizarInicialFoto() {
        String nombre = campoNombre != null ? campoNombre.getText() : perfilActual.getNombreUsuario();
        String inicial = inicialDesdeNombre(nombre);
        labelInicialFoto.setText(inicial);
    }

    private static String inicialDesdeNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return "?";
        }
        return nombre.trim().substring(0, 1).toUpperCase();
    }

    private void cargarImagenPerfilSiExiste() {
        String url = perfilActual.getFotoPerfilUrl();
        if (url == null || url.isBlank()) {
            imagePerfil.setVisible(false);
            labelInicialFoto.setVisible(true);
            return;
        }
        try {
            String uri = url.startsWith("file:") ? url : Path.of(url).toUri().toString();
            Image img = new Image(uri, 160, 160, true, true);
            if (!img.isError()) {
                imagePerfil.setImage(img);
                imagePerfil.setVisible(true);
                labelInicialFoto.setVisible(false);
            } else {
                imagePerfil.setVisible(false);
                labelInicialFoto.setVisible(true);
            }
        } catch (Exception e) {
            imagePerfil.setVisible(false);
            labelInicialFoto.setVisible(true);
        }
    }

    /**
     * Valida y persiste nombre, género, fecha de nacimiento y profesión en {@code perfiles}.
     */
    @FXML
    public void guardarPerfil() {
        labelMensajePerfil.setText("");
        if (perfilActual == null || usuarioActual == null) {
            return;
        }
        String nombre = campoNombre.getText() != null ? campoNombre.getText().trim() : "";
        if (nombre.isEmpty()) {
            labelMensajePerfil.setText("El nombre no puede estar vacío.");
            labelMensajePerfil.setStyle("-fx-text-fill: " + COLOR_ERROR + ";");
            return;
        }

        perfilActual.setNombreUsuario(nombre);
        String genEtiqueta = comboGenero.getValue();
        perfilActual.setGenero(genEtiqueta != null ? etiquetaGeneroABd.get(genEtiqueta) : null);
        perfilActual.setFechaNacimiento(dateNacimiento.getValue());
        String prof = campoProfesion.getText() != null ? campoProfesion.getText().trim() : "";
        perfilActual.setProfesion(prof.isEmpty() ? null : prof);

        if (perfilDAO.actualizar(perfilActual)) {
            labelMensajePerfil.setText("Perfil actualizado correctamente");
            labelMensajePerfil.setStyle("-fx-text-fill: " + COLOR_EXITO + ";");
            actualizarInicialFoto();
        } else {
            labelMensajePerfil.setText("Error al guardar");
            labelMensajePerfil.setStyle("-fx-text-fill: " + COLOR_ERROR + ";");
        }
    }

    /**
     * Guarda tema e idioma en base de datos, aplica el tema a la escena actual y avisa sobre el idioma.
     */
    @FXML
    public void aplicarApariencia() {
        if (perfilActual == null) {
            return;
        }
        ToggleButton sel = (ToggleButton) grupoTema.getSelectedToggle();
        if (sel != null && sel.getUserData() instanceof String s) {
            temaSeleccionado = s;
        }
        perfilActual.setTema(temaSeleccionado);

        String idiLabel = comboIdioma.getValue();
        perfilActual.setIdioma("English".equals(idiLabel) ? "en" : "es");

        if (!perfilDAO.actualizar(perfilActual)) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Apariencia");
            a.setHeaderText(null);
            a.setContentText("No se pudieron guardar las preferencias.");
            a.showAndWait();
            return;
        }

        Scene escena = circuloFoto.getScene();
        System.out.println("Escena: " + escena);
        System.out.println("Stylesheets antes: " + escena.getStylesheets());
        if (escena != null) {
            TemaService.aplicar(temaSeleccionado, escena);
            System.out.println("Stylesheets después: " + escena.getStylesheets());
        }

        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Apariencia");
        info.setHeaderText(null);
        info.setContentText("El cambio de idioma se aplicará al reiniciar la aplicación");
        info.showAndWait();
    }

    /**
     * Cambia la contraseña tras validar coincidencia y fortaleza mínima (al menos «Media»).
     */
    @FXML
    public void cambiarPassword() {
        labelMensajePassword.setText("");
        if (usuarioActual == null) {
            return;
        }
        String actual = campoPasswordActual.getText();
        String nueva = campoPasswordNueva.getText();
        String conf = campoPasswordConfirmar.getText();

        if (actual == null || actual.isBlank()) {
            labelMensajePassword.setText("Indica la contraseña actual.");
            labelMensajePassword.setStyle("-fx-text-fill: " + COLOR_ERROR + ";");
            return;
        }
        if (nueva == null || conf == null || !nueva.equals(conf)) {
            labelMensajePassword.setText("La nueva contraseña y la confirmación no coinciden.");
            labelMensajePassword.setStyle("-fx-text-fill: " + COLOR_ERROR + ";");
            return;
        }
        if (nivelFortaleza(nueva) < 1) {
            labelMensajePassword.setText("La contraseña debe ser al menos de fortaleza media.");
            labelMensajePassword.setStyle("-fx-text-fill: " + COLOR_ERROR + ";");
            return;
        }

        boolean ok = tokenService.cambiarPassword(usuarioActual.getIdUsuario(), actual, nueva);
        if (ok) {
            labelMensajePassword.setText("Contraseña actualizada correctamente.");
            labelMensajePassword.setStyle("-fx-text-fill: " + COLOR_EXITO + ";");
            campoPasswordActual.clear();
            campoPasswordNueva.clear();
            campoPasswordConfirmar.clear();
            calcularFortaleza("");
        } else {
            labelMensajePassword.setText("La contraseña actual no es correcta o no se pudo actualizar.");
            labelMensajePassword.setStyle("-fx-text-fill: " + COLOR_ERROR + ";");
        }
    }

    /**
     * Calcula la fortaleza visual de la contraseña (débil / media / fuerte) y actualiza la barra.
     *
     * @param password texto de la nueva contraseña (puede ser vacío)
     */
    public void calcularFortaleza(String password) {
        barraFortaleza.getStyleClass().removeAll("barra-fortaleza-debil", "barra-fortaleza-media", "barra-fortaleza-fuerte");
        if (password == null || password.isEmpty()) {
            barraFortaleza.setProgress(0);
            labelFortaleza.setText("");
            return;
        }
        int nivel = nivelFortaleza(password);
        if (nivel == 0) {
            barraFortaleza.setProgress(0.33);
            barraFortaleza.getStyleClass().add("barra-fortaleza-debil");
            labelFortaleza.setText("Débil");
            labelFortaleza.setStyle("-fx-text-fill: " + COLOR_ERROR + ";");
        } else if (nivel == 1) {
            barraFortaleza.setProgress(0.66);
            barraFortaleza.getStyleClass().add("barra-fortaleza-media");
            labelFortaleza.setText("Media");
            labelFortaleza.setStyle("-fx-text-fill: " + COLOR_MEDIA + ";");
        } else {
            barraFortaleza.setProgress(1.0);
            barraFortaleza.getStyleClass().add("barra-fortaleza-fuerte");
            labelFortaleza.setText("Fuerte");
            labelFortaleza.setStyle("-fx-text-fill: " + COLOR_EXITO + ";");
        }
    }

    private static int nivelFortaleza(String p) {
        if (p.length() < 6) {
            return 0;
        }
        boolean letras = p.chars().anyMatch(Character::isLetter);
        boolean digitos = p.chars().anyMatch(Character::isDigit);
        boolean simbolo = p.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));
        if (p.length() >= 10 && letras && digitos && simbolo) {
            return 2;
        }
        if (letras && digitos) {
            return 1;
        }
        return 0;
    }

    /**
     * Permite elegir una imagen local, la copia a la carpeta del usuario y actualiza el perfil.
     */
    @FXML
    public void btnCambiarFotoClick() {
        if (perfilActual == null || usuarioActual == null) {
            return;
        }
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Elegir foto de perfil");
        chooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.png", "*.jpg", "*.jpeg", "*.PNG", "*.JPG", "*.JPEG"));
        java.io.File elegido = chooser.showOpenDialog(circuloFoto.getScene().getWindow());
        if (elegido == null) {
            return;
        }
        try {
            Path dir = Paths.get(System.getProperty("user.home"), "Tracklify", "profile_photos");
            Files.createDirectories(dir);
            String ext = obtenerExtension(elegido.getName());
            Path destino = dir.resolve("user_" + usuarioActual.getIdUsuario() + "_avatar" + ext);
            Files.copy(elegido.toPath(), destino, StandardCopyOption.REPLACE_EXISTING);

            perfilActual.setFotoPerfilUrl(destino.toAbsolutePath().toString());
            if (perfilDAO.actualizar(perfilActual)) {
                Image img = new Image(destino.toUri().toString(), 80, 80, false, true);
                imagePerfil.setImage(img);
                imagePerfil.setVisible(true);
                labelInicialFoto.setVisible(false);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String obtenerExtension(String nombre) {
        int i = nombre.lastIndexOf('.');
        return i >= 0 ? nombre.substring(i) : ".png";
    }

    /**
     * Solicita confirmación, verifica la contraseña, desactiva la cuenta y vuelve al login.
     */
    @FXML
    public void btnEliminarCuentaClick() {
        if (usuarioActual == null) {
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Eliminar cuenta");
        confirm.setHeaderText(null);
        confirm.setContentText(
            "¿Estás seguro? Esta acción no se puede deshacer. "
                + "Tu cuenta y todos tus hábitos serán eliminados permanentemente.");
        Optional<ButtonType> resConfirm = confirm.showAndWait();
        if (resConfirm.isEmpty() || resConfirm.get() != ButtonType.OK) {
            return;
        }

        PasswordField campoPasswordConfirmacion = new PasswordField();
        campoPasswordConfirmacion.setPromptText("Contraseña actual");
        campoPasswordConfirmacion.setMaxWidth(Double.MAX_VALUE);
        GridPane panelPassword = new GridPane();
        panelPassword.setHgap(10);
        panelPassword.setVgap(10);
        panelPassword.setMaxWidth(Double.MAX_VALUE);
        panelPassword.add(new Label("Contraseña:"), 0, 0);
        panelPassword.add(campoPasswordConfirmacion, 1, 0);
        GridPane.setHgrow(campoPasswordConfirmacion, javafx.scene.layout.Priority.ALWAYS);

        Alert alertPassword = new Alert(Alert.AlertType.CONFIRMATION);
        alertPassword.setTitle("Verificar identidad");
        alertPassword.setHeaderText(null);
        alertPassword.setContentText("Introduce tu contraseña actual para confirmar la eliminación de la cuenta.");
        alertPassword.getDialogPane().setContent(panelPassword);
        Optional<ButtonType> resPassword = alertPassword.showAndWait();
        if (resPassword.isEmpty() || resPassword.get() != ButtonType.OK) {
            return;
        }

        String password = campoPasswordConfirmacion.getText();
        if (!usuarioDAO.verificarPassword(usuarioActual.getIdUsuario(), password)) {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Eliminar cuenta");
            err.setHeaderText(null);
            err.setContentText("Contraseña incorrecta. No se ha eliminado la cuenta.");
            err.showAndWait();
            return;
        }

        if (usuarioDAO.desactivarCuenta(usuarioActual.getIdUsuario())) {
            try {
                navegarALogin();
            } catch (Exception e) {
                e.printStackTrace();
                Alert err = new Alert(Alert.AlertType.ERROR);
                err.setTitle("Eliminar cuenta");
                err.setHeaderText(null);
                err.setContentText("La cuenta se desactivó pero no se pudo volver al inicio de sesión.");
                err.showAndWait();
            }
        } else {
            Alert err = new Alert(Alert.AlertType.ERROR);
            err.setTitle("Eliminar cuenta");
            err.setHeaderText(null);
            err.setContentText("No se pudo eliminar la cuenta. Inténtalo de nuevo más tarde.");
            err.showAndWait();
        }
    }

    /**
     * Informa y cierra la sesión actual (placeholder hasta sesiones multi-dispositivo).
     */
    @FXML
    public void btnCerrarSesionesClick() {
        Alert info = new Alert(Alert.AlertType.INFORMATION);
        info.setTitle("Sesiones");
        info.setHeaderText(null);
        info.setContentText("Sesión cerrada en todos los dispositivos");
        info.showAndWait();
        try {
            navegarALogin();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Detiene notificaciones, cierra la sesión y muestra la pantalla de inicio de sesión.
     */
    private void navegarALogin() throws Exception {
        NotificacionScheduler.getInstancia().detener();
        SessionManager.getInstancia().cerrarSesion();
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login_view.fxml"));
        Stage stage = (Stage) circuloFoto.getScene().getWindow();
        stage.setScene(new Scene(loader.load(), 800, 480));
    }
}
