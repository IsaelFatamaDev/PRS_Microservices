package pe.edu.vallegrande.vgmsauthentication.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuración de seguridad para el microservicio de autenticación
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

     @Bean
     public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
          return http
                    .csrf(csrf -> csrf.disable())
                    .authorizeExchange(exchanges -> exchanges
                              // Endpoints públicos de autenticación
                              .pathMatchers("/api/auth/register").permitAll()
                              .pathMatchers("/api/auth/login").permitAll()
                              .pathMatchers("/api/auth/refresh").permitAll()
                              .pathMatchers("/api/auth/health").permitAll() // AGREGADO: Health endpoint público
                              .pathMatchers("/api/auth/first-password-change").permitAll() // AGREGADO: Primer cambio
                                                                                           // de contraseña

                              // Endpoint para registro de usuarios desde MS-USERS
                              .pathMatchers("/api/auth/accounts").permitAll() // CORREGIDO: URL correcta

                              // Actuator y monitoreo
                              .pathMatchers("/actuator/**").permitAll()

                              // Swagger UI
                              .pathMatchers("/swagger-ui/**").permitAll()
                              .pathMatchers("/swagger-ui.html").permitAll()
                              .pathMatchers("/v3/api-docs/**").permitAll()
                              .pathMatchers("/webjars/**").permitAll()

                              // Todo lo demás requiere autenticación
                              .anyExchange().authenticated())
                    .oauth2ResourceServer(oauth2 -> oauth2
                              .jwt(jwt -> {
                              }))
                    .build();
     }
}
