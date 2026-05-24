package com.mycompany.tracklify.utils;

import com.mycompany.tracklify.dao.ConfiguracionEmailDAO;
import com.mycompany.tracklify.dao.UsuarioDAO;
import com.mycompany.tracklify.models.Usuario;
import java.time.LocalDateTime;
import java.util.Map;
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

    /** Crea el servicio con DAOs por defecto para tokens y usuarios. */
    public TokenService() {
        this.configuracionEmailDAO = new ConfiguracionEmailDAO();
        this.usuarioDAO = new UsuarioDAO();
    }

    /**
     * @param configuracionEmailDAO DAO de la tabla {@code configuracion_email}
     * @param usuarioDAO          DAO de usuarios para validar tokens
     */
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

    /**
     * Envía una petición POST a un webhook de n8n con los datos indicados (sin leer el cuerpo de respuesta).
     *
     * @param urlWebhook URL completa del webhook de n8n
     * @param datos      mapa con los parámetros a enviar en el body JSON
     */
    public static void llamarWebhook(String urlWebhook, Map<String, String> datos) {
        try {
            String json = construirJsonWebhook(datos);

            java.net.URL url = new java.net.URL(urlWebhook);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);

            try (java.io.OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            int status = con.getResponseCode();
            System.out.println("Webhook response: " + status);
            con.disconnect();

        } catch (Exception e) {
            System.err.println("Error llamando webhook n8n: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Envía una petición POST al webhook y devuelve el cuerpo de la respuesta como texto.
     *
     * @param urlWebhook URL completa del endpoint (por ejemplo n8n)
     * @param datos      pares clave-valor serializados como objeto JSON plano (valores escapados)
     * @return cuerpo de la respuesta HTTP, o un mensaje de error legible si falla la conexión
     */
    public static String llamarWebhookConRespuesta(String urlWebhook, Map<String, String> datos) {
        try {
            String json = construirJsonWebhook(datos);

            java.net.URL url = new java.net.URL(urlWebhook);
            java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            con.setConnectTimeout(10000);
            con.setReadTimeout(30000);

            try (java.io.OutputStream os = con.getOutputStream()) {
                os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }

            StringBuilder respuesta = new StringBuilder();
            try (java.io.BufferedReader br = new java.io.BufferedReader(
                new java.io.InputStreamReader(con.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)
            )) {
                String linea;
                while ((linea = br.readLine()) != null) {
                    respuesta.append(linea);
                }
            }
            con.disconnect();
            return respuesta.toString();

        } catch (Exception e) {
            System.err.println("Error webhook: " + e.getMessage());
            return "Lo siento, no puedo responder ahora mismo.";
        }
    }

    /**
     * Construye un JSON {@code {"clave":"valor",...}} escapando comillas y saltos en los valores.
     *
     * @param datos mapa de campos; si está vacío devuelve {@code {}}
     * @return cadena JSON UTF-8
     */
    private static String construirJsonWebhook(Map<String, String> datos) {
        if (datos == null || datos.isEmpty()) {
            return "{}";
        }
        StringBuilder json = new StringBuilder("{");
        datos.forEach((k, v) -> {
            String valor = v != null ? v : "";
            json.append("\"").append(escaparJson(k)).append("\":\"")
                .append(escaparJson(valor)).append("\",");
        });
        json.deleteCharAt(json.length() - 1);
        json.append("}");
        return json.toString();
    }

    private static String escaparJson(String s) {
        return s.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\r", "\\r")
            .replace("\n", "\\n")
            .replace("\t", "\\t");
    }
}
