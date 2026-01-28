package pe.edu.vallegrande.vgmsorganizations.application.dto.street;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStreetRequest {

     @NotBlank(message = "El ID de zona es obligatorio")
     private String zoneId;

     @NotBlank(message = "El nombre de calle es obligatorio")
     @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
     private String streetName;

     @NotBlank(message = "El código de calle es obligatorio")
     @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "El código debe tener entre 2 y 10 caracteres alfanuméricos")
     private String streetCode;

     private String description;

     @NotBlank(message = "El usuario creador es obligatorio")
     private String createdBy;
}
