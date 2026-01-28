package pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request para cambio de contraseña
 */
@Data
public class ChangePasswordRequest {

     @NotBlank(message = "La contraseña actual es obligatoria")
     private String currentPassword;

     @NotBlank(message = "La nueva contraseña es obligatoria")
     @Size(min = 8, message = "La nueva contraseña debe tener al menos 8 caracteres")
     private String newPassword;

     @NotBlank(message = "La confirmación de contraseña es obligatoria")
     private String confirmPassword;
}
