package pe.edu.vallegrande.msdistribution.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;

import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Configuración de seguridad para el microservicio de distribución.
 * 
 * FUNCIONALIDAD:
 * - Autenticación mediante JWT (JSON Web Tokens)
 * - Integración con Keycloak como proveedor OAuth2
 * - Extracción de roles desde el token JWT
 * - Control de acceso basado en roles (RBAC)
 * 
 * RUTAS PÚBLICAS (sin autenticación):
 * - /actuator/** - Endpoints de monitoreo y health check
 * - /api/public/** - Endpoints públicos de consulta
 * 
 * RUTAS PROTEGIDAS (requieren JWT):
 * - /internal/** - Endpoints administrativos (requiere rol ADMIN)
 * 
 * FLUJO DE AUTENTICACIÓN:
 * 1. Cliente envía JWT en header: Authorization: Bearer <token>
 * 2. Spring Security valida el token con Keycloak
 * 3. Se extraen los roles del claim "realm_access.roles"
 * 4. Se convierten a GrantedAuthority con prefijo "ROLE_"
 * 5. Se evalúan las anotaciones @PreAuthorize
 * 
 * @author Valle Grande
 * @version 2.0.0
 */
@Configuration
@EnableReactiveMethodSecurity
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad para WebFlux.
     * 
     * CONFIGURACIÓN:
     * - CSRF deshabilitado (API REST stateless)
     * - Rutas públicas: /actuator/**, /api/public/**
     * - Todas las demás rutas requieren autenticación JWT
     * - OAuth2 Resource Server con validación JWT
     * 
     * @param http Configurador de seguridad HTTP
     * @return Cadena de filtros de seguridad configurada
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchange -> exchange
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/api/public/**").permitAll()
                .anyExchange().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(grantedAuthoritiesExtractor()))
            );
        return http.build();
    }

    /**
     * Extrae los roles del JWT y los convierte en GrantedAuthority.
     * 
     * PROCESO:
     * 1. Lee el claim "realm_access" del JWT de Keycloak
     * 2. Extrae el array "roles" dentro de realm_access
     * 3. Convierte cada rol a formato Spring Security: "ROLE_<NOMBRE>"
     * 4. Ejemplo: "admin" -> "ROLE_ADMIN"
     * 
     * ESTRUCTURA DEL JWT:
     * {
     *   "realm_access": {
     *     "roles": ["admin", "user"]
     *   }
     * }
     * 
     * @return Convertidor de JWT a AbstractAuthenticationToken
     */
    @SuppressWarnings("unchecked")
    private Converter<Jwt, Mono<AbstractAuthenticationToken>> grantedAuthoritiesExtractor() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Object realmAccessObj = jwt.getClaims().get("realm_access");
            if (realmAccessObj == null) {
                return List.of();
            }
            Map<String, Object> realmAccess = (Map<String, Object>) realmAccessObj;
            if (realmAccess.isEmpty()) {
                return List.of();
            }
            Object rolesObj = realmAccess.get("roles");
            if (rolesObj == null) {
                return List.of();
            }
            return ((List<String>) rolesObj).stream()
                    .map(roleName -> "ROLE_" + roleName.toUpperCase())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}