package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareRequest {
    private String zoneId;
    private String fareName;
    private String fareDescription;
    private String fareAmount;
}
