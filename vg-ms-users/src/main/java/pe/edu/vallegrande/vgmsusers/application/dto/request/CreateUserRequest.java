package pe.edu.vallegrande.vgmsusers.application.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.DocumentType;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {
     @NotBlank(message = "Username is required")
     private String username;

     @NotBlank(message = "Password is required")
     private String password;

     @NotBlank(message = "First name is required")
     private String firstName;

     @NotBlank(message = "Last name is required")
     private String lastName;

     @NotNull(message = "Document type is required")
     private DocumentType documentType;

     @NotBlank(message = "Document number is required")
     private String documentNumber;

     @NotBlank(message = "Email is required")
     @Email(message = "Email must be valid")
     private String email;

     @NotBlank(message = "Phone is required")
     private String phone;

     private String address;

     @NotNull(message = "Role is required")
     private Role role;

     @NotBlank(message = "Organization ID is required")
     private String organizationId;

     private String zoneId;
     private String streetId;
}
