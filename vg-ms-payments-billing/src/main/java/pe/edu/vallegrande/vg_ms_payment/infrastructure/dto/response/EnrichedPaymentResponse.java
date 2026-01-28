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
public class EnrichedPaymentResponse {

    // Información del pago
    private String paymentId;

    // Información de la organización
    private String organizationId;
    private String organizationName;
    private String organizationLogo;

    // Información del pago
    private String paymentCode;

    // Información completa del usuario
    private String userId;
    private String userName;
    private String firstName;
    private String lastName;
    private String userDocument;
    private String email;
    private String phone;
    private String userAddress;
    private String fareAmount;
    
    // Información de la asignación de caja de agua
    private String waterBoxId;
    private Integer assignedWaterBoxId;
    private String boxCode;

    // Información del pago
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