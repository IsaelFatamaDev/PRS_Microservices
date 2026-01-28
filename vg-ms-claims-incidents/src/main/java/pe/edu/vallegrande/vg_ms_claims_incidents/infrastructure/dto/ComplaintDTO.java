package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplaintDTO {
    private String id;

    @JsonProperty("organization_id")
    private String organizationId;

    @JsonProperty("complaint_code")
    private String complaintCode;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("category_id")
    private String categoryId;

    @JsonProperty("water_box_id")
    private String waterBoxId;

    @JsonProperty("complaint_date")
    private Instant complaintDate;

    private String subject;
    private String description;
    private String priority;
    private String status; // RECEIVED, IN_PROGRESS, RESOLVED, CLOSED

    @JsonProperty("assigned_to_user_id")
    private String assignedToUserId;

    @JsonProperty("expected_resolution_date")
    private Instant expectedResolutionDate;

    @JsonProperty("actual_resolution_date")
    private Instant actualResolutionDate;

    @JsonProperty("satisfaction_rating")
    private Integer satisfactionRating; // 1-5 cuando se resuelve

    @JsonProperty("created_at")
    private Instant createdAt;
}