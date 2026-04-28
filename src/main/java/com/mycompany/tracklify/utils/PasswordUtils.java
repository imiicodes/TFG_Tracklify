package com.mycompany.tracklify.utils;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtils {

    // 🔐 Encriptar contraseña
    public static String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    // 🔍 Verificar contraseña
    public static boolean checkPassword(String password, String hashedPassword) {
        return BCrypt.checkpw(password, hashedPassword);
    }
}