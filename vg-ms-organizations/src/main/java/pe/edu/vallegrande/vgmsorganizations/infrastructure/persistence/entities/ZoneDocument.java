package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "zones")
public class ZoneDocument {

    @Id
    private String id;

    @Indexed
    private String organizationId;

    private String zoneName;

    @Indexed(unique = true)
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
