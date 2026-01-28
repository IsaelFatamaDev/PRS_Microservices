package pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request para primer cambio de contraseña (sin autenticación previa)
 */
@Data
public class FirstPasswordChangeRequest {

     @NotBlank(message = "El username es obligatorio")
     private String username;

     @NotBlank(message = "La contraseña temporal es obligatoria")
     private String temporaryPassword;

     @NotBlank(message = "La nueva contraseña es obligatoria")
     @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
     private String newPassword;

     @NotBlank(message = "La confirmación de contraseña es obligatoria")
     private String confirmPassword;
}
