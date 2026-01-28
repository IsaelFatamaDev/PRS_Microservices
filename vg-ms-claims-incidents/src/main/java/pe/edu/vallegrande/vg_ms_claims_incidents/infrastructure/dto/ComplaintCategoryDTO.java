package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ComplaintCategoryDTO {
    private String id;
    private String organizationId;
    private String categoryCode;
    private String categoryName;
    private String description;
    private String priorityLevel;
    private Integer maxResponseTime;
    private String status;
    private Instant createdAt;
}