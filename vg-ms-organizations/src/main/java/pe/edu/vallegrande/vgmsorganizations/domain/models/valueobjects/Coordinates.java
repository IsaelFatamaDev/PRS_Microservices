package pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Coordenadas geográficas
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Coordinates {
     private Double latitude;
     private Double longitude;

     public boolean isValid() {
          return latitude != null && longitude != null
                    && latitude >= -90 && latitude <= 90
                    && longitude >= -180 && longitude <= 180;
     }
}
