package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetailResponse {

     private String purchaseDetailId;
     private String purchaseId;
     private String productId;
     private String productName;
     private String productCode;
     private Integer quantityOrdered;
     private Integer quantityReceived;
     private BigDecimal unitCost;
     private BigDecimal subtotal;
     private String observations;
}
