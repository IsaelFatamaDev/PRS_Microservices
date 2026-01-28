package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.LocalDate;

/**
 * DTO para la actualización parcial de un programa de distribución.
 * Todos los campos son opcionales.
 * 
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class DistributionProgramUpdateRequest {

    /** ID de la organización. */
    String organizationId;

    /** ID del horario. */
    String scheduleId;

    /** ID de la ruta. */
    String routeId;

    /** ID de la zona. */
    String zoneId;

    /** ID de la calle. */
    String streetId;

    /** Fecha del programa. */
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate programDate;

    /** Hora planificada de inicio. */
    String plannedStartTime;

    /** Hora planificada de fin. */
    String plannedEndTime;

    /** Hora real de inicio. */
    String actualStartTime;

    /** Hora real de fin. */
    String actualEndTime;

    /** Usuario responsable. */
    String responsibleUserId;

    /** Observaciones. */
    String observations;
}
