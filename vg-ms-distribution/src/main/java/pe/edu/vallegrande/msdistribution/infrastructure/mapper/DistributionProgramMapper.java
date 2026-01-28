package pe.edu.vallegrande.msdistribution.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.msdistribution.infrastructure.document.DistributionProgramDocument;

/**
 * Componente Mapper para transformar entre el modelo de dominio y el documento
 * de persistencia.
 * Extiende de BaseMapper para utilidades comunes.
 * 
 * @version 1.0
 */
@Component
public class DistributionProgramMapper extends BaseMapper {

    /**
     * Convierte un documento de MongoDB a una entidad de dominio.
     * 
     * @param document Documento de persistencia.
     * @return Entidad de dominio DistributionProgram o null si el input es null.
     */
    public DistributionProgram toDomain(DistributionProgramDocument document) {
        if (document == null) {
            return null;
        }

        return DistributionProgram.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .programCode(document.getProgramCode())
                .scheduleId(document.getScheduleId())
                .routeId(document.getRouteId())
                .zoneId(document.getZoneId())
                .streetId(document.getStreetId())
                .programDate(document.getProgramDate())
                .plannedStartTime(document.getPlannedStartTime())
                .plannedEndTime(document.getPlannedEndTime())
                .actualStartTime(document.getActualStartTime())
                .actualEndTime(document.getActualEndTime())
                .status(document.getStatus())
                .responsibleUserId(document.getResponsibleUserId())
                .observations(document.getObservations())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    /**
     * Convierte una entidad de dominio a un documento de MongoDB.
     * 
     * @param domain Entidad de dominio.
     * @return Documento de persistencia DistributionProgramDocument o null.
     */
    public DistributionProgramDocument toDocument(DistributionProgram domain) {
        if (domain == null) {
            return null;
        }

        DistributionProgramDocument document = DistributionProgramDocument.builder()
                .organizationId(domain.getOrganizationId())
                .programCode(domain.getProgramCode())
                .scheduleId(domain.getScheduleId())
                .routeId(domain.getRouteId())
                .zoneId(domain.getZoneId())
                .streetId(domain.getStreetId())
                .programDate(domain.getProgramDate())
                .plannedStartTime(domain.getPlannedStartTime())
                .plannedEndTime(domain.getPlannedEndTime())
                .actualStartTime(domain.getActualStartTime())
                .actualEndTime(domain.getActualEndTime())
                .responsibleUserId(domain.getResponsibleUserId())
                .observations(domain.getObservations())
                .build();

        document.setId(domain.getId());
        document.setStatus(domain.getStatus());
        document.setCreatedAt(domain.getCreatedAt());
        document.setUpdatedAt(domain.getUpdatedAt());

        return document;
    }
}
