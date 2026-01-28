package pe.edu.vallegrande.vgmsinventorypurchases.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetail {

     private String purchaseDetailId;
     private String purchaseId;
     private String productId;
     private Integer quantityOrdered;
     private Integer quantityReceived;
     private BigDecimal unitCost;
     private BigDecimal subtotal;
     private String observations;
}
