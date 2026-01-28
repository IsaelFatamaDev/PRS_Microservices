package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InfrastructureServiceRoute {

        @Value("${infrastructure.service.url}")
        private String waterBoxesServiceUrl;

        private static final String REWRITE_REGEX = "/(?<segment>.*)";
        private static final String TARGET_PREFIX = "/api/admin";
        private static final String INTERNAL_PREFIX = "/api/internal";

        @Bean
        public RouteLocator waterBoxesServiceRoutes(RouteLocatorBuilder builder) {
                return builder.routes()

                                // ===============================
                                // GESTIÓN DE WATER BOXES
                                // ===============================

                                // Obtener cajas activas
                                .route("admin-water-boxes-active", route -> route
                                                .path("/admin/water-boxes/active")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Obtener cajas inactivas
                                .route("admin-water-boxes-inactive", route -> route
                                                .path("/admin/water-boxes/inactive")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Obtener una caja específica por ID
                                .route("admin-water-boxes-by-id", route -> route
                                                .path("/admin/water-boxes/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Crear una nueva caja
                                .route("admin-water-boxes-create", route -> route
                                                .path("/admin/water-boxes")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Actualizar una caja existente
                                .route("admin-water-boxes-update", route -> route
                                                .path("/admin/water-boxes/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Eliminar una caja
                                .route("admin-water-boxes-delete", route -> route
                                                .path("/admin/water-boxes/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Restaurar una caja eliminada
                                .route("admin-water-boxes-restore", route -> route
                                                .path("/admin/water-boxes/*/restore")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // ===============================
                                // GESTIÓN DE WATER BOX ASSIGNMENTS
                                // ===============================

                                // Obtener asignaciones activas
                                .route("admin-water-box-assignments-active", route -> route
                                                .path("/admin/water-box-assignments/active")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Obtener asignaciones inactivas
                                .route("admin-water-box-assignments-inactive", route -> route
                                                .path("/admin/water-box-assignments/inactive")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Obtener asignación por ID
                                .route("admin-water-box-assignments-by-id", route -> route
                                                .path("/admin/water-box-assignments/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Crear nueva asignación
                                .route("admin-water-box-assignments-create", route -> route
                                                .path("/admin/water-box-assignments")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Actualizar asignación
                                .route("admin-water-box-assignments-update", route -> route
                                                .path("/admin/water-box-assignments/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Eliminar asignación
                                .route("admin-water-box-assignments-delete", route -> route
                                                .path("/admin/water-box-assignments/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Restaurar asignación eliminada
                                .route("admin-water-box-assignments-restore", route -> route
                                                .path("/admin/water-box-assignments/*/restore")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // ===============================
                                // GESTIÓN DE WATER BOX TRANSFERS
                                // ===============================

                                // Obtener todas las transferencias
                                .route("admin-water-box-transfers-all", route -> route
                                                .path("/admin/water-box-transfers")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Obtener transferencia por ID
                                .route("admin-water-box-transfers-by-id", route -> route
                                                .path("/admin/water-box-transfers/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Crear nueva transferencia
                                .route("admin-water-box-transfers-create", route -> route
                                                .path("/admin/water-box-transfers")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // ===============================
                                // HEALTH CHECKS
                                // ===============================

                                .route("water-boxes-health", route -> route
                                                .path("/health/water-boxes")
                                                .filters(f -> f.rewritePath("/health/water-boxes",
                                                                "/api/actuator/health"))
                                                .uri(waterBoxesServiceUrl))

                                // ===============================
                                // ENDPOINTS INTERNOS (SIN JWT)
                                // ===============================

                                // Health check interno
                                .route("internal-health", route -> route
                                                .path("/internal/health")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Test simple interno
                                .route("internal-test", route -> route
                                                .path("/internal/test")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                // Obtener asignación activa por usuario ID
                                .route("internal-user-water-box-assignment", route -> route
                                                .path("/internal/users/*/water-box-assignment")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(waterBoxesServiceUrl))

                                .build();
        }
}
