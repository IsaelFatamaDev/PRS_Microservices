package pe.edu.vallegrande.vgmsinventorypurchases.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Purchase {

     private String purchaseId;
     private String organizationId;
     private String purchaseCode;
     private String supplierId;
     private LocalDate purchaseDate;
     private LocalDate deliveryDate;
     private BigDecimal totalAmount;
     private PurchaseStatus status;
     private String requestedByUserId;
     private String approvedByUserId;
     private String invoiceNumber;
     private String observations;
     private LocalDateTime createdAt;
}
