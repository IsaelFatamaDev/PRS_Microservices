package pe.edu.vallegrande.vgmsauthentication.application.services;
import reactor.core.publisher.Mono;
/**
 * Domain Service para gestión automática de Keycloak
 * Maneja la creación/configuración automática de realm, roles, clientes
 */
public interface KeycloakDomainService {
        /**
         * Inicializa completamente Keycloak si no existe
         * - Crea realm si no existe
         * - Crea roles básicos (SUPER_ADMIN, ADMIN, CLIENT)
         * - Crea cliente con configuración de tokens (30min)
         * - Genera secrets automáticamente
         */
        Mono<Void> initializeKeycloakIfNeeded();
        /**
         * Crea un usuario en Keycloak con contraseña temporal
         */
        Mono<UserCreationResult> createUserAccount(String username, String email, String firstName, String lastName,
                        String temporaryPassword);
        /**
         * NUEVO: Verificar si un username está disponible
         */
        Mono<Boolean> isUsernameAvailable(String username);
        /**
         * NUEVO: Renovar contraseña temporal (generar nueva contraseña temporal)
         */
        Mono<String> renewTemporaryPassword(String username);
        /**
         * Asigna roles a un usuario (busca por username)
         */
        Mono<Void> assignRoles(String username, String... roles);
        /**
         * OPTIMIZADO: Asigna roles a un usuario directamente por userId (sin búsqueda)
         */
        Mono<Void> assignRolesByUserId(String userId, String... roles);
        /**
         * Autentica usuario y obtiene tokens
         */
        Mono<TokenInfo> authenticateUser(String username, String password);
        /**
         * Refresca token
         */
        Mono<TokenInfo> refreshToken(String refreshToken);
        /**
         * Valida token
         */
        Mono<Boolean> validateToken(String accessToken);
        /**
         * Marca usuario para cambio obligatorio de contraseña
         */
        Mono<Void> requirePasswordChange(String username);
        /**
         * Cambia la contraseña de un usuario
         */
        Mono<Void> changePassword(String username, String currentPassword, String newPassword);
        /**
         * Obtiene información del usuario desde el token
         */
        Mono<UserInfo> getUserInfoFromToken(String accessToken);
        /**
         * Actualiza atributos personalizados del usuario en Keycloak
         */
        Mono<Void> updateUserAttributes(String username, String organizationId, String userId);
        /**
         * Configura mappers para incluir atributos personalizados en el token
         */
        Mono<Void> configureTokenMappers();
        /**
         * Record para información de tokens
         */
        record TokenInfo(
                        String accessToken,
                        String refreshToken,
                        String tokenType,
                        long expiresIn,
                        long refreshExpiresIn) {
        }
        /**
         * Record para información del usuario
         */
        record UserInfo(
                        String sub,
                        String preferredUsername,
                        String email,
                        String givenName,
                        String familyName) {
        }
        /**
         * NUEVO: Record para resultado de creación de usuario
         */
        record UserCreationResult(
                        String keycloakUserId,
                        String temporaryPassword) {
        }
}
