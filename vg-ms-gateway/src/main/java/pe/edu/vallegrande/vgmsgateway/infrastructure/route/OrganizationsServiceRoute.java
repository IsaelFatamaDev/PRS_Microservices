package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrganizationsServiceRoute {

        @Value("${organizations.service.url}")
        private String organizationsServiceUrl;

        private static final String REWRITE_REGEX = "/(?<segment>.*)";
        private static final String MANAGEMENT_PREFIX = "/jass/ms-organization/api/management";
        private static final String ADMIN_PREFIX = "/jass/ms-organization/api/admin";
        private static final String INTERNAL_PREFIX = "/jass/ms-organization/api/internal";

        @Bean
        public RouteLocator organizationsServiceRoutes(RouteLocatorBuilder builder) {
                return builder.routes()
                                // ==================== DEBUG ROUTE ====================
                                .route("debug-organizations", route -> route
                                                .path("/debug/organizations")
                                                .filters(f -> f.rewritePath("/debug/organizations",
                                                                MANAGEMENT_PREFIX + "/organizations"))
                                                .uri(organizationsServiceUrl))

                                // ==================== MANAGEMENT ROUTES (SUPER_ADMIN) ====================
                                // 📊 Organizaciones con administradores
                                .route("management-organizations-admins", route -> route
                                                .path("/management/organizations/admins")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 📈 Estadísticas generales
                                .route("management-organizations-statistics", route -> route
                                                .path("/management/organizations/statistics")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 🚀 Organizaciones ligeras (sin zonas/calles)
                                .route("management-organizations-light", route -> route
                                                .path("/management/organizations/light")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 🖼️ Servir logos de organizaciones
                                .route("management-organizations-logos", route -> route
                                                .path("/management/organizations/logos/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 📄 Organizaciones paginadas
                                .route("management-organizations-paginated", route -> route
                                                .path("/management/organizations/paginated")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("management-organizations-create-admin", route -> route
                                                .path("/management/organizations/*/create-admin")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("management-organizations-restore", route -> route
                                                .path("/management/organizations/*/restore")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("management-organizations-by-id", route -> route
                                                .path("/management/organizations/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("management-organizations-update", route -> route
                                                .path("/management/organizations/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("management-organizations-delete", route -> route
                                                .path("/management/organizations/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("management-organizations-create", route -> route
                                                .path("/management/organizations")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("management-organizations-list", route -> route
                                                .path("/management/organizations")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                MANAGEMENT_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // ==================== ADMIN ROUTES (ADMIN) ====================
                                .route("admin-zones-by-organization", route -> route
                                                .path("/admin/zones/organization/*")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-zones-restore", route -> route
                                                .path("/admin/zones/*/restore")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-zones-by-id", route -> route
                                                .path("/admin/zones/*")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-zones", route -> route
                                                .path("/admin/zones")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-streets-by-zone", route -> route
                                                .path("/admin/streets/zone/*")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-streets-restore", route -> route
                                                .path("/admin/streets/*/restore")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-streets-by-id", route -> route
                                                .path("/admin/streets/*")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-streets", route -> route
                                                .path("/admin/streets")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-organization-summary", route -> route
                                                .path("/admin/organization/*/summary")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-organization", route -> route
                                                .path("/admin/organization/*")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // ==================== FARE API ROUTES ====================
                                .route("admin-fare-restore", route -> route
                                                .path("/admin/fare/restore/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-fare-delete", route -> route
                                                .path("/admin/fare/delete/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-fare-by-zone", route -> route
                                                .path("/admin/fare/zone/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-fare-update", route -> route
                                                .path("/admin/fare/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-fare-by-id", route -> route
                                                .path("/admin/fare/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-fare-create", route -> route
                                                .path("/admin/fare")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                .route("admin-fare-list", route -> route
                                                .path("/admin/fare")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // ==================== PARAMETER MANAGEMENT ROUTES ====================
                                // ♻ Restaurar parámetro
                                .route("management-parameters-restore", route -> route
                                                .path("/admin/parameters/restore/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // ❌ Eliminación lógica (inactivar)
                                .route("management-parameters-delete", route -> route
                                                .path("/admin/parameters/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 📝 Actualizar parámetro por ID
                                .route("management-parameters-update", route -> route
                                                .path("/admin/parameters/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 🔍 Buscar parámetro por ID
                                .route("management-parameters-by-id", route -> route
                                                .path("/admin/parameters/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // ➕ Crear parámetro
                                .route("management-parameters-create", route -> route
                                                .path("/admin/parameters")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 📋 Listar todos los parámetros
                                .route("management-parameters-list", route -> route
                                                .path("/admin/parameters")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                ADMIN_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // ==================== INTERNAL API ROUTES ====================
                                // 📊 Organizaciones con estadísticas por ID
                                .route("internal-organizations-stats-by-id", route -> route
                                                .path("/internal/organizations/*/stats")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 📈 Todas las organizaciones con estadísticas
                                .route("internal-organizations-stats", route -> route
                                                .path("/internal/organizations/stats")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 🔍 Organización completa por ID (con zonas, calles, parámetros)
                                .route("internal-organizations-complete", route -> route
                                                .path("/internal/organizations/*/complete")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 🚀 Organización ligera por ID (sin zonas/calles)
                                .route("internal-organizations-by-id", route -> route
                                                .path("/internal/organizations/*")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // 📋 Todas las organizaciones ligeras (sin zonas/calles)
                                .route("internal-organizations-list", route -> route
                                                .path("/internal/organizations")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(organizationsServiceUrl))

                                // ==================== HEALTH CHECKS ====================
                                .route("organizations-health", route -> route
                                                .path("/health/organizations")
                                                .filters(f -> f.rewritePath("/health/organizations",
                                                                "/jass/ms-organization/api/actuator/health"))
                                                .uri(organizationsServiceUrl))

                                .build();
        }
}
