package pe.edu.vallegrande.vgmsorganizations.application.mapper;

import pe.edu.vallegrande.vgmsorganizations.application.dto.zone.CreateZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.zone.ZoneResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.model.valueobject.Coordinates;
import pe.edu.vallegrande.vgmsorganizations.domain.model.valueobject.RecordStatus;

/**
 * Mapper entre DTOs de Zone y Domain Models
 * Responsabilidad: Conversión DTO ↔ Domain (Zone)
 */
public class ZoneMapper {

    /**
     * CreateRequest → Domain Model
     */
    public static Zone toDomain(CreateZoneRequest request) {
        Coordinates coordinates = (request.getLatitude() != null && request.getLongitude() != null)
            ? new Coordinates(request.getLatitude(), request.getLongitude())
            : null;

        return Zone.createNew(
            null, // ID will be generated
            request.getOrganizationId(),
            request.getZoneName(),
            request.getZoneCode(),
            request.getDescription(),
            request.getCurrentMonthlyFee(),
            coordinates,
            request.getTotalWaterBoxes() != null ? request.getTotalWaterBoxes() : 0,
            request.getActiveWaterBoxes() != null ? request.getActiveWaterBoxes() : 0,
            RecordStatus.ACTIVE,
            request.getCreatedBy()
        );
    }

    /**
     * Domain → Response DTO
     */
    public static ZoneResponse toResponse(Zone zone) {
        return ZoneResponse.builder()
            .id(zone.getId())
            .organizationId(zone.getOrganizationId())
            .zoneName(zone.getZoneName())
            .zoneCode(zone.getZoneCode())
            .description(zone.getDescription())
            .currentMonthlyFee(zone.getCurrentMonthlyFee())
            .latitude(zone.getCoordinates() != null ? zone.getCoordinates().getLatitude() : null)
            .longitude(zone.getCoordinates() != null ? zone.getCoordinates().getLongitude() : null)
            .totalWaterBoxes(zone.getTotalWaterBoxes())
            .activeWaterBoxes(zone.getActiveWaterBoxes())
            .status(zone.getStatus().name())
            .createdAt(zone.getCreatedAt())
            .createdBy(zone.getCreatedBy())
            .build();
    }
}
