package pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

    private String paymentId;
    private String organizationId;
    private String paymentCode;
    private String userId;
    private String waterBoxId;
    private String paymentType;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private LocalDate paymentDate;
    private String paymentStatus;
    private String externalReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<PaymentDResponse> details;
}
