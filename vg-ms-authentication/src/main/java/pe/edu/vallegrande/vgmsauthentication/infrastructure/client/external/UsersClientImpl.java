package pe.edu.vallegrande.vgmsauthentication.infrastructure.client.external;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.ApiResponse;

import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * Cliente optimizado para comunicarse con el microservicio de usuarios
 * - Pool de conexiones reducido (10 max)
 * - Timeouts cortos (3 segundos)
 * - Buffer limitado (256KB)
 */
@Component
@Slf4j
public class UsersClientImpl implements UsersClient {

     private final WebClient webClient;

     public UsersClientImpl(@Value("${app.services.users.url}") String usersServiceUrl,
               @Value("${app.services.users.timeout:3000}") Integer timeout,
               @Value("${app.services.users.max-connections:10}") Integer maxConnections,
               @Value("${app.services.users.max-in-memory-size:262144}") Integer maxInMemorySize) {

          log.info("🔧 Configurando UsersClient: URL={}, Timeout={}ms, MaxConn={}, MaxBuffer={}KB",
                    usersServiceUrl, timeout, maxConnections, maxInMemorySize / 1024);

          // Connection provider optimizado para bajo consumo de memoria
          ConnectionProvider connectionProvider = ConnectionProvider.builder("users-client-pool")
                    .maxConnections(maxConnections)
                    .maxIdleTime(Duration.ofSeconds(10))
                    .maxLifeTime(Duration.ofSeconds(30))
                    .pendingAcquireMaxCount(10)
                    .evictInBackground(Duration.ofSeconds(30))
                    .build();

          // HttpClient con timeouts
          HttpClient httpClient = HttpClient.create(connectionProvider)
                    .responseTimeout(Duration.ofMillis(timeout))
                    .compress(true);

          // WebClient optimizado
          this.webClient = WebClient.builder()
                    .baseUrl(usersServiceUrl)
                    .clientConnector(new ReactorClientHttpConnector(httpClient))
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                    .build();
     }

     @Override
     public Mono<UserData> getUserByEmail(String email) {
          log.debug("Obteniendo usuario por email: {}", email);

          return webClient.get()
                    .uri("/users/email/{email}", email)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .flatMap(response -> {
                         if (response.isSuccess() && response.getData() != null) {
                              return Mono.just(mapToUserData(response.getData()));
                         } else {
                              return Mono.error(new RuntimeException("Usuario no encontrado: " + email));
                         }
                    })
                    .doOnError(error -> log.error("Error obteniendo usuario por email {}: {}", email,
                              error.getMessage()));
     }

     @Override
     public Mono<UserData> getUserByUsername(String username) {
          log.debug("🔍 Obteniendo usuario por username: {}", username);

          return webClient.get()
                    .uri("/common/user/username/{username}", username)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .flatMap(response -> {
                         if (response.isSuccess() && response.getData() != null) {
                              log.info("✅ Usuario encontrado por username: {}", username);
                              return Mono.just(mapToUserData(response.getData()));
                         } else {
                              log.warn("⚠️ Usuario NO encontrado por username: {}", username);
                              return Mono.error(new RuntimeException("Usuario no encontrado: " + username));
                         }
                    })
                    .doOnError(error -> log.error("💥 Error obteniendo usuario por username {}: {}", username,
                              error.getMessage()));
     }

     /**
      * Mapea la respuesta del servicio de usuarios a UserData
      */
     private UserData mapToUserData(Object data) {
          if (data instanceof java.util.Map<?, ?> userMap) {
               @SuppressWarnings("unchecked")
               var map = (java.util.Map<String, Object>) userMap;

               // Extraer roles como lista simple de strings
               @SuppressWarnings("unchecked")
               var rolesData = (java.util.List<String>) map.get("roles");
               var roles = rolesData != null ? java.util.Set.copyOf(rolesData) : java.util.Set.<String>of();

               return new UserData(
                         String.valueOf(map.get("id")),
                         (String) map.get("userCode"),
                         (String) map.get("username"),
                         (String) map.get("firstName"),
                         (String) map.get("lastName"),
                         (String) map.get("email"),
                         String.valueOf(map.get("organizationId")),
                         roles,
                         (String) map.get("status"));
          }

          throw new IllegalArgumentException("Formato de datos de usuario no válido");
     }
}
