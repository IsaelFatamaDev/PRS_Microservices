package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.Complaint;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.ComplaintDocument;

/**
 * Mapper para convertir entre Complaint (dominio) y ComplaintDocument (persistencia).
 */
@Component
public class ComplaintMapper extends BaseMapper<Complaint, ComplaintDocument> {
    
    @Override
    public ComplaintDocument toDocument(Complaint domain) {
        if (domain == null) {
            return null;
        }
        
        ComplaintDocument document = new ComplaintDocument();
        document.setId(domain.getId());
        document.setOrganizationId(domain.getOrganizationId());
        document.setComplaintCode(domain.getComplaintCode());
        document.setUserId(domain.getUserId());
        document.setCategoryId(domain.getCategoryId());
        document.setWaterBoxId(domain.getWaterBoxId());
        document.setComplaintDate(domain.getComplaintDate());
        document.setSubject(domain.getSubject());
        document.setDescription(domain.getDescription());
        document.setPriority(domain.getPriority());
        document.setStatus(domain.getStatus());
        document.setAssignedToUserId(domain.getAssignedToUserId());
        document.setExpectedResolutionDate(domain.getExpectedResolutionDate());
        document.setActualResolutionDate(domain.getActualResolutionDate());
        document.setSatisfactionRating(domain.getSatisfactionRating());
        
        return document;
    }
    
    @Override
    public Complaint toDomain(ComplaintDocument document) {
        if (document == null) {
            return null;
        }
        
        Complaint domain = new Complaint();
        domain.setId(document.getId());
        domain.setOrganizationId(document.getOrganizationId());
        domain.setComplaintCode(document.getComplaintCode());
        domain.setUserId(document.getUserId());
        domain.setCategoryId(document.getCategoryId());
        domain.setWaterBoxId(document.getWaterBoxId());
        domain.setComplaintDate(document.getComplaintDate());
        domain.setSubject(document.getSubject());
        domain.setDescription(document.getDescription());
        domain.setPriority(document.getPriority());
        domain.setStatus(document.getStatus());
        domain.setAssignedToUserId(document.getAssignedToUserId());
        domain.setExpectedResolutionDate(document.getExpectedResolutionDate());
        domain.setActualResolutionDate(document.getActualResolutionDate());
        domain.setSatisfactionRating(document.getSatisfactionRating());
        domain.setCreatedAt(document.getCreatedAt());
        
        return domain;
    }
}
