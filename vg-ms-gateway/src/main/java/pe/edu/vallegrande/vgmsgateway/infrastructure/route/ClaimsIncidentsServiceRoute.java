package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClaimsIncidentsServiceRoute {

     @Value("${claim-incidents.service.url}")
     private String claimsIncidentsServiceUrl;

     private static final String REWRITE_REGEX = "/(?<segment>.*)";
     private static final String TARGET_PREFIX = "/jass/ms-claim-incidents/api/admin";

    @Bean
    public RouteLocator claimsIncidentsServiceRoutes(RouteLocatorBuilder builder) {
        return builder.routes()

                // ==================== RUTAS ADMIN (SUPER_ADMIN, ADMIN) ====================

                // CRUD de Incidentes
                .route("claims-incidents-admin-incidents-list", route -> route
                        .path("/admin/incidents")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-create-direct", route -> route
                        .path("/admin/incidents")
                        .and().method("POST")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-get-direct", route -> route
                        .path("/admin/incidents/{id}")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-get", route -> route
                        .path("/admin/claims-incidents/incidents/{id}")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-create", route -> route
                        .path("/admin/claims-incidents/incidents")
                        .and().method("POST")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-update", route -> route
                        .path("/admin/claims-incidents/incidents/{id}")
                        .and().method("PUT")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-delete", route -> route
                        .path("/admin/claims-incidents/incidents/{id}")
                        .and().method("DELETE")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // Añadir la ruta que falta para actualizar incidencias directamente en /admin/incidents/{id}
                .route("claims-incidents-admin-incidents-update-direct", route -> route
                        .path("/admin/incidents/{id}")
                        .and().method("PUT")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-delete-direct", route -> route
                        .path("/admin/incidents/{id}")
                        .and().method("DELETE")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-restore-direct", route -> route
                        .path("/admin/incidents/{id}/restore")
                        .and().method("PATCH")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // Gestión especial de incidentes
                .route("claims-incidents-admin-incidents-restore", route -> route
                        .path("/admin/claims-incidents/incidents/{id}/restore")
                        .and().method("PATCH")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-assign", route -> route
                        .path("/admin/claims-incidents/incidents/assign")
                        .and().method("PUT")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-resolve", route -> route
                        .path("/admin/claims-incidents/incidents/resolve")
                        .and().method("PUT")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-manage", route -> route
                        .path("/admin/claims-incidents/incidents/manage")
                        .and().method("PUT")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // Consultas especializadas de incidentes
                .route("claims-incidents-admin-incidents-zone", route -> route
                        .path("/admin/claims-incidents/incidents/zone/{zoneId}")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-severity", route -> route
                        .path("/admin/claims-incidents/incidents/severity/{severity}")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-status", route -> route
                        .path("/admin/claims-incidents/incidents/status/{status}")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-assigned", route -> route
                        .path("/admin/claims-incidents/incidents/assigned/{userId}")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-organization", route -> route
                        .path("/admin/claims-incidents/incidents/organization/{organizationId}")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // Incidentes enriquecidos
                .route("claims-incidents-admin-incidents-enriched", route -> route
                        .path("/admin/claims-incidents/incidents/enriched")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-incidents-enriched-by-id", route -> route
                        .path("/admin/claims-incidents/incidents/{id}/enriched")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // Gestión de tipos de incidencias - ADMIN
                .route("claims-incidents-admin-types-list", route -> route
                        .path("/admin/incident-types")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-types-get", route -> route
                        .path("/admin/incident-types/{id}")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-types-create", route -> route
                        .path("/admin/incident-types")
                        .and().method("POST")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-types-update", route -> route
                        .path("/admin/incident-types/{id}")
                        .and().method("PUT")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-types-delete", route -> route
                        .path("/admin/incident-types/{id}")
                        .and().method("DELETE")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-types-restore", route -> route
                        .path("/admin/incident-types/{id}/restore")
                        .and().method("PATCH")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // Gestión de usuarios - ADMIN
                .route("claims-incidents-admin-users-admins", route -> route
                        .path("/admin/users/admins")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-users-clients", route -> route
                        .path("/admin/users/clients")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-users-by-id", route -> route
                        .path("/admin/users/{userId}")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-users-by-username", route -> route
                        .path("/admin/users/username/{username}")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // Gestión de resoluciones - ADMIN
                .route("claims-incidents-admin-resolutions-list", route -> route
                        .path("/admin/incident-resolutions")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-resolutions-get", route -> route
                        .path("/admin/incident-resolutions/{id}")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-resolutions-create", route -> route
                        .path("/admin/incident-resolutions")
                        .and().method("POST")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-resolutions-update", route -> route
                        .path("/admin/incident-resolutions/{id}")
                        .and().method("PUT")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-resolutions-delete", route -> route
                        .path("/admin/incident-resolutions/{id}")
                        .and().method("DELETE")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-resolutions-by-incident", route -> route
                        .path("/admin/incident-resolutions/incident/{incidentId}")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-resolutions-resolve-incident", route -> route
                        .path("/admin/incident-resolutions/resolve-incident/{incidentId}")
                        .and().method("POST")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-resolutions-stats", route -> route
                        .path("/admin/incident-resolutions/stats")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // Test y utilidades - ADMIN
                .route("claims-incidents-admin-test", route -> route
                        .path("/admin/test")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                .route("claims-incidents-admin-test-user-integration", route -> route
                        .path("/admin/test/user-integration/{username}")
                        .and().method("GET")
                        .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(claimsIncidentsServiceUrl))

                // ==================== RUTAS PÚBLICAS (Sin autenticación) ====================

                // Health check del microservicio
                .route("claims-incidents-health-check", route -> route
                        .path("/health/claims-incidents")
                        .filters(f -> f.rewritePath("/health/claims-incidents",
                                "/actuator/health"))
                        .uri(claimsIncidentsServiceUrl))

                .build();
    }
}