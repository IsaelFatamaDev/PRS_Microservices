package pe.edu.vallegrande.vgmsorganizations.application.config;

import io.netty.channel.ChannelOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Configuración de WebClient optimizada para comunicación con microservicios externos
 * Siguiendo estándares PRS1 - Constructor explícito
 */
@Configuration
public class WebClientConfig {

    private final String msUsersBaseUrl;
    private final int timeout;

    public WebClientConfig(
            @Value("${microservices.users.base-url:https://lab.vallegrande.edu.pe/jass/ms-users}") String msUsersBaseUrl,
            @Value("${microservices.users.timeout:30}") int timeout) {
        // Agregar validación para asegurar que la URL base no esté vacía
        this.msUsersBaseUrl = msUsersBaseUrl != null && !msUsersBaseUrl.trim().isEmpty() 
            ? msUsersBaseUrl 
            : "https://lab.vallegrande.edu.pe/jass/ms-users"; // Valor por defecto
        this.timeout = timeout > 0 ? timeout : 30; // Asegurar valor positivo
    }

    /**
     * WebClient configurado para MS-USERS
     * - Pool de conexiones limitado
     * - Timeouts configurados
     * - Optimizado para bajo consumo de memoria
     */
    @Bean("msUsersWebClient")
    public WebClient msUsersWebClient() {
        try {
            // Verificar que la URL base sea válida
            if (msUsersBaseUrl == null || msUsersBaseUrl.trim().isEmpty()) {
                throw new IllegalStateException("MS-USERS base URL is not configured");
            }
            
            // Pool de conexiones ultra-minimalista para ahorrar memoria
            ConnectionProvider connectionProvider = ConnectionProvider.builder("msUsers")
                    .maxConnections(2)
                    .maxIdleTime(Duration.ofSeconds(15))
                    .maxLifeTime(Duration.ofMinutes(1))
                    .pendingAcquireTimeout(Duration.ofSeconds(3))
                    .evictInBackground(Duration.ofSeconds(20))
                    .build();

            // HttpClient optimizado
            HttpClient httpClient = HttpClient.create(connectionProvider)
                    .responseTimeout(Duration.ofSeconds(timeout))
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true);

            return WebClient.builder()
                    .baseUrl(msUsersBaseUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(256 * 1024)) // 256KB para ahorrar memoria
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "application/json")
                    .build();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create msUsersWebClient: " + e.getMessage(), e);
        }
    }
}


