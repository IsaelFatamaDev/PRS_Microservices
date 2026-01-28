package pe.edu.vallegrande.vg_ms_payment.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad PostgreSQL para la tabla payments
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Table("payments")
public class PaymentEntity extends BaseEntity implements Persistable<String> {
    
    @Id
    @Column("payment_id")
    private String paymentId;

    @Transient
    private boolean isNew = true;

    @Column("organization_id")
    private String organizationId;

    @Column("payment_code")
    private String paymentCode;

    @Column("user_id")
    private String userId;

    @Column("water_box_id")
    private String waterBoxId;

    @Column("payment_type")
    private String paymentType;

    @Column("payment_method")
    private String paymentMethod;

    @Column("total_amount")
    private BigDecimal totalAmount;

    @Column("payment_date")
    private LocalDate paymentDate;

    @Column("payment_status")
    private String paymentStatus;

    @Column("external_reference")
    private String externalReference;

    @Override
    public String getId() {
        return paymentId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}