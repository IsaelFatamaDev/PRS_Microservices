package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IAuthenticationClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationClientImpl implements IAuthenticationClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.authentication.url}")
    private String authenticationUrl;

    @Override
    @CircuitBreaker(name = "authenticationService", fallbackMethod = "createCredentialsFallback")
    @Retry(name = "authenticationService")
    public Mono<Void> createCredentials(UUID userId, String username, String password, String role) {
        return webClientBuilder.build()
                .post()
                .uri(authenticationUrl + "/api/auth/credentials")
                .bodyValue(Map.of(
                        "userId", userId.toString(),
                        "username", username,
                        "password", password,
                        "role", role))
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(v -> log.info("Credentials created for user: {}", userId))
                .doOnError(e -> log.error("Error creating credentials for user: {}", userId, e));
    }

    private Mono<Void> createCredentialsFallback(UUID userId, String username, String password, String role,
            Exception e) {
        log.warn("Fallback: Cannot create credentials for user: {} due to: {}", userId, e.getMessage());
        return Mono.empty();
    }
}
