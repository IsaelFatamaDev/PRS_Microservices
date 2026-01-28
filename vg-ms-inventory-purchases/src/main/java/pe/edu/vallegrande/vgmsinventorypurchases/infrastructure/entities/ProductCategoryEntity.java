package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.GeneralStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("product_categories")
public class ProductCategoryEntity {

    @Id
    @Column("category_id")
    private String categoryId;

    @Column("organization_id")
    private String organizationId;

    @Column("category_code")
    private String categoryCode;

    @Column("category_name")
    private String categoryName;

    @Column("description")
    private String description;

    @Column("status")
    private GeneralStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
