package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseDetailRequest {

     @NotNull(message = "El ID del producto es requerido")
     private String productId;

     @NotNull(message = "La cantidad ordenada es requerida")
     @Min(value = 1, message = "La cantidad ordenada debe ser mayor que 0")
     private Integer quantityOrdered;

     @NotNull(message = "El costo unitario es requerido")
     @DecimalMin(value = "0.0", inclusive = false, message = "El costo unitario debe ser mayor que 0")
     @Digits(integer = 8, fraction = 2, message = "El costo unitario debe tener máximo 8 dígitos enteros y 2 decimales")
     private BigDecimal unitCost;

     @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
     private String observations;
}
