package pe.edu.vallegrande.vgmsauthentication.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración optimizada de Keycloak para 250 MiB
 * - Pool de conexiones reducido
 * - Timeouts optimizados
 */
@Configuration
@Slf4j
public class KeycloakAdapter {

     @Value("${keycloak.url}")
     private String keycloakUrl;

     @Value("${keycloak.admin-username}")
     private String adminUsername;

     @Value("${keycloak.admin-password}")
     private String adminPassword;

     @Value("${keycloak.admin-client-id}")
     private String clientId;

     @Value("${keycloak.connection-pool-size:3}")
     private Integer connectionPoolSize;

     @Bean
     public Keycloak keycloak() {
          try {
               log.info("🔧 Conectando a Keycloak: {}", keycloakUrl);
               log.info("👤 Usuario: {} | Cliente: {} | Pool: {}", adminUsername, clientId, connectionPoolSize);

               Keycloak keycloak = KeycloakBuilder.builder()
                         .serverUrl(keycloakUrl)
                         .realm("master")
                         .clientId(clientId)
                         .username(adminUsername)
                         .password(adminPassword)
                         // Reducir pool de conexiones para ahorrar memoria
                         .resteasyClient(
                                   new org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl()
                                             .connectionPoolSize(connectionPoolSize)
                                             .connectionTTL(30, java.util.concurrent.TimeUnit.SECONDS)
                                             .build())
                         .build();

               keycloak.tokenManager().getAccessToken();
               log.info("✅ Keycloak conectado (Pool: {} conexiones)", connectionPoolSize);

               return keycloak;

          } catch (Exception e) {
               log.error("❌ Error conectando a Keycloak: {}", e.getMessage());
               throw new IllegalStateException("No se pudo conectar a Keycloak", e);
          }
     }
}
