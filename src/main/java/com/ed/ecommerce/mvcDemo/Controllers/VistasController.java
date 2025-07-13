// Archivo: src/main/java/com/ed/ecommerce/mvcDemo/Controllers/VistasController.java
package com.ed.ecommerce.mvcDemo.Controllers;

import com.ed.ecommerce.mvcDemo.Model.Producto;
import com.ed.ecommerce.mvcDemo.Service.ProductoService;
import com.ed.ecommerce.mvcDemo.Service.VentaService; // Importar VentaService
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired; // Importar Autowired
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class VistasController {

    private final ProductoService productoService;
    private final VentaService ventaService; // Inyección de VentaService

    // Usamos un único constructor para inyectar todas las dependencias.
    // Spring se encarga de proveer las instancias de los servicios.
    @Autowired
    public VistasController(ProductoService productoService, VentaService ventaService) {
        this.productoService = productoService;
        this.ventaService = ventaService;
    }

    /**
     * Función de ayuda para manejar los mensajes de alerta que se pasan entre redirecciones.
     */
    private void addAlertMessageToModel(HttpSession session, Model model) {
        if (session.getAttribute("alertMessage") != null) {
            model.addAttribute("alertMessage", session.getAttribute("alertMessage"));
            session.removeAttribute("alertMessage");
        }
    }

    // --- MAPPINGS DE PÁGINAS PRINCIPALES ---

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

    // --- MAPPINGS DE CATEGORÍAS ---

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

    // --- MAPPINGS PARA LAS PÁGINAS DE RESULTADO DE PAGO (MODIFICADO) ---

    @GetMapping("/pago-exitoso")
    public String mostrarPagoExitoso(@RequestParam("external_reference") String ventaIdStr) {
        System.out.println("--- DENTRO DE /pago-exitoso ---");
        System.out.println("Recibido external_reference (ventaId): " + ventaIdStr);

        try {
            Integer ventaId = Integer.parseInt(ventaIdStr);

            // Verificamos que el servicio no sea nulo antes de llamarlo para depurar
            if (this.ventaService == null) {
                System.err.println("¡ERROR CRÍTICO! VentaService no fue inyectado en VistasController.");
                // Aunque hay un error interno, el usuario pagó, así que le mostramos la página de éxito.
                return "pago-exitoso";
            }

            System.out.println("Llamando a ventaService.confirmarYProcesarVentaPorId con ID: " + ventaId);
            // ¡Llamamos directamente a la lógica de confirmación!
            // Esto simula la llegada del webhook instantáneamente para la demo.
            ventaService.confirmarYProcesarVentaPorId(ventaId);
            System.out.println("Llamada a confirmarYProcesarVentaPorId completada exitosamente.");

        } catch (NumberFormatException e) {
            System.err.println("Error: El external_reference '" + ventaIdStr + "' no es un número válido.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Error fatal al procesar la confirmación de la venta en VistasController:");
            e.printStackTrace(); // Imprimimos el stack trace completo del error para saber qué pasó.
        }

        // Siempre devolvemos la página de éxito al usuario, ya que su pago en Mercado Pago fue exitoso.
        return "pago-exitoso";
    }

    @GetMapping("/pago-fallido")
    public String mostrarPagoFallido() {
        return "pago-fallido";
    }

    @GetMapping("/pago-pendiente")
    public String mostrarPagoPendiente() {
        // Por simplicidad, la tratamos como un fallo.
        return "pago-fallido";
    }

    // --- MÉTODO PRIVADO AUXILIAR (SIN CAMBIOS) ---
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