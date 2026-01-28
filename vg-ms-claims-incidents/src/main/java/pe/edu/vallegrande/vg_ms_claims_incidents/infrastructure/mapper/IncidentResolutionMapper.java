package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.IncidentResolution;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.IncidentResolutionDocument;

/**
 * Mapper para convertir entre IncidentResolution (dominio) e IncidentResolutionDocument (persistencia).
 */
@Component
public class IncidentResolutionMapper extends BaseMapper<IncidentResolution, IncidentResolutionDocument> {
    
    @Autowired
    private MaterialUsedMapper materialUsedMapper;
    
    @Override
    public IncidentResolutionDocument toDocument(IncidentResolution domain) {
        if (domain == null) {
            return null;
        }
        
        IncidentResolutionDocument document = new IncidentResolutionDocument();
        document.setId(domain.getId());
        document.setIncidentId(domain.getIncidentId());
        document.setResolutionDate(domain.getResolutionDate());
        document.setResolutionType(domain.getResolutionType());
        document.setActionsTaken(domain.getActionsTaken());
        document.setMaterialsUsed(materialUsedMapper.toDocumentList(domain.getMaterialsUsed()));
        document.setLaborHours(domain.getLaborHours());
        document.setTotalCost(domain.getTotalCost());
        document.setResolvedByUserId(domain.getResolvedByUserId());
        document.setQualityCheck(domain.getQualityCheck());
        document.setFollowUpRequired(domain.getFollowUpRequired());
        document.setResolutionNotes(domain.getResolutionNotes());
        
        return document;
    }
    
    @Override
    public IncidentResolution toDomain(IncidentResolutionDocument document) {
        if (document == null) {
            return null;
        }
        
        IncidentResolution domain = new IncidentResolution();
        domain.setId(document.getId());
        domain.setIncidentId(document.getIncidentId());
        domain.setResolutionDate(document.getResolutionDate());
        domain.setResolutionType(document.getResolutionType());
        domain.setActionsTaken(document.getActionsTaken());
        domain.setMaterialsUsed(materialUsedMapper.toDomainList(document.getMaterialsUsed()));
        domain.setLaborHours(document.getLaborHours());
        domain.setTotalCost(document.getTotalCost());
        domain.setResolvedByUserId(document.getResolvedByUserId());
        domain.setQualityCheck(document.getQualityCheck());
        domain.setFollowUpRequired(document.getFollowUpRequired());
        domain.setResolutionNotes(document.getResolutionNotes());
        
        // Convertir Instant → Date
        if (document.getCreatedAt() != null) {
            domain.setCreatedAt(java.util.Date.from(document.getCreatedAt()));
        }
        
        return domain;
    }
}
