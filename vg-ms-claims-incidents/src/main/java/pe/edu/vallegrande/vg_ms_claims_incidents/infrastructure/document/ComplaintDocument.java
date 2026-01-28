package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.bson.types.ObjectId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Documento MongoDB para la colección de quejas/reclamos.
 * Representa la capa de persistencia para la entidad Complaint del dominio.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "complaints")
public class ComplaintDocument extends BaseDocument {

    @Id
    private ObjectId id;

    @Field("organization_id")
    private ObjectId organizationId;

    @Field("complaint_code")
    private String complaintCode;

    @Field("user_id")
    private ObjectId userId;

    @Field("category_id")
    private ObjectId categoryId;

    @Field("water_box_id")
    private ObjectId waterBoxId;

    @Field("complaint_date")
    private Instant complaintDate;

    private String subject;
    
    private String description;
    
    private String priority;
    
    private String status; // RECEIVED, IN_PROGRESS, RESOLVED, CLOSED

    @Field("assigned_to_user_id")
    private ObjectId assignedToUserId;

    @Field("expected_resolution_date")
    private Instant expectedResolutionDate;

    @Field("actual_resolution_date")
    private Instant actualResolutionDate;

    @Field("satisfaction_rating")
    private Integer satisfactionRating; // 1-5 cuando se resuelve
}
