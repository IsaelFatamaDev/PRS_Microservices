package pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response;

import lombok.Builder;

/**
 * Response para la creación de cuenta
 * Se devuelve al MS-users para que guarde el username generado
 */
@Builder
public record CreateAccountResponse(
        String userId,           // ID en Keycloak
        String username,         // Usuario generado: nombre.apellido@jass.gob.pe
        String temporaryPassword, // Contraseña temporal generada
        boolean accountEnabled,  // Si la cuenta está habilitada
        String message          // Mensaje informativo
) {
}