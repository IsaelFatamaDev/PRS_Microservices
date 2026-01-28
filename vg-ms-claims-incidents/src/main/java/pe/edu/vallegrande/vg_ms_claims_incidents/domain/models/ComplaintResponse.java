package pe.edu.vallegrande.vg_ms_claims_incidents.domain.models;

import java.time.Instant;

/**
 * Entidad de dominio pura que representa una Respuesta a Queja.
 * No contiene anotaciones de infraestructura (MongoDB, JPA, etc.)
 */
public class ComplaintResponse {
    private String id;
    private String complaintId;
    private Instant responseDate;
    private String responseType; // INVESTIGACION, SOLUCION, SEGUIMIENTO
    private String message;
    private String respondedByUserId;
    private String internalNotes;
    private Instant createdAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public Instant getResponseDate() {
        return responseDate;
    }

    public void setResponseDate(Instant responseDate) {
        this.responseDate = responseDate;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getRespondedByUserId() {
        return respondedByUserId;
    }

    public void setRespondedByUserId(String respondedByUserId) {
        this.respondedByUserId = respondedByUserId;
    }

    public String getInternalNotes() {
        return internalNotes;
    }

    public void setInternalNotes(String internalNotes) {
        this.internalNotes = internalNotes;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}