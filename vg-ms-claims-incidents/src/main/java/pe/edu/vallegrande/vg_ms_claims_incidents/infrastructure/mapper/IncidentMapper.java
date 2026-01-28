package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.Incident;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.IncidentDocument;

/**
 * Mapper para convertir entre Incident (dominio) e IncidentDocument (persistencia).
 */
@Component
public class IncidentMapper extends BaseMapper<Incident, IncidentDocument> {
    
    @Override
    public IncidentDocument toDocument(Incident domain) {
        if (domain == null) {
            return null;
        }
        
        IncidentDocument document = new IncidentDocument();
        document.setId(domain.getId());
        document.setOrganizationId(domain.getOrganizationId());
        document.setIncidentCode(domain.getIncidentCode());
        document.setIncidentTypeId(domain.getIncidentTypeId());
        document.setIncidentCategory(domain.getIncidentCategory());
        document.setZoneId(domain.getZoneId());
        document.setIncidentDate(domain.getIncidentDate());
        document.setTitle(domain.getTitle());
        document.setDescription(domain.getDescription());
        document.setSeverity(domain.getSeverity());
        document.setStatus(domain.getStatus());
        document.setAffectedBoxesCount(domain.getAffectedBoxesCount());
        document.setReportedByUserId(domain.getReportedByUserId());
        document.setAssignedToUserId(domain.getAssignedToUserId());
        document.setResolvedByUserId(domain.getResolvedByUserId());
        document.setResolved(domain.getResolved());
        document.setResolutionNotes(domain.getResolutionNotes());
        document.setRecordStatus(domain.getRecordStatus());
        
        return document;
    }
    
    @Override
    public Incident toDomain(IncidentDocument document) {
        if (document == null) {
            return null;
        }
        
        Incident domain = new Incident();
        domain.setId(document.getId());
        domain.setOrganizationId(document.getOrganizationId());
        domain.setIncidentCode(document.getIncidentCode());
        domain.setIncidentTypeId(document.getIncidentTypeId());
        domain.setIncidentCategory(document.getIncidentCategory());
        domain.setZoneId(document.getZoneId());
        domain.setIncidentDate(document.getIncidentDate());
        domain.setTitle(document.getTitle());
        domain.setDescription(document.getDescription());
        domain.setSeverity(document.getSeverity());
        domain.setStatus(document.getStatus());
        domain.setAffectedBoxesCount(document.getAffectedBoxesCount());
        domain.setReportedByUserId(document.getReportedByUserId());
        domain.setAssignedToUserId(document.getAssignedToUserId());
        domain.setResolvedByUserId(document.getResolvedByUserId());
        domain.setResolved(document.getResolved());
        domain.setResolutionNotes(document.getResolutionNotes());
        domain.setRecordStatus(document.getRecordStatus());
        
        return domain;
    }
}
