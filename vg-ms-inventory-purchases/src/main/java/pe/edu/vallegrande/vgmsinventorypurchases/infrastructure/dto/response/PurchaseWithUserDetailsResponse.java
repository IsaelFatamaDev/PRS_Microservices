package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseWithUserDetailsResponse {

     private String purchaseId;
     private String organizationId;
     private String purchaseCode;
     private String supplierId;
     private String supplierName;
     private String supplierCode;

     @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
     private LocalDate purchaseDate;

     @JsonFormat(pattern = "yyyy-MM-dd", timezone = "UTC")
     private LocalDate deliveryDate;

     private BigDecimal totalAmount;
     private PurchaseStatus status;
     private String requestedByUserId;
     private String approvedByUserId;
     private String invoiceNumber;
     private String observations;

     @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "UTC")
     private LocalDateTime createdAt;

     private List<PurchaseDetailResponse> details;

     // Información enriquecida del usuario responsable
     private UserInfo requestedByUser;

     // Información enriquecida de la organización
     private OrganizationInfo organizationInfo;

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class UserInfo {
          private String id;
          private String userCode;
          private String firstName;
          private String lastName;
          private String fullName;
          private String documentType;
          private String documentNumber;
          private String email;
          private String phone;
          private String address;
          private List<String> roles;
          private String status;
     }

     @Data
     @Builder
     @NoArgsConstructor
     @AllArgsConstructor
     public static class OrganizationInfo {
          private String organizationId;
          private String organizationCode;
          private String organizationName;
          private String address;
          private String phone;
          private String legalRepresentative;
          private String status;
     }
}
