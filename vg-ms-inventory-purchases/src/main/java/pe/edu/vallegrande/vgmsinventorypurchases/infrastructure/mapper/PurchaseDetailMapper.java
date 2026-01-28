package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.PurchaseDetail;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.PurchaseDetailEntity;

@Component
public class PurchaseDetailMapper {

    public PurchaseDetail toDomain(PurchaseDetailEntity entity) {
        if (entity == null) return null;
        return PurchaseDetail.builder()
                .purchaseDetailId(entity.getPurchaseDetailId())
                .purchaseId(entity.getPurchaseId())
                .productId(entity.getProductId())
                .quantityOrdered(entity.getQuantityOrdered())
                .quantityReceived(entity.getQuantityReceived())
                .unitCost(entity.getUnitCost())
                .subtotal(entity.getSubtotal())
                .observations(entity.getObservations())
                .build();
    }

    public PurchaseDetailEntity toEntity(PurchaseDetail domain) {
        if (domain == null) return null;
        return PurchaseDetailEntity.builder()
                .purchaseDetailId(domain.getPurchaseDetailId())
                .purchaseId(domain.getPurchaseId())
                .productId(domain.getProductId())
                .quantityOrdered(domain.getQuantityOrdered())
                .quantityReceived(domain.getQuantityReceived())
                .unitCost(domain.getUnitCost())
                .subtotal(domain.getSubtotal())
                .observations(domain.getObservations())
                .build();
    }
}
