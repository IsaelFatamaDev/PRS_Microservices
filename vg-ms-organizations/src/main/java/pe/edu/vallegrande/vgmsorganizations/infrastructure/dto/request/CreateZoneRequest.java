package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para creación de zona
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateZoneRequest {
    
    @NotBlank(message = "El ID de organización es obligatorio")
    private String organizationId;
    
    @NotBlank(message = "El nombre de la zona es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String zoneName;
    
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String description;
}
