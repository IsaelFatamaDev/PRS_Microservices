package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrganizationWithAdminsResponse {
    private String organizationId;
    private String organizationCode;
    private String organizationName;
    private String legalRepresentative;
    private String address;
    private String phone;
    private String logo;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
    private List<AdminUserResponse> admins;
}
