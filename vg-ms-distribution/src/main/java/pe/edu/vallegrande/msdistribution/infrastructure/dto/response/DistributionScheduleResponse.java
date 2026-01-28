package pe.edu.vallegrande.msdistribution.infrastructure.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO de respuesta para horarios de distribución.
 * Provee información detallada del cronograma.
 * 
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class DistributionScheduleResponse {

    /** ID único del horario. */
    String id;

    /** ID de la organización. */
    String organizationId;

    /** Código de horario. */
    String scheduleCode;

    /** ID de la zona. */
    String zoneId;

    /** ID de la calle. */
    String streetId;

    /** Nombre del horario. */
    String scheduleName;

    /** Lista de días de la semana activos. */
    @Builder.Default
    List<String> daysOfWeek = new ArrayList<>();

    /** Hora de inicio. */
    String startTime;

    /** Hora de fin. */
    String endTime;

    /** Duración en horas. */
    Integer durationHours;

    /** Estado del horario. */
    String status;

    /** Fecha de creación. */
    Instant createdAt;
}