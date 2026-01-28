package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IUserServiceClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserServiceClientImpl implements IUserServiceClient {

    private final WebClient userServiceWebClient;

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "userExistsFallback")
    public Mono<Boolean> userExists(String userId) {
        return userServiceWebClient.get()
                .uri("/api/users/{id}/exists", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(exists -> log.debug("Usuario {} existe: {}", userId, exists))
                .doOnError(error -> log.error("Error verificando usuario {}: {}", userId, error.getMessage()))
                .onErrorReturn(false);
    }

    @Override
    @CircuitBreaker(name = "userService", fallbackMethod = "createAdminFallback")
    public Mono<Map<String, Object>> createAdmin(String organizationId, String username, String email) {
        Map<String, Object> request = Map.of(
                "organizationId", organizationId,
                "username", username,
                "email", email,
                "role", "ADMIN_JASS");

        return userServiceWebClient.post()
                .uri("/api/users/create-admin")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Admin creado para organización: {}", organizationId))
                .doOnError(error -> log.error("Error creando admin: {}", error.getMessage()));
    }

    // Fallback methods
    private Mono<Boolean> userExistsFallback(String userId, Exception ex) {
        log.warn("Circuit breaker activated for userExists. Returning false. Error: {}", ex.getMessage());
        return Mono.just(false);
    }

    private Mono<Map<String, Object>> createAdminFallback(String organizationId, String username, String email,
            Exception ex) {
        log.warn("Circuit breaker activated for createAdmin. Error: {}", ex.getMessage());
        return Mono.just(Map.of("success", false, "message", "Servicio de usuarios no disponible"));
    }
}
