package pe.edu.vallegrande.vgmsorganizations.application.mapper;

import pe.edu.vallegrande.vgmsorganizations.application.dto.street.CreateStreetRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.street.StreetResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.model.valueobject.RecordStatus;

/**
 * Mapper entre DTOs de Street y Domain Models
 * Responsabilidad: Conversión DTO ↔ Domain (Street)
 */
public class StreetMapper {

    /**
     * CreateRequest → Domain Model
     */
    public static Street toDomain(CreateStreetRequest request) {
        return Street.createNew(
            null, // ID will be generated
            request.getZoneId(),
            request.getStreetName(),
            request.getStreetCode(),
            request.getDescription(),
            RecordStatus.ACTIVE,
            request.getCreatedBy()
        );
    }

    /**
     * Domain → Response DTO
     */
    public static StreetResponse toResponse(Street street) {
        return StreetResponse.builder()
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
}
