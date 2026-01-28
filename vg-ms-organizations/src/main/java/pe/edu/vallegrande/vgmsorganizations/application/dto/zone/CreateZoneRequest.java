package pe.edu.vallegrande.vgmsorganizations.application.dto.zone;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateZoneRequest {

    @NotBlank(message = "El ID de organización es obligatorio")
    private String organizationId;

    @NotBlank(message = "El nombre de zona es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String zoneName;

    @NotBlank(message = "El código de zona es obligatorio")
    @Pattern(regexp = "^[A-Z0-9]{2,10}$", message = "El código debe tener entre 2 y 10 caracteres alfanuméricos")
    private String zoneCode;

    private String description;

    @NotNull(message = "La tarifa mensual es obligatoria")
    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa debe ser mayor a 0")
    private BigDecimal currentMonthlyFee;

    private Double latitude;
    private Double longitude;

    @Min(value = 0, message = "El total de cajas debe ser mayor o igual a 0")
    private Integer totalWaterBoxes;

    @Min(value = 0, message = "Las cajas activas deben ser mayor o igual a 0")
    private Integer activeWaterBoxes;

    @NotBlank(message = "El usuario creador es obligatorio")
    private String createdBy;
}
