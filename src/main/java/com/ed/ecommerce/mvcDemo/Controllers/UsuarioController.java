package com.ed.ecommerce.mvcDemo.Controllers;

import com.ed.ecommerce.mvcDemo.Model.Usuario;
import com.ed.ecommerce.mvcDemo.Service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

// Controlador para manejar las operaciones relacionadas con los usuarios.
@Controller
public class UsuarioController {

    // Inyección de dependencia del servicio de usuarios.
    private final UsuarioService usuarioService;

    // Constructor para inicializar el servicio de usuarios.
    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    // Maneja las solicitudes POST para el registro de nuevos usuarios.
    // Recibe un objeto Usuario desde el formulario y utiliza RedirectAttributes
    // para añadir mensajes de éxito o error antes de redirigir.
    @PostMapping("/registro")
    public String guardarUsuario(@ModelAttribute Usuario usuario, RedirectAttributes redirectAttributes) {
        boolean registrado = usuarioService.registrar(usuario);
        if (registrado) {
            redirectAttributes.addFlashAttribute("success", "¡Usuario registrado! Ya puedes iniciar sesión.");
            return "redirect:/login";
        } else {
            redirectAttributes.addFlashAttribute("error", "Error al registrar. El correo ya podría estar en uso.");
            return "redirect:/login";
        }
    }
}