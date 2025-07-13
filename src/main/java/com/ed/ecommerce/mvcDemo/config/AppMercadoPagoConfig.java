
package com.ed.ecommerce.mvcDemo.config;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.mercadopago.MercadoPagoConfig;

@Configuration
public class AppMercadoPagoConfig {

    // Inyecta el valor desde application.properties
    @Value("${mercadopago.access_token}")
    private String accessToken;

    @PostConstruct
    public void init() {
        // Inicializa el SDK con tus credenciales de prueba
        MercadoPagoConfig.setAccessToken(accessToken);
    }
}