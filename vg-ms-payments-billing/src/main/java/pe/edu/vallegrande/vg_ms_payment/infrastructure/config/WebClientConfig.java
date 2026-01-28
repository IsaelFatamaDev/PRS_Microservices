package pe.edu.vallegrande.vg_ms_payment.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@Slf4j
public class WebClientConfig {

    /**
     * Configuración básica de WebClient sin autenticación
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json");
    }

    /**
     * WebClient para microservicios internos sin autenticación
     */
    @Bean("internalWebClient")
    public WebClient internalWebClient() {
        return webClientBuilder().build();
    }
}