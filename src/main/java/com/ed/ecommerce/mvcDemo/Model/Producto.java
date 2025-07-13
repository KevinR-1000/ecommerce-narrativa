package com.ed.ecommerce.mvcDemo.Model;

import jakarta.persistence.*;
import java.math.BigDecimal; // Importación necesaria para BigDecimal

@Entity
// CAMBIO: Nombre de la tabla a minúsculas.
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "id_producto")
    private int idProducto;

    @Column(name = "titulo")
    private String titulo;

    @Column(name = "autor")
    private String autor;

    @Column(name = "precio")
    private BigDecimal precio;

    @Column(name = "categoria")
    private String categoria;

    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "url_imagen")
    private String urlImagen;

    @Column(name = "stock")
    private int stock;

    @Column(name = "subcategoria")
    private String subcategoria;

    // Constructor vacío requerido por JPA
    public Producto() {
    }

    // --- Getters y Setters (Estos no cambian) ---

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getAutor() {
        return autor;
    }

    public void setAutor(String autor) {
        this.autor = autor;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getUrlImagen() {
        return urlImagen;
    }

    public void setUrlImagen(String urlImagen) {
        this.urlImagen = urlImagen;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getSubcategoria() {
        return subcategoria;
    }

    public void setSubcategoria(String subcategoria) {
        this.subcategoria = subcategoria;
    }
}