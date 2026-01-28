package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.mappers;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities.StreetDocument;

/**
 * Mapper entre Domain Model Street y MongoDB Document
 * Responsabilidad: Conversión Domain ↔ Document (Persistence)
 */
public class StreetDomainMapper {

    /**
     * Domain → Document (para guardar en MongoDB)
     */
    public static StreetDocument toDocument(Street street) {
        return StreetDocument.builder()
                .id(street.getId())
                .zoneId(street.getZoneId())
                .streetName(street.getStreetName())
                .streetCode(street.getStreetCode())
                .description(street.getDescription())
                .status(street.getStatus().name())
                .createdAt(street.getCreatedAt())
                .createdBy(street.getCreatedBy())
                .build();
    }

    /**
     * Document → Domain (al leer de MongoDB)
     */
    public static Street toDomain(StreetDocument document) {
        return Street.builder()
                .id(document.getId())
                .zoneId(document.getZoneId())
                .streetName(document.getStreetName())
                .streetCode(document.getStreetCode())
                .description(document.getDescription())
                .status(RecordStatus.valueOf(document.getStatus()))
                .createdAt(document.getCreatedAt())
                .createdBy(document.getCreatedBy())
                .build();
    }
}
