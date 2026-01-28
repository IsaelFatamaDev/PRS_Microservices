package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentServiceRoute {

     @Value("${payments.service.url}")
     private String paymentsServiceUrl;

     private static final String REWRITE_REGEX = "/(?<segment>.*)";
     private static final String ADMIN_PREFIX = "/jass/ms-payments/api/admin";
     private static final String CLIENT_PREFIX = "/jass/ms-payments/api/client";

     @Bean
     public RouteLocator paymentsServiceRoutes(RouteLocatorBuilder builder) {
          return builder.routes()
                    // ==================== ADMIN ROUTES (ADMIN) ====================
                    // Obtener todos los pagos enriquecidos
                    .route("admin-payments-enriched-list", route -> route
                              .path("/admin/payments/enriched")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        ADMIN_PREFIX + "/${segment}"))
                              .uri(paymentsServiceUrl))

                    // Obtener un pago específico enriquecido por ID
                    .route("admin-payments-enriched-by-id", route -> route
                              .path("/admin/payments/*/enriched")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        ADMIN_PREFIX + "/${segment}"))
                              .uri(paymentsServiceUrl))

                    // Crear un nuevo pago
                    .route("admin-payments-create", route -> route
                              .path("/admin/payments")
                              .and().method("POST")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        ADMIN_PREFIX + "/${segment}"))
                              .uri(paymentsServiceUrl))

                    // Actualizar un pago por ID
                    .route("admin-payments-update", route -> route
                              .path("/admin/payments/*")
                              .and().method("PUT")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        ADMIN_PREFIX + "/${segment}"))
                              .uri(paymentsServiceUrl))

                    // Eliminar un pago específico por ID
                    .route("admin-payments-delete-by-id", route -> route
                              .path("/admin/payments/*")
                              .and().method("DELETE")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        ADMIN_PREFIX + "/${segment}"))
                              .uri(paymentsServiceUrl))

                    // Eliminar todos los pagos
                    .route("admin-payments-delete-all", route -> route
                              .path("/admin/payments")
                              .and().method("DELETE")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        ADMIN_PREFIX + "/${segment}"))
                              .uri(paymentsServiceUrl))

                    // ==================== CLIENT ROUTES (CLIENT) ====================
                    // Obtener todos los pagos del cliente
                    .route("client-payments-list", route -> route
                              .path("/client/payments")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/client" + REWRITE_REGEX,
                                        CLIENT_PREFIX + "/${segment}"))
                              .uri(paymentsServiceUrl))

                    // Obtener un pago específico del cliente por ID
                    .route("client-payments-by-id", route -> route
                              .path("/client/payments/*")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/client" + REWRITE_REGEX,
                                        CLIENT_PREFIX + "/${segment}"))
                              .uri(paymentsServiceUrl))

                    // ==================== HEALTH CHECKS ====================
                    .route("payments-health", route -> route
                              .path("/health/payments")
                              .filters(f -> f.rewritePath("/health/payments", "/jass/ms-payments/api/actuator/health"))
                              .uri(paymentsServiceUrl))

                    .build();
     }
}