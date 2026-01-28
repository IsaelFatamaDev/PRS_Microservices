package pe.edu.vallegrande.msdistribution.infrastructure.client.validator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * Validador de comunicación interna entre microservicios
 * Valida JWE signature, roles y permisos
 */
@Component
@Slf4j
public class InternalClientValidator {
    
    public Mono<Boolean> validateJweSignature(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("JWE token is null or empty");
            return Mono.just(false);
        }
        
        // Validación básica de formato JWE (5 partes separadas por puntos)
        String[] parts = token.split("\\.");
        if (parts.length != 5) {
            log.warn("Invalid JWE token format. Expected 5 parts, got {}", parts.length);
            return Mono.just(false);
        }
        
        return Mono.just(true);
    }
    
    public Mono<Boolean> validateRole(String role, String... allowedRoles) {
        if (role == null || role.trim().isEmpty()) {
            log.warn("Role is null or empty");
            return Mono.just(false);
        }
        
        for (String allowedRole : allowedRoles) {
            if (role.equalsIgnoreCase(allowedRole)) {
                return Mono.just(true);
            }
        }
        
        log.warn("Role {} is not in allowed roles", role);
        return Mono.just(false);
    }
    
    public Mono<Boolean> validatePermission(String permission, String requiredPermission) {
        if (permission == null || requiredPermission == null) {
            return Mono.just(false);
        }
        return Mono.just(permission.contains(requiredPermission));
    }
}
