package com.mycompany.tracklify.utils;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utilidades para hashear y verificar contraseñas con BCrypt (coste 12).
 *
 * @author Tracklify
 */
public class PasswordUtils {

    /**
     * Genera un hash BCrypt de la contraseña en texto plano.
     *
     * @param password contraseña sin cifrar
     * @return hash almacenable en base de datos
     */
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    /**
     * Comprueba si la contraseña en texto plano coincide con el hash guardado.
     *
     * @param password       contraseña introducida por el usuario
     * @param hashedPassword hash BCrypt persistido
     * @return {@code true} si la verificación es correcta
     */
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}
