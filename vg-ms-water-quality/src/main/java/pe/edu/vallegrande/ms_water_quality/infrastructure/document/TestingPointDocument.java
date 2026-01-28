package pe.edu.vallegrande.ms_water_quality.infrastructure.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB Document - TestingPoint
 * Modelo de persistencia para puntos de muestreo.
 * Contiene anotaciones específicas de MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "testing_points")
public class TestingPointDocument {

    @Id
    private String id;

    private String organizationId;
    private String pointCode;
    private String pointName;
    private String pointType;
    private String zoneId;
    private String locationDescription;
    private String street;
    private CoordinatesEmbedded coordinates;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Embedded Document - Coordinates
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CoordinatesEmbedded {
        private double latitude;
        private double longitude;
    }
}
