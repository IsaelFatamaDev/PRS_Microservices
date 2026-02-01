package pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.ZoneDocument;

/**
 * Mapper para conversión entre Zone (Domain) y ZoneDocument (Persistence)
 * Ubicado en infrastructure/mapper según estándar PRS1
 * 
 * Responsabilidad: Conversión entre capa de persistencia (Documents) y dominio
 */
@Component
public class ZoneMapper implements BaseMapper<Zone, ZoneDocument> {

    @Override
    public Zone toDomain(ZoneDocument document) {
        if (document == null) {
            return null;
        }
        
        return Zone.builder()
                .zoneId(document.getZoneId())
                .organizationId(document.getOrganizationId())
                .zoneCode(document.getZoneCode())
                .zoneName(document.getZoneName())
                .description(document.getDescription())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    @Override
    public ZoneDocument toEntity(Zone domain) {
        if (domain == null) {
            return null;
        }
        
        ZoneDocument document = ZoneDocument.builder()
                .zoneId(domain.getZoneId())
                .organizationId(domain.getOrganizationId())
                .zoneCode(domain.getZoneCode())
                .zoneName(domain.getZoneName())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .build();
        
        if (domain.getCreatedAt() != null) {
            document.setCreatedAt(domain.getCreatedAt());
        }
        if (domain.getUpdatedAt() != null) {
            document.setUpdatedAt(domain.getUpdatedAt());
        }
        
        return document;
    }
}
