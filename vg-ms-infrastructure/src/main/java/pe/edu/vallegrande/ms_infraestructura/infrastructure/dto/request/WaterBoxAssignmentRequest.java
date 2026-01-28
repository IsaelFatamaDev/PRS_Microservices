package pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaterBoxAssignmentRequest {

    @NotNull(message = "El ID de la caja de agua no puede ser nulo.")
    @JsonProperty("waterBoxId")
    private Long waterBoxId;

    @NotBlank(message = "El ID del usuario no puede estar vacío.")
    @Size(max = 50, message = "El ID del usuario no puede exceder los 50 caracteres.")
    @JsonProperty("userId")
    private String userId;

    @NotNull(message = "La fecha de inicio no puede ser nula.")
    @JsonProperty("startDate")
    private OffsetDateTime startDate;

    @JsonProperty("endDate")
    private OffsetDateTime endDate;

    @NotNull(message = "La tarifa mensual no puede ser nula.")
    @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa mensual debe ser mayor que 0.")
    @JsonProperty("monthlyFee")
    private BigDecimal monthlyFee;

    @JsonProperty("status")
    private String status;
}
