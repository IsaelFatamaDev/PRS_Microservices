package pe.edu.vallegrande.msdistribution.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Entidad de dominio para DistributionProgram (Programa de Distribución).
 * Representa la programación de la distribución de agua para una fecha y ruta
 * específicas.
 * POJO limpio sin anotaciones de persistencia (DDD).
 * 
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistributionProgram {

    /** Identificador único del programa. */
    private String id;

    /** ID de la organización a la que pertenece el programa. */
    private String organizationId;

    /** Código legible para humanos (ej. PRG001). */
    private String programCode;

    /** ID del horario asignado. */
    private String scheduleId;

    /** ID de la ruta de distribución. */
    private String routeId;

    /** ID de la zona geográfica. */
    private String zoneId;

    /** ID de la calle específica (si aplica). */
    private String streetId;

    /** Fecha programada para la distribución. */
    private LocalDate programDate;

    /** Hora planificada de inicio. */
    private String plannedStartTime;

    /** Hora planificada de fin. */
    private String plannedEndTime;

    /** Hora real de inicio (registrada durante la operación). */
    private String actualStartTime;

    /** Hora real de fin. */
    private String actualEndTime;

    /** Estado del programa (ej. ACTIVE, INACTIVE, CON_AGUA, SIN_AGUA). */
    private String status;

    /** ID del usuario responsable de la programación. */
    private String responsibleUserId;

    /** Observaciones o notas adicionales. */
    private String observations;

    /** Marca de tiempo de creación. */
    private Instant createdAt;

    /** Marca de tiempo de última actualización. */
    private Instant updatedAt;
}