package pe.edu.vallegrande.vg_ms_claims_incidents.domain.models;

import java.time.Instant;

/**
 * Entidad de dominio pura que representa un Tipo de Incidente.
 * No contiene anotaciones de infraestructura (MongoDB, JPA, etc.)
 */
public class IncidentType {
    private String id;
    private String organizationId;
    private String typeCode;
    private String typeName;
    private String description;
    private String priorityLevel;
    private Integer estimatedResolutionTime; // hours
    private Boolean requiresExternalService;
    private String status;
    private Instant createdAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPriorityLevel() {
        return priorityLevel;
    }

    public void setPriorityLevel(String priorityLevel) {
        this.priorityLevel = priorityLevel;
    }

    public Integer getEstimatedResolutionTime() {
        return estimatedResolutionTime;
    }

    public void setEstimatedResolutionTime(Integer estimatedResolutionTime) {
        this.estimatedResolutionTime = estimatedResolutionTime;
    }

    public Boolean getRequiresExternalService() {
        return requiresExternalService;
    }

    public void setRequiresExternalService(Boolean requiresExternalService) {
        this.requiresExternalService = requiresExternalService;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}