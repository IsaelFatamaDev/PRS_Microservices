package pe.edu.vallegrande.msdistribution.infrastructure.dto.request;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import java.util.List;

/**
 * DTO para actualización parcial de ruta.
 * 
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class DistributionRouteUpdateRequest {

    /** ID de la organización. */
    String organizationId;

    /** Nombre de la ruta. */
    String routeName;

    /** Lista de zonas. */
    List<ZoneEntry> zones;

    /** Duración total estimada. */
    Integer totalEstimatedDuration;

    /** Usuario responsable. */
    String responsibleUserId;

    /**
     * DTO interno para zona en actualización.
     */
    @Value
    @Builder
    @Jacksonized
    public static class ZoneEntry {
        /** ID de la zona. */
        String zoneId;

        /** Orden. */
        Integer order;

        /** Duración estimada. */
        Integer estimatedDuration;
    }
}
