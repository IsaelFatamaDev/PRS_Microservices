package pe.edu.vallegrande.msdistribution.infrastructure.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

/**
 * Documento MongoDB para la entidad DistributionProgram.
 * Separado de la entidad de dominio para mantener arquitectura limpia y
 * desacoplar persistencia.
 * Mapeado a la colección 'program'.
 * 
 * @version 1.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "program")
@CompoundIndex(def = "{'status': 1, 'organizationId': 1}")
@CompoundIndex(def = "{'programCode': 1}", unique = true)
public class DistributionProgramDocument extends BaseDocument {

    /** ID de la organización (indexado). */
    @Indexed
    private String organizationId;

    /** Código único del programa (indexado y único). */
    @Indexed(unique = true)
    private String programCode;

    /** ID del horario (indexado). */
    @Indexed
    private String scheduleId;

    /** ID de la ruta (indexado). */
    @Indexed
    private String routeId;

    /** ID de la zona (indexado). */
    @Indexed
    private String zoneId;

    /** ID de la calle (indexado). */
    @Indexed
    private String streetId;

    /** Fecha del programa. */
    private LocalDate programDate;

    /** Hora planificada de inicio. */
    private String plannedStartTime;

    /** Hora planificada de fin. */
    private String plannedEndTime;

    /** Hora real de inicio. */
    private String actualStartTime;

    /** Hora real de fin. */
    private String actualEndTime;

    /** Usuario responsable (indexado). */
    @Indexed
    private String responsibleUserId;

    /** Observaciones generales. */
    private String observations;
}
