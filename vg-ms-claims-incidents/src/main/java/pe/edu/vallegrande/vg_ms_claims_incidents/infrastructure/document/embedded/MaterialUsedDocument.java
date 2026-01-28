package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.embedded;

import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

/**
 * Documento embebido que representa materiales utilizados en resoluciones.
 * Este documento se embebe dentro de IncidentResolutionDocument.
 */
@Data
public class MaterialUsedDocument {
    
    @Field("product_id")
    private String productId;
    
    private Integer quantity;
    
    private String unit;
    
    @Field("unit_cost")
    private Double unitCost;
}
