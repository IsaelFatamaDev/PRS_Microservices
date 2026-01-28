package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Supplier;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.SupplierEntity;

@Component
public class SupplierMapper {

    public Supplier toDomain(SupplierEntity entity) {
        if (entity == null) return null;
        return Supplier.builder()
                .supplierId(entity.getSupplierId())
                .organizationId(entity.getOrganizationId())
                .supplierCode(entity.getSupplierCode())
                .supplierName(entity.getSupplierName())
                .contactPerson(entity.getContactPerson())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .address(entity.getAddress())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public SupplierEntity toEntity(Supplier domain) {
        if (domain == null) return null;
        return SupplierEntity.builder()
                .supplierId(domain.getSupplierId())
                .organizationId(domain.getOrganizationId())
                .supplierCode(domain.getSupplierCode())
                .supplierName(domain.getSupplierName())
                .contactPerson(domain.getContactPerson())
                .phone(domain.getPhone())
                .email(domain.getEmail())
                .address(domain.getAddress())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
