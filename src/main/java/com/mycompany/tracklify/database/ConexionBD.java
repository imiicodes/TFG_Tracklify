package com.mycompany.tracklify.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Gestiona la conexión JDBC a MySQL leyendo URL, usuario y contraseña desde
 * {@code config.properties} en el classpath.
 *
 * @author Tracklify
 */
public class ConexionBD {

    private static final String URL;
    private static final String USUARIO;
    private static final String PASSWORD;

    static {
        Properties props = new Properties();
        try (InputStream in = ConexionBD.class.getResourceAsStream("/config.properties")) {
            if (in == null) {
                throw new IllegalStateException("No se encontró config.properties en el classpath");
            }
            props.load(in);
            URL = props.getProperty("db.url");
            USUARIO = props.getProperty("db.user");
            PASSWORD = props.getProperty("db.password");
        } catch (IOException e) {
            throw new IllegalStateException("Error al cargar config.properties", e);
        }
    }

    /**
     * Abre una conexión con la base de datos usando las credenciales cargadas al iniciar la clase.
     *
     * @return conexión activa, o {@code null} si falla el intento (se registra el error en consola)
     */
    public static Connection conectar() {
        Connection conexion = null;

        try {
            conexion = DriverManager.getConnection(URL, USUARIO, PASSWORD);
           // System.out.println("Conexión a la base de datos correcta");
        } catch (SQLException e) {
            System.out.println("Error al conectar con la base de datos");
            e.printStackTrace();
        }

        return conexion;
    }
}
