package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class ComplaintResponseDTO {
    private String id;
    private String complaintId;
    private Instant responseDate;
    private String responseType;
    private String message;
    private String respondedByUserId;
    private String internalNotes;
    private Instant createdAt;
}