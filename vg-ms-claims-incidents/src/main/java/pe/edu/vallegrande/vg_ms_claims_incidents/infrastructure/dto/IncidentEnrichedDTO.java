package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * DTO de incidente enriquecido con información completa de usuarios
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class IncidentEnrichedDTO extends IncidentDTO {
    
    // Información completa del usuario que reportó
    private UserDTO reporterInfo;
    
    // Información completa del usuario asignado
    private UserDTO assignedUserInfo;
    
    // Información completa del usuario que resolvió
    private UserDTO resolverInfo;
    
    // Métodos de conveniencia
    public String getReporterFullName() {
        return reporterInfo != null ? reporterInfo.getFullName() : "Usuario desconocido";
    }
    
    public String getReporterEmail() {
        return reporterInfo != null ? reporterInfo.getContactEmail() : null;
    }
    
    public String getReporterPhone() {
        return reporterInfo != null ? reporterInfo.getContactPhone() : null;
    }
    
    public String getAssignedUserFullName() {
        return assignedUserInfo != null ? assignedUserInfo.getFullName() : "No asignado";
    }
    
    public String getAssignedUserEmail() {
        return assignedUserInfo != null ? assignedUserInfo.getContactEmail() : null;
    }
    
    public String getResolverFullName() {
        return resolverInfo != null ? resolverInfo.getFullName() : "No resuelto";
    }
    
    public String getResolverEmail() {
        return resolverInfo != null ? resolverInfo.getContactEmail() : null;
    }
    
    public boolean hasReporterInfo() {
        return reporterInfo != null;
    }
    
    public boolean hasAssignedUserInfo() {
        return assignedUserInfo != null;
    }
    
    public boolean hasResolverInfo() {
        return resolverInfo != null;
    }
}
