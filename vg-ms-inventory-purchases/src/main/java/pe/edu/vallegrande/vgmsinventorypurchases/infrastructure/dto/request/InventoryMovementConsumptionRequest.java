package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

/**
 * DTO para registrar consumo de productos y generar movimientos de inventario
 * de salida
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementConsumptionRequest {

     /**
      * ID de la organización
      */
     @NotBlank(message = "Organization ID is required")
     private String organizationId;

     /**
      * ID del producto que se está consumiendo
      */
     @NotBlank(message = "Product ID is required")
     private String productId;

     /**
      * Cantidad consumida (positiva, será registrada como salida)
      */
     @NotNull(message = "Quantity is required")
     @Positive(message = "Quantity must be positive")
     private Integer quantity;

     /**
      * Costo unitario del producto en el momento del consumo
      */
     @NotNull(message = "Unit cost is required")
     private BigDecimal unitCost;

     /**
      * Motivo del movimiento (por defecto USO_INTERNO)
      */
     @Builder.Default
     private MovementReason movementReason = MovementReason.USO_INTERNO;

     /**
      * ID del usuario que realiza el consumo
      */
     @NotBlank(message = "User ID is required")
     private String userId;

     /**
      * Documento de referencia (opcional)
      */
     private String referenceDocument;

     /**
      * ID de referencia (opcional, puede ser el ID del pedido, proyecto, etc.)
      */
     private String referenceId;

     /**
      * Observaciones adicionales
      */
     private String observations;

     /**
      * Stock anterior (antes del consumo)
      */
     @NotNull(message = "Previous stock is required")
     private Integer previousStock;

     /**
      * Nuevo stock (después del consumo)
      */
     @NotNull(message = "New stock is required")
     private Integer newStock;
}
