package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para actualización de organización
 * Todos los campos son opcionales (actualización parcial)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateOrganizationRequest {
    
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String organizationName;
    
    @Size(min = 3, max = 100, message = "El representante legal debe tener entre 3 y 100 caracteres")
    private String legalRepresentative;
    
    @Size(max = 200, message = "La dirección no puede exceder 200 caracteres")
    private String address;
    
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener 9 dígitos")
    private String phone;
    
    private String logo; // Base64 o URL
}
