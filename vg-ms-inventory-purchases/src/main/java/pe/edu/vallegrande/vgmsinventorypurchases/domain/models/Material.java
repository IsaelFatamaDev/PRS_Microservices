package pe.edu.vallegrande.vgmsinventorypurchases.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.ProductStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Material {

     private String productId;
     private String organizationId;
     private String productCode;
     private String productName;
     private String categoryId;
     private UnitOfMeasure unitOfMeasure;
     private Integer minimumStock;
     private Integer maximumStock;
     private Integer currentStock;
     private BigDecimal unitCost;
     private ProductStatus status;
     private LocalDateTime createdAt;
     private LocalDateTime updatedAt;
}
