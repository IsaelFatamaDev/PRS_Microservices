package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class IncidentTypeDTO {
    private String id;
    private String organizationId;
    private String typeCode;
    private String typeName;
    private String description;
    private String priorityLevel;
    private Integer estimatedResolutionTime;
    private Boolean requiresExternalService;
    private String status;
    private Instant createdAt;
}