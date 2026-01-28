package pe.edu.vallegrande.vg_ms_claims_incidents.domain.models;

import org.bson.types.ObjectId;
import java.time.Instant;

/**
 * Entidad de dominio pura que representa una Queja/Reclamo.
 * No contiene anotaciones de infraestructura (MongoDB, JPA, etc.)
 */
public class Complaint {
    private ObjectId id;
    private ObjectId organizationId;
    private String complaintCode;
    private ObjectId userId;
    private ObjectId categoryId;
    private ObjectId waterBoxId;
    private Instant complaintDate;
    private String subject;
    private String description;
    private String priority;
    private String status; // RECEIVED, IN_PROGRESS, RESOLVED, CLOSED
    private ObjectId assignedToUserId;
    private Instant expectedResolutionDate;
    private Instant actualResolutionDate;
    private Integer satisfactionRating; // 1-5 cuando se resuelve
    private Instant createdAt;

    // Getters and Setters
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public ObjectId getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(ObjectId organizationId) {
        this.organizationId = organizationId;
    }

    public String getComplaintCode() {
        return complaintCode;
    }

    public void setComplaintCode(String complaintCode) {
        this.complaintCode = complaintCode;
    }

    public ObjectId getUserId() {
        return userId;
    }

    public void setUserId(ObjectId userId) {
        this.userId = userId;
    }

    public ObjectId getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(ObjectId categoryId) {
        this.categoryId = categoryId;
    }

    public ObjectId getWaterBoxId() {
        return waterBoxId;
    }

    public void setWaterBoxId(ObjectId waterBoxId) {
        this.waterBoxId = waterBoxId;
    }

    public Instant getComplaintDate() {
        return complaintDate;
    }

    public void setComplaintDate(Instant complaintDate) {
        this.complaintDate = complaintDate;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public ObjectId getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(ObjectId assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public Instant getExpectedResolutionDate() {
        return expectedResolutionDate;
    }

    public void setExpectedResolutionDate(Instant expectedResolutionDate) {
        this.expectedResolutionDate = expectedResolutionDate;
    }

    public Instant getActualResolutionDate() {
        return actualResolutionDate;
    }

    public void setActualResolutionDate(Instant actualResolutionDate) {
        this.actualResolutionDate = actualResolutionDate;
    }

    public Integer getSatisfactionRating() {
        return satisfactionRating;
    }

    public void setSatisfactionRating(Integer satisfactionRating) {
        this.satisfactionRating = satisfactionRating;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}