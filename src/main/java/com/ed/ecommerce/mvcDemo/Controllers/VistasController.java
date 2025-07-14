package com.ed.ecommerce.mvcDemo.Controllers;

import com.ed.ecommerce.mvcDemo.Model.Producto;
import com.ed.ecommerce.mvcDemo.Service.ProductoService;
import com.ed.ecommerce.mvcDemo.Service.VentaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class VistasController {

    private final ProductoService productoService;
    private final VentaService ventaService;

    @Autowired
    public VistasController(ProductoService productoService, VentaService ventaService) {
        this.productoService = productoService;
        this.ventaService = ventaService;
    }

    private void addAlertMessageToModel(HttpSession session, Model model) {
        if (session.getAttribute("alertMessage") != null) {
            model.addAttribute("alertMessage", session.getAttribute("alertMessage"));
            session.removeAttribute("alertMessage");
        }
    }

    @GetMapping({"/", "/index"})
    public String mostrarIndex(Model model, HttpSession session) {
        addAlertMessageToModel(session, model);
        return "index";
    }

    @GetMapping("/login")
    public String mostrarPaginaDeLogin(@RequestParam(name = "logout", required = false) String logout,
                                       @RequestParam(name = "error", required = false) String error,
                                       Model model) {
        if (logout != null) {
            model.addAttribute("alertMessage", "Has cerrado sesión exitosamente.");
        }
        if (error != null) {
            model.addAttribute("alertMessage", "Error: Usuario o contraseña incorrectos.");
        }
        return "ingreso";
    }

    @GetMapping("/ficcion")
    public String mostrarFiccion(@RequestParam(required = false) String subcategoria, Model model, HttpSession session) {
        addAlertMessageToModel(session, model);
        return cargarPaginaCategoria("Ficción", subcategoria, model, "ficcion");
    }

    @GetMapping("/no-ficcion")
    public String mostrarNoFiccion(@RequestParam(required = false) String subcategoria, Model model, HttpSession session) {
        addAlertMessageToModel(session, model);
        return cargarPaginaCategoria("No Ficción", subcategoria, model, "no-ficcion");
    }

    @GetMapping("/infantil-juvenil")
    public String mostrarInfantilJuvenil(@RequestParam(required = false) String subcategoria, Model model, HttpSession session) {
        addAlertMessageToModel(session, model);
        return cargarPaginaCategoria("Infantil y Juvenil", subcategoria, model, "infantil-juvenil");
    }

    // --- MAPPINGS DE PAGO SIMPLIFICADOS ---

    @GetMapping("/pago-exitoso")
    public String mostrarPagoExitoso() {
        // La única responsabilidad de esta página es mostrar un mensaje de éxito al usuario.
        // La confirmación real de la venta y la actualización del stock se manejan
        // de forma asíncrona a través del Webhook.
        System.out.println("Redirigido a /pago-exitoso. Esperando notificación del webhook para confirmar la venta.");
        return "pago-exitoso";
    }

    @GetMapping("/pago-fallido")
    public String mostrarPagoFallido() {
        return "pago-fallido";
    }

    @GetMapping("/pago-pendiente")
    public String mostrarPagoPendiente() {
        return "pago-fallido";
    }

    private String cargarPaginaCategoria(String categoria, String subcategoria, Model model, String nombreVista) {
        List<Producto> productos;
        if (subcategoria != null && !subcategoria.trim().isEmpty()) {
            productos = productoService.findByCategoriaAndSubcategoria(categoria, subcategoria);
            model.addAttribute("categoriaActual", subcategoria);
        } else {
            productos = productoService.findByCategoria(categoria);
            model.addAttribute("categoriaActual", "Todos");
        }
        List<String> listaSubcategorias = productoService.findSubcategoriasByCategoria(categoria);
        model.addAttribute("productos", productos);
        model.addAttribute("subcategorias", listaSubcategorias);
        model.addAttribute("nombreCategoria", categoria);
        return nombreVista;
    }
}