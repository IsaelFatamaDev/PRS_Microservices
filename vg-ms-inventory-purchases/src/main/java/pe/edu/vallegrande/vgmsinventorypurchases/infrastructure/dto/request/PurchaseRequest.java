package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;

import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseRequest {

     @NotNull(message = "El ID de la organización es requerido")
     private String organizationId;

     @Size(max = 20, message = "El código de compra no puede exceder 20 caracteres")
     private String purchaseCode;

     @NotNull(message = "El ID del proveedor es requerido")
     private String supplierId;

     @NotNull(message = "La fecha de compra es requerida")
     private LocalDate purchaseDate;

     @Future(message = "La fecha de entrega debe ser futura")
     private LocalDate deliveryDate;

     @NotNull(message = "El ID del usuario solicitante es requerido")
     private String requestedByUserId;

     @Size(max = 50, message = "El número de factura no puede exceder 50 caracteres")
     private String invoiceNumber;

     @Size(max = 500, message = "Las observaciones no pueden exceder 500 caracteres")
     private String observations;

     private PurchaseStatus status;

     @NotNull(message = "Los detalles de la compra son requeridos")
     @NotEmpty(message = "Debe incluir al menos un detalle de compra")
     @Valid
     private List<PurchaseDetailRequest> details;
}
