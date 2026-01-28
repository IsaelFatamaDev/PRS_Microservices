package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.IncidentType;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.IncidentTypeDocument;

/**
 * Mapper para convertir entre IncidentType (dominio) e IncidentTypeDocument (persistencia).
 */
@Component
public class IncidentTypeMapper extends BaseMapper<IncidentType, IncidentTypeDocument> {
    
    @Override
    public IncidentTypeDocument toDocument(IncidentType domain) {
        if (domain == null) {
            return null;
        }
        
        IncidentTypeDocument document = new IncidentTypeDocument();
        document.setId(domain.getId());
        document.setOrganizationId(domain.getOrganizationId());
        document.setTypeCode(domain.getTypeCode());
        document.setTypeName(domain.getTypeName());
        document.setDescription(domain.getDescription());
        document.setPriorityLevel(domain.getPriorityLevel());
        document.setEstimatedResolutionTime(domain.getEstimatedResolutionTime());
        document.setRequiresExternalService(domain.getRequiresExternalService());
        document.setStatus(domain.getStatus());
        
        return document;
    }
    
    @Override
    public IncidentType toDomain(IncidentTypeDocument document) {
        if (document == null) {
            return null;
        }
        
        IncidentType domain = new IncidentType();
        domain.setId(document.getId());
        domain.setOrganizationId(document.getOrganizationId());
        domain.setTypeCode(document.getTypeCode());
        domain.setTypeName(document.getTypeName());
        domain.setDescription(document.getDescription());
        domain.setPriorityLevel(document.getPriorityLevel());
        domain.setEstimatedResolutionTime(document.getEstimatedResolutionTime());
        domain.setRequiresExternalService(document.getRequiresExternalService());
        domain.setStatus(document.getStatus());
        domain.setCreatedAt(document.getCreatedAt());
        
        return domain;
    }
}
