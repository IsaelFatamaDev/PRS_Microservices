package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseResponse {

     private String purchaseId;
     private String organizationId;
     private String purchaseCode;
     private String supplierId;
     private String supplierName;
     private String supplierCode;

     @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
     private LocalDate purchaseDate;

     @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
     private LocalDate deliveryDate;
     private BigDecimal totalAmount;
     private PurchaseStatus status;
     private String requestedByUserId;
     private String approvedByUserId;
     private String invoiceNumber;
     private String observations;

     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
     private LocalDateTime createdAt;

     private List<PurchaseDetailResponse> details;
}
