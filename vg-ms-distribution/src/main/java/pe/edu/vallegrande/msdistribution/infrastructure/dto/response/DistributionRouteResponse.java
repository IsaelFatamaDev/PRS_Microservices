package pe.edu.vallegrande.msdistribution.infrastructure.dto.response;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import java.time.Instant;
import java.util.List;

/**
 * DTO de respuesta para rutas de distribución.
 * Incluye detalles de las zonas asociadas.
 * 
 * @version 1.0
 */
@Value
@Builder
@Jacksonized
public class DistributionRouteResponse {

    /** ID único de la ruta. */
    String id;

    /** ID de la organización. */
    String organizationId;

    /** Código de ruta. */
    String routeCode;

    /** Nombre de la ruta. */
    String routeName;

    /** ID de zona principal (deprecado/compatibilidad). */
    String zoneId;

    /** Lista detallada de zonas en la ruta. */
    List<ZoneDetail> zones;

    /** Duración total estimada. */
    Integer totalEstimatedDuration;

    /** Usuario responsable. */
    String responsibleUserId;

    /** Estado de la ruta. */
    String status;

    /** Fecha de creación. */
    Instant createdAt;

    /**
     * DTO interno para detalles de zona en respuesta.
     */
    @Value
    @Builder
    @Jacksonized
    public static class ZoneDetail {

        /** ID de la zona. */
        String zoneId;

        /** Orden en la ruta. */
        Integer order;

        /** Duración estimada. */
        Integer estimatedDuration;
    }
}