package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Purchase;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.PurchaseEntity;

@Component
public class PurchaseMapper {

    public Purchase toDomain(PurchaseEntity entity) {
        if (entity == null) return null;
        return Purchase.builder()
                .purchaseId(entity.getPurchaseId())
                .organizationId(entity.getOrganizationId())
                .purchaseCode(entity.getPurchaseCode())
                .supplierId(entity.getSupplierId())
                .purchaseDate(entity.getPurchaseDate())
                .deliveryDate(entity.getDeliveryDate())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .requestedByUserId(entity.getRequestedByUserId())
                .approvedByUserId(entity.getApprovedByUserId())
                .invoiceNumber(entity.getInvoiceNumber())
                .observations(entity.getObservations())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public PurchaseEntity toEntity(Purchase domain) {
        if (domain == null) return null;
        return PurchaseEntity.builder()
                .purchaseId(domain.getPurchaseId())
                .organizationId(domain.getOrganizationId())
                .purchaseCode(domain.getPurchaseCode())
                .supplierId(domain.getSupplierId())
                .purchaseDate(domain.getPurchaseDate())
                .deliveryDate(domain.getDeliveryDate())
                .totalAmount(domain.getTotalAmount())
                .status(domain.getStatus())
                .requestedByUserId(domain.getRequestedByUserId())
                .approvedByUserId(domain.getApprovedByUserId())
                .invoiceNumber(domain.getInvoiceNumber())
                .observations(domain.getObservations())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
