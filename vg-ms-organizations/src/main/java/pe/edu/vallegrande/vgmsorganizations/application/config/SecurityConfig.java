package pe.edu.vallegrande.vgmsorganizations.application.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import reactor.core.publisher.Mono;
import jakarta.annotation.PostConstruct;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri:}")
    private String jwkSetUri;

    @PostConstruct
    public void logSecurityConfiguration() {
        if (issuerUri.isEmpty()) {
            log.error("JWT issuer-uri no está configurado. La validación de tokens puede fallar.");
        }
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .cors(ServerHttpSecurity.CorsSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint((exchange, ex) -> {
                            log.warn("Authentication failed: {}", ex.getMessage());
                            return Mono.error(ex);
                        })
                        .accessDeniedHandler((exchange, ex) -> {
                            log.warn("Access denied - Path: {}", exchange.getRequest().getPath());
                            return Mono.error(ex);
                        }))
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers("/actuator/**")
                        .permitAll()
                        .pathMatchers("/api/admin/fare/**").permitAll()
                        .pathMatchers("/api/admin/parameters/**").permitAll()
                        .pathMatchers("/api/admin/**")
                        .hasAnyAuthority("ADMIN", "ROLE_ADMIN", "SUPER_ADMIN", "ROLE_SUPER_ADMIN", "SUPERADMIN",
                                "ROLE_SUPERADMIN")
                        // Endpoints de super-admin - requiere rol SUPER_ADMIN (con o sin prefijo ROLE_)
                        // También acepta /management/** por si el Gateway reenvía sin /api
                        .pathMatchers("/api/management/**", "/management/**")
                        .hasAnyAuthority("SUPER_ADMIN", "ROLE_SUPER_ADMIN", "SUPERADMIN", "ROLE_SUPERADMIN")
                        // Endpoints internos - SIN autenticación (comunicación interna entre
                        // microservicios)
                        .pathMatchers("/api/internal/**").permitAll()
                        // Todos los demás endpoints requieren autenticación
                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(this::extractAuthorities);
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }

    @SuppressWarnings("unchecked")
    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        List<String> allRoles = new java.util.ArrayList<>();

        // Opción 1: realm_access.roles (Keycloak estándar)
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj instanceof List) {
                List<String> roles = (List<String>) rolesObj;
                if (roles != null && !roles.isEmpty()) {
                    allRoles.addAll(roles);
                }
            }
        }

        // Opción 2: resource_access (roles de cliente específico)
        Map<String, Object> resourceAccess = jwt.getClaimAsMap("resource_access");
        if (resourceAccess != null) {
            for (Map.Entry<String, Object> entry : resourceAccess.entrySet()) {
                Object clientObj = entry.getValue();
                if (clientObj instanceof Map) {
                    Map<String, Object> client = (Map<String, Object>) clientObj;
                    Object rolesObj = client.get("roles");
                    if (rolesObj instanceof List) {
                        List<String> roles = (List<String>) rolesObj;
                        if (roles != null && !roles.isEmpty()) {
                            allRoles.addAll(roles);
                        }
                    }
                }
            }
        }

        // Opción 3: claim "roles" directo
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null && !roles.isEmpty()) {
            allRoles.addAll(roles);
        }

        // Opción 4: claim "authorities"
        List<String> authorities = jwt.getClaimAsStringList("authorities");
        if (authorities != null && !authorities.isEmpty()) {
            allRoles.addAll(authorities);
        }

        if (!allRoles.isEmpty()) {
            return allRoles.stream()
                    .map(role -> new SimpleGrantedAuthority(role))
                    .map(GrantedAuthority.class::cast)
                    .toList();
        }

        return List.of();
    }
}
