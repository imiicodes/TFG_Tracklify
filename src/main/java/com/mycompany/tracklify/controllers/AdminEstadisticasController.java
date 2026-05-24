package com.mycompany.tracklify.controllers;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.AdminEstadisticaUsuarioFila;
import com.mycompany.tracklify.utils.RachaService;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Sección de estadísticas globales del administrador ({@code admin/admin_estadisticas.fxml}).
 *
 * <p>Las métricas agregadas se obtienen con consultas JDBC directas; la tabla cruza
 * {@code v_resumen_usuario} con el cálculo de racha vía {@link RachaService}.</p>
 *
 * @author Tracklify
 */
public class AdminEstadisticasController implements Initializable {

    @FXML
    private Label labelMetricUsuarios;

    @FXML
    private Label labelMetricHabitos;

    @FXML
    private Label labelMetricTasa;

    @FXML
    private Label labelMetricPopular;

    @FXML
    private TableView<AdminEstadisticaUsuarioFila> tablaEstadisticas;

    @FXML
    private TableColumn<AdminEstadisticaUsuarioFila, String> colUsuario;

    @FXML
    private TableColumn<AdminEstadisticaUsuarioFila, String> colEmail;

    @FXML
    private TableColumn<AdminEstadisticaUsuarioFila, Integer> colHabitosActivos;

    @FXML
    private TableColumn<AdminEstadisticaUsuarioFila, Integer> colRachaMax;

    @FXML
    private TableColumn<AdminEstadisticaUsuarioFila, String> colTasaExito;

    private final RachaService rachaService = new RachaService();

    /**
     * Enlaza columnas de la tabla y carga métricas globales y filas por usuario.
     *
     * @param url            ubicación del FXML
     * @param resourceBundle recursos de internacionalización; no usado
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("nombreUsuario"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("emailUsuario"));
        colHabitosActivos.setCellValueFactory(new PropertyValueFactory<>("habitosActivos"));
        colRachaMax.setCellValueFactory(new PropertyValueFactory<>("rachaMaxima"));
        colTasaExito.setCellValueFactory(new PropertyValueFactory<>("tasaExitoTexto"));

        refrescarTodo();
    }

    /**
     * Vuelve a cargar tarjetas métricas y filas de la tabla desde la base de datos.
     */
    private void refrescarTodo() {
        cargarTarjetasMetricas();
        cargarTablaUsuarios();
    }

    /**
     * Ejecuta las cuatro consultas de agregados globales y actualiza las etiquetas.
     */
    private void cargarTarjetasMetricas() {
        labelMetricUsuarios.setText(consultarTotalUsuariosActivos());
        labelMetricHabitos.setText(consultarTotalHabitosActivos());
        labelMetricTasa.setText(consultarTasaExitoGlobal());
        labelMetricPopular.setText(consultarHabitoMasPopular());
    }

    /**
     * @return total de usuarios con {@code cuenta_activa = 1}, o «Sin datos» si falla
     */
    private String consultarTotalUsuariosActivos() {
        String sql = "SELECT COUNT(*) FROM usuarios WHERE cuenta_activa = 1";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Sin datos";
    }

    /**
     * @return total de hábitos en estado ACTIVO, o «Sin datos»
     */
    private String consultarTotalHabitosActivos() {
        String sql = "SELECT COUNT(*) FROM habitos WHERE estado = 'ACTIVO'";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return String.valueOf(rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Sin datos";
    }

    /**
     * @return media redondeada de {@code tasa_exito_global}, o «Sin datos» si no hay valores
     */
    private String consultarTasaExitoGlobal() {
        String sql = "SELECT ROUND(AVG(tasa_exito_global), 1) FROM v_resumen_usuario";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                double avg = rs.getDouble(1);
                if (rs.wasNull()) {
                    return "Sin datos";
                }
                double porcentaje = (avg >= 0 && avg <= 1.0) ? avg * 100.0 : avg;
                return String.format("%.1f%%", porcentaje);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Sin datos";
    }

    /**
     * @return nombre del hábito con más registros COMPLETADO, o «Sin datos»
     */
    private String consultarHabitoMasPopular() {
        String sql = "SELECT h.nombre_habito, COUNT(*) AS total "
            + "FROM registros_habitos rh "
            + "JOIN habitos h ON h.id_habito = rh.id_habito "
            + "WHERE rh.estado_registro = 'COMPLETADO' "
            + "GROUP BY h.nombre_habito "
            + "ORDER BY total DESC LIMIT 1";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                String nombre = rs.getString("nombre_habito");
                return nombre != null && !nombre.isBlank() ? nombre : "Sin datos";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "Sin datos";
    }

    /**
     * Rellena la tabla con {@code v_resumen_usuario} unido a email y la racha calculada.
     */
    private void cargarTablaUsuarios() {
        List<AdminEstadisticaUsuarioFila> filas = new ArrayList<>();
        String sql = "SELECT v.id_usuario, v.nombre_usuario, u.email_usuario, "
            + "v.total_habitos_activos, v.tasa_exito_global "
            + "FROM v_resumen_usuario v "
            + "INNER JOIN usuarios u ON u.id_usuario = v.id_usuario "
            + "ORDER BY v.nombre_usuario";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int idUsuario = rs.getInt("id_usuario");
                String nombre = rs.getString("nombre_usuario");
                String email = rs.getString("email_usuario");
                int habitos = rs.getInt("total_habitos_activos");
                double tasa = rs.getDouble("tasa_exito_global");
                if (rs.wasNull()) {
                    tasa = 0;
                }
                double porcentaje = (tasa >= 0 && tasa <= 1.0) ? tasa * 100.0 : tasa;
                String tasaTxt = String.format("%.1f%%", porcentaje);

                Integer idHabitoTop = buscarIdHabitoMasCompletados(idUsuario);
                int racha = idHabitoTop != null ? rachaService.calcularRachaActual(idHabitoTop) : 0;

                filas.add(new AdminEstadisticaUsuarioFila(nombre, email, habitos, racha, tasaTxt));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        tablaEstadisticas.setItems(FXCollections.observableArrayList(filas));
    }

    /**
     * Obtiene el {@code id_habito} del usuario con más filas COMPLETADO en {@code registros_habitos}.
     *
     * @param idUsuario propietario de hábitos
     * @return identificador o {@code null} si no hay datos
     */
    private Integer buscarIdHabitoMasCompletados(int idUsuario) {
        String sql = "SELECT rh.id_habito, COUNT(*) AS total "
            + "FROM registros_habitos rh "
            + "JOIN habitos h ON h.id_habito = rh.id_habito "
            + "WHERE h.id_usuario = ? AND rh.estado_registro = 'COMPLETADO' "
            + "GROUP BY rh.id_habito "
            + "ORDER BY total DESC LIMIT 1";
        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_habito");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
