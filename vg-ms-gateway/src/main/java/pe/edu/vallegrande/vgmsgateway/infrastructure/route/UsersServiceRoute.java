package pe.edu.vallegrande.vgmsgateway.infrastructure.route;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UsersServiceRoute {

        @Value("${users.service.url}")
        private String usersServiceUrl;

        private static final String REWRITE_REGEX = "/(?<segment>.*)";
        private static final String TARGET_PREFIX = "/api";
        private static final String INTERNAL_PREFIX = "/internal";

        @Bean
        public RouteLocator usersServiceRoutes(RouteLocatorBuilder builder) {
                return builder.routes()
                                // ==================== MANAGEMENT ROUTES (SUPER_ADMIN) ====================
                                .route("management-admins-create", r -> r.path("/management/admins")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("management-admins-list", r -> r.path("/management/admins")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("management-admins-active", r -> r.path("/management/admins/active")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("management-admins-inactive", r -> r.path("/management/admins/inactive")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("management-admins-delete-permanent", r -> r
                                                .path("/management/admins/*/permanent")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("management-admins-restore", r -> r.path("/management/admins/*/restore")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                // Gestión general de usuarios
                                .route("management-users-status", r -> r.path("/management/users/*/status")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("management-users-delete", r -> r.path("/management/users/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                // Códigos de usuario
                                .route("management-user-codes-reset", r -> r.path("/management/user-codes/reset/*")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                // Estadísticas
                                .route("management-stats", r -> r.path("/management/stats")
                                                .filters(f -> f.rewritePath("/management" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/management/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== ADMIN ROUTES (ADMIN) ====================
                                // Gestión de clientes
                                .route("admin-clients-create", r -> r.path("/admin/clients")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-list", r -> r.path("/admin/clients")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-by-id", r -> r.path("/admin/clients/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-update", r -> r.path("/admin/clients/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-delete", r -> r.path("/admin/clients/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-status", r -> r.path("/admin/clients/*/status")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-restore", r -> r.path("/admin/clients/*/restore")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-active", r -> r.path("/admin/clients/active")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-inactive", r -> r.path("/admin/clients/inactive")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-clients-all", r -> r.path("/admin/clients/all")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                // Códigos de usuario para admins
                                .route("admin-user-codes-generate", r -> r.path("/admin/user-codes/generate")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-user-codes-next", r -> r.path("/admin/user-codes/next")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== OPERATOR MANAGEMENT ROUTES (ADMIN) ====================
                                .route("admin-operators-create", r -> r.path("/admin/operators")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-list", r -> r.path("/admin/operators")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-by-id", r -> r.path("/admin/operators/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-update", r -> r.path("/admin/operators/*")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-delete", r -> r.path("/admin/operators/*")
                                                .and().method("DELETE")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-status", r -> r.path("/admin/operators/*/status")
                                                .and().method("PATCH")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-restore", r -> r.path("/admin/operators/*/restore")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-active", r -> r.path("/admin/operators/active")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-inactive", r -> r.path("/admin/operators/inactive")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operators-all", r -> r.path("/admin/operators/all")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operator-codes-generate", r -> r.path("/admin/operator-codes/generate")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("admin-operator-codes-next", r -> r.path("/admin/operator-codes/next")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/admin" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/admin/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== CLIENT ROUTES (CLIENT) ====================
                                .route("client-profile-get", r -> r.path("/client/profile")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/client" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/client/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("client-profile-update", r -> r.path("/client/profile")
                                                .and().method("PUT")
                                                .filters(f -> f.rewritePath("/client" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/client/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("client-profile-by-code", r -> r.path("/client/profile/code/*")
                                                .filters(f -> f.rewritePath("/client" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/client/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("client-profile-status", r -> r.path("/client/profile/status")
                                                .filters(f -> f.rewritePath("/client" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/client/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("client-user-code", r -> r.path("/client/user-code")
                                                .filters(f -> f.rewritePath("/client" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/client/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== COMMON ROUTES ====================
                                .route("common-health", r -> r.path("/common/health")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-ping", r -> r.path("/common/ping")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-roles", r -> r.path("/common/roles")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-user-basic-info", r -> r.path("/common/user/code/*/basic")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-user-exists", r -> r.path("/common/user/code/*/exists")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-user-email-available", r -> r.path("/common/user/email/*/available")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-user-by-username", r -> r.path("/common/user/username/*")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-setup-first-user", r -> r.path("/common/setup/first-user")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-reniec-dni", r -> r.path("/common/users/reniec/dni/*")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-profile", r -> r.path("/common/profile/**")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== VERIFICATION ROUTES ====================
                                .route("common-user-dni-exists", r -> r.path("/common/user/dni/*/exists")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-user-phone-exists", r -> r.path("/common/user/phone/*/exists")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-user-email-exists", r -> r.path("/common/user/email/*/exists")
                                                .filters(f -> f.rewritePath("/common" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== INTERNAL ROUTES ====================
                                .route("internal-organization-users", r -> r.path("/internal/organizations/*/users")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("internal-organization-clients", r -> r.path("/internal/organizations/*/clients")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("internal-organization-admins", r -> r.path("/internal/organizations/*/admins")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("internal-users-by-id", r -> r.path("/internal/users/*")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("internal-create-admin", r -> r.path("/internal/organizations/*/create-admin")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("internal-clients-by-id", r -> r.path("/internal/clients/*")
                                                .and().method("GET")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("internal-create-super-admin", r -> r.path("/internal/create-super-admin")
                                                .and().method("POST")
                                                .filters(f -> f.rewritePath("/internal" + REWRITE_REGEX,
                                                                INTERNAL_PREFIX + "/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== LEGACY SUPPORT ====================
                                .route("common-users-me", r -> r.path("/common/users/me")
                                                .filters(f -> f.rewritePath("/common/users" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/user/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-users-profile", r -> r.path("/common/users/profile/**")
                                                .filters(f -> f.rewritePath("/common/users" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/user/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-users-update-profile", r -> r.path("/common/users/update-profile")
                                                .filters(f -> f.rewritePath("/common/users" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/user/${segment}"))
                                                .uri(usersServiceUrl))

                                .route("common-users-change-password", r -> r.path("/common/users/change-password")
                                                .filters(f -> f.rewritePath("/common/users" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/common/user/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== MIGRATION ROUTES ====================
                                .route("migration-routes", r -> r.path("/migration/**")
                                                .filters(f -> f.rewritePath("/migration" + REWRITE_REGEX,
                                                                TARGET_PREFIX + "/migration/${segment}"))
                                                .uri(usersServiceUrl))

                                // ==================== HEALTH CHECKS ====================
                                .route("users-health", r -> r.path("/health/users")
                                                .filters(f -> f.rewritePath("/health/users",
                                                                "/actuator/health"))
                                                .uri(usersServiceUrl))

                                .build();
        }
}
