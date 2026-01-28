package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "zone_fare_history")
public class ZoneFareHistoryDocument {

    @Id
    private String id;

    @Indexed
    private String zoneId;

    private BigDecimal previousFee;
    private BigDecimal newFee;
    private LocalDate effectiveDate;
    private LocalDateTime changeDate;
    private String changedBy;
    private String reason;
}
