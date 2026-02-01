package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Street {
    
    private String streetId;
    private String zoneId;
    private String streetCode;
    private String streetName;
    private String streetType;
    
    @Builder.Default
    private String status = Constants.ACTIVE;
    
    private Instant createdAt;
    private Instant updatedAt;
}
