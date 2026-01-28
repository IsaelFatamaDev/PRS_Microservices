package pe.edu.vallegrande.vgmsorganizations.application.dto.street;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StreetResponse {
     private String id;
     private String zoneId;
     private String streetName;
     private String streetCode;
     private String description;
     private String status;
     private LocalDateTime createdAt;
     private String createdBy;
}
