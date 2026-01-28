package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("purchase_details")
public class PurchaseDetailEntity {

    @Id
    @Column("purchase_detail_id")
    private String purchaseDetailId;

    @Column("purchase_id")
    private String purchaseId;

    @Column("product_id")
    private String productId;

    @Column("quantity_ordered")
    private Integer quantityOrdered;

    @Column("quantity_received")
    private Integer quantityReceived;

    @Column("unit_cost")
    private BigDecimal unitCost;

    @Column("subtotal")
    private BigDecimal subtotal;

    @Column("observations")
    private String observations;
}
