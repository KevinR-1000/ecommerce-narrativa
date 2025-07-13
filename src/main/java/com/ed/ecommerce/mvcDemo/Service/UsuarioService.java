package com.ed.ecommerce.mvcDemo.Service;

import com.ed.ecommerce.mvcDemo.Model.Usuario;
import com.ed.ecommerce.mvcDemo.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    // Dependemos ÚNICAMENTE de la interfaz de JPA y del encriptador.
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean registrar(Usuario usuario) {
        // 1. Verificar si el correo ya existe usando el método que creamos en la interfaz.
        if (usuarioRepository.findByCorreo(usuario.getCorreo()).isPresent()) {
            return false; // El correo ya está registrado.
        }

        // 2. Encriptar la contraseña.
        String contrasenaEncriptada = passwordEncoder.encode(usuario.getContrasena());
        usuario.setContrasena(contrasenaEncriptada);

        // 3. Guardar el usuario. JPA se encarga de todo el SQL.
        try {
            usuarioRepository.save(usuario);
            return true;
        } catch (Exception e) {
            System.err.println("Error al guardar el usuario con JPA: " + e.getMessage());
            return false;
        }
    }
}