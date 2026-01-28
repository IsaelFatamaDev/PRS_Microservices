package pe.edu.vallegrande.vgmsauthentication.application.services;

import reactor.core.publisher.Mono;

/**
 * Servicio para integración con el microservicio de usuarios
 */
public interface UserIntegrationService {

    /**
     * Obtener información completa del usuario por username
     */
    Mono<UserInfo> getUserByUsername(String username);

    /**
     * Obtener información del usuario por ID
     */
    Mono<UserInfo> getUserById(String userId);

    /**
     * Obtener información del usuario por código
     */
    Mono<UserInfo> getUserByCode(String userCode);

    /**
     * Validar si un usuario existe y está activo
     */
    Mono<Boolean> validateUserExists(String email);

    /**
     * DTO para información del usuario desde el MS de usuarios
     */
    record UserInfo(
            String id,
            String userCode,
            String firstName,
            String lastName,
            String email,
            String phone,
            String organizationId,
            String streetId,
            String zoneId,
            java.util.Set<String> roles,
            String status,
            java.time.LocalDateTime createdAt,
            java.time.LocalDateTime updatedAt) {
        /**
         * Genera el username basado en primer nombre y primer apellido
         */
        public String generateUsername() {
            if (firstName == null || lastName == null) {
                return null;
            }

            String firstNamePart = firstName.toLowerCase().trim().split("\\s+")[0];
            String lastNamePart = lastName.toLowerCase().trim().split("\\s+")[0];

            return firstNamePart + "." + lastNamePart + "@jass.gob.pe";
        }

        /**
         * Verifica si el usuario está activo
         */
        public boolean isActive() {
            return "ACTIVE".equals(status);
        }
    }
}
