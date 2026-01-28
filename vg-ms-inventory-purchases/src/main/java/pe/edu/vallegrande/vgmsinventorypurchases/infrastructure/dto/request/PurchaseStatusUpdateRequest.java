package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;

import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseStatusUpdateRequest {

     @NotNull(message = "El estado es requerido")
     private PurchaseStatus status;

     private String approvedByUserId;

     private String observations;
}
