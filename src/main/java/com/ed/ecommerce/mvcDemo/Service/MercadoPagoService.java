package com.ed.ecommerce.mvcDemo.Service;

import com.mercadopago.client.preference.*;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.exceptions.MPException;
import com.mercadopago.resources.preference.Preference;
import com.ed.ecommerce.mvcDemo.Dto.CarritoItemDTO;
import com.ed.ecommerce.mvcDemo.Model.Venta;
// AÑADIDO: Import para PostConstruct
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
// AÑADIDO: Import para el SDK de MercadoPago
import com.mercadopago.MercadoPagoConfig;

import java.util.ArrayList;
import java.util.List;

@Service
public class MercadoPagoService {

    // AÑADIDO: Lectura del Access Token desde las propiedades/variables de entorno
    @Value("${mercadopago.access_token}")
    private String accessToken;

    @Value("${mercadopago.seller_email}")
    private String sellerEmail;

    @Value("${ngrok.url}")
    private String ngrokUrl;

    @Autowired
    private VentaService ventaService;

    // AÑADIDO: Método de inicialización que se ejecuta una sola vez cuando la aplicación arranca.
    // Aquí es donde configuramos el SDK de Mercado Pago con nuestro token.
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

        String payerEmailToSend = loggedInUserEmail.equalsIgnoreCase(sellerEmail)
                ? "test_user_12345678@testuser.com"
                : loggedInUserEmail;
        PreferencePayerRequest payer = PreferencePayerRequest.builder().email(payerEmailToSend).build();

        // CORRECCIÓN IMPORTANTE: Las URLs de retorno deben ser las de tu aplicación en Render
        PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                .success(ngrokUrl + "/pago-exitoso")
                .failure(ngrokUrl + "/pago-fallido")
                .pending(ngrokUrl + "/pago-pendiente")
                .build();

        // NOTA: El notificationUrl está mal, debería ser /webhook, no /api/pagos/notificacion,
        // basándonos en lo que configuramos en Mercado Pago.
        PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                .items(mpItems)
                .payer(payer)
                .backUrls(backUrls)
                .notificationUrl(ngrokUrl + "/webhook") // URL del webhook
                .externalReference(ventaPendiente.getIdVenta().toString())
                .build();

        PreferenceClient client = new PreferenceClient();
        Preference preference = client.create(preferenceRequest);

        ventaPendiente.setPreferenceId(preference.getId());
        ventaService.guardarVenta(ventaPendiente);

        System.out.println("Preferencia creada. Venta local ID: " + ventaPendiente.getIdVenta() + ", Preference de MP ID: " + preference.getId());
        return preference;
    }
}