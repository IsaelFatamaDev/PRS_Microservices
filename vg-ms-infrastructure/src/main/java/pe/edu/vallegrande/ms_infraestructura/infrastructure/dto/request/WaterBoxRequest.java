package pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.BoxType;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaterBoxRequest {
    @NotBlank(message = "El ID de la organización no puede estar vacío.")
    @JsonProperty("organizationId")
    private String organizationId;

    @NotBlank(message = "El código de la caja no puede estar vacío.")
    @Size(max = 50, message = "El código de la caja no puede exceder los 50 caracteres.")
    @JsonProperty("boxCode")
    private String boxCode;

    @NotNull(message = "El tipo de caja no puede ser nulo.")
    @JsonProperty("boxType")
    private BoxType boxType;

    @NotNull(message = "La fecha de instalación no puede ser nula.")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("installationDate")
    private LocalDate installationDate;

    @JsonProperty("currentAssignmentId")
    private Long currentAssignmentId;

    @JsonProperty("status")
    private Status status;
}
