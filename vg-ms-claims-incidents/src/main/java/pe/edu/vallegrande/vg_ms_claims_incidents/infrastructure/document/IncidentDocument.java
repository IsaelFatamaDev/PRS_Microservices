package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.DateDeserializers;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * Documento MongoDB para la colección de incidentes.
 * Representa la capa de persistencia para la entidad Incident del dominio.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "incidents")
public class IncidentDocument extends BaseDocument {
    
    @Id
    private String id;
    
    @Field("organization_id")
    private String organizationId;
    
    @Field("incident_code")
    private String incidentCode;
    
    @Field("incident_type_id")
    private String incidentTypeId;
    
    @Field("incident_category")
    private String incidentCategory;
    
    @Field("zone_id")
    private String zoneId;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @JsonDeserialize(using = DateDeserializers.DateDeserializer.class)
    @JsonSerialize(using = DateSerializer.class)
    @Field("incident_date")
    private Date incidentDate;
    
    private String title;
    
    private String description;
    
    private String severity;
    
    private String status;
    
    @Field("affected_boxes_count")
    private Integer affectedBoxesCount;
    
    @Field("reported_by_user_id")
    private String reportedByUserId;
    
    @Field("assigned_to_user_id")
    private String assignedToUserId;
    
    @Field("resolved_by_user_id")
    private String resolvedByUserId;
    
    private Boolean resolved;
    
    @Field("resolution_notes")
    private String resolutionNotes;
}
