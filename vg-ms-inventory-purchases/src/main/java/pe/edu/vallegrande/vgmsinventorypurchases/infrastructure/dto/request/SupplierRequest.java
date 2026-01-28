package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.SupplierStatus;

import jakarta.validation.constraints.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierRequest {

     @NotNull(message = "El ID de la organización es requerido")
     private String organizationId;

     @NotBlank(message = "El código de proveedor es requerido")
     @Size(max = 20, message = "El código de proveedor no puede exceder 20 caracteres")
     private String supplierCode;

     @NotBlank(message = "El nombre del proveedor es requerido")
     @Size(max = 200, message = "El nombre del proveedor no puede exceder 200 caracteres")
     private String supplierName;

     @Size(max = 100, message = "La persona de contacto no puede exceder 100 caracteres")
     private String contactPerson;

     @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
     private String phone;

     @Email(message = "El formato del email no es válido")
     @Size(max = 100, message = "El email no puede exceder 100 caracteres")
     private String email;

     @Size(max = 500, message = "La dirección no puede exceder 500 caracteres")
     private String address;

     private SupplierStatus status;
}
