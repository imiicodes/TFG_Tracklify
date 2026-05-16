package com.mycompany.tracklify.utils;

import com.mycompany.tracklify.dao.EstadisticaDAO;
import com.mycompany.tracklify.dao.PerfilDAO;
import com.mycompany.tracklify.models.Perfil;
import com.mycompany.tracklify.models.ProgresoSemanal;
import com.mycompany.tracklify.models.ResumenUsuario;
import com.mycompany.tracklify.models.Usuario;
import java.awt.Desktop;
import java.io.File;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

/**
 * Servicio para la generación de informes PDF con JasperReports.
 * Compila el template .jrxml en tiempo de ejecución, lo rellena con datos del usuario
 * desde las vistas SQL y exporta a PDF. El diálogo de guardado se ejecuta siempre en el hilo de JavaFX.
 *
 * @author Tracklify
 * @version 1.0
 */
public class InformeService {

    private final EstadisticaDAO estadisticaDAO = new EstadisticaDAO();
    private final PerfilDAO perfilDAO = new PerfilDAO();

    /**
     * Genera el informe semanal PDF del usuario: prepara el informe (puede ejecutarse fuera del hilo FX),
     * muestra el {@link FileChooser} en el hilo de JavaFX, exporta y abre el PDF si el usuario confirma ruta.
     *
     * @param usuario el usuario para el que se genera el informe; si es {@code null} no hace nada
     * @param stage   el {@link Stage} actual (necesario para el FileChooser); si es {@code null} no hace nada
     */
    public void generarInformeSemanal(Usuario usuario, Stage stage) {
        if (usuario == null || stage == null) {
            return;
        }
        try {
            JasperPrint jasperPrint = construirJasperPrint(usuario);
            Perfil perfil = perfilDAO.obtenerPorUsuario(usuario.getIdUsuario());

            File archivo;
            if (Platform.isFxApplicationThread()) {
                archivo = mostrarDialogoGuardarPdf(stage, perfil, usuario);
            } else {
                CountDownLatch latch = new CountDownLatch(1);
                AtomicReference<File> ref = new AtomicReference<>();
                Platform.runLater(() -> {
                    try {
                        ref.set(mostrarDialogoGuardarPdf(stage, perfil, usuario));
                    } finally {
                        latch.countDown();
                    }
                });
                latch.await();
                archivo = ref.get();
            }

            if (archivo != null) {
                JasperExportManager.exportReportToPdfFile(jasperPrint, archivo.getAbsolutePath());
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(archivo);
                }
            }

        } catch (Exception e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            System.err.println("Error generando informe: " + e.getMessage());
            e.printStackTrace();
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("No se pudo generar el informe");
                alert.setContentText(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                alert.showAndWait();
            });
        }
    }

    /**
     * Compila el JRXML, rellena parámetros y devuelve el {@link JasperPrint} listo para exportar.
     *
     * @param usuario propietario de los datos
     * @return informe rellenado
     * @throws Exception si falla la compilación, la plantilla no existe o el rellenado
     */
    public JasperPrint construirJasperPrint(Usuario usuario) throws Exception {
        Perfil perfil = perfilDAO.obtenerPorUsuario(usuario.getIdUsuario());
        ResumenUsuario resumen = estadisticaDAO.obtenerResumenUsuario(usuario.getIdUsuario());
        List<ProgresoSemanal> progreso = estadisticaDAO.obtenerInformeSemanal(usuario.getIdUsuario());

        RachaService rachaService = new RachaService();
        int rachaMax = progreso.stream()
            .mapToInt(p -> rachaService.calcularRachaActual(p.getIdHabito()))
            .max()
            .orElse(0);

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("NOMBRE_USUARIO", perfil != null && perfil.getNombreUsuario() != null
            ? perfil.getNombreUsuario() : "Usuario");
        parametros.put("EMAIL_USUARIO", usuario.getEmailUsuario());
        parametros.put("SEMANA_ACTUAL", "Semana " + LocalDate.now().get(
            java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()) + " / " + LocalDate.now().getYear());
        double tasa = resumen != null ? resumen.getTasaExitoGlobal() : 0;
        double tasaPct = (tasa >= 0 && tasa <= 1.0) ? tasa * 100.0 : tasa;
        parametros.put("TASA_EXITO", String.format("%.0f", tasaPct));
        parametros.put("RACHA_ACTUAL", rachaMax + (rachaMax == 1 ? " día" : " días"));
        parametros.put("TOTAL_HABITOS", resumen != null
            ? String.valueOf(resumen.getTotalHabitosActivos()) : "0");
        parametros.put("FECHA_GENERACION", LocalDate.now().getDayOfMonth() + " de "
            + LocalDate.now().getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"))
            + " de " + LocalDate.now().getYear());

        List<Map<String, ?>> datos = new ArrayList<>();
        for (ProgresoSemanal p : progreso) {
            Map<String, Object> fila = new HashMap<>();
            fila.put("nombre_habito", p.getNombreHabito());
            fila.put("completados_esta_semana", p.getVecesCompletado());
            fila.put("objetivo_semana", p.getObjetivoVeces());
            fila.put("extras_esta_semana", 0);
            fila.put("segundos_semana", p.getSegundosTotalesSemana());
            datos.add(fila);
        }

        if (datos.isEmpty()) {
            Map<String, Object> filaVacia = new HashMap<>();
            filaVacia.put("nombre_habito", "Sin hábitos registrados esta semana");
            filaVacia.put("completados_esta_semana", 0);
            filaVacia.put("objetivo_semana", 0);
            filaVacia.put("extras_esta_semana", 0);
            filaVacia.put("segundos_semana", 0L);
            datos.add(filaVacia);
        }

        JRMapCollectionDataSource dataSource = new JRMapCollectionDataSource(datos);

        InputStream jrxmlStream = getClass().getResourceAsStream("/reports/informe_semanal_tracklify.jrxml");
        if (jrxmlStream == null) {
            throw new RuntimeException("No se encontró el template del informe en /reports/");
        }
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlStream);
        return JasperFillManager.fillReport(jasperReport, parametros, dataSource);
    }

    /**
     * Muestra el cuadro «Guardar como» para el PDF. Solo debe invocarse desde el hilo de JavaFX.
     *
     * @param stage   ventana propietaria
     * @param perfil  perfil del usuario (nombre sugerido en el fichero)
     * @param usuario usuario (identificador en nombre de archivo)
     * @return archivo elegido o {@code null} si cancela
     */
    private File mostrarDialogoGuardarPdf(Stage stage, Perfil perfil, Usuario usuario) {
        String baseNombre = "usuario";
        if (perfil != null && perfil.getNombreUsuario() != null && !perfil.getNombreUsuario().isBlank()) {
            baseNombre = perfil.getNombreUsuario().replaceAll("\\s+", "_");
        } else if (usuario.getEmailUsuario() != null) {
            baseNombre = usuario.getEmailUsuario().replaceAll("[^a-zA-Z0-9._-]", "_");
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar informe PDF");
        fileChooser.setInitialFileName("informe_tracklify_" + baseNombre + "_semana"
            + LocalDate.now().get(java.time.temporal.WeekFields.ISO.weekOfWeekBasedYear()) + ".pdf");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        return fileChooser.showSaveDialog(stage);
    }
}
