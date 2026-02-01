package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneRequest {
    private String organizationId;
    private String zoneName;
    private String description;
} 