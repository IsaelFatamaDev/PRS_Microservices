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
import java.util.List;

/**
 * DTO simplificado para transferencias que crea automáticamente la nueva
 * asignación
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpleWaterBoxTransferRequest {

     @NotNull(message = "El ID de la caja de agua no puede ser nulo.")
     @JsonProperty("waterBoxId")
     private Long waterBoxId;

     @NotBlank(message = "El ID del usuario antiguo no puede estar vacío.")
     @JsonProperty("oldUserId")
     private String oldUserId;

     @NotBlank(message = "El ID del nuevo usuario no puede estar vacío.")
     @JsonProperty("newUserId")
     private String newUserId;

     @NotNull(message = "La fecha de inicio no puede ser nula.")
     @JsonProperty("startDate")
     private OffsetDateTime startDate;

     @NotNull(message = "La tarifa mensual no puede ser nula.")
     @DecimalMin(value = "0.0", inclusive = false, message = "La tarifa mensual debe ser mayor que 0.")
     @JsonProperty("monthlyFee")
     private BigDecimal monthlyFee;

     @NotBlank(message = "La razón de la transferencia no puede estar vacía.")
     @Size(max = 500, message = "La razón de la transferencia no puede exceder los 500 caracteres.")
     @JsonProperty("transferReason")
     private String transferReason;

     @JsonProperty("documents")
     private List<String> documents;
}
