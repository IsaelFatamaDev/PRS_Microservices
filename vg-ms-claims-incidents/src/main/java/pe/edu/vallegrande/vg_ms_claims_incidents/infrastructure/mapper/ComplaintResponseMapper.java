package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.ComplaintResponse;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.ComplaintResponseDocument;

/**
 * Mapper para convertir entre ComplaintResponse (dominio) y ComplaintResponseDocument (persistencia).
 */
@Component
public class ComplaintResponseMapper extends BaseMapper<ComplaintResponse, ComplaintResponseDocument> {
    
    @Override
    public ComplaintResponseDocument toDocument(ComplaintResponse domain) {
        if (domain == null) {
            return null;
        }
        
        ComplaintResponseDocument document = new ComplaintResponseDocument();
        document.setId(domain.getId());
        document.setComplaintId(domain.getComplaintId());
        document.setResponseDate(domain.getResponseDate());
        document.setResponseType(domain.getResponseType());
        document.setMessage(domain.getMessage());
        document.setRespondedByUserId(domain.getRespondedByUserId());
        document.setInternalNotes(domain.getInternalNotes());
        
        return document;
    }
    
    @Override
    public ComplaintResponse toDomain(ComplaintResponseDocument document) {
        if (document == null) {
            return null;
        }
        
        ComplaintResponse domain = new ComplaintResponse();
        domain.setId(document.getId());
        domain.setComplaintId(document.getComplaintId());
        domain.setResponseDate(document.getResponseDate());
        domain.setResponseType(document.getResponseType());
        domain.setMessage(document.getMessage());
        domain.setRespondedByUserId(document.getRespondedByUserId());
        domain.setInternalNotes(document.getInternalNotes());
        domain.setCreatedAt(document.getCreatedAt());
        
        return domain;
    }
}
