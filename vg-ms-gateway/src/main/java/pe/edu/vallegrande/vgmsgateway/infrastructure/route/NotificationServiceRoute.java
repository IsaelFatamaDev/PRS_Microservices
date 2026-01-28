package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationServiceRoute {

    @Value("${notifications.service.url}")
    private String notificationServiceUrl;

    private static final String REWRITE_REGEX = "/(?<segment>.*)";
    private static final String TARGET_PREFIX = "/api";

    @Bean
    public RouteLocator notificationServiceRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("notifications-service", route -> route
                        .path("/notifications/**")
                        .filters(f -> f.rewritePath("/notifications" + REWRITE_REGEX,
                                TARGET_PREFIX + "/${segment}"))
                        .uri(notificationServiceUrl))
                .build();
    }
}
