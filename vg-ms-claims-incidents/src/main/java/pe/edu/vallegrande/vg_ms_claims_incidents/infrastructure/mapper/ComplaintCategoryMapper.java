package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.ComplaintCategoryDocument;

/**
 * Mapper para convertir entre ComplaintCategory (dominio) y ComplaintCategoryDocument (persistencia).
 */
@Component
public class ComplaintCategoryMapper extends BaseMapper<ComplaintCategory, ComplaintCategoryDocument> {
    
    @Override
    public ComplaintCategoryDocument toDocument(ComplaintCategory domain) {
        if (domain == null) {
            return null;
        }
        
        ComplaintCategoryDocument document = new ComplaintCategoryDocument();
        document.setId(domain.getId());
        document.setOrganizationId(domain.getOrganizationId());
        document.setCategoryCode(domain.getCategoryCode());
        document.setCategoryName(domain.getCategoryName());
        document.setDescription(domain.getDescription());
        document.setPriorityLevel(domain.getPriorityLevel());
        document.setMaxResponseTime(domain.getMaxResponseTime());
        document.setStatus(domain.getStatus());
        
        return document;
    }
    
    @Override
    public ComplaintCategory toDomain(ComplaintCategoryDocument document) {
        if (document == null) {
            return null;
        }
        
        ComplaintCategory domain = new ComplaintCategory();
        domain.setId(document.getId());
        domain.setOrganizationId(document.getOrganizationId());
        domain.setCategoryCode(document.getCategoryCode());
        domain.setCategoryName(document.getCategoryName());
        domain.setDescription(document.getDescription());
        domain.setPriorityLevel(document.getPriorityLevel());
        domain.setMaxResponseTime(document.getMaxResponseTime());
        domain.setStatus(document.getStatus());
        domain.setCreatedAt(document.getCreatedAt());
        
        return domain;
    }
}
