package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vg_ms_claims_incidents.domain.models.MaterialUsed;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.embedded.MaterialUsedDocument;

/**
 * Mapper para convertir entre MaterialUsed (dominio) y MaterialUsedDocument (persistencia).
 */
@Component
public class MaterialUsedMapper extends BaseMapper<MaterialUsed, MaterialUsedDocument> {
    
    @Override
    public MaterialUsedDocument toDocument(MaterialUsed domain) {
        if (domain == null) {
            return null;
        }
        
        MaterialUsedDocument document = new MaterialUsedDocument();
        document.setProductId(domain.getProductId());
        document.setQuantity(domain.getQuantity());
        document.setUnit(domain.getUnit());
        document.setUnitCost(domain.getUnitCost());
        
        return document;
    }
    
    @Override
    public MaterialUsed toDomain(MaterialUsedDocument document) {
        if (document == null) {
            return null;
        }
        
        MaterialUsed domain = new MaterialUsed();
        domain.setProductId(document.getProductId());
        domain.setQuantity(document.getQuantity());
        domain.setUnit(document.getUnit());
        domain.setUnitCost(document.getUnitCost());
        
        return domain;
    }
}
