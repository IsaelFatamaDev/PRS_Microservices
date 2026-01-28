package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Documento MongoDB para la colección de respuestas a quejas.
 * Representa la capa de persistencia para la entidad ComplaintResponse del dominio.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "complaint_responses")
public class ComplaintResponseDocument extends BaseDocument {
    
    @Id
    private String id;
    
    @Field("complaint_id")
    private String complaintId;
    
    @Field("response_date")
    private Instant responseDate;
    
    @Field("response_type")
    private String responseType; // INVESTIGACION, SOLUCION, SEGUIMIENTO
    
    private String message;
    
    @Field("responded_by_user_id")
    private String respondedByUserId;
    
    @Field("internal_notes")
    private String internalNotes;
}
