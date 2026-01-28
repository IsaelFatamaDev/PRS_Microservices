package pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * DTO para registrar un usuario en Keycloak desde el microservicio de usuarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterUserRequest {
     private String username;
     private String email;
     private String firstName;
     private String lastName;
     private String temporaryPassword;
     private Set<String> roles;
     private String userCode;
     private String userId;
}
