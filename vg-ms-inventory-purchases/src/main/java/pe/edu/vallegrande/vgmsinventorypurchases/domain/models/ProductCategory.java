package pe.edu.vallegrande.vgmsinventorypurchases.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.GeneralStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategory {

     private String categoryId;
     private String organizationId;
     private String categoryCode;
     private String categoryName;
     private String description;
     private GeneralStatus status;
     private LocalDateTime createdAt;
}
