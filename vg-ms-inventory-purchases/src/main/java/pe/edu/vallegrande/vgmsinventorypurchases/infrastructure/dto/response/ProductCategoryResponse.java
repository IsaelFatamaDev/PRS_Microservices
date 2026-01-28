package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ProductCategoryResponse {

     private String categoryId;
     private String organizationId;
     private String categoryCode;
     private String categoryName;
     private String description;
     private GeneralStatus status;

     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
     private LocalDateTime createdAt;
}
