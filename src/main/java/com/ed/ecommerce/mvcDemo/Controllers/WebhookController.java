package com.ed.ecommerce.mvcDemo.Controllers;

import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.payment.Payment;
import com.ed.ecommerce.mvcDemo.Service.VentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/webhook") // Escuchando en /webhook
public class WebhookController {

    private final VentaService ventaService;

    public WebhookController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<Void> recibirNotificacion(@RequestBody Map<String, Object> notification) {
        // Log #1: Imprime el cuerpo completo de la notificación
        System.out.println("--- Webhook Recibido ---");
        System.out.println("Cuerpo de la notificación: " + notification);

        try {
            String type = (String) notification.get("type");
            if (type == null) {
                System.out.println("El campo 'type' es nulo. Ignorando notificación.");
                return ResponseEntity.badRequest().build();
            }

            // Nos interesa solo el evento de 'payment'
            if ("payment".equals(type)) {
                System.out.println("Notificación de tipo 'payment' detectada.");

                Object dataObj = notification.get("data");
                if (!(dataObj instanceof Map)) {
                    System.err.println("El campo 'data' no es un objeto válido.");
                    return ResponseEntity.badRequest().build();
                }

                Map<String, Object> data = (Map<String, Object>) dataObj;
                Object idObj = data.get("id");

                if (idObj == null) {
                    System.err.println("El campo 'data.id' es nulo.");
                    return ResponseEntity.badRequest().build();
                }

                long paymentId = Long.parseLong(idObj.toString());
                System.out.println("ID del Pago notificado: " + paymentId);

                // Log #2: Justo antes de consultar la API de Mercado Pago
                System.out.println("Consultando el estado del pago a la API de Mercado Pago...");
                PaymentClient client = new PaymentClient();
                Payment payment = client.get(paymentId);
                // Log #3: Justo después de consultar la API
                System.out.println("Consulta a la API exitosa.");

                if (payment == null) {
                    System.err.println("No se encontró información para el pago con ID: " + paymentId);
                    return ResponseEntity.notFound().build();
                }

                String status = payment.getStatus();
                System.out.println("Estado del pago en Mercado Pago: " + status);

                if ("approved".equals(status)) {
                    String ventaIdStr = payment.getExternalReference();
                    if (ventaIdStr != null && !ventaIdStr.isEmpty()) {
                        System.out.println("Pago APROBADO. Procesando venta local con ID: " + ventaIdStr);
                        ventaService.confirmarYProcesarVentaPorId(Integer.parseInt(ventaIdStr));
                        System.out.println("Venta procesada exitosamente.");
                    } else {
                        System.err.println("Error: No se encontró external_reference en el pago aprobado " + paymentId);
                    }
                } else {
                    System.out.println("El pago no está aprobado. Estado: " + status + ". No se procesa la venta.");
                }
            } else {
                System.out.println("Notificación de tipo '" + type + "' recibida. No es un evento de pago, se ignora.");
            }

            // Si todo va bien, devolvemos un 200 OK.
            return ResponseEntity.ok().build();

        } catch (MPException | com.mercadopago.exceptions.MPApiException apiException) {
            System.err.println("Error de la API de Mercado Pago al procesar el webhook: " + apiException.getMessage());
            apiException.printStackTrace();
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build(); // Indicar a MP que reintente
        } catch (Exception e) {
            // Si cualquier otra cosa falla, lo registramos.
            System.err.println("Error fatal al procesar el webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}git add .