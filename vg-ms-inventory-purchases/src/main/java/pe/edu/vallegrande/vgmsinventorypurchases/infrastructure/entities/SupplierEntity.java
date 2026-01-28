package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.SupplierStatus;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("suppliers")
public class SupplierEntity {

    @Id
    @Column("supplier_id")
    private String supplierId;

    @Column("organization_id")
    private String organizationId;

    @Column("supplier_code")
    private String supplierCode;

    @Column("supplier_name")
    private String supplierName;

    @Column("contact_person")
    private String contactPerson;

    @Column("phone")
    private String phone;

    @Column("email")
    private String email;

    @Column("address")
    private String address;

    @Column("status")
    private SupplierStatus status;

    @Column("created_at")
    private LocalDateTime createdAt;
}
