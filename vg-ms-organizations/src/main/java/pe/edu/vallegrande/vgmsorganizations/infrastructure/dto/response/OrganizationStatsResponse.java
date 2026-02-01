package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrganizationStatsResponse {
    private String organizationId;
    private String organizationCode;
    private String organizationName;
    private String legalRepresentative;
    private String address;
    private String phone;
    private String logo;
    private String status;
    private List<ParameterResponse> parameters;
    
    // Estadísticas
    private Long totalZones;
    private Long totalStreets;
}