package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.embedded.MaterialUsedDocument;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * Documento MongoDB para la colección de resoluciones de incidentes.
 * Representa la capa de persistencia para la entidad IncidentResolution del dominio.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "incident_resolutions")
public class IncidentResolutionDocument extends BaseDocument {
    
    @Id
    private String id;
    
    @Field("incident_id")
    private String incidentId;
    
    @Field("resolution_date")
    private Date resolutionDate;
    
    @Field("resolution_type")
    private String resolutionType; // REPARACION_TEMPORAL, REPARACION_COMPLETA, REEMPLAZO
    
    @Field("actions_taken")
    private String actionsTaken;
    
    @Field("materials_used")
    private List<MaterialUsedDocument> materialsUsed;
    
    @Field("labor_hours")
    private Integer laborHours;
    
    @Field("total_cost")
    private Double totalCost;
    
    @Field("resolved_by_user_id")
    private String resolvedByUserId;
    
    @Field("quality_check")
    private Boolean qualityCheck;
    
    @Field("follow_up_required")
    private Boolean followUpRequired;
    
    @Field("resolution_notes")
    private String resolutionNotes;
}
