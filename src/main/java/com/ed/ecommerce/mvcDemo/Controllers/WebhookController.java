// Archivo: src/main/java/com/ed/ecommerce/mvcDemo/Controllers/WebhookController.java
package com.ed.ecommerce.mvcDemo.Controllers;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.ed.ecommerce.mvcDemo.Service.VentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/pagos")
public class WebhookController {

    private final VentaService ventaService;

    public WebhookController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping("/notificacion")
    public ResponseEntity<Void> recibirNotificacion(@RequestBody Map<String, Object> notification) {

        System.out.println("--- ¡Webhook de Mercado Pago recibido! ---");

        try {
            if ("payment".equals(notification.get("type"))) {
                String paymentIdStr = ((Map<String, Object>) notification.get("data")).get("id").toString();
                Long paymentId = Long.parseLong(paymentIdStr);

                System.out.println("ID del Pago notificado: " + paymentId);

                PaymentClient client = new PaymentClient();
                Payment payment = client.get(paymentId);

                if ("approved".equals(payment.getStatus())) {
                    System.out.println("Estado del pago: APROBADO.");

                    String ventaIdStr = payment.getExternalReference();

                    if (ventaIdStr != null) {
                        System.out.println("Procesando venta local con ID (External Reference): " + ventaIdStr);
                        ventaService.confirmarYProcesarVentaPorId(Integer.parseInt(ventaIdStr));
                    } else {
                        System.err.println("Error: No se encontró el external_reference en el pago " + paymentId);
                    }
                } else {
                    System.out.println("Estado del pago: " + payment.getStatus() + ". No se procesa la venta.");
                }
            }
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Error fatal al procesar el webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}