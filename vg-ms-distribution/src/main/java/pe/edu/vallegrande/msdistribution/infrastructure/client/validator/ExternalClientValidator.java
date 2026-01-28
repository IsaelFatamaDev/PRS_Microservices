package pe.edu.vallegrande.msdistribution.infrastructure.client.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

/**
 * Validador de responses de clientes externos
 * Valida timeout, formato y reglas de negocio
 */
@Component
@Slf4j
public class ExternalClientValidator {
    
    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(5);
    
    public <T> Mono<T> validateResponse(Mono<T> response, String serviceName) {
        return response
            .timeout(DEFAULT_TIMEOUT)
            .doOnError(error -> log.error("Error validating response from {}: {}", 
                serviceName, error.getMessage()))
            .onErrorResume(error -> {
                log.error("External service {} failed validation", serviceName);
                return Mono.empty();
            });
    }
    
    public Mono<Boolean> validateNotNull(Object value, String fieldName) {
        if (value == null) {
            log.warn("Validation failed: {} is null", fieldName);
            return Mono.just(false);
        }
        return Mono.just(true);
    }
    
    public Mono<Boolean> validateTimeout(Duration duration) {
        return Mono.just(duration.compareTo(DEFAULT_TIMEOUT) <= 0);
    }
}
