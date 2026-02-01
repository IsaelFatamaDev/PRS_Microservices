package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserCreateRequest {
    @jakarta.validation.constraints.NotBlank(message = "First name is required")
    private String firstName;

    @jakarta.validation.constraints.NotBlank(message = "Last name is required")
    private String lastName;

    @jakarta.validation.constraints.NotBlank(message = "Document type is required")
    private String documentType;

    @jakarta.validation.constraints.NotBlank(message = "Document number is required")
    private String documentNumber;

    @jakarta.validation.constraints.NotBlank(message = "Email is required")
    @jakarta.validation.constraints.Email(message = "Email must be valid")
    private String email;

    private String phone;

    private String address;

    private String organizationId;
    private String streetId;
    private String zoneId;
    private List<String> roles;

    // Helper method to get full name
    public String getName() {
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        } else if (firstName != null) {
            return firstName;
        } else if (lastName != null) {
            return lastName;
        }
        return "";
    }
}
