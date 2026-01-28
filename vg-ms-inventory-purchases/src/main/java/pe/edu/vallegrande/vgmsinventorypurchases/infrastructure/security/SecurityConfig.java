package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverterAdapter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
// ❌ CORS DESHABILITADO - Se maneja completamente en el Gateway
// import org.springframework.web.cors.CorsConfiguration;
// import org.springframework.web.cors.reactive.CorsConfigurationSource;
// import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

     @Bean
     public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
          return http
                    .csrf(ServerHttpSecurity.CsrfSpec::disable)
                    // ❌ CORS COMPLETAMENTE DESHABILITADO - El Gateway maneja todo CORS
                    .cors(ServerHttpSecurity.CorsSpec::disable)
                    .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                    .authorizeExchange(exchanges -> exchanges
                              .pathMatchers(
                                        "/actuator/**",
                                        "/v3/api-docs/**",
                                        "/swagger-ui/**",
                                        "/swagger-ui.html",
                                        "/webjars/**")
                              .permitAll()
                              .pathMatchers("/api/admin/**").hasAuthority("ADMIN")
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

     // ❌ BEAN DE CORS COMPLETAMENTE ELIMINADO
     // El Gateway es el único responsable de manejar CORS
     /*
      * @Bean
      * public CorsConfigurationSource corsConfigurationSource() {
      * CorsConfiguration configuration = new CorsConfiguration();
      * configuration.setAllowedOriginPatterns(List.of("*"));
      * configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE",
      * "PATCH", "OPTIONS", "HEAD"));
      * configuration.setAllowedHeaders(List.of("*"));
      * configuration.setAllowCredentials(true);
      * configuration.setMaxAge(3600L);
      * 
      * UrlBasedCorsConfigurationSource source = new
      * UrlBasedCorsConfigurationSource();
      * source.registerCorsConfiguration("/**", configuration);
      * return source;
      * }
      */

     @SuppressWarnings("unchecked")
     private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
          Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

          if (realmAccess == null) {
               return List.of();
          }

          List<String> roles = (List<String>) realmAccess.get("roles");

          if (roles == null) {
               return List.of();
          }

          return roles.stream()
                    .map(SimpleGrantedAuthority::new)
                    .map(GrantedAuthority.class::cast)
                    .toList();
     }
}
