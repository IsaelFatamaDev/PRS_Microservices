package pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request para crear cuenta de usuario desde MS-users
 * Username se genera automáticamente como:
 * primernombre.primerapellido@jass.gob.pe
 * Password se establece como documentNumber del usuario
 */
@Data
public class CreateAccountRequest {

    @NotBlank(message = "userId es obligatorio")
    private String userId;

    // Username se genera automáticamente en formato email:
    // nombre.apellido@jass.gob.pe
    // Ejemplo: "José María García López" -> "jose.garcia@jass.gob.pe"

    @NotBlank(message = "firstName es obligatorio")
    private String firstName;

    @NotBlank(message = "lastName es obligatorio")
    private String lastName;

    @NotBlank(message = "email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @NotBlank(message = "organizationId es obligatorio")
    private String organizationId;

    // Contraseña temporal (debe ser el documentNumber del usuario)
    // Si no se proporciona, se genera una automáticamente
    private String temporaryPassword; // Debe ser el documentNumber del usuario

    private String[] roles = { "CLIENT" }; // Por defecto CLIENT
}
