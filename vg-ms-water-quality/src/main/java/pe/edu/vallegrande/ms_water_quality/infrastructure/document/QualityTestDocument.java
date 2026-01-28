package pe.edu.vallegrande.ms_water_quality.infrastructure.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB Document - QualityTest
 * Modelo de persistencia para pruebas de calidad de agua.
 * Contiene anotaciones específicas de MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "quality_tests")
public class QualityTestDocument {

    @Id
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
    private List<TestResultEmbedded> results;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;

    /**
     * Embedded Document - TestResult
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TestResultEmbedded {
        private String parameterId;
        private String parameterCode;
        private Double measuredValue;
        private String unit;
        private String status;
        private String observations;
    }
}
