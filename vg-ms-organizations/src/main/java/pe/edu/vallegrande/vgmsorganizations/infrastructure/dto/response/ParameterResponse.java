package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class ParameterResponse {
    private String id;
    private String organizationId;
    private String parameterCode;
    private String parameterName;
    private String parameterValue;
    private String parameterDescription;
    private String status; // Enum en vez de String
    private Instant createdAt;
    private Instant updatedAt;
}
