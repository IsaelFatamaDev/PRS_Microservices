package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementFilterDto {

     private String organizationId;
     private String productId;
     private String productCode;
     private String productName;
     private MovementType movementType;
     private MovementReason movementReason;
     private String referenceDocument;
     private String userId;

     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private LocalDateTime startDate;

     @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
     private LocalDateTime endDate;

     // Parámetros de paginación
     @Builder.Default
     private Integer page = 0;

     @Builder.Default
     private Integer size = 20;

     @Builder.Default
     private String sortBy = "movementDate";

     @Builder.Default
     private String sortDirection = "DESC";
}
