package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetRequest {
    private String zoneId;
    private String streetName;
    private String streetType;
}
