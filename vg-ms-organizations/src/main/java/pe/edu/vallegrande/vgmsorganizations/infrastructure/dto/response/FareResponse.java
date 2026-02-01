package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareResponse {
    private String id;
    private String fareCode;
    private String zoneId;
    private String zoneName; // Incluir nombre de la zona para conveniencia
    private String fareName;
    private String fareDescription;
    private String fareAmount;
    private String status;
    private Instant createdAt;
    private Instant updatedAt;
}
