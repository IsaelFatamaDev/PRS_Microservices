package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.mappers;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.Coordinates;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities.ZoneDocument;

/**
 * Mapper entre Domain Model Zone y MongoDB Document
 * Responsabilidad: Conversión Domain ↔ Document (Persistence)
 */
public class ZoneDomainMapper {

    /**
     * Domain → Document (para guardar en MongoDB)
     */
    public static ZoneDocument toDocument(Zone zone) {
        return ZoneDocument.builder()
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

    /**
     * Document → Domain (al leer de MongoDB)
     */
    public static Zone toDomain(ZoneDocument document) {
        Coordinates coordinates = (document.getLatitude() != null && document.getLongitude() != null)
                ? new Coordinates(document.getLatitude(), document.getLongitude())
                : null;

        return Zone.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .zoneName(document.getZoneName())
                .zoneCode(document.getZoneCode())
                .description(document.getDescription())
                .currentMonthlyFee(document.getCurrentMonthlyFee())
                .coordinates(coordinates)
                .totalWaterBoxes(document.getTotalWaterBoxes())
                .activeWaterBoxes(document.getActiveWaterBoxes())
                .status(RecordStatus.valueOf(document.getStatus()))
                .createdAt(document.getCreatedAt())
                .createdBy(document.getCreatedBy())
                .build();
    }
}
