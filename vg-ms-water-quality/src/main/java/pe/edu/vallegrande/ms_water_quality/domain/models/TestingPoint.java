package pe.edu.vallegrande.ms_water_quality.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Domain Model - TestingPoint
 * Representa un punto de muestreo de agua en el dominio del negocio.
 * NO contiene anotaciones de persistencia (arquitectura hexagonal).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestingPoint {

    private String id;
    private String organizationId;
    private String pointCode;
    private String pointName;
    private String pointType; // RESERVORIO, RED_DISTRIBUCION, DOMICILIO
    private String zoneId;
    private String locationDescription;
    private String street;
    private Coordinates coordinates;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /**
     * Value Object - Coordinates
     * Representa las coordenadas geográficas de un punto de muestreo.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Coordinates {
        private double latitude;
        private double longitude;
    }
}
