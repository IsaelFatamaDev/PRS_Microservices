package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.ProductCategory;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.ProductCategoryEntity;

@Component
public class ProductCategoryMapper {

    public ProductCategory toDomain(ProductCategoryEntity entity) {
        if (entity == null) return null;
        return ProductCategory.builder()
                .categoryId(entity.getCategoryId())
                .organizationId(entity.getOrganizationId())
                .categoryCode(entity.getCategoryCode())
                .categoryName(entity.getCategoryName())
                .description(entity.getDescription())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public ProductCategoryEntity toEntity(ProductCategory domain) {
        if (domain == null) return null;
        return ProductCategoryEntity.builder()
                .categoryId(domain.getCategoryId())
                .organizationId(domain.getOrganizationId())
                .categoryCode(domain.getCategoryCode())
                .categoryName(domain.getCategoryName())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
