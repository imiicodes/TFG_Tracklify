package com.mycompany.tracklify.utils;

import com.mycompany.tracklify.dao.ConfiguracionEmailDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import java.time.LocalDateTime;
import java.util.UUID;
import org.mindrot.jbcrypt.BCrypt;

/**
 * Generación y validación de tokens almacenados en {@code configuracion_email}.
 *
 * @author Tracklify
 */
public class TokenService {

    private static final String TIPO_VERIFICACION = "VERIFICACION";
    private static final String TIPO_RESET_PASSWORD = "RESET_PASSWORD";

    private final ConfiguracionEmailDAO configuracionEmailDAO;
    private final UsuarioDAO usuarioDAO;

    public TokenService() {
        this.configuracionEmailDAO = new ConfiguracionEmailDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    public TokenService(ConfiguracionEmailDAO configuracionEmailDAO, UsuarioDAO usuarioDAO) {
        this.configuracionEmailDAO = configuracionEmailDAO;
        this.usuarioDAO = usuarioDAO;
    }

    /**
     * Genera un token de verificación de email (válido 15 minutos).
     *
     * @param idUsuario usuario asociado
     * @return id del registro insertado, o {@code 0} si falla
     */
    public int generarTokenVerificacion(int idUsuario) {
        return insertarToken(idUsuario, TIPO_VERIFICACION);
    }

    /**
     * Genera un token para restablecer contraseña (válido 15 minutos).
     *
     * @param idUsuario usuario asociado
     * @return id del registro insertado, o {@code 0} si falla
     */
    public int generarTokenResetPassword(int idUsuario) {
        return insertarToken(idUsuario, TIPO_RESET_PASSWORD);
    }

    private int insertarToken(int idUsuario, String tipo) {
        String codigo = UUID.randomUUID().toString();
        LocalDateTime expira = LocalDateTime.now().plusMinutes(15);
        return configuracionEmailDAO.insertar(idUsuario, codigo, tipo, expira);
    }

    /**
     * Valida el token y lo marca como usado si aún es válido y no ha expirado.
     *
     * @param idUsuario identificador del usuario
     * @param codigo    UUID del token
     * @param tipo      {@code VERIFICACION} o {@code RESET_PASSWORD}
     * @return {@code true} si el token era válido y se consumió
     */
    public boolean validarToken(int idUsuario, String codigo, String tipo) {
        return configuracionEmailDAO.marcarTokenValidoComoUsado(idUsuario, codigo, tipo);
    }

    /**
     * Restablece la contraseña tras validar el token de tipo {@code RESET_PASSWORD}.
     *
     * @param idUsuario     usuario
     * @param codigo        token recibido
     * @param nuevaPassword contraseña en texto plano (se guarda con BCrypt)
     * @return {@code true} si el token era válido y se actualizó la contraseña
     */
    public boolean resetearPassword(int idUsuario, String codigo, String nuevaPassword) {
        if (!validarToken(idUsuario, codigo, TIPO_RESET_PASSWORD)) {
            return false;
        }
        String hash = BCrypt.hashpw(nuevaPassword, BCrypt.gensalt(10));
        return usuarioDAO.actualizarPassword(idUsuario, hash);
    }

    /**
     * Cambia la contraseña comprobando la actual con BCrypt y guardando el nuevo hash.
     *
     * @param idUsuario       identificador del usuario
     * @param passwordActual  contraseña en texto plano tal como la introduce el usuario
     * @param passwordNueva   nueva contraseña en texto plano
     * @return {@code true} si la contraseña actual era correcta y se actualizó el hash en BD
     */
    public boolean cambiarPassword(int idUsuario, String passwordActual, String passwordNueva) {
        if (passwordActual == null || passwordActual.isBlank() || passwordNueva == null) {
            return false;
        }
        Usuario u = usuarioDAO.obtenerPorId(idUsuario);
        if (u == null || u.getPasswordUsuario() == null) {
            return false;
        }
        if (!BCrypt.checkpw(passwordActual, u.getPasswordUsuario())) {
            return false;
        }
        String hash = BCrypt.hashpw(passwordNueva, BCrypt.gensalt(10));
        return usuarioDAO.actualizarPassword(idUsuario, hash);
    }
}
