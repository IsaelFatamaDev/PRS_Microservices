package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Documento MongoDB para la colección de tipos de incidentes.
 * Representa la capa de persistencia para la entidad IncidentType del dominio.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "incident_types")
public class IncidentTypeDocument extends BaseDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("type_code")
    private String typeCode;
    
    @Field("type_name")
    private String typeName;
    
    private String description;
    
    @Field("priority_level")
    private String priorityLevel;
    
    @Field("estimated_resolution_time")
    private Integer estimatedResolutionTime; // hours
    
    @Field("requires_external_service")
    private Boolean requiresExternalService;
    
    private String status;
}
