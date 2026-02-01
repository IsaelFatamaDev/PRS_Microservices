package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import lombok.Data;

@Data
public class OrganizationRequest {

    @jakarta.validation.constraints.NotBlank(message = "Organization name is required")
    @jakarta.validation.constraints.Size(min = 3, max = 100, message = "Organization name must be between 3 and 100 characters")
    private String organizationName;

    @jakarta.validation.constraints.NotBlank(message = "Legal representative is required")
    private String legalRepresentative;

    @jakarta.validation.constraints.NotBlank(message = "Address is required")
    private String address;

    @jakarta.validation.constraints.NotBlank(message = "Phone is required")
    @jakarta.validation.constraints.Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Phone must be valid")
    private String phone;

    private String logo;
}