// Archivo: src/main/java/com/ed/ecommerce/mvcDemo/Service/MercadoPagoService.java
package com.ed.ecommerce.mvcDemo.Service;

import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.ed.ecommerce.mvcDemo.Dto.CarritoItemDTO;
import com.ed.ecommerce.mvcDemo.Model.Venta;
import org.springframework.beans.factory.annotation.Autowired; // ¡IMPORTANTE!
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.seller_email}")
    private String sellerEmail;

    @Value("${ngrok.url}")
    private String ngrokUrl;

    // --- INYECCIÓN CORRECTA DE VentaService ---
    @Autowired
    private VentaService ventaService;

    public Preference createPaymentPreference(List<CarritoItemDTO> itemsDelCarrito, String loggedInUserEmail) throws MPException, MPApiException {
        // --- PASO 1: CREAR LA VENTA PENDIENTE EN NUESTRA BD ---
        Venta ventaPendiente = ventaService.crearVentaPendiente(itemsDelCarrito, loggedInUserEmail);

        // --- PASO 2: PREPARAR LOS ITEMS PARA LA API DE MERCADO PAGO ---
        List<PreferenceItemRequest> mpItems = new ArrayList<>();
        for (CarritoItemDTO item : itemsDelCarrito) {
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(item.getProductId().toString())
                    .title(item.getName())
                    .quantity(item.getQuantity())
                    .unitPrice(item.getPrice())
                    .currencyId("PEN")
                    .build();
            mpItems.add(itemRequest);
        }

        // --- PASO 3: PREPARAR EL COMPRADOR (PAYER) ---
        String payerEmailToSend = loggedInUserEmail.equalsIgnoreCase(sellerEmail)
                ? "test_user_12345678@testuser.com"
                : loggedInUserEmail;
        PreferencePayerRequest payer = PreferencePayerRequest.builder().email(payerEmailToSend).build();

        // --- PASO 4: PREPARAR LAS URLS DE REDIRECCIÓN ---
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success("http://localhost:8080/pago-exitoso")
                .failure("http://localhost:8080/pago-fallido")
                .pending("http://localhost:8080/pago-pendiente")
                .build();

        // --- PASO 5: CONSTRUIR LA PETICIÓN DE PREFERENCIA COMPLETA ---
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(mpItems)
                .payer(payer)
                .backUrls(backUrls)
                .notificationUrl(ngrokUrl + "/api/pagos/notificacion")
                .externalReference(ventaPendiente.getIdVenta().toString())
                .build();

        // --- PASO 6: CREAR LA PREFERENCIA EN MERCADO PAGO ---
        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        // --- PASO 7: ACTUALIZAR NUESTRA VENTA CON EL ID DE PREFERENCIA DE MP ---
        ventaPendiente.setPreferenceId(preference.getId());
        ventaService.guardarVenta(ventaPendiente);

        System.out.println("Preferencia creada. Venta local ID: " + ventaPendiente.getIdVenta() + ", Preference de MP ID: " + preference.getId());
        return preference;
    }
}