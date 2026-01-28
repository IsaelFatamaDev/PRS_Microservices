package pe.edu.vallegrande.ms_water_quality.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.ms_water_quality.domain.models.TestingPoint;
import pe.edu.vallegrande.ms_water_quality.infrastructure.document.TestingPointDocument;

/**
 * Mapper: TestingPoint Domain ↔ Document
 * Convierte entre el modelo de dominio y el documento de persistencia.
 */
@Component
public class TestingPointMapper {

    /**
     * Convierte de Document a Domain Model
     */
    public TestingPoint toDomain(TestingPointDocument document) {
        if (document == null) {
            return null;
        }

        return TestingPoint.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .pointCode(document.getPointCode())
                .pointName(document.getPointName())
                .pointType(document.getPointType())
                .zoneId(document.getZoneId())
                .locationDescription(document.getLocationDescription())
                .street(document.getStreet())
                .coordinates(toCoordinatesDomain(document.getCoordinates()))
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    /**
     * Convierte de Domain Model a Document
     */
    public TestingPointDocument toDocument(TestingPoint domain) {
        if (domain == null) {
            return null;
        }

        return TestingPointDocument.builder()
                .id(domain.getId())
                .organizationId(domain.getOrganizationId())
                .pointCode(domain.getPointCode())
                .pointName(domain.getPointName())
                .pointType(domain.getPointType())
                .zoneId(domain.getZoneId())
                .locationDescription(domain.getLocationDescription())
                .street(domain.getStreet())
                .coordinates(toCoordinatesDocument(domain.getCoordinates()))
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    /**
     * Convierte Coordinates de Document a Domain
     */
    private TestingPoint.Coordinates toCoordinatesDomain(TestingPointDocument.CoordinatesEmbedded embedded) {
        if (embedded == null) {
            return null;
        }

        return TestingPoint.Coordinates.builder()
                .latitude(embedded.getLatitude())
                .longitude(embedded.getLongitude())
                .build();
    }

    /**
     * Convierte Coordinates de Domain a Document
     */
    private TestingPointDocument.CoordinatesEmbedded toCoordinatesDocument(TestingPoint.Coordinates coordinates) {
        if (coordinates == null) {
            return null;
        }

        return TestingPointDocument.CoordinatesEmbedded.builder()
                .latitude(coordinates.getLatitude())
                .longitude(coordinates.getLongitude())
                .build();
    }
}
