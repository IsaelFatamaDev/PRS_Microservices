package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

// Historial de cambios de tarifa de zona
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZoneFareHistory {

    private String id;
    private String zoneId;
    private BigDecimal previousFee;
    private BigDecimal newFee;
    private LocalDate effectiveDate;
    private LocalDateTime changeDate;
    private String changedBy;
    private String reason;
}
