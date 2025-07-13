// Archivo: src/main/java/com/ed/ecommerce/mvcDemo/Service/VentaService.java
package com.ed.ecommerce.mvcDemo.Service;

import com.ed.ecommerce.mvcDemo.Dto.CarritoItemDTO;
import com.ed.ecommerce.mvcDemo.Model.DetalleVenta;
import com.ed.ecommerce.mvcDemo.Model.Producto;
import com.ed.ecommerce.mvcDemo.Model.Usuario;
import com.ed.ecommerce.mvcDemo.Model.Venta;
import com.ed.ecommerce.mvcDemo.Repository.ProductoRepository;
import com.ed.ecommerce.mvcDemo.Repository.UsuarioRepository;
import com.ed.ecommerce.mvcDemo.Repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public VentaService(VentaRepository ventaRepository, ProductoRepository productoRepository,
                        UsuarioRepository usuarioRepository) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    @Transactional
    public Venta crearVentaPendiente(List<CarritoItemDTO> itemsCarrito, String correoUsuario) {
        Usuario usuario = usuarioRepository.findByCorreo(correoUsuario)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado: " + correoUsuario));

        Venta nuevaVenta = new Venta();
        nuevaVenta.setUsuario(usuario);
        nuevaVenta.setFecha(LocalDateTime.now());
        nuevaVenta.setEstado("PENDIENTE");

        BigDecimal totalVenta = BigDecimal.ZERO;

        for (CarritoItemDTO itemDTO : itemsCarrito) {
            Producto producto = productoRepository.findById(itemDTO.getProductId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + itemDTO.getProductId()));

            if (producto.getStock() < itemDTO.getQuantity()) {
                throw new RuntimeException("Stock insuficiente para el producto: " + producto.getTitulo());
            }

            DetalleVenta detalle = new DetalleVenta();
            detalle.setProducto(producto);
            detalle.setCantidad(itemDTO.getQuantity());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalle.setVenta(nuevaVenta);

            nuevaVenta.getDetalles().add(detalle);
            totalVenta = totalVenta.add(producto.getPrecio().multiply(new BigDecimal(itemDTO.getQuantity())));
        }

        nuevaVenta.setTotal(totalVenta);
        return ventaRepository.save(nuevaVenta);
    }

    @Transactional
    public void confirmarYProcesarVentaPorId(Integer ventaId) {
        Venta venta = ventaRepository.findById(ventaId)
                .orElseThrow(() -> new RuntimeException("Venta no encontrada con ID: " + ventaId));

        if (!"PENDIENTE".equals(venta.getEstado())) {
            System.out.println("La venta " + venta.getIdVenta() + " ya fue procesada o cancelada.");
            return;
        }

        System.out.println("Confirmando y actualizando venta ID: " + venta.getIdVenta());

        for (DetalleVenta detalle : venta.getDetalles()) {
            Producto producto = detalle.getProducto();
            int cantidadComprada = detalle.getCantidad();
            int stockActual = producto.getStock();
            int nuevoStock = stockActual - cantidadComprada;

            if (nuevoStock < 0) {
                venta.setEstado("ERROR_STOCK");
                ventaRepository.save(venta);
                throw new RuntimeException("Error crítico: Stock insuficiente para " + producto.getTitulo());
            }

            System.out.println("Actualizando stock para '" + producto.getTitulo() + "': de " + stockActual + " a " + nuevoStock);
            producto.setStock(nuevoStock);
            productoRepository.save(producto);
        }

        venta.setEstado("PAGADO");
        ventaRepository.save(venta);

        System.out.println("¡VENTA ID " + venta.getIdVenta() + " CONFIRMADA, estado PAGADO y STOCK ACTUALIZADO!");
    }
}