package pe.edu.vallegrande.msdistribution.domain.models;

import java.time.Instant;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad de dominio que representa una Ruta de Distribución.
 * Mapeada a la colección 'route' en MongoDB.
 * 
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "route")
public class DistributionRoute {

    /** Identificador único de la ruta. */
    @Id
    private String id;

    /** Identificador de la organización. */
    private String organizationId;

    /** Código de la ruta (ej. ROT001). */
    private String routeCode;

    /** Nombre descriptivo de la ruta. */
    private String routeName;

    /** Lista ordenada de zonas que componen la ruta. */
    private List<ZoneOrder> zones;

    /** Duración total estimada en horas. */
    private int totalEstimatedDuration;

    /** Usuario responsable de crear o gestionar la ruta. */
    private String responsibleUserId;

    /** Estado de la ruta (ACTIVE, INACTIVE). */
    private String status;

    /** Fecha de creación. */
    private Instant createdAt;

    /**
     * Clase interna que define el orden de las zonas en una ruta.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ZoneOrder {
        /** ID de la zona. */
        private String zoneId;

        /** Número de orden en la secuencia de ruta. */
        private int order;

        /** Duración estimada en esta zona (horas). */
        private int estimatedDuration;
    }
}