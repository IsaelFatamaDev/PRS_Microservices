package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fare {
    private String id;
    private String fareCode;
    private String zoneId;
    private String fareName;
    private String fareDescription;
    private String fareAmount;
    @Builder.Default
    private String status = Constants.ACTIVE;
    private Instant createdAt;
    private Instant updatedAt;
}
