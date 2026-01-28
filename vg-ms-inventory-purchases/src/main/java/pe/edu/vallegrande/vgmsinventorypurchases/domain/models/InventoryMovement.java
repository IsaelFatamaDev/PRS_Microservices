package pe.edu.vallegrande.vgmsinventorypurchases.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

     private String movementId;
     private String organizationId;
     private String productId;
     private MovementType movementType;
     private MovementReason movementReason;
     private Integer quantity;
     private BigDecimal unitCost;
     private String referenceDocument;
     private String referenceId;
     private Integer previousStock;
     private Integer newStock;
     private LocalDateTime movementDate;
     private String userId;
     private String observations;
     private LocalDateTime createdAt;
}
