package com.mycompany.tracklify.utils;

import javafx.scene.Scene;

/**
 * Aplica hojas de estilo de tema claro u oscuro sobre la {@link Scene} actual.
 *
 * <p>Las rutas de recursos son {@code /css/tema_claro.css} y {@code /css/tema_oscuro.css}.
 * Se eliminan hojas cuyo nombre contiene {@code "tema_"} antes de añadir la nueva.</p>
 *
 * @author Tracklify
 */
public final class TemaService {

    private static final String CSS_CLARO = "/css/tema_claro.css";
    private static final String CSS_OSCURO = "/css/tema_oscuro.css";

    private TemaService() {
    }

    /**
     * Añade la hoja de tema correspondiente a la escena, quitando cualquier hoja previa cuyo nombre contenga {@code "tema_"}.
     *
     * @param tema  {@code CLARO}, {@code OSCURO} o {@code SISTEMA} (por defecto se aplica el tema claro)
     * @param scene escena raíz de la ventana
     */
    public static void aplicar(String tema, Scene scene) {
        if (scene == null || scene.getStylesheets() == null) {
            return;
        }
        scene.getStylesheets().removeIf(s -> s != null && s.contains("tema_"));
        String ruta;
        if ("OSCURO".equalsIgnoreCase(tema)) {
            ruta = CSS_OSCURO;
        } else if ("SISTEMA".equalsIgnoreCase(tema)) {
            ruta = CSS_CLARO;
        } else {
            ruta = CSS_CLARO;
        }
        var url = TemaService.class.getResource(ruta);
        if (url != null) {
            scene.getStylesheets().add(url.toExternalForm());
        }
    }
}
