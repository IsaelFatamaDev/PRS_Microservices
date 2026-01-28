package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Material;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.MaterialEntity;

@Component
public class MaterialMapper {

    public Material toDomain(MaterialEntity entity) {
        if (entity == null) return null;
        return Material.builder()
                .productId(entity.getProductId())
                .organizationId(entity.getOrganizationId())
                .productCode(entity.getProductCode())
                .productName(entity.getProductName())
                .categoryId(entity.getCategoryId())
                .unitOfMeasure(entity.getUnitOfMeasure())
                .minimumStock(entity.getMinimumStock())
                .maximumStock(entity.getMaximumStock())
                .currentStock(entity.getCurrentStock())
                .unitCost(entity.getUnitCost())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public MaterialEntity toEntity(Material domain) {
        if (domain == null) return null;
        return MaterialEntity.builder()
                .productId(domain.getProductId())
                .organizationId(domain.getOrganizationId())
                .productCode(domain.getProductCode())
                .productName(domain.getProductName())
                .categoryId(domain.getCategoryId())
                .unitOfMeasure(domain.getUnitOfMeasure())
                .minimumStock(domain.getMinimumStock())
                .maximumStock(domain.getMaximumStock())
                .currentStock(domain.getCurrentStock())
                .unitCost(domain.getUnitCost())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
