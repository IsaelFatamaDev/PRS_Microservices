package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO para movimientos de inventario ENRIQUECIDOS con información de usuario y
 * producto
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedInventoryMovementResponse {

     // ============ INFORMACIÓN DEL MOVIMIENTO ============
     private String movementId;
     private String organizationId;
     private String productId;
     private MovementType movementType;
     private MovementReason movementReason;
     private Integer quantity;
     private BigDecimal unitCost;
     private BigDecimal totalValue;
     private String referenceDocument;
     private String referenceId;
     private Integer previousStock;
     private Integer newStock;
     private LocalDateTime movementDate;
     private String userId;
     private String observations;
     private LocalDateTime createdAt;

     // ============ INFORMACIÓN DEL PRODUCTO ============
     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class ProductInfo {
          private String productId;
          private String productCode;
          private String productName;
          private String categoryId;
          private String categoryName;
          private String unitOfMeasure;
          private Integer minimumStock;
          private Integer maximumStock;
          private Integer currentStock;
          private BigDecimal unitCost;
          private String status;
     }

     private ProductInfo productInfo;

     // ============ INFORMACIÓN DEL USUARIO ============
     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class UserInfo {
          private String id;
          private String userCode;
          private String firstName;
          private String lastName;
          private String fullName;
          private String email;
          private String phone;
     }

     private UserInfo userInfo;

     // ============ CAMPOS CALCULADOS/FORMATEADOS ============
     private String formattedDate;
     private String formattedCost;
     private String formattedValue;
     private String performedBy; // Nombre completo del usuario
     private String productName; // Nombre del producto (acceso directo)
     private String productCode; // Código del producto (acceso directo)
}
