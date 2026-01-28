package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebFluxConfig {

    // Comentando la configuración que causa conflicto con Swagger
    // La configuración de SpringDoc ya maneja automáticamente las rutas de Swagger
    /*
    @Bean
    public RouterFunction<ServerResponse> swaggerRouterFunction() {
        return RouterFunctions.resources("/jass/ms-claims-incidents/swagger-ui/**", new ClassPathResource("META-INF/resources/webjars/swagger-ui/"));
    }
    */

    /**
     * Configura el WebClient para comunicación con microservicios externos
     * 
     * @return WebClient.Builder configurado
     */
    @Bean
    public WebClient.Builder webClientBuilder() {
        // Configuración del pool de conexiones
        ConnectionProvider connectionProvider = ConnectionProvider.builder("custom")
                .maxConnections(50)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .build();

        // Configuración del cliente HTTP
        HttpClient httpClient = HttpClient.create(connectionProvider)
                .responseTimeout(Duration.ofSeconds(30))
                .option(io.netty.channel.ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000)
                .compress(true);

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json")
                .codecs(configurer -> {
                    configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024); // 2MB
                    configurer.defaultCodecs().enableLoggingRequestDetails(true);
                });
    }

    /**
     * WebClient configurado específicamente para el microservicio de usuarios
     * 
     * @param webClientBuilder el builder base
     * @return WebClient para el servicio de usuarios
     */
    @Bean("userServiceWebClient")
    public WebClient userServiceWebClient(WebClient.Builder webClientBuilder) {
        return webClientBuilder
                .defaultHeader("X-Service-Name", "claims-incidents")
                .defaultHeader("X-Service-Version", "1.0.0")
                .build();
    }
}
