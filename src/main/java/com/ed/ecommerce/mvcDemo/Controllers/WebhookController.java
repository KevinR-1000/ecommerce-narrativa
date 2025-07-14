package com.ed.ecommerce.mvcDemo.Controllers;

import com.mercadopago.client.merchantorder.MerchantOrderClient;
import com.mercadopago.client.payment.PaymentClient;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.merchantorder.MerchantOrder;
import com.mercadopago.resources.payment.Payment;
import com.ed.ecommerce.mvcDemo.Service.VentaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private final VentaService ventaService;

    public WebhookController(VentaService ventaService) {
        this.ventaService = ventaService;
    }

    @PostMapping
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(required = false) String topic,
            @RequestParam(required = false) String id,
            @RequestBody(required = false) Map<String, Object> body) {

        System.out.println("--- Webhook Recibido ---");
        System.out.println("Topic: " + topic);
        System.out.println("ID (del query param): " + id);
        System.out.println("Cuerpo de la notificación (si existe): " + body);

        try {
            // Mercado Pago a veces envía el ID del pago en el query param 'id'
            // Y el topic (tema) también en un query param
            if ("payment".equals(topic) && id != null) {
                System.out.println("Notificación de tipo 'payment' por Query Params detectada. ID: " + id);
                procesarPago(Long.parseLong(id));
            }
            // Otras veces, lo envía en el cuerpo (RequestBody)
            else if (body != null && "payment".equals(body.get("type"))) {
                System.out.println("Notificación de tipo 'payment' por Request Body detectada.");
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                long paymentId = Long.parseLong(data.get("id").toString());
                procesarPago(paymentId);
            }
            else {
                System.out.println("Notificación no reconocida o sin ID de pago. Ignorando.");
            }

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            System.err.println("Error fatal al procesar el webhook: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private void procesarPago(long paymentId) throws MPException, com.mercadopago.exceptions.MPApiException {
        System.out.println("Consultando el estado del pago a la API de Mercado Pago para el ID: " + paymentId);
        PaymentClient client = new PaymentClient();
        Payment payment = client.get(paymentId);

        if (payment == null) {
            System.err.println("No se encontró información para el pago con ID: " + paymentId);
            return;
        }

        System.out.println("Consulta a la API exitosa. Estado del pago: " + payment.getStatus());

        if ("approved".equals(payment.getStatus())) {
            String ventaIdStr = payment.getExternalReference();
            if (ventaIdStr != null && !ventaIdStr.isEmpty()) {
                System.out.println("Pago APROBADO. Procesando venta local con ID: " + ventaIdStr);
                ventaService.confirmarYProcesarVentaPorId(Integer.parseInt(ventaIdStr));
                System.out.println("Venta procesada exitosamente.");
            } else {
                System.err.println("Error: No se encontró external_reference en el pago aprobado " + paymentId);
            }
        } else {
            System.out.println("El pago no está aprobado. Estado: " + payment.getStatus() + ". No se procesa la venta.");
        }
    }
}