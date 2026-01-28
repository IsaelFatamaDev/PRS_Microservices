package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO de respuesta para movimientos de inventario por consumo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementConsumptionResponse {

     /**
      * ID del movimiento generado
      */
     private String movementId;

     /**
      * ID de la organización
      */
     private String organizationId;

     /**
      * ID del producto
      */
     private String productId;

     /**
      * Tipo de movimiento (siempre SALIDA)
      */
     private MovementType movementType;

     /**
      * Motivo del movimiento
      */
     private MovementReason movementReason;

     /**
      * Cantidad consumida
      */
     private Integer quantity;

     /**
      * Costo unitario
      */
     private BigDecimal unitCost;

     /**
      * Valor total del movimiento (cantidad * costo unitario)
      */
     private BigDecimal totalValue;

     /**
      * Stock anterior
      */
     private Integer previousStock;

     /**
      * Stock nuevo
      */
     private Integer newStock;

     /**
      * Fecha del movimiento
      */
     private LocalDateTime movementDate;

     /**
      * ID del usuario que realizó el consumo
      */
     private String userId;

     /**
      * Observaciones del movimiento
      */
     private String observations;

     /**
      * Documento de referencia
      */
     private String referenceDocument;

     /**
      * ID de referencia
      */
     private String referenceId;

     /**
      * Fecha de creación del registro
      */
     private LocalDateTime createdAt;

     /**
      * Mensaje de confirmación
      */
     private String message;

     /**
      * Estado de la operación
      */
     private boolean success;
}
