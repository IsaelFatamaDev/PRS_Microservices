package pe.edu.vallegrande.ms_water_quality.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service para extraer información del JWT Token
 * Según estándar PRS01
 */
@Service
@Slf4j
public class JwtService {

    /**
     * Extrae el organizationId del JWT token del usuario autenticado
     * NOTA: El JWT de Keycloak NO contiene organizationId directamente,
     * por lo que se debe obtener del microservicio de usuarios usando el userId (sub)
     * 
     * @return Mono con el organizationId o error si no existe
     */
    public Mono<String> getOrganizationIdFromToken() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth instanceof JwtAuthenticationToken)
            .map(auth -> (JwtAuthenticationToken) auth)
            .map(JwtAuthenticationToken::getToken)
            .flatMap(jwt -> {
                // Primero intenta extraer organizationId directamente del JWT
                try {
                    String orgId = extractOrganizationId(jwt);
                    log.debug("Organization ID extracted from JWT: {}", orgId);
                    return Mono.just(orgId);
                } catch (Exception e) {
                    // Si no está en el JWT, devuelve error para que use el fallback
                    log.warn("organizationId not found in JWT, service should use fallback or call MS-Users");
                    return Mono.error(new RuntimeException("organizationId no encontrado en JWT. Usar parámetro o consultar MS-Users"));
                }
            })
            .switchIfEmpty(Mono.error(new RuntimeException("No se pudo obtener organizationId del token")));
    }

    /**
     * Extrae el userId del JWT token del usuario autenticado
     * 
     * @return Mono con el userId o error si no existe
     */
    public Mono<String> getUserIdFromToken() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth instanceof JwtAuthenticationToken)
            .map(auth -> (JwtAuthenticationToken) auth)
            .map(JwtAuthenticationToken::getToken)
            .map(this::extractUserId)
            .doOnNext(userId -> log.debug("User ID extracted from JWT: {}", userId))
            .switchIfEmpty(Mono.error(new RuntimeException("No se pudo obtener userId del token")));
    }

    /**
     * Extrae el email del JWT token del usuario autenticado
     * 
     * @return Mono con el email o error si no existe
     */
    public Mono<String> getEmailFromToken() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth instanceof JwtAuthenticationToken)
            .map(auth -> (JwtAuthenticationToken) auth)
            .map(JwtAuthenticationToken::getToken)
            .map(jwt -> jwt.getClaimAsString("email"))
            .doOnNext(email -> log.debug("Email extracted from JWT: {}", email))
            .switchIfEmpty(Mono.error(new RuntimeException("No se pudo obtener email del token")));
    }

    /**
     * Obtiene todos los claims del JWT
     * 
     * @return Mono con el JWT completo
     */
    public Mono<Jwt> getJwtToken() {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth instanceof JwtAuthenticationToken)
            .map(auth -> (JwtAuthenticationToken) auth)
            .map(JwtAuthenticationToken::getToken);
    }

    /**
     * Verifica si el usuario tiene un rol específico
     * 
     * @param role Rol a verificar
     * @return Mono<Boolean> true si tiene el rol
     */
    public Mono<Boolean> hasRole(String role) {
        return ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .map(Authentication::getAuthorities)
            .map(authorities -> authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role)))
            .defaultIfEmpty(false);
    }

    /**
     * Extrae el organizationId del JWT
     * Intenta varios claims comunes según el estándar PRS01
     */
    private String extractOrganizationId(Jwt jwt) {
        // Intentar diferentes claims según configuración de Keycloak
        String orgId = jwt.getClaimAsString("organizationId");
        if (orgId != null) return orgId;

        orgId = jwt.getClaimAsString("organization_id");
        if (orgId != null) return orgId;

        orgId = jwt.getClaimAsString("org_id");
        if (orgId != null) return orgId;

        // Si no encuentra, lanzar excepción
        log.error("No se encontró organizationId en el JWT. Claims disponibles: {}", jwt.getClaims().keySet());
        throw new RuntimeException("organizationId no encontrado en el token JWT");
    }

    /**
     * Extrae el userId del JWT
     * Intenta varios claims comunes
     */
    private String extractUserId(Jwt jwt) {
        // Intentar diferentes claims
        String userId = jwt.getClaimAsString("sub");
        if (userId != null) return userId;

        userId = jwt.getClaimAsString("user_id");
        if (userId != null) return userId;

        userId = jwt.getClaimAsString("userId");
        if (userId != null) return userId;

        log.error("No se encontró userId en el JWT. Claims disponibles: {}", jwt.getClaims().keySet());
        throw new RuntimeException("userId no encontrado en el token JWT");
    }
}
