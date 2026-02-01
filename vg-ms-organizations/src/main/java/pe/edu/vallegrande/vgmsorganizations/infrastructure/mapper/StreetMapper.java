package pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.StreetDocument;

/**
 * Mapper para conversión entre Street (Domain) y StreetDocument (Persistence)
 * Ubicado en infrastructure/mapper según estándar PRS1
 * 
 * Responsabilidad: Conversión entre capa de persistencia (Documents) y dominio
 */
@Component
public class StreetMapper implements BaseMapper<Street, StreetDocument> {

    @Override
    public Street toDomain(StreetDocument document) {
        if (document == null) {
            return null;
        }
        
        return Street.builder()
                .streetId(document.getStreetId())
                .zoneId(document.getZoneId())
                .streetCode(document.getStreetCode())
                .streetName(document.getStreetName())
                .streetType(document.getStreetType())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    @Override
    public StreetDocument toEntity(Street domain) {
        if (domain == null) {
            return null;
        }
        
        StreetDocument document = StreetDocument.builder()
                .streetId(domain.getStreetId())
                .zoneId(domain.getZoneId())
                .streetCode(domain.getStreetCode())
                .streetName(domain.getStreetName())
                .streetType(domain.getStreetType())
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
