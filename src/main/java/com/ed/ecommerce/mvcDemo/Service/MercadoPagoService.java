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
        Venta ventaPendiente = ventaService.crearVentaPendiente(itemsDelCarrito, loggedInUserEmail);

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

        // --- CAMBIO CLAVE Y DEFINITIVO ---
        // Para el Sandbox, SIEMPRE usaremos un comprador de prueba válido.
        // Esto evita que Mercado Pago rechace la transacción.
        String payerEmailToSend = "test_user_12345678@testuser.com";
        System.out.println("Email del comprador (Payer) forzado para Sandbox: " + payerEmailToSend);

        PreferencePayerRequest payer = PreferencePayerRequest.builder().email(payerEmailToSend).build();

        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(ngrokUrl + "/pago-exitoso")
                .failure(ngrokUrl + "/pago-fallido")
                .pending(ngrokUrl + "/pago-pendiente")
                .build();

        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(mpItems)
                .payer(payer)
                .backUrls(backUrls)
                .notificationUrl(ngrokUrl + "/webhook")
                .externalReference(ventaPendiente.getIdVenta().toString())
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        ventaPendiente.setPreferenceId(preference.getId());
        ventaService.guardarVenta(ventaPendiente);

        System.out.println("Preferencia creada exitosamente. Preference de MP ID: " + preference.getId());
        return preference;
    }
}