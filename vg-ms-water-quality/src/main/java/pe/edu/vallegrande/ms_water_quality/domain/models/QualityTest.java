package pe.edu.vallegrande.ms_water_quality.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain Model - QualityTest
 * Representa una prueba de calidad de agua en el dominio del negocio.
 * NO contiene anotaciones de persistencia (arquitectura hexagonal).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityTest {

    private String id;

    private String organizationId;
    private String testCode;
    private List<String> testingPointId;  
    private LocalDateTime testDate;
    private String testType;
    private String testedByUserId;
    private String weatherConditions;
    private Double waterTemperature;
    private String generalObservations;
    private String status;
    private List<TestResult> results;

    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    /**
     * Value Object - TestResult
     * Representa el resultado de un parámetro medido en una prueba de calidad.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResult {
        private String parameterId;
        private String parameterCode;
        private Double measuredValue;
        private String unit;
        private String status;
        private String observations;
    }
}