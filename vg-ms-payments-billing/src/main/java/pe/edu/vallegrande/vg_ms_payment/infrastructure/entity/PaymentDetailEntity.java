package pe.edu.vallegrande.vg_ms_payment.infrastructure.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad PostgreSQL para la tabla payment_details
 * Coincide exactamente con el esquema de base de datos real
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("payment_details")
public class PaymentDetailEntity implements Persistable<String> {
    
    @Id
    @Column("payment_detail_id")
    private String paymentDetailId;

    @Transient
    private boolean isNew = true;

    @Column("payment_id")
    private String paymentId;

    @Column("concept")
    private String concept;

    @Column("year")
    private Integer year;

    @Column("month")
    private Integer month;

    @Column("amount")
    private BigDecimal amount;

    @Column("description")
    private String description;

    @Column("period_start")
    private LocalDate periodStart;

    @Column("period_end")
    private LocalDate periodEnd;

    @Override
    public String getId() {
        return paymentDetailId;
    }

    @Override
    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }
}