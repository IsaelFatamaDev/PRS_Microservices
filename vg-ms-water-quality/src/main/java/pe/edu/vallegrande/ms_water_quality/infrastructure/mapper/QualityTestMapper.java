package pe.edu.vallegrande.ms_water_quality.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.ms_water_quality.domain.models.QualityTest;
import pe.edu.vallegrande.ms_water_quality.infrastructure.document.QualityTestDocument;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper: QualityTest Domain ↔ Document
 * Convierte entre el modelo de dominio y el documento de persistencia.
 */
@Component
public class QualityTestMapper {

    /**
     * Convierte de Document a Domain Model
     */
    public QualityTest toDomain(QualityTestDocument document) {
        if (document == null) {
            return null;
        }

        return QualityTest.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .testCode(document.getTestCode())
                .testingPointId(document.getTestingPointId())
                .testDate(document.getTestDate())
                .testType(document.getTestType())
                .testedByUserId(document.getTestedByUserId())
                .weatherConditions(document.getWeatherConditions())
                .waterTemperature(document.getWaterTemperature())
                .generalObservations(document.getGeneralObservations())
                .status(document.getStatus())
                .results(toResultsDomain(document.getResults()))
                .createdAt(document.getCreatedAt())
                .deletedAt(document.getDeletedAt())
                .build();
    }

    /**
     * Convierte de Domain Model a Document
     */
    public QualityTestDocument toDocument(QualityTest domain) {
        if (domain == null) {
            return null;
        }

        return QualityTestDocument.builder()
                .id(domain.getId())
                .organizationId(domain.getOrganizationId())
                .testCode(domain.getTestCode())
                .testingPointId(domain.getTestingPointId())
                .testDate(domain.getTestDate())
                .testType(domain.getTestType())
                .testedByUserId(domain.getTestedByUserId())
                .weatherConditions(domain.getWeatherConditions())
                .waterTemperature(domain.getWaterTemperature())
                .generalObservations(domain.getGeneralObservations())
                .status(domain.getStatus())
                .results(toResultsDocument(domain.getResults()))
                .createdAt(domain.getCreatedAt())
                .deletedAt(domain.getDeletedAt())
                .build();
    }

    /**
     * Convierte lista de TestResult de Document a Domain
     */
    private List<QualityTest.TestResult> toResultsDomain(List<QualityTestDocument.TestResultEmbedded> embedded) {
        if (embedded == null) {
            return null;
        }

        return embedded.stream()
                .map(this::toResultDomain)
                .collect(Collectors.toList());
    }

    /**
     * Convierte TestResult de Document a Domain
     */
    private QualityTest.TestResult toResultDomain(QualityTestDocument.TestResultEmbedded embedded) {
        if (embedded == null) {
            return null;
        }

        return QualityTest.TestResult.builder()
                .parameterId(embedded.getParameterId())
                .parameterCode(embedded.getParameterCode())
                .measuredValue(embedded.getMeasuredValue())
                .unit(embedded.getUnit())
                .status(embedded.getStatus())
                .observations(embedded.getObservations())
                .build();
    }

    /**
     * Convierte lista de TestResult de Domain a Document
     */
    private List<QualityTestDocument.TestResultEmbedded> toResultsDocument(List<QualityTest.TestResult> results) {
        if (results == null) {
            return null;
        }

        return results.stream()
                .map(this::toResultDocument)
                .collect(Collectors.toList());
    }

    /**
     * Convierte TestResult de Domain a Document
     */
    private QualityTestDocument.TestResultEmbedded toResultDocument(QualityTest.TestResult result) {
        if (result == null) {
            return null;
        }

        return QualityTestDocument.TestResultEmbedded.builder()
                .parameterId(result.getParameterId())
                .parameterCode(result.getParameterCode())
                .measuredValue(result.getMeasuredValue())
                .unit(result.getUnit())
                .status(result.getStatus())
                .observations(result.getObservations())
                .build();
    }
}
