package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DistributionServiceRoute {

        @Value("${distribution.service.url}")
        private String distributionServiceUrl;

        private static final String REWRITE_REGEX = "/(?<segment>.*)";
        private static final String TARGET_PREFIX = "/internal";

        @Bean
        public RouteLocator distributionServiceRoutes(RouteLocatorBuilder builder) {
                return builder.routes()

                                // ==================== DASHBOARD & STATISTICS ====================
                                .route("internal-dashboard-stats", route -> route
                                                .path("/internal/dashboard/stats")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-dashboard-summary", route -> route
                                                .path("/internal/dashboard/summary")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                // ==================== PROGRAMS ====================
                                .route("internal-programs-list", route -> route
                                                .path("/internal/program")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-list-org", route -> route
                                                .path("/internal/program")
                                                .and().method("GET")
                                                .and().query("organizationId")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-enriched", route -> route
                                                .path("/internal/program/enriched")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-create", route -> route
                                                .path("/internal/program")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-by-id", route -> route
                                                .path("/internal/program/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-update", route -> route
                                                .path("/internal/program/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-delete", route -> route
                                                .path("/internal/program/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-activate", route -> route
                                                .path("/internal/program/activate/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-deactivate", route -> route
                                                .path("/internal/program/deactivate/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-physical-delete", route -> route
                                                .path("/internal/program/physical/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                // ==================== ROUTES ====================
                                .route("internal-routes-list", route -> route
                                                .path("/internal/route")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-routes-list-org", route -> route
                                                .path("/internal/route")
                                                .and().method("GET")
                                                .and().query("organizationId")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-routes-active", route -> route
                                                .path("/internal/route/active")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-routes-create", route -> route
                                                .path("/internal/route")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-routes-by-id", route -> route
                                                .path("/internal/route/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-routes-update", route -> route
                                                .path("/internal/route/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-routes-delete", route -> route
                                                .path("/internal/route/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-routes-activate", route -> route
                                                .path("/internal/route/activate/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-routes-deactivate", route -> route
                                                .path("/internal/route/deactivate/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                // ==================== SCHEDULES ====================
                                .route("internal-schedules-list", route -> route
                                                .path("/internal/schedule")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-schedules-list-org", route -> route
                                                .path("/internal/schedule")
                                                .and().method("GET")
                                                .and().query("organizationId")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-schedules-active", route -> route
                                                .path("/internal/schedule/active")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-schedules-create", route -> route
                                                .path("/internal/schedule")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-schedules-by-id", route -> route
                                                .path("/internal/schedule/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-schedules-update", route -> route
                                                .path("/internal/schedule/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-schedules-delete", route -> route
                                                .path("/internal/schedule/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-schedules-activate", route -> route
                                                .path("/internal/schedule/activate/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-schedules-deactivate", route -> route
                                                .path("/internal/schedule/deactivate/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                // ==================== WATER DELIVERY STATUS ====================
                                .route("internal-programs-water-status-update", route -> route
                                                .path("/internal/program/*/water-status")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-mark-with-water", route -> route
                                                .path("/internal/program/*/mark-with-water")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-mark-without-water", route -> route
                                                .path("/internal/program/*/mark-without-water")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-by-water-status", route -> route
                                                .path("/internal/program/water-status/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-with-water", route -> route
                                                .path("/internal/program/with-water")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-without-water", route -> route
                                                .path("/internal/program/without-water")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                .route("internal-programs-water-stats", route -> route
                                                .path("/internal/program/water-stats")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(distributionServiceUrl))

                                // ==================== HEALTH CHECK ====================
                                .route("distribution-health", route -> route
                                                .path("/health/distribution")
                                                .filters(f -> f.rewritePath("/health/distribution",
                                                                "/jass/ms-distribution/actuator/health"))
                                                .uri(distributionServiceUrl))

                                .build();
        }
}