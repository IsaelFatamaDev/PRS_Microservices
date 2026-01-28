package pe.edu.vallegrande.vgmsauthentication.infrastructure.client.external;

import reactor.core.publisher.Mono;

import java.util.Set;

/**
 * Cliente para integración con MS-users usando WebClient
 */
public interface UsersClient {

    /**
     * Obtiene información de usuario por email desde MS-users
     */
    Mono<UserData> getUserByEmail(String email);

    /**
     * NUEVO: Obtiene información de usuario por USERNAME desde MS-users
     * Este es el método que necesitamos para el login
     */
    Mono<UserData> getUserByUsername(String username);

    /**
     * Record para datos de usuario desde MS-users
     */
    record UserData(
            String id,
            String userCode,
            String username,
            String firstName,
            String lastName,
            String email,
            String organizationId,
            Set<String> roles,
            String status) {

    }
}
