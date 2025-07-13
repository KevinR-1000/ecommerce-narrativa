package com.ed.ecommerce.mvcDemo.Repository;

import com.ed.ecommerce.mvcDemo.Model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Integer> {


    List<Producto> findByCategoria(String categoria);
    List<Producto> findByCategoriaAndSubcategoria(String categoria, String subcategoria);
    @Query("SELECT DISTINCT p.subcategoria FROM Producto p WHERE p.categoria = ?1 AND p.subcategoria IS NOT NULL")
    List<String> findSubcategoriasByCategoria(String categoria);

    // Metodo para el buscador
    @Query("SELECT p FROM Producto p WHERE LOWER(p.titulo) LIKE LOWER(CONCAT('%', :termino, '%'))")
    List<Producto> buscarPorTitulo(@Param("termino") String termino);
}