package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Perfil;
import com.mycompany.tracklify.models.Usuario;
import com.mycompany.tracklify.utils.TokenService;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Clase de acceso a datos (DAO) para la entidad {@link Usuario}.
 *
 * @author Tracklify
 * @version 1.0
 */
public class UsuarioDAO {

    private static LocalDateTime getTsOrNull(ResultSet rs, String col) throws SQLException {
        Timestamp t = rs.getTimestamp(col);
        return t == null ? null : t.toLocalDateTime();
    }

    private static boolean getBool01(ResultSet rs, String col, boolean siNull) throws SQLException {
        int v = rs.getInt(col);
        return rs.wasNull() ? siNull : (v != 0);
    }

    private static Usuario mapUsuario(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setIdUsuario(rs.getInt("id_usuario"));
        u.setEmailUsuario(rs.getString("email_usuario"));
        u.setPasswordUsuario(rs.getString("password_usuario"));
        u.setRolId(rs.getInt("rol_id"));
        u.setOnboardingCompletado(getBool01(rs, "onboarding_completado", false));
        u.setEmailConfirmado(getBool01(rs, "email_confirmado", false));
        u.setCuentaActiva(getBool01(rs, "cuenta_activa", true));
        u.setFechaRegistro(getTsOrNull(rs, "fecha_registro"));
        u.setFechaConfirmacionEmail(getTsOrNull(rs, "fecha_confirmacion_email"));
        u.setFechaUltModificacion(getTsOrNull(rs, "fecha_ult_modificacion"));
        u.setFechaUltAcceso(getTsOrNull(rs, "fecha_ult_acceso"));
        return u;
    }

    public Usuario login(String email, String password) {

        String sql = "SELECT * FROM usuarios WHERE email_usuario = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashGuardado = rs.getString("password_usuario");

                if (BCrypt.checkpw(password, hashGuardado)) {
                    Usuario u = mapUsuario(rs);
                    u.setPasswordUsuario(hashGuardado);
                    return u;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Comprueba si el correo ya está registrado (ignora mayúsculas y espacios alrededor).
     *
     * @param email correo a comprobar
     * @return {@code true} si existe un usuario con ese email
     */
    public boolean existeEmail(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        String sql = "SELECT 1 FROM usuarios WHERE LOWER(TRIM(email_usuario)) = LOWER(?) LIMIT 1";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email.trim());
            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Registra un usuario y crea su perfil básico con el nombre indicado.
     *
     * @param usuario      datos de acceso (email, contraseña hasheada, rol)
     * @param nombrePerfil nombre visible guardado en {@code perfiles}
     * @return id del usuario insertado, o {@code 0} si falla (p. ej. email duplicado)
     */
    public int registrar(Usuario usuario, String nombrePerfil) {
        System.out.println("REGISTRAR llamado para: " + usuario.getEmailUsuario());
        String sql = "INSERT INTO usuarios (email_usuario, password_usuario, rol_id, onboarding_completado, "
            + "email_confirmado, cuenta_activa, fecha_registro, fecha_ult_modificacion) "
            + "VALUES (?, ?, ?, 0, 0, 1, ?, ?)";

        LocalDateTime ahora = LocalDateTime.now();

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, usuario.getEmailUsuario());
            ps.setString(2, usuario.getPasswordUsuario());
            ps.setInt(3, usuario.getRolId());
            ps.setTimestamp(4, Timestamp.valueOf(ahora));
            ps.setTimestamp(5, Timestamp.valueOf(ahora));

            int filas = ps.executeUpdate();
            if (filas <= 0) {
                return 0;
            }

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) {
                    return 0;
                }
                int id = keys.getInt(1);

                Perfil perfil = new Perfil();
                perfil.setIdUsuario(id);
                perfil.setNombreUsuario(nombrePerfil != null ? nombrePerfil : "");

                PerfilDAO perfilDAO = new PerfilDAO();
                if (!perfilDAO.insertar(perfil)) {
                    return 0;
                }
                System.out.println("Llamando webhook bienvenida para: " + usuario.getEmailUsuario());
                TokenService.llamarWebhook(
                    "http://localhost:5678/webhook/bienvenida",
                    Map.of("email", usuario.getEmailUsuario(), "nombre", nombrePerfil)
                );
                return id;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        

    }

