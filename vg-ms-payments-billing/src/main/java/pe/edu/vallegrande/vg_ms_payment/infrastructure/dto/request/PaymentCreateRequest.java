package pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCreateRequest {

    @NotNull(message = "Organization ID is required")
    private String organizationId;

    @NotBlank(message = "Payment code is required")
    private String paymentCode;

    @NotNull(message = "User ID is required")
    private String userId;

    @NotNull(message = "Water box ID is required")
    private String waterBoxId;

    @NotBlank(message = "Payment type is required")
    private String paymentType;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotBlank(message = "Payment status is required")
    private String paymentStatus;

    private String externalReference;

    @Valid
    private List<PaymentDRequest> details;
}
