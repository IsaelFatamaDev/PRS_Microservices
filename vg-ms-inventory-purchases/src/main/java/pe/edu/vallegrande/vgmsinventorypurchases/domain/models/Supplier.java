package pe.edu.vallegrande.vgmsinventorypurchases.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.SupplierStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

     private String supplierId;
     private String organizationId;
     private String supplierCode;
     private String supplierName;
     private String contactPerson;
     private String phone;
     private String email;
     private String address;
     private SupplierStatus status;
     private LocalDateTime createdAt;
}
