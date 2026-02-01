package pe.edu.vallegrande.vgmsusers.infrastructure.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

        private final JwtRoleConverter jwtRoleConverter;
        private final CustomAuthenticationEntryPoint authenticationEntryPoint;
        private final CustomAccessDeniedHandler accessDeniedHandler;

        public SecurityConfig(JwtRoleConverter jwtRoleConverter,
                        CustomAuthenticationEntryPoint authenticationEntryPoint,
                        CustomAccessDeniedHandler accessDeniedHandler) {
                this.jwtRoleConverter = jwtRoleConverter;
                this.authenticationEntryPoint = authenticationEntryPoint;
                this.accessDeniedHandler = accessDeniedHandler;
        }

        @Bean
        public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
                return http
                                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                                .authorizeExchange(exchanges -> exchanges
                                                // Endpoints públicos
                                                .pathMatchers("/internal/**").permitAll()
                                                .pathMatchers("/api/users/**").permitAll()
                                                .pathMatchers("/actuator/**").permitAll()
                                                .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html")
                                                .permitAll()
                                                .pathMatchers("/api/common/**").permitAll()
                                                .pathMatchers("/api/management/admins/initial").permitAll() // Crear
                                                                                                            // primer
                                                                                                            // SUPER_ADMIN

                                                // Endpoints protegidos por rol
                                                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                                                .pathMatchers("/api/management/**").hasRole("SUPER_ADMIN")
                                                .pathMatchers("/api/client/**").hasRole("CLIENT")

                                                // Endpoints comunes (requieren autenticación y uno de los roles
                                                // válidos)
                                                // .pathMatchers("/api/common/**").hasAnyRole("ADMIN", "SUPER_ADMIN",
                                                // "CLIENT")

                                                // Cualquier otra petición requiere autenticación
                                                .anyExchange().authenticated())
                                .oauth2ResourceServer(oauth2 -> oauth2
                                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtRoleConverter))
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .exceptionHandling(exception -> exception
                                                .authenticationEntryPoint(authenticationEntryPoint)
                                                .accessDeniedHandler(accessDeniedHandler))
                                .build();
        }
}
