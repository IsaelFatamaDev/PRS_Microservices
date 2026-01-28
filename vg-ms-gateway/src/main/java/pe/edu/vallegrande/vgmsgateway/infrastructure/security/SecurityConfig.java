package pe.edu.vallegrande.vgmsgateway.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Collections;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(
                Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD", "TRACE", "CONNECT"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Disposition", "X-Total-Count",
                "Access-Control-Allow-Origin", "Access-Control-Allow-Credentials"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.GET).permitAll() // Allow all GET for now, or restrict as needed
                        .pathMatchers("/auth/**", "/api/auth/**").permitAll()
                        .pathMatchers("/actuator/**").permitAll()
                        .pathMatchers("/health/**").permitAll()
                        .pathMatchers("/docs/**", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**",
                                "/swagger-resources/**")
                        .permitAll()
                        .pathMatchers("/").permitAll()

                        .pathMatchers("/api/admin/fare/**").permitAll()

                        // Role based access
                        .pathMatchers("/management/**").hasRole("SUPER_ADMIN")
                        .pathMatchers("/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .pathMatchers("/common/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CLIENT")
                        .pathMatchers("/api/admin/**").hasAnyRole("SUPER_ADMIN", "ADMIN")
                        .pathMatchers("/api/common/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CLIENT")
                        .pathMatchers("/api/user/**", "/api/users/**").hasAnyRole("SUPER_ADMIN", "ADMIN", "CLIENT")

                        .anyExchange().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    @Bean
    public ReactiveJwtAuthenticationConverterAdapter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            try {
                Map<String, Object> realmAccess = jwt.getClaim("realm_access");
                if (realmAccess != null) {
                    @SuppressWarnings("unchecked")
                    List<String> roles = (List<String>) realmAccess.get("roles");

                    if (roles != null) {
                        return roles.stream()
                                .filter(role -> role.equals("SUPER_ADMIN") || role.equals("ADMIN")
                                        || role.equals("CLIENT"))
                                .map(role -> "ROLE_" + role)
                                .map(org.springframework.security.core.authority.SimpleGrantedAuthority::new)
                                .collect(java.util.stream.Collectors.toList());
                    }
                }
                return Collections.emptyList();
            } catch (Exception e) {
                return Collections.emptyList();
            }
        });
        return new ReactiveJwtAuthenticationConverterAdapter(converter);
    }
}
