package pe.edu.vallegrande.vgmsorganizations.application.dto.zone;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateZoneFeeRequest {

     @NotNull(message = "La nueva tarifa es obligatoria")
     @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a 0")
     private BigDecimal newFee;

     @NotBlank(message = "El usuario que realiza el cambio es obligatorio")
     private String changedBy;

     @NotBlank(message = "La razón del cambio es obligatoria")
     @Size(min = 10, max = 500, message = "La razón debe tener entre 10 y 500 caracteres")
     private String reason;
}
