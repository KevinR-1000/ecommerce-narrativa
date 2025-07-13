// Archivo: src/main/java/com/ed/ecommerce/mvcDemo/Repository/VentaRepository.java
package com.ed.ecommerce.mvcDemo.Repository;

import com.ed.ecommerce.mvcDemo.Model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Integer> {

    List<Venta> findByUsuario_IdUsuario(int idUsuario);

    // --- MÉTODO NUEVO ---
    // Busca una venta usando el ID de la preferencia de Mercado Pago
    Optional<Venta> findByPreferenceId(String preferenceId);
}