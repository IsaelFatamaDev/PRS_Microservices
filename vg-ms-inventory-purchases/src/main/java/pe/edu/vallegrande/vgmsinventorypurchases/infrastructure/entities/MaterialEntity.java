package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.ProductStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.UnitOfMeasure;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("products")
public class MaterialEntity {

    @Id
    @Column("product_id")
    private String productId;

    @Column("organization_id")
    private String organizationId;

    @Column("product_code")
    private String productCode;

    @Column("product_name")
    private String productName;

    @Column("category_id")
    private String categoryId;

    @Column("unit_of_measure")
    private UnitOfMeasure unitOfMeasure;

    @Column("minimum_stock")
    private Integer minimumStock;

    @Column("maximum_stock")
    private Integer maximumStock;

    @Column("current_stock")
    private Integer currentStock;

    @Column("unit_cost")
    private BigDecimal unitCost;

    @Column("status")
    private ProductStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("updated_at")
    private LocalDateTime updatedAt;
}
