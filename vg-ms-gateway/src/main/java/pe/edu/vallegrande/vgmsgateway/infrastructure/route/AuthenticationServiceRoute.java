package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationServiceRoute {

    @Value("${authentication.service.url}")
    private String authenticationServiceUrl;

    private static final String REWRITE_REGEX = "/(?<segment>.*)";
    private static final String TARGET_PREFIX = "/jass/ms-authentication/api/auth";

    @Bean
    public RouteLocator authenticationServiceRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // ✅ CORS eliminado - Se maneja con globalcors
                .route("auth-register", r -> r.path("/auth/register")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-accounts", r -> r.path("/auth/accounts")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-login", r -> r.path("/auth/login")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-refresh", r -> r.path("/auth/refresh")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-validate", r -> r.path("/auth/validate")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-me", r -> r.path("/auth/me")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-logout", r -> r.path("/auth/logout")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-change-password", r -> r.path("/auth/change-password")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-first-password-change", r -> r.path("/auth/first-password-change")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                .route("auth-renew-temporary-password", r -> r.path("/auth/renew-temporary-password")
                        .filters(f -> f.rewritePath("/auth" + REWRITE_REGEX, TARGET_PREFIX + "/${segment}"))
                        .uri(authenticationServiceUrl))

                // Ruta de health check específica de auth
                .route("auth-health-check", r -> r.path("/auth/health")
                        .filters(f -> f.rewritePath("/auth/health", TARGET_PREFIX + "/health"))
                        .uri(authenticationServiceUrl))

                // Ruta de health check del actuator
                .route("auth-health-actuator", r -> r.path("/health/auth")
                        .filters(f -> f.rewritePath("/health/auth", "/actuator/health"))
                        .uri(authenticationServiceUrl))

                .build();
    }
}
