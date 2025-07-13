package com.ed.ecommerce.mvcDemo.Controllers;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.resources.payment.Payment;
import com.ed.ecommerce.mvcDemo.Service.VentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
// CAMBIO 1: La ruta base ahora es solo "/webhook".
@RequestMapping("/webhook")
public class WebhookController {

    private final VentaService ventaService;

    public WebhookController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    // CAMBIO 2: El método ahora escucha en la raíz del controlador,
    // por lo que la ruta completa es simplemente "/webhook".
    @PostMapping
    public ResponseEntity<Void> recibirNotificacion(@RequestBody Map<String, Object> notification) {

        System.out.println("--- ¡Webhook de Mercado Pago recibido en /webhook! ---");

        try {
            if ("payment".equals(notification.get("type"))) {
                // El resto del código es correcto y no necesita cambios.
                String paymentIdStr = ((Map<String, Object>) notification.get("data")).get("id").toString();
                Long paymentId = Long.parseLong(paymentIdStr);

                System.out.println("ID del Pago notificado: " + paymentId);

                PaymentClient client = new PaymentClient();
                Payment payment = client.get(paymentId);

                if (payment != null && "approved".equals(payment.getStatus())) {
                    System.out.println("Estado del pago: APROBADO.");

                    String ventaIdStr = payment.getExternalReference();

                    if (ventaIdStr != null) {
                        System.out.println("Procesando venta local con ID (External Reference): " + ventaIdStr);
                        ventaService.confirmarYProcesarVentaPorId(Integer.parseInt(ventaIdStr));
                    } else {
                        System.err.println("Error: No se encontró el external_reference en el pago " + paymentId);
                    }
                } else {
                    String status = (payment != null) ? payment.getStatus() : "desconocido";
                    System.out.println("Estado del pago: " + status + ". No se procesa la venta.");
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