package com.ed.ecommerce.mvcDemo.Model;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
// CAMBIO: Nombre de la tabla a minúsculas.
@Table(name = "detalleventa")
public class DetalleVenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "id_detalle_venta")
    private Integer idDetalleVenta;

    @ManyToOne(fetch = FetchType.LAZY)
    // CAMBIO: Nombre de la columna de la clave foránea a snake_case.
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    // CAMBIO: Nombre de la columna de la clave foránea a snake_case.
    @JoinColumn(name = "id_producto", nullable = false)
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    // CAMBIO: Nombre de la columna a snake_case.
    @Column(name = "precio_unitario", nullable = false)
    private BigDecimal precioUnitario;

    // Constructor vacío (requerido por JPA)
    public DetalleVenta() {}

    // --- Getters y Setters ---

    public Integer getIdDetalleVenta() {
        return idDetalleVenta;
    }

    public void setIdDetalleVenta(Integer idDetalleVenta) {
        this.idDetalleVenta = idDetalleVenta;
    }

    public Venta getVenta() {
        return venta;
    }

    public void setVenta(Venta venta) {
        this.venta = venta;
    }

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }
}