package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryServiceRoute {

     @Value("${inventory.service.url}")
     private String inventoryServiceUrl;

     private static final String REWRITE_REGEX = "/(?<segment>.*)";
     private static final String TARGET_PREFIX = "/api/admin";

     @Bean
     public RouteLocator inventoryServiceRoutes(RouteLocatorBuilder builder) {
          return builder.routes()

                    // =============== MATERIALS/PRODUCTS ROUTES ===============
                    .route("materials-list", route -> route
                              .path("/admin/materials")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("materials-get-by-id", route -> route
                              .path("/admin/materials/{id}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("materials-create", route -> route
                              .path("/admin/materials")
                              .and().method("POST")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("materials-update", route -> route
                              .path("/admin/materials/{id}")
                              .and().method("PUT")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("materials-delete", route -> route
                              .path("/admin/materials/{id}")
                              .and().method("DELETE")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("materials-by-status", route -> route
                              .path("/admin/materials/status/{status}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("materials-by-category", route -> route
                              .path("/admin/materials/category/{categoryId}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("materials-by-category-and-status", route -> route
                              .path("/admin/materials/category/{categoryId}/status/{status}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("materials-restore", route -> route
                              .path("/admin/materials/{id}/restore")
                              .and().method("PATCH")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    // =============== PURCHASES ROUTES ===============
                    .route("purchases-list", route -> route
                              .path("/admin/purchases")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("purchases-by-organization", route -> route
                              .path("/admin/purchases/organization/{organizationId}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("purchases-get-by-id", route -> route
                              .path("/admin/purchases/{id}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("purchases-create", route -> route
                              .path("/admin/purchases")
                              .and().method("POST")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("purchases-update", route -> route
                              .path("/admin/purchases/{id}")
                              .and().method("PUT")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("purchases-delete", route -> route
                              .path("/admin/purchases/{id}")
                              .and().method("DELETE")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("purchases-restore", route -> route
                              .path("/admin/purchases/{id}/restore")
                              .and().method("POST")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("purchases-by-status", route -> route
                              .path("/admin/purchases/status/{status}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("purchases-update-status", route -> route
                              .path("/admin/purchases/{id}/status")
                              .and().method("PATCH")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    // =============== INVENTORY MOVEMENTS ROUTES ===============
                    .route("inventory-movements-by-organization", route -> route
                              .path("/admin/inventory-movements/organization/{organizationId}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    // 🆕 ENDPOINT ENRIQUECIDO CON INFORMACIÓN DE PRODUCTOS Y USUARIOS
                    .route("inventory-movements-by-organization-enriched", route -> route
                              .path("/admin/inventory-movements/organization/{organizationId}/enriched")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("inventory-movements-by-product", route -> route
                              .path("/admin/inventory-movements/organization/{organizationId}/product/{productId}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("inventory-movements-get-by-id", route -> route
                              .path("/admin/inventory-movements/{movementId}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("inventory-movements-create", route -> route
                              .path("/admin/inventory-movements")
                              .and().method("POST")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("inventory-movements-current-stock", route -> route
                              .path("/admin/inventory-movements/stock/{organizationId}/{productId}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("inventory-movements-count", route -> route
                              .path("/admin/inventory-movements/count/{organizationId}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("inventory-movements-consumption", route -> route
                              .path("/admin/inventory-movements/consumption")
                              .and().method("POST")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("inventory-movements-last-movement", route -> route
                              .path("/admin/inventory-movements/last-movement/{organizationId}/{productId}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    // =============== PRODUCT CATEGORIES ROUTES ===============
                    .route("product-categories-list", route -> route
                              .path("/admin/product-categories")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("product-categories-get-by-id", route -> route
                              .path("/admin/product-categories/{id}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("product-categories-create", route -> route
                              .path("/admin/product-categories")
                              .and().method("POST")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("product-categories-update", route -> route
                              .path("/admin/product-categories/{id}")
                              .and().method("PUT")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("product-categories-delete", route -> route
                              .path("/admin/product-categories/{id}")
                              .and().method("DELETE")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("product-categories-by-status", route -> route
                              .path("/admin/product-categories/status/{status}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("product-categories-restore", route -> route
                              .path("/admin/product-categories/{id}/restore")
                              .and().method("PATCH")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    // =============== SUPPLIERS ROUTES ===============
                    .route("suppliers-list", route -> route
                              .path("/admin/suppliers")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("suppliers-get-by-id", route -> route
                              .path("/admin/suppliers/{id}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("suppliers-create", route -> route
                              .path("/admin/suppliers")
                              .and().method("POST")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("suppliers-update", route -> route
                              .path("/admin/suppliers/{id}")
                              .and().method("PUT")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("suppliers-delete", route -> route
                              .path("/admin/suppliers/{id}")
                              .and().method("DELETE")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("suppliers-by-status", route -> route
                              .path("/admin/suppliers/status/{status}")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("suppliers-restore", route -> route
                              .path("/admin/suppliers/{id}/restore")
                              .and().method("PATCH")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    // =============== HEALTH CHECK ROUTES ===============
                    .route("inventory-health-check", route -> route
                              .path("/admin/health")
                              .and().method("GET")
                              .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                        TARGET_PREFIX + "/${segment}"))
                              .uri(inventoryServiceUrl))

                    .route("inventory-health-actuator", route -> route
                              .path("/health/inventory")
                              .filters(f -> f.rewritePath("/health/inventory", "/actuator/health"))
                              .uri(inventoryServiceUrl))

                    .build();
     }
}
