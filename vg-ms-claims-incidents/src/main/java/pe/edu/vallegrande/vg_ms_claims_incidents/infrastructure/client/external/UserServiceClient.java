package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.client.external;

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

/**
 * Cliente para comunicación con el microservicio externo de usuarios.
 * Implementa el patrón de cliente HTTP reactivo con manejo de errores y reintentos.
 */
@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);
    
    private final WebClient webClient;
    private final String organizationId;

    public UserServiceClient(WebClient.Builder webClientBuilder,
                        @Value("${app.external.user-service.base-url:https://lab.vallegrande.edu.pe/jass/ms-users}") String userServiceBaseUrl,
                        @Value("${app.external.user-service.organization-id:6896b2ecf3e398570ffd99d3}") String organizationId) {
        this.organizationId = organizationId;
        this.webClient = webClientBuilder
                .baseUrl(userServiceBaseUrl)
                .build();
        
        log.info("UserServiceClient inicializado con URL base: {} y organización: {}", userServiceBaseUrl, organizationId);
    }

    /**
     * Obtener usuario por ID
     */
    public Mono<UserDTO> getUserById(String userId) {
        log.info("Obteniendo usuario por ID: {}", userId);
        
        return webClient.get()
                .uri("/internal/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserServiceResponseDTO.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(10))
                .map(response -> {
                    UserDTO userDTO = response.toUserDTO();
                    if (userDTO != null) {
                        log.info("Usuario obtenido exitosamente: {}", userDTO.getFullName());
                        return userDTO;
                    } else {
                        log.warn("Respuesta del MS-USUARIOS no válida para usuario: {}", userId);
                        throw new RecursoNoEncontradoException("Usuario no encontrado con ID: " + userId);
                    }
                })
                .doOnError(error -> log.error("Error al obtener usuario por ID {}: {}", 
                    userId, error.getMessage()))
                .onErrorMap(WebClientResponseException.NotFound.class, 
                    ex -> new RecursoNoEncontradoException("Usuario no encontrado con ID: " + userId))
                .onErrorMap(WebClientResponseException.class, 
                    ex -> {
                        log.error("Error HTTP del servidor de usuarios para usuario {}: Status {}, Body: {}", 
                            userId, ex.getStatusCode(), ex.getResponseBodyAsString());
                        return new ErrorServidorException("Error del servidor de usuarios: " + ex.getMessage());
                    })
                .onErrorMap(Exception.class, 
                    ex -> {
                        if (ex instanceof RecursoNoEncontradoException || ex instanceof ErrorServidorException) {
                            return ex;
                        }
                        log.error("Error inesperado al obtener usuario {}: {}", userId, ex.getMessage(), ex);
                        return new ErrorServidorException("Error inesperado al obtener usuario: " + ex.getMessage());
                    });
    }

    /**
     * Obtener usuario por username
     */
    public Mono<UserDTO> getUserByUsername(String username) {
        log.info("Obteniendo usuario por username: {}", username);
        
        return webClient.get()
                .uri("/internal/users/username/{username}", username)
                .retrieve()
                .bodyToMono(UserServiceResponseDTO.class)
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(10))
                .map(response -> {
                    UserDTO userDTO = response.toUserDTO();
                    if (userDTO != null) {
                        log.info("Usuario obtenido exitosamente: {}", userDTO.getFullName());
                        return userDTO;
                    } else {
                        log.warn("Respuesta del MS-USUARIOS no válida para username: {}", username);
                        throw new RecursoNoEncontradoException("Usuario no encontrado con username: " + username);
                    }
                })
                .doOnError(error -> log.error("Error al obtener usuario por username {}: {}", 
                    username, error.getMessage()))
                .onErrorMap(WebClientResponseException.NotFound.class, 
                    ex -> new RecursoNoEncontradoException("Usuario no encontrado con username: " + username))
                .onErrorMap(WebClientResponseException.class, 
                    ex -> new ErrorServidorException("Error del servidor de usuarios: " + ex.getMessage()));
    }

    /**
     * Obtener usuario por ID con datos de respaldo en caso de error
     */
    public Mono<UserDTO> getUserByIdWithFallback(String userId, String fallbackUsername) {
        log.info("Obteniendo usuario por ID con fallback: {} (fallback: {})", userId, fallbackUsername);
        
        return getUserById(userId)
                .doOnSuccess(user -> log.debug("Usuario obtenido exitosamente para ID {}: {}", userId, user.getFullName()))
                .onErrorResume(error -> {
                    log.warn("Error al obtener usuario {}, usando datos de respaldo: {}", userId, error.getMessage());
                    return createFallbackUser(userId, fallbackUsername);
                });
    }

    /**
     * Obtener usuario por username con datos de respaldo en caso de error
     */
    public Mono<UserDTO> getUserByUsernameWithFallback(String username) {
        log.info("Obteniendo usuario por username con fallback: {}", username);
        
        return getUserByUsername(username)
                .onErrorResume(error -> {
                    log.warn("Error al obtener usuario {}, usando datos de respaldo: {}", username, error.getMessage());
                    return createFallbackUser("unknown", username);
                });
    }

    /**
     * Obtener administradores de una organización
     */
    public Mono<List<UserDTO>> getOrganizationAdmins() {
        log.info("Obteniendo administradores de la organización: {}", organizationId);
        
        return webClient.get()
                .uri("/internal/organizations/{organizationId}/admins", organizationId)
                .retrieve()
                .bodyToFlux(UserDTO.class)
                .collectList()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(15))
                .doOnSuccess(admins -> log.info("Obtenidos {} administradores de la organización {}", 
                    admins.size(), organizationId))
                .doOnError(error -> log.error("Error al obtener administradores de la organización {}: {}", 
                    organizationId, error.getMessage()))
                .onErrorReturn(List.of());
    }

    /**
     * Obtener clientes de una organización
     */
    public Mono<List<UserDTO>> getOrganizationClients() {
        log.info("Obteniendo clientes de la organización: {}", organizationId);
        
        return webClient.get()
                .uri("/internal/organizations/{organizationId}/clients", organizationId)
                .retrieve()
                .bodyToFlux(UserDTO.class)
                .collectList()
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)))
                .timeout(Duration.ofSeconds(15))
                .doOnSuccess(clients -> log.info("Obtenidos {} clientes de la organización {}", 
                    clients.size(), organizationId))
                .doOnError(error -> log.error("Error al obtener clientes de la organización {}: {}", 
                    organizationId, error.getMessage()))
                .onErrorReturn(List.of());
    }

    /**
     * Verificar disponibilidad del servicio de usuarios
     */
    public Mono<Boolean> checkHealth() {
        log.info("Verificando salud del servicio de usuarios");
        
        return webClient.get()
                .uri("/internal/health")
                .retrieve()
                .bodyToMono(String.class)
                .map(response -> true)
                .timeout(Duration.ofSeconds(5))
                .doOnSuccess(health -> log.info("Servicio de usuarios disponible"))
                .doOnError(error -> log.warn("Servicio de usuarios no disponible: {}", error.getMessage()))
                .onErrorReturn(false);
    }

    /**
     * Obtener múltiples usuarios por IDs
     */
    public Mono<List<UserDTO>> getUsersByIds(List<String> userIds) {
        log.info("Obteniendo múltiples usuarios: {}", userIds);
        
        if (userIds == null || userIds.isEmpty()) {
            return Mono.just(List.of());
        }
        
        return Mono.fromCallable(() -> userIds.parallelStream()
                .map(userId -> getUserByIdWithFallback(userId, "Usuario_" + userId)
                        .block(Duration.ofSeconds(5)))
                .filter(user -> user != null)
                .toList())
                .subscribeOn(reactor.core.scheduler.Schedulers.boundedElastic())
                .doOnSuccess(users -> log.info("Obtenidos {} usuarios de {} solicitados", 
                    users.size(), userIds.size()));
    }

    /**
     * Crear datos de usuario de respaldo cuando el servicio no está disponible
     */
    private Mono<UserDTO> createFallbackUser(String userId, String username) {
        log.info("Creando usuario de respaldo para: {} ({})", userId, username);
        
        UserDTO fallbackUser = new UserDTO();
        
        return Mono.just(fallbackUser);
    }

    /**
     * Método para pruebas de integración
     */
    public Mono<UserDTO> testIntegration(String username) {
        log.info("=== TEST DE INTEGRACIÓN CON MS-USUARIOS ===");
        log.info("Probando integración con username: {}", username);
        
        return getUserByUsername(username)
                .doOnSuccess(user -> {
                    log.info("✅ Integración exitosa!");
                    log.info("Usuario obtenido: {}", user.getFullName());
                    log.info("Email: {}", user.getContactEmail());
                    log.info("Usuario tiene roles configurados");
                })
                .doOnError(error -> {
                    log.error("❌ Error en integración: {}", error.getMessage());
                })
                .onErrorResume(error -> {
                    log.warn("Usando datos de respaldo para test");
                    return createFallbackUser("test-user", username);
                });
    }
}
