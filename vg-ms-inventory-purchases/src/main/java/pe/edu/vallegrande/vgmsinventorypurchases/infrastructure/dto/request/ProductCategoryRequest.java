package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.GeneralStatus;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryRequest {

     @NotNull(message = "El ID de la organización es requerido")
     private String organizationId;

     @NotBlank(message = "El código de categoría es requerido")
     @Size(max = 20, message = "El código de categoría no puede exceder 20 caracteres")
     private String categoryCode;

     @NotBlank(message = "El nombre de la categoría es requerido")
     @Size(max = 100, message = "El nombre de la categoría no puede exceder 100 caracteres")
     private String categoryName;

     @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
     private String description;

     private GeneralStatus status;
}
