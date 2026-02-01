package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParameterRequest {
    private String organizationId;
    private String parameterName;
    private String parameterValue;
    private String parameterDescription;
    private String status ;
}
