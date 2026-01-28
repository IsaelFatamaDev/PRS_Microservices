package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.ProductStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.UnitOfMeasure;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

     @NotNull(message = "El ID de la organización es requerido")
     private String organizationId;

     @NotBlank(message = "El código de producto es requerido")
     @Size(max = 20, message = "El código de producto no puede exceder 20 caracteres")
     private String productCode;

     @NotBlank(message = "El nombre de producto es requerido")
     @Size(max = 200, message = "El nombre de producto no puede exceder 200 caracteres")
     private String productName;

     @NotNull(message = "El ID de la categoría es requerido")
     private String categoryId;

     @NotNull(message = "La unidad de medida es requerida")
     private UnitOfMeasure unitOfMeasure;

     @Min(value = 0, message = "El stock mínimo no puede ser negativo")
     private Integer minimumStock;

     @Min(value = 0, message = "El stock máximo no puede ser negativo")
     private Integer maximumStock;

     @Min(value = 0, message = "El stock actual no puede ser negativo")
     private Integer currentStock;

     @DecimalMin(value = "0.0", inclusive = false, message = "El costo unitario debe ser mayor que 0")
     @Digits(integer = 8, fraction = 2, message = "El costo unitario debe tener máximo 8 dígitos enteros y 2 decimales")
     private BigDecimal unitCost;

     private ProductStatus status;
}
