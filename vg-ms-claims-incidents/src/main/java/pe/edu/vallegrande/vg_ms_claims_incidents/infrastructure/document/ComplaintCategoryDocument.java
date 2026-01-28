package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Documento MongoDB para la colección de categorías de quejas.
 * Representa la capa de persistencia para la entidad ComplaintCategory del dominio.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "complaint_categories")
public class ComplaintCategoryDocument extends BaseDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("category_code")
    private String categoryCode;
    
    @Field("category_name")
    private String categoryName;
    
    private String description;
    
    @Field("priority_level")
    private String priorityLevel; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Field("max_response_time")
    private Integer maxResponseTime; // hours
    
    private String status;
}
