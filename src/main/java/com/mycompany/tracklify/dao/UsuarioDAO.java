package com.mycompany.tracklify.dao;

import com.mycompany.tracklify.database.ConexionBD;
import com.mycompany.tracklify.models.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Clase de acceso a datos (DAO) para la entidad {@link Usuario}.
 *
 * <p>Proporciona operaciones CRUD sobre la tabla {@code usuarios}
 * de la base de datos, así como métodos de autenticación y
 * gestión de roles para el sistema Tracklify.</p>
 *
 * @author Tracklify
 * @version 1.0
 * @see Usuario
 * @see ConexionBD
 */
public class UsuarioDAO {

    /**
     * Autentica a un usuario mediante su email y contraseña.
     *
     * @param email    el correo electrónico del usuario
     * @param password la contraseña del usuario en texto plano
     * @return el {@link Usuario} autenticado, o {@code null} si las credenciales son incorrectas
     */
    public Usuario login(String email, String password) {

        String sql = "SELECT * FROM usuarios WHERE email_usuario = ? AND password_usuario = ?";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setEmailUsuario(rs.getString("email_usuario"));
                u.setPasswordUsuario(rs.getString("password_usuario"));
                u.setRolId(rs.getInt("rol_id"));
                return u;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Registra un nuevo usuario en la base de datos.
     *
     * <p>CORRECCIÓN CRÍTICA: incluye {@code onboarding_completado = 0}
     * explícitamente en el INSERT para garantizar que el flag quede
     * en {@code 0} y no en {@code NULL}, lo que causaría que el
     * {@link OnboardingDAO#haCompletadoOnboarding(int)} devolviera
     * {@code false} incorrectamente al no encontrar el valor esperado.</p>
     *
     * @param usuario el {@link Usuario} con los datos a insertar
     * @return {@code true} si el registro fue exitoso, {@code false} si el email ya estaba en uso
     */
    public boolean registrar(Usuario usuario) {

        // IMPORTANTE: incluimos onboarding_completado = 0 explícitamente
        // para evitar que quede NULL y rompa la comprobación del LoginController
        String sql = "INSERT INTO usuarios "
                   + "(nombre_usuario, email_usuario, password_usuario, rol_id, onboarding_completado) "
                   + "VALUES (?, ?, ?, ?, 0)";

        try (Connection conexion = ConexionBD.conectar();
             PreparedStatement ps = conexion.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getEmailUsuario());
            ps.setString(3, usuario.getPasswordUsuario());
            ps.setInt(4, usuario.getRolId());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            // El email ya existe en la base de datos (restricción UNIQUE KEY)
            return false;
        }
    }

    /**
     * Obtiene todos los usuarios registrados en el sistema.
     *
     * @return lista de todos los {@link Usuario} registrados, o lista vacía si no hay ninguno
     */
    public List<Usuario> obtenerTodos() {

        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios ORDER BY nombre_usuario ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setEmailUsuario(rs.getString("email_usuario"));
                u.setPasswordUsuario(rs.getString("password_usuario"));
                u.setRolId(rs.getInt("rol_id"));
                usuarios.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuarios;
    }

    /**
     * Busca un usuario por su identificador único.
     *
     * @param idUsuario el identificador del usuario a buscar
     * @return el {@link Usuario} encontrado, o {@code null} si no existe
     */
    public Usuario obtenerPorId(int idUsuario) {

        String sql = "SELECT * FROM usuarios WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setEmailUsuario(rs.getString("email_usuario"));
                u.setPasswordUsuario(rs.getString("password_usuario"));
                u.setRolId(rs.getInt("rol_id"));
                return u;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Actualiza los datos de un usuario existente.
     *
     * @param usuario el {@link Usuario} con los nuevos datos
     * @return {@code true} si la actualización fue exitosa, {@code false} en caso contrario
     */
    public boolean actualizar(Usuario usuario) {

        String sql = "UPDATE usuarios SET nombre_usuario = ?, email_usuario = ?, "
                   + "password_usuario = ? WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getEmailUsuario());
            ps.setString(3, usuario.getPasswordUsuario());
            ps.setInt(4, usuario.getIdUsuario());

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Elimina un usuario del sistema por su identificador.
     *
     * @param idUsuario el identificador del usuario a eliminar
     * @return {@code true} si se eliminó correctamente, {@code false} en caso contrario
     */
    public boolean eliminar(int idUsuario) {

        String sql = "DELETE FROM usuarios WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Cambia el rol de un usuario.
     *
     * @param idUsuario  el identificador del usuario
     * @param nuevoRolId el nuevo rol ({@code 1} = usuario, {@code 2} = administrador)
     * @return {@code true} si el cambio fue exitoso, {@code false} en caso contrario
     */
    public boolean cambiarRol(int idUsuario, int nuevoRolId) {

        String sql = "UPDATE usuarios SET rol_id = ? WHERE id_usuario = ?";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, nuevoRolId);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Obtiene todos los usuarios que tienen un rol específico.
     *
     * @param rolId el identificador del rol a filtrar
     * @return lista de {@link Usuario} con ese rol
     */
    public List<Usuario> obtenerPorRol(int rolId) {

        List<Usuario> usuarios = new ArrayList<>();
        String sql = "SELECT * FROM usuarios WHERE rol_id = ? ORDER BY nombre_usuario ASC";

        try (Connection con = ConexionBD.conectar();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, rolId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setEmailUsuario(rs.getString("email_usuario"));
                u.setPasswordUsuario(rs.getString("password_usuario"));
                u.setRolId(rs.getInt("rol_id"));
                usuarios.add(u);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return usuarios;
    }
}