package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WaterQualityServiceRoute {

        @Value("${waterquality.service.url}")
        private String waterQualityServiceUrl;

        private static final String REWRITE_REGEX = "/(?<segment>.*)";
        private static final String TARGET_PREFIX = "/jass/ms-water-quality/api/admin";

        @Bean
        public RouteLocator waterQualityServiceRoutes(RouteLocatorBuilder builder) {
                return builder.routes()
                                // ==================== ADMIN ROUTES FOR WATER QUALITY SERVICE
                                // ====================

                                // Sampling Points Routes - Organization specific
                                .route("admin-quality-sampling-points-by-org", route -> route
                                                .path("/admin/quality/sampling-points")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-active-by-org", route -> route
                                                .path("/admin/quality/sampling-points/active")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-inactive-by-org", route -> route
                                                .path("/admin/quality/sampling-points/inactive")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-by-id-and-org", route -> route
                                                .path("/admin/quality/sampling-points")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-create", route -> route
                                                .path("/admin/quality/sampling-points")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-update", route -> route
                                                .path("/admin/quality/sampling-points/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-delete", route -> route
                                                .path("/admin/quality/sampling-points/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-activate", route -> route
                                                .path("/admin/quality/sampling-points/activate/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-deactivate", route -> route
                                                .path("/admin/quality/sampling-points/deactivate/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-sampling-points-debug", route -> route
                                                .path("/admin/quality/sampling-points/debug")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                // Quality Tests Routes - Organization specific
                                .route("admin-quality-tests-by-org", route -> route
                                                .path("/admin/quality/tests")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-tests-by-id-and-org", route -> route
                                                .path("/admin/quality/tests")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-tests-create", route -> route
                                                .path("/admin/quality/tests")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-tests-update", route -> route
                                                .path("/admin/quality/tests/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-tests-delete", route -> route
                                                .path("/admin/quality/tests/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-tests-delete-physical", route -> route
                                                .path("/admin/quality/tests/physical/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-tests-restore", route -> route
                                                .path("/admin/quality/tests/restore/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                // Daily Records Routes - Organization specific
                                .route("admin-quality-daily-records-by-org", route -> route
                                                .path("/admin/quality/daily-records")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-daily-records-by-id-and-org", route -> route
                                                .path("/admin/quality/daily-records/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-daily-records-create", route -> route
                                                .path("/admin/quality/daily-records")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-daily-records-update", route -> route
                                                .path("/admin/quality/daily-records/*")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-daily-records-delete", route -> route
                                                .path("/admin/quality/daily-records/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-daily-records-delete-physical", route -> route
                                                .path("/admin/quality/daily-records/physical/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                .route("admin-quality-daily-records-restore", route -> route
                                                .path("/admin/quality/daily-records/restore/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/${segment}"))
                                                .uri(waterQualityServiceUrl))

                                // ==================== HEALTH CHECK ====================
                                .route("water-quality-health", route -> route
                                                .path("/health/water-quality")
                                                .filters(f -> f.rewritePath("/health/water-quality",
                                                                "/api/actuator/health"))
                                                .uri(waterQualityServiceUrl))

                                .build();
        }
}