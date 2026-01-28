package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para crear un nuevo horario de distribución.
 * 
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class DistributionScheduleCreateRequest {

    /** ID de la organización. */
    @NotBlank(message = "El ID de organización es obligatorio")
    String organizationId;

    /** Código de horario (autogenerado si es nulo). */
    String scheduleCode;

    /** ID de la zona. */
    @NotBlank(message = "El ID de zona es obligatorio")
    String zoneId;

    /** ID de la calle. */
    @NotBlank(message = "El ID de calle es obligatorio")
    String streetId;

    /** Nombre del horario. */
    @NotBlank(message = "El nombre de horario es obligatorio")
    String scheduleName;

    /** Días de la semana que aplica el horario. */
    @NotEmpty(message = "Debe incluir al menos un día de la semana")
    @Builder.Default
    List<String> daysOfWeek = new ArrayList<>();

    /** Hora de inicio (HH:mm). */
    @NotBlank(message = "La hora de inicio es obligatoria")
    String startTime;

    /** Hora de fin (HH:mm). */
    @NotBlank(message = "La hora de fin es obligatoria")
    String endTime;

    /** Duración en horas. */
    @NotNull(message = "La duración en horas es obligatoria")
    Integer durationHours;
}