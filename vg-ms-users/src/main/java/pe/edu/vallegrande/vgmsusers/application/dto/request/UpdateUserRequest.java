package pe.edu.vallegrande.vgmsusers.application.dto.request;

import jakarta.validation.constraints.Email;
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
public class UpdateUserRequest {
     private String firstName;
     private String lastName;
     private DocumentType documentType;
     private String documentNumber;

     @Email(message = "Email must be valid")
     private String email;

     private String phone;
     private String address;
     private Role role;
     private String organizationId;
     private String zoneId;
     private String streetId;
}
