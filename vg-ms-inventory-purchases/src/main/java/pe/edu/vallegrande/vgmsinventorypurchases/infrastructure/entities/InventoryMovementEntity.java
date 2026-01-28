package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementReason;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("inventory_movements")
public class InventoryMovementEntity {

    @Id
    @Column("movement_id")
    private String movementId;

    @Column("organization_id")
    private String organizationId;

    @Column("product_id")
    private String productId;

    @Column("movement_type")
    private MovementType movementType;

    @Column("movement_reason")
    private MovementReason movementReason;

    @Column("quantity")
    private Integer quantity;

    @Column("unit_cost")
    private BigDecimal unitCost;

    @Column("reference_document")
    private String referenceDocument;

    @Column("reference_id")
    private String referenceId;

    @Column("previous_stock")
    private Integer previousStock;

    @Column("new_stock")
    private Integer newStock;

    @Column("movement_date")
    private LocalDateTime movementDate;

    @Column("user_id")
    private String userId;

    @Column("observations")
    private String observations;

    @Column("created_at")
    private LocalDateTime createdAt;
}
