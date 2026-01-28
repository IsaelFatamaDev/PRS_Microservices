package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementDto {

     private String movementId;

     @NotBlank(message = "El ID de organización es obligatorio")
     private String organizationId;

     @NotBlank(message = "El ID del producto es obligatorio")
     private String productId;

     @NotNull(message = "El tipo de movimiento es obligatorio")
     private MovementType movementType;

     @NotNull(message = "La razón del movimiento es obligatoria")
     private MovementReason movementReason;

     @NotNull(message = "La cantidad es obligatoria")
     @Min(value = 1, message = "La cantidad debe ser mayor a 0")
     private Integer quantity;

     @DecimalMin(value = "0.0", inclusive = true, message = "El costo unitario debe ser mayor o igual a 0")
     private BigDecimal unitCost;

     @Size(max = 50, message = "El documento de referencia no puede exceder 50 caracteres")
     private String referenceDocument;

     private String referenceId;

     @NotNull(message = "El stock previo es obligatorio")
     @Min(value = 0, message = "El stock previo no puede ser negativo")
     private Integer previousStock;

     @NotNull(message = "El nuevo stock es obligatorio")
     @Min(value = 0, message = "El nuevo stock no puede ser negativo")
     private Integer newStock;

     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private LocalDateTime movementDate;

     @NotBlank(message = "El ID del usuario es obligatorio")
     private String userId;

     @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
     private String observations;

     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private LocalDateTime createdAt;

     // Campos adicionales para mostrar información relacionada
     private String productName;
     private String productCode;
     private String userName;
     private BigDecimal totalValue; // quantity * unitCost
}
