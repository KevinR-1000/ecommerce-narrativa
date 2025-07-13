// Archivo: src/main/java/com/ed/ecommerce/mvcDemo/Service/ProductoService.java
package com.ed.ecommerce.mvcDemo.Service;

import com.ed.ecommerce.mvcDemo.Model.Producto;
import com.ed.ecommerce.mvcDemo.Repository.ProductoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    @Autowired
    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Métodos
    public List<Producto> findByCategoria(String categoria) {
        return productoRepository.findByCategoria(categoria);
    }
    public List<Producto> findByCategoriaAndSubcategoria(String categoria, String subcategoria) {
        return productoRepository.findByCategoriaAndSubcategoria(categoria, subcategoria);
    }
    public List<String> findSubcategoriasByCategoria(String categoria) {
        return productoRepository.findSubcategoriasByCategoria(categoria);
    }

    // Metodo para buscador
    public List<Producto> buscarProductos(String termino) {
        if (termino == null || termino.trim().length() < 2) {
            return List.of(); // Devuelve lista vacía si el término es muy corto
        }
        // Limitamos los resultados a los primeros 5 para el autocompletado
        return productoRepository.buscarPorTitulo(termino).stream()
                .limit(5)
                .collect(Collectors.toList());
    }
}