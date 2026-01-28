package pe.edu.vallegrande.vgmsorganizations.application.dto.zone;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneResponse {
    private String id;
    private String organizationId;
    private String zoneName;
    private String zoneCode;
    private String description;
    private BigDecimal currentMonthlyFee;
    private Double latitude;
    private Double longitude;
    private Integer totalWaterBoxes;
    private Integer activeWaterBoxes;
    private String status;
    private LocalDateTime createdAt;
    private String createdBy;
}
