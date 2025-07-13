package com.ed.ecommerce.mvcDemo.Service;

import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.ed.ecommerce.mvcDemo.Dto.CarritoItemDTO;
import com.ed.ecommerce.mvcDemo.Model.Venta;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.mercadopago.MercadoPagoConfig;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    @Value("${mercadopago.access_token}")
    private String accessToken;

    @Value("${mercadopago.seller_email}")
    private String sellerEmail;

    @Value("${ngrok.url}")
    private String ngrokUrl;

    @Autowired
    private VentaService ventaService;

    @PostConstruct
    public void init() {
        MercadoPagoConfig.setAccessToken(accessToken);
    }

    public Preference createPaymentPreference(List<CarritoItemDTO> itemsDelCarrito, String loggedInUserEmail) throws MPException, MPApiException {
        // --- 1. CREAR VENTA PENDIENTE ---
        // Esto crea la venta en tu base de datos con estado "PENDIENTE"
        Venta ventaPendiente = ventaService.crearVentaPendiente(itemsDelCarrito, loggedInUserEmail);

        // --- 2. PREPARAR ITEMS PARA MERCADO PAGO ---
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

        // --- 3. LÓGICA CORRECTA PARA EL COMPRADOR ---
        // Se usa el email del usuario logueado, a menos que sea el mismo vendedor.
        // Esto previene errores de autopago en Mercado Pago.
        String payerEmailToSend = loggedInUserEmail.equalsIgnoreCase(sellerEmail)
                ? "test_user_12345678@testuser.com" // Si el vendedor compra, usamos un email genérico
                : loggedInUserEmail;               // Si no, usamos el email real del comprador

        System.out.println("Email del comprador (Payer) que se enviará a Mercado Pago: " + payerEmailToSend);
        PreferencePayerRequest payer = PreferencePayerRequest.builder().email(payerEmailToSend).build();

        // --- 4. CONFIGURAR URLS DE RETORNO Y NOTIFICACIÓN ---
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(ngrokUrl + "/pago-exitoso")
                .failure(ngrokUrl + "/pago-fallido")
                .pending(ngrokUrl + "/pago-pendiente")
                .build();

        // Esta es la URL a la que Mercado Pago enviará la notificación de pago aprobado.
        String notificationUrl = ngrokUrl + "/webhook";

        // --- 5. CONSTRUIR LA PREFERENCIA DE PAGO ---
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(mpItems)
                .payer(payer)
                .backUrls(backUrls)
                .notificationUrl(notificationUrl) // Usamos la URL del webhook
                .externalReference(ventaPendiente.getIdVenta().toString()) // Vinculamos la venta de MP con nuestra venta
                .build();

        // --- 6. CREAR LA PREFERENCIA Y GUARDAR EL ID ---
        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        ventaPendiente.setPreferenceId(preference.getId());
        ventaService.guardarVenta(ventaPendiente);

        System.out.println("Preferencia creada exitosamente. Preference de MP ID: " + preference.getId());
        return preference;
    }
}