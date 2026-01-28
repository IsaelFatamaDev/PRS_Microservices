package pe.edu.vallegrande.ms_infraestructura.domain.models;

import lombok.*;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaterBoxAssignment {
    private Long id;
    private Long waterBoxId;
    private String userId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private BigDecimal monthlyFee;
    private Status status;
    private LocalDateTime createdAt;
    private Long transferId;
}
