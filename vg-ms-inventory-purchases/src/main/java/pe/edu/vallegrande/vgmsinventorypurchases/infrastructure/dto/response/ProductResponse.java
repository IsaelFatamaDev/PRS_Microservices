package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ProductResponse {

     private String productId;
     private String organizationId;
     private String productCode;
     private String productName;
     private String categoryId;
     private String categoryName;
     private UnitOfMeasure unitOfMeasure;
     private Integer minimumStock;
     private Integer maximumStock;
     private Integer currentStock;
     private BigDecimal unitCost;
     private ProductStatus status;

     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
     private LocalDateTime createdAt;

     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
     private LocalDateTime updatedAt;
}
