package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para creación de calle
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStreetRequest {
    
    @NotBlank(message = "El ID de zona es obligatorio")
    private String zoneId;
    
    @NotBlank(message = "El nombre de la calle es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String streetName;
    
    @Size(max = 50, message = "El tipo no puede exceder 50 caracteres")
    private String streetType;
}
