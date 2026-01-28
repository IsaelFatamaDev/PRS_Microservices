package pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request para login de usuario
 */
@Data
public class LoginRequest {

    @NotBlank(message = "username es obligatorio")
    private String username;

    @NotBlank(message = "password es obligatorio")
    private String password;

    private boolean rememberMe = false;
}
