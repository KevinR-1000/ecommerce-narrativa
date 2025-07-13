package com.ed.ecommerce.mvcDemo.Model;
// O el paquete donde quieras ponerla

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Una clase de utilidad para encriptar contraseñas usando BCrypt.
 * Puedes ejecutar el método main para obtener el hash de una contraseña específica.
 */
public class EncryptarContraseña {

    public static void main(String[] args) {
        // La contraseña que quieres encriptar.
        // ¡Cámbiala por la que necesites!
        String contrasenaEnTextoPlano = "kevin123";

        // 1. Creamos una instancia del encriptador BCrypt.
        // Es el mismo que usas en tu configuración de Spring Security.
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        // 2. Usamos el método encode() para generar el hash.
        String contrasenaEncriptada = passwordEncoder.encode(contrasenaEnTextoPlano);

        // 3. Imprimimos el resultado en la consola.
        System.out.println("=============================================================");
        System.out.println("Contraseña Original: " + contrasenaEnTextoPlano);
        System.out.println("Contraseña Encriptada (Hash BCrypt): " + contrasenaEncriptada);
        System.out.println("=============================================================");
        System.out.println("Copia y pega la contraseña encriptada en tu script SQL.");
    }
}