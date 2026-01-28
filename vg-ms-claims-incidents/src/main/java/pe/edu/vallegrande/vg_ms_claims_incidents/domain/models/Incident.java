package pe.edu.vallegrande.vg_ms_claims_incidents.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Entidad de dominio pura que representa un Incidente.
 * No contiene anotaciones de infraestructura (MongoDB, JPA, etc.)
 * Sigue los principios de DDD (Domain-Driven Design)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incident {
    
    private String id;
    private String organizationId;
    private String incidentCode;
    private String incidentTypeId;
    private String incidentCategory;
    private String zoneId;
    private Date incidentDate;
    private String title;
    private String description;
    private String severity;
    private String status;
    private Integer affectedBoxesCount;
    private String reportedByUserId;
    private String assignedToUserId;
    private String resolvedByUserId;
    
    @Builder.Default
    private Boolean resolved = false;
    
    private String resolutionNotes;
    
    @Builder.Default
    private String recordStatus = "ACTIVE";
    
    /**
     * Método de dominio para verificar si el incidente es crítico
     */
    public boolean isCritical() {
        return "CRITICAL".equalsIgnoreCase(this.severity);
    }
    
    /**
     * Método de dominio para verificar si el incidente está activo
     */
    public boolean isActive() {
        return "ACTIVE".equalsIgnoreCase(this.recordStatus);
    }
    
    /**
     * Método de dominio para verificar si el incidente puede ser asignado
     */
    public boolean canBeAssigned() {
        return "REPORTED".equalsIgnoreCase(this.status) && isActive();
    }
    
    /**
     * Método de dominio para verificar si el incidente puede ser resuelto
     */
    public boolean canBeResolved() {
        return ("ASSIGNED".equalsIgnoreCase(this.status) || 
                "IN_PROGRESS".equalsIgnoreCase(this.status)) && 
                isActive();
    }
    
    /**
     * Método de dominio para asignar un técnico
     */
    public void assignTo(String userId) {
        if (!canBeAssigned()) {
            throw new IllegalStateException("El incidente no puede ser asignado en su estado actual");
        }
        this.assignedToUserId = userId;
        this.status = "ASSIGNED";
    }
    
    /**
     * Método de dominio para resolver el incidente
     */
    public void resolve(String userId, String notes) {
        if (!canBeResolved()) {
            throw new IllegalStateException("El incidente no puede ser resuelto en su estado actual");
        }
        this.resolvedByUserId = userId;
        this.resolutionNotes = notes;
        this.resolved = true;
        this.status = "RESOLVED";
    }
}