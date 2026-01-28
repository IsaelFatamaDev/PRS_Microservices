package pe.edu.vallegrande.vgmsorganizations.application.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationResponse {
    private String id;
    private String name;
    private String ruc;
    private String address;
    private String phone;
    private String email;
    private String region;
    private String province;
    private String district;
    private String legalRepresentative;
    private String type;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
