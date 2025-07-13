// Archivo: src/main/java/com/ed/ecommerce/mvcDemo/Controllers/PagoController.java
package com.ed.ecommerce.mvcDemo.Controllers;

import com.mercadopago.resources.preference.Preference;
import com.ed.ecommerce.mvcDemo.Service.MercadoPagoService;
import com.ed.ecommerce.mvcDemo.Dto.CompraRequestDTO;
import org.springframework.http.ResponseEntity;
// --- AÑADE ESTOS IMPORTS ---
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import com.mercadopago.exceptions.MPApiException;

@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final MercadoPagoService mercadoPagoService;

    public PagoController(MercadoPagoService mercadoPagoService) {
        this.mercadoPagoService = mercadoPagoService;
    }

    @PostMapping("/crear-preferencia")
    public ResponseEntity<?> crearPreferenciaDePago(@RequestBody CompraRequestDTO compraRequest,
                                                    @AuthenticationPrincipal UserDetails userDetails) { // <-- AÑADIDO
        if (userDetails == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Usuario no autenticado"));
        }

        try {
            String userEmail = userDetails.getUsername(); // Obtenemos el email
            Preference preferencia = mercadoPagoService.createPaymentPreference(compraRequest.getItems(), userEmail); // Pasamos el email
            return ResponseEntity.ok(Map.of("checkoutUrl", preferencia.getInitPoint()));
        } catch (MPApiException e) {
            // --- ESTE ES EL CAMBIO MÁS IMPORTANTE ---
            // Capturamos la excepción específica de la API y mostramos los detalles.
            System.err.println("--- ERROR DE LA API DE MERCADO PAGO ---");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Response Body: " + e.getApiResponse().getContent());
            System.err.println("------------------------------------");

            // Devolvemos un error más informativo al frontend
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("error", "Error de la API de Mercado Pago", "details", e.getApiResponse().getContent()));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Error interno al crear la preferencia de pago: " + e.getMessage()));
        }
    }
}