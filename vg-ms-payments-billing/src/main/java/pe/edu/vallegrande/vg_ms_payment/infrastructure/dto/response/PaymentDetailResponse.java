package pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta detallada para Payment con información completa
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponse {
    
    private String paymentId;
    private String organizationId;
    private String paymentCode;
    private String userId;
    private String waterBoxId;
    private String paymentType;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private String paymentStatus;
    private String externalReference;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Información enriquecida
    private String organizationName;
    private String userFullName;
    private String userEmail;
}