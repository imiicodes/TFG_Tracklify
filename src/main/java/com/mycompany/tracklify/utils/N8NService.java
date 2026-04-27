package com.mycompany.tracklify.utils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Clase encargada de enviar eventos a n8n mediante HTTP.
 */
public class N8NService {

    private static final String WEBHOOK_URL = "http://localhost:5678/webhook/tracklify-onboarding";

    /**
     * Envía respuestas del onboarding a n8n.
     *
     * @param usuarioId ID del usuario
     * @param respuestas Array de respuestas seleccionadas
     */
    public static void enviarOnboarding(int usuarioId, String[] respuestas) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            // Convertir array a JSON manual
            StringBuilder respuestasJson = new StringBuilder("[");
            for (int i = 0; i < respuestas.length; i++) {
                respuestasJson.append("\"").append(respuestas[i]).append("\"");
                if (i < respuestas.length - 1) {
                    respuestasJson.append(",");
                }
            }
            respuestasJson.append("]");

            String jsonInput = "{"
                    + "\"usuario_id\":" + usuarioId + ","
                    + "\"tipo\":\"onboarding\","
                    + "\"respuestas\":" + respuestasJson
                    + "}";

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Respuesta n8n: " + responseCode);

        } catch (Exception e) {
            System.out.println("Error enviando a n8n:");
            e.printStackTrace();
        }
    }

    /**
     * Envía texto de hábito manual a n8n.
     */
    public static void enviarHabito(int usuarioId, String texto) {
        try {
            URL url = new URL(WEBHOOK_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInput = "{"
                    + "\"usuario_id\":" + usuarioId + ","
                    + "\"tipo\":\"habito_manual\","
                    + "\"texto\":\"" + texto + "\""
                    + "}";

            OutputStream os = conn.getOutputStream();
            os.write(jsonInput.getBytes());
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            System.out.println("Respuesta n8n: " + responseCode);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}