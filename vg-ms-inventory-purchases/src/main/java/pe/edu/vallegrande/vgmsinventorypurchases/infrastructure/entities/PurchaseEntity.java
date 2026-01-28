package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("purchases")
public class PurchaseEntity {

    @Id
    @Column("purchase_id")
    private String purchaseId;

    @Column("organization_id")
    private String organizationId;

    @Column("purchase_code")
    private String purchaseCode;

    @Column("supplier_id")
    private String supplierId;

    @Column("purchase_date")
    private LocalDate purchaseDate;

    @Column("delivery_date")
    private LocalDate deliveryDate;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("status")
    private PurchaseStatus status;

    @Column("requested_by_user_id")
    private String requestedByUserId;

    @Column("approved_by_user_id")
    private String approvedByUserId;

    @Column("invoice_number")
    private String invoiceNumber;

    @Column("observations")
    private String observations;

    @Column("created_at")
    private LocalDateTime createdAt;
}
