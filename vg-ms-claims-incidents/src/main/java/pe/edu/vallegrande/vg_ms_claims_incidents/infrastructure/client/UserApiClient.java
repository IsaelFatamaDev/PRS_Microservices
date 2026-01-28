package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.UserDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.UserServiceResponseDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.exception.custom.RecursoNoEncontradoException;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.exception.custom.ErrorServidorException;

import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Component
public class UserApiClient {

    private static final Logger log = LoggerFactory.getLogger(UserApiClient.class);
    
    private final WebClient webClient;

    public UserApiClient(WebClient.Builder webClientBuilder,
                        @Value("${app.external.user-service.base-url:https://lab.vallegrande.edu.pe/jass/ms-gateway}") String apiGatewayBaseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(apiGatewayBaseUrl)
                .build();
        
        log.info("UserApiClient inicializado con API Gateway: {}", apiGatewayBaseUrl);
    }

    /**
     * Obtener todos los clientes de una organización (usuarios del sistema)
     * GET /admin/clients/all?organizationId={orgId}
     */
    public Mono<List<UserDTO>> getClientsByOrganization(String organizationId) {
        log.info("Obteniendo clientes de la organización: {}", organizationId);
        
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/admin/clients/all")
                        .queryParam("organizationId", organizationId)
                        .build())
                .retrieve()
                .bodyToFlux(UserDTO.class)
                .collectList()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(15))
                .doOnSuccess(clients -> log.info("✅ Obtenidos {} clientes de la organización {}", 
                    clients.size(), organizationId))
                .doOnError(error -> log.error("❌ Error al obtener clientes de la organización {}: {}", 
                    organizationId, error.getMessage()))
                .onErrorMap(WebClientResponseException.class, 
                    ex -> {
                        log.error("Error HTTP al obtener clientes: Status {}, Body: {}", 
                            ex.getStatusCode(), ex.getResponseBodyAsString());
                        return new ErrorServidorException("Error al obtener usuarios: " + ex.getMessage());
                    })
                .onErrorReturn(List.of());
    }

    /**
     * Obtener usuario por ID
     */
    public Mono<UserDTO> getUserById(String userId, String organizationId) {
        log.info("Obteniendo usuario por ID: {} de organización: {}", userId, organizationId);
        
        return getClientsByOrganization(organizationId)
                .flatMapMany(users -> reactor.core.publisher.Flux.fromIterable(users))
                .filter(user -> userId.equals(user.getUserId()))
                .next()
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoException("Usuario no encontrado con ID: " + userId)))
                .doOnSuccess(user -> log.info("✅ Usuario obtenido: {}", user.getFullName()))
                .doOnError(error -> log.error("❌ Error al obtener usuario por ID {}: {}", 
                    userId, error.getMessage()));
    }

    /**
     * Obtener usuario por username
     */
    public Mono<UserDTO> getUserByUsername(String username, String organizationId) {
        log.info("Obteniendo usuario por username: {} de organización: {}", username, organizationId);
        
        return getClientsByOrganization(organizationId)
                .flatMapMany(users -> reactor.core.publisher.Flux.fromIterable(users))
                .filter(user -> username.equals(user.getUsername()))
                .next()
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoException("Usuario no encontrado con username: " + username)))
                .doOnSuccess(user -> log.info("✅ Usuario obtenido: {}", user.getFullName()))
                .doOnError(error -> log.error("❌ Error al obtener usuario por username {}: {}", 
                    username, error.getMessage()));
    }

    /**
     * Obtener usuario por ID con datos de respaldo en caso de error
     */
    public Mono<UserDTO> getUserByIdWithFallback(String userId, String organizationId, String fallbackUsername) {
        log.info("Obteniendo usuario por ID con fallback: {} (fallback: {})", userId, fallbackUsername);
        
        return getUserById(userId, organizationId)
                .doOnSuccess(user -> log.debug("Usuario obtenido exitosamente para ID {}: {}", userId, user.getFullName()))
                .onErrorResume(error -> {
                    log.warn("Error al obtener usuario {}, usando datos de respaldo: {}", userId, error.getMessage());
                    return createFallbackUser(userId, fallbackUsername);
                });
    }

    /**
     * Obtener usuario por username con datos de respaldo en caso de error
     */
    public Mono<UserDTO> getUserByUsernameWithFallback(String username, String organizationId) {
        log.info("Obteniendo usuario por username con fallback: {}", username);
        
        return getUserByUsername(username, organizationId)
                .onErrorResume(error -> {
                    log.warn("Error al obtener usuario {}, usando datos de respaldo: {}", username, error.getMessage());
                    return createFallbackUser("unknown", username);
                });
    }

    /**
     * Obtener administradores de una organización
     * Filtra usuarios con rol ADMIN
     */
    public Mono<List<UserDTO>> getOrganizationAdmins(String organizationId) {
        log.info("Obteniendo administradores de la organización: {}", organizationId);
        
        return getClientsByOrganization(organizationId)
                .map(users -> users.stream()
                        .filter(user -> user.getRoles() != null && 
                                user.getRoles().stream().anyMatch(role -> "ADMIN".equalsIgnoreCase(role)))
                        .toList())
                .doOnSuccess(admins -> log.info("✅ Obtenidos {} administradores de la organización {}", 
                    admins.size(), organizationId))
                .doOnError(error -> log.error("❌ Error al obtener administradores de la organización {}: {}", 
                    organizationId, error.getMessage()))
                .onErrorReturn(List.of());
    }

    /**
     * Verificar disponibilidad del API Gateway
     */
    public Mono<Boolean> checkHealth() {
        log.info("Verificando salud del API Gateway");
        
        return webClient.get()
                .uri("/actuator/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(health -> log.info("✅ API Gateway disponible"))
                .doOnError(error -> log.warn("⚠️ API Gateway no disponible: {}", error.getMessage()))
                .onErrorReturn(false);
    }

    /**
     * Obtener múltiples usuarios por IDs
     */
    public Mono<List<UserDTO>> getUsersByIds(List<String> userIds, String organizationId) {
        log.info("Obteniendo múltiples usuarios: {}", userIds);
        
        if (userIds == null || userIds.isEmpty()) {
            return Mono.just(List.of());
        }
        
        return getClientsByOrganization(organizationId)
                .map(allUsers -> allUsers.stream()
                        .filter(user -> userIds.contains(user.getUserId()))
                        .toList())
                .doOnSuccess(users -> log.info("✅ Obtenidos {} usuarios de {} solicitados", 
                    users.size(), userIds.size()));
    }

    /**
     * Crear datos de usuario de respaldo cuando el servicio no está disponible
     */
    private Mono<UserDTO> createFallbackUser(String userId, String username) {
        log.info("Creando usuario de respaldo para: {} ({})", userId, username);
        
        // Crear usuario básico de respaldo
        UserDTO fallbackUser = new UserDTO();
        
        return Mono.just(fallbackUser);
    }

    /**
     * Método para pruebas de integración
     */
    public Mono<List<UserDTO>> testIntegration(String organizationId) {
        log.info("=== TEST DE INTEGRACIÓN CON API GATEWAY ===");
        log.info("Probando integración con organizationId: {}", organizationId);
        
        return getClientsByOrganization(organizationId)
                .doOnSuccess(users -> {
                    log.info("✅ Integración exitosa!");
                    log.info("Usuarios obtenidos: {}", users.size());
                    users.stream().limit(3).forEach(user -> 
                        log.info("  - {}: {} ({})", user.getUserId(), user.getFullName(), user.getUsername()));
                })
                .doOnError(error -> {
                    log.error("❌ Error en integración: {}", error.getMessage());
                })
                .onErrorReturn(List.of());
    }
}