package pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WaterBoxTransferRequest {

    @NotNull(message = "El ID de la caja de agua no puede ser nulo.")
    @JsonProperty("waterBoxId")
    private Long waterBoxId;

    @NotNull(message = "El ID de la asignación anterior no puede ser nulo.")
    @JsonProperty("oldAssignmentId")
    private Long oldAssignmentId;

    @NotNull(message = "El ID de la nueva asignación no puede ser nulo.")
    @JsonProperty("newAssignmentId")
    private Long newAssignmentId;

    @NotBlank(message = "La razón de la transferencia no puede estar vacía.")
    @Size(max = 255, message = "La razón de la transferencia no puede exceder los 255 caracteres.")
    @JsonProperty("transferReason")
    private String transferReason;

    @JsonProperty("documents")
    private List<String> documents;

}