    /**
     * Inserta un intento de login en la tabla de auditoría, incluyendo la marca temporal.
     *
     * <p>La tabla {@code auditoria_login} debe disponer de la columna {@code fecha_intento}
     * (por ejemplo {@code TIMESTAMP} con valor por defecto o explícito {@code NOW()}).</p>
     *
     * @param email     correo usado en el intento
     * @param ip        dirección IP del cliente
     * @param resultado código de resultado (p. ej. {@code EXITO}, {@code FALLO_PASSWORD})
     */
    public void registrarIntento(String email, String ip, String resultado) {

        String sql = "INSERT INTO auditoria_login (email_intento, ip_address, resultado, fecha_intento) "
            + "VALUES (?, ?, ?, NOW())";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, ip);
            ps.setString(3, resultado);
            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Usuario> obtenerTodos() {

        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.* FROM usuarios u "
            + "LEFT JOIN perfiles p ON p.id_usuario = u.id_usuario "
            + "ORDER BY COALESCE(p.nombre_usuario, u.email_usuario) ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                usuarios.add(mapUsuario(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuarios;
    }

    /**
     * Obtiene un usuario por su clave primaria.
     *
     * @param idUsuario identificador {@code id_usuario}
     * @return entidad mapeada o {@code null} si no existe o falla la consulta
     */
    public Usuario obtenerPorId(int idUsuario) {

        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return mapUsuario(rs);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actualiza solo la contraseña (hash BCrypt) y la fecha de última modificación.
     */
    public boolean actualizarPassword(int idUsuario, String passwordHash) {

        String sql = "UPDATE usuarios SET password_usuario = ?, fecha_ult_modificacion = ? WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, passwordHash);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, idUsuario);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean actualizar(Usuario usuario) {

        String sql = "UPDATE usuarios SET email_usuario = ?, password_usuario = ?, "
            + "fecha_ult_modificacion = ? WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getEmailUsuario());
            ps.setString(2, usuario.getPasswordUsuario());
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, usuario.getIdUsuario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina el usuario y, en la misma transacción, las filas dependientes habituales
     * (descansos, registros de hábitos, notificaciones, hábitos, tokens de email, onboarding, baneos y perfil).
     *
     * @param idUsuario clave del usuario a borrar
     * @return {@code true} si se eliminó la fila de {@code usuarios}
     */
    public boolean eliminar(int idUsuario) {

        try (Connection con = ConexionBD.conectar()) {
            con.setAutoCommit(false);
            try {
                ejecutarUpdate(con,
                    "DELETE d FROM descansos d "
                        + "INNER JOIN registros_habitos r ON d.id_registro = r.id_registro "
                        + "INNER JOIN habitos h ON r.id_habito = h.id_habito WHERE h.id_usuario = ?",
                    idUsuario);
                ejecutarUpdate(con,
                    "DELETE r FROM registros_habitos r "
                        + "INNER JOIN habitos h ON r.id_habito = h.id_habito WHERE h.id_usuario = ?",
                    idUsuario);
                ejecutarUpdate(con,
                    "DELETE n FROM notificaciones n "
                        + "LEFT JOIN habitos h ON n.id_habito = h.id_habito "
                        + "WHERE n.id_usuario = ? OR h.id_usuario = ?",
                    idUsuario, idUsuario);
                ejecutarUpdate(con, "DELETE FROM habitos WHERE id_usuario = ?", idUsuario);
                ejecutarUpdate(con, "DELETE FROM configuracion_email WHERE id_usuario = ?", idUsuario);
                ejecutarUpdate(con, "DELETE FROM onboarding_respuestas WHERE id_usuario = ?", idUsuario);
                ejecutarUpdate(con, "DELETE FROM baneos_usuario WHERE id_usuario = ?", idUsuario);
                ejecutarUpdate(con, "DELETE FROM perfiles WHERE id_usuario = ?", idUsuario);

                try (PreparedStatement ps = con.prepareStatement("DELETE FROM usuarios WHERE id_usuario = ?")) {
                    ps.setInt(1, idUsuario);
                    int filas = ps.executeUpdate();
                    con.commit();
                    return filas > 0;
                }
            } catch (SQLException e) {
                con.rollback();
                e.printStackTrace();
                return false;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void ejecutarUpdate(Connection con, String sql, int... params) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setInt(i + 1, params[i]);
            }
            ps.executeUpdate();
        }
    }

    /**
     * Cambia el rol de un usuario ({@code 1} = administrador, {@code 2} = usuario estándar).
     */
    public boolean cambiarRol(int idUsuario, int nuevoRolId) {

        String sql = "UPDATE usuarios SET rol_id = ?, fecha_ult_modificacion = ? WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, nuevoRolId);
            ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(3, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Usuario> obtenerPorRol(int rolId) {

        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT u.* FROM usuarios u "
            + "LEFT JOIN perfiles p ON p.id_usuario = u.id_usuario "
            + "WHERE u.rol_id = ? ORDER BY COALESCE(p.nombre_usuario, u.email_usuario) ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, rolId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                usuarios.add(mapUsuario(rs));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuarios;
    }
    
}
