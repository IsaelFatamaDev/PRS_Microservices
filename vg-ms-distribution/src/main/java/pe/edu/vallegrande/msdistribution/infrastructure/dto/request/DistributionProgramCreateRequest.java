package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

/**
 * DTO para la creación de un nuevo programa de distribución.
 * Contiene todos los campos obligatorios para programar una distribución.
 * 
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class DistributionProgramCreateRequest {

    /** ID de la organización. */
    @NotBlank(message = "El ID de organización es obligatorio")
    String organizationId;

    // programCode se genera automáticamente, no se recibe en la solicitud

    /** ID del horario asignado. */
    @NotBlank(message = "El ID de horario es obligatorio")
    String scheduleId;

    /** ID de la ruta asignada. */
    @NotBlank(message = "El ID de ruta es obligatorio")
    String routeId;

    /** ID de la zona. */
    @NotBlank(message = "El ID de zona es obligatorio")
    String zoneId;

    /** ID de la calle. */
    @NotBlank(message = "El ID de calle es obligatorio")
    String streetId;

    /** Fecha programada para la distribución. */
    @NotNull(message = "La fecha del programa es obligatoria")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate programDate;

    /** Hora de inicio planificada (HH:mm). */
    @NotBlank(message = "La hora de inicio planificada es obligatoria")
    String plannedStartTime;

    /** Hora de fin planificada (HH:mm). */
    @NotBlank(message = "La hora de fin planificada es obligatoria")
    String plannedEndTime;

    /** Hora real de inicio (opcional al crear). */
    String actualStartTime;

    /** Hora real de fin (opcional al crear). */
    String actualEndTime;

    /** ID del usuario responsable. */
    @NotBlank(message = "El ID del usuario responsable es obligatorio")
    String responsibleUserId;

    /** Notas generales o sobre la entrega de agua. */
    String observations;

    /** Estado inicial de entrega de agua (ej. CON_AGUA). */
    String waterDeliveryStatus;
}
