package pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDResponse {

    private String paymentDetailId;
    private String paymentId;
    private String concept;
    private Integer year;
    private Integer month;
    private BigDecimal amount;
    private String description;
    private LocalDate periodStart;
    private LocalDate periodEnd;
}
