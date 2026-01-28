package pe.edu.vallegrande.msdistribution.infrastructure.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Configuración de clientes web reactivos (WebClient).
 * Define clientes para comunicarse con otros microservicios.
 * 
 * @version 1.0
 */
@Configuration
public class WebClientConfig {

    /** URL base del servicio de organización. */
    @Value("${organization-service.base-url}")
    private String organizationServiceBaseUrl;

    /** Token de servicio para autenticación entre microservicios. */
    @Value("${organization-service.token}")
    private String organizationServiceToken;

    /**
     * Crea un WebClient preconfigurado para el servicio de organización.
     * Incluye URL base y header de autorización por defecto.
     * 
     * @return WebClient configurado.
     */
    @Bean
    @Qualifier("organizationWebClient")
    public WebClient organizationWebClient() {
        // Logs temporales para depuración de configuración
        System.out.println("Organization service base URL: " + organizationServiceBaseUrl);
        System.out.println("Organization service token: " + organizationServiceToken);
        System.out.println(
                "Token length: " + (organizationServiceToken != null ? organizationServiceToken.length() : "null"));

        return WebClient.builder()
                .baseUrl(organizationServiceBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + organizationServiceToken)
                .build();
    }
}