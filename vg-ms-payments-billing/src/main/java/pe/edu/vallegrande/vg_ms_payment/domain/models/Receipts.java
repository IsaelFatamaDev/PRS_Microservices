package pe.edu.vallegrande.vg_ms_payment.domain.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("receipts")
public class Receipts {
    @Id
    @Column("receipts_id")
    private String receiptsId;

    @Column("organization_id")
    private String organizationId;

    @Column("payment_id")
    private String paymentId;

    @Column("payment_detail_id")
    private String paymentDetailId;

    @Column("receipt_series")
    private String receiptSeries;

    @Column("receipt_number")
    private String receiptNumber;

    @Column("receipt_type")
    private String receiptType;

    @Column("issue_date")
    private LocalDateTime issueDate;

    private BigDecimal amount;
    private Integer year;
    private Integer month;
    private String concept;

    @Column("customer_full_name")
    private String customerFullName;

    @Column("customer_document")
    private String customerDocument;

    @Column("pdf_generated")
    private Boolean pdfGenerated;

    @Column("pdf_path")
    private String pdfPath;

    @Column("created_at")
    private String createdAt;
}
