package com.ed.ecommerce.mvcDemo.Controllers;

import com.ed.ecommerce.mvcDemo.Model.Producto;
import com.ed.ecommerce.mvcDemo.Service.ProductoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Este controlador maneja las solicitudes API relacionadas con los productos.
@RestController
@RequestMapping("/api/productos")
public class ProductoApiController {

    // Inyección de dependencia del servicio de productos.
    private final ProductoService productoService;

    // Constructor para inicializar el servicio de productos.
    public ProductoApiController(ProductoService productoService) {
        this.productoService = productoService;
    }

    // Maneja las solicitudes GET a "/api/productos/buscar".
    // y devuelve una lista de productos que coinciden.
    @GetMapping("/buscar")
    public List<Producto> buscarProductosSugerencias(@RequestParam("q") String query) {
        return productoService.buscarProductos(query);
    }
}