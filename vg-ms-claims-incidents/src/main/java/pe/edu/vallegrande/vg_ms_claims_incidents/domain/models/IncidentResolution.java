package pe.edu.vallegrande.vg_ms_claims_incidents.domain.models;

import java.util.Date;
import java.util.List;

/**
 * Entidad de dominio pura que representa una Resolución de Incidente.
 * No contiene anotaciones de infraestructura (MongoDB, JPA, etc.)
 */
public class IncidentResolution {
    private String id;
    private String incidentId;
    private Date resolutionDate;
    private String resolutionType; // REPARACION_TEMPORAL, REPARACION_COMPLETA, REEMPLAZO
    private String actionsTaken;
    private List<MaterialUsed> materialsUsed;
    private Integer laborHours;
    private Double totalCost;
    private String resolvedByUserId;
    private Boolean qualityCheck;
    private Boolean followUpRequired;
    private String resolutionNotes;
    private Date createdAt;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIncidentId() {
        return incidentId;
    }

    public void setIncidentId(String incidentId) {
        this.incidentId = incidentId;
    }

    public Date getResolutionDate() {
        return resolutionDate;
    }

    public void setResolutionDate(Date resolutionDate) {
        this.resolutionDate = resolutionDate;
    }

    public String getResolutionType() {
        return resolutionType;
    }

    public void setResolutionType(String resolutionType) {
        this.resolutionType = resolutionType;
    }

    public String getActionsTaken() {
        return actionsTaken;
    }

    public void setActionsTaken(String actionsTaken) {
        this.actionsTaken = actionsTaken;
    }

    public List<MaterialUsed> getMaterialsUsed() {
        return materialsUsed;
    }

    public void setMaterialsUsed(List<MaterialUsed> materialsUsed) {
        this.materialsUsed = materialsUsed;
    }

    public Integer getLaborHours() {
        return laborHours;
    }

    public void setLaborHours(Integer laborHours) {
        this.laborHours = laborHours;
    }

    public Double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(Double totalCost) {
        this.totalCost = totalCost;
    }

    public String getResolvedByUserId() {
        return resolvedByUserId;
    }

    public void setResolvedByUserId(String resolvedByUserId) {
        this.resolvedByUserId = resolvedByUserId;
    }

    public Boolean getQualityCheck() {
        return qualityCheck;
    }

    public void setQualityCheck(Boolean qualityCheck) {
        this.qualityCheck = qualityCheck;
    }

    public Boolean getFollowUpRequired() {
        return followUpRequired;
    }

    public void setFollowUpRequired(Boolean followUpRequired) {
        this.followUpRequired = followUpRequired;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}