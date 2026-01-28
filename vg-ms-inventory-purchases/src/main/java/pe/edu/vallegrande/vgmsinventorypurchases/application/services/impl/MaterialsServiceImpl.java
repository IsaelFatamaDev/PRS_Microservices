package pe.edu.vallegrande.vgmsinventorypurchases.application.services.impl;

import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.MaterialsService;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.ProductStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.Material;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.ProductRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.ProductResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.MaterialMapper;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.MaterialRepository;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.ProductCategoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class MaterialsServiceImpl implements MaterialsService {

     private final MaterialRepository materialRepository;
     private final ProductCategoryRepository productCategoryRepository;
     private final MaterialMapper materialMapper;

     public MaterialsServiceImpl(MaterialRepository materialRepository,
               ProductCategoryRepository productCategoryRepository,
               MaterialMapper materialMapper) {
          this.materialRepository = materialRepository;
          this.productCategoryRepository = productCategoryRepository;
          this.materialMapper = materialMapper;
     }

     @Override
     public Flux<ProductResponse> findAll(String organizationId) {
          return materialRepository.findByOrganizationId(organizationId)
                    .map(materialMapper::toDomain)
                    .flatMap(this::enrichWithCategory);
     }

     @Override
     public Mono<ProductResponse> findById(String id) {
          return materialRepository.findById(id)
                    .map(materialMapper::toDomain)
                    .flatMap(this::enrichWithCategory)
                    .switchIfEmpty(Mono.error(new CustomException("Producto no encontrado", "PRODUCT_NOT_FOUND", 404)));
     }

     @Override
     public Mono<ProductResponse> create(ProductRequest request) {
          return materialRepository
                    .existsByProductCodeAndOrganizationId(request.getProductCode(), request.getOrganizationId())
                    .flatMap(exists -> {
                         if (Boolean.TRUE.equals(exists)) {
                              return Mono.error(new CustomException("Ya existe un producto con este código",
                                        "PRODUCT_CODE_EXISTS", 400));
                         }

                         return productCategoryRepository.findById(request.getCategoryId())
                                   .switchIfEmpty(Mono.error(
                                             new CustomException("Categoría no encontrada", "CATEGORY_NOT_FOUND", 404)))
                                   .flatMap(categoryEntity -> {
                                        Material material = mapToDomain(request);
                                        material.setCreatedAt(LocalDateTime.now());
                                        material.setUpdatedAt(LocalDateTime.now());

                                        if (material.getStatus() == null) {
                                             material.setStatus(ProductStatus.ACTIVO);
                                        }

                                        return materialRepository.save(materialMapper.toEntity(material))
                                                  .map(materialMapper::toDomain)
                                                  .map(savedMaterial -> {
                                                       ProductResponse response = mapToResponse(savedMaterial);
                                                       response.setCategoryName(categoryEntity.getCategoryName());
                                                       return response;
                                                  });
                                   });
                    });
     }

     @Override
     public Mono<ProductResponse> update(String id, ProductRequest request) {
          return materialRepository.findById(id)
                    .switchIfEmpty(Mono.error(new CustomException("Producto no encontrado", "PRODUCT_NOT_FOUND", 404)))
                    .map(materialMapper::toDomain)
                    .flatMap(existingMaterial -> {
                         if (!existingMaterial.getProductCode().equals(request.getProductCode())) {
                              return materialRepository
                                        .existsByProductCodeAndOrganizationId(request.getProductCode(),
                                                  request.getOrganizationId())
                                        .flatMap(exists -> {
                                             if (Boolean.TRUE.equals(exists)) {
                                                  return Mono.error(
                                                            new CustomException("Ya existe un producto con este código",
                                                                      "PRODUCT_CODE_EXISTS", 400));
                                             }
                                             return updateMaterial(existingMaterial, request);
                                        });
                         }
                         return updateMaterial(existingMaterial, request);
                    });
     }

     private Mono<ProductResponse> updateMaterial(Material existingMaterial, ProductRequest request) {
          return productCategoryRepository.findById(request.getCategoryId())
                    .switchIfEmpty(
                              Mono.error(new CustomException("Categoría no encontrada", "CATEGORY_NOT_FOUND", 404)))
                    .flatMap(categoryEntity -> {
                         existingMaterial.setProductCode(request.getProductCode());
                         existingMaterial.setProductName(request.getProductName());
                         existingMaterial.setCategoryId(request.getCategoryId());
                         existingMaterial.setUnitOfMeasure(request.getUnitOfMeasure());
                         existingMaterial.setMinimumStock(request.getMinimumStock());
                         existingMaterial.setMaximumStock(request.getMaximumStock());
                         existingMaterial.setCurrentStock(request.getCurrentStock());
                         existingMaterial.setUnitCost(request.getUnitCost());
                         existingMaterial.setUpdatedAt(LocalDateTime.now());

                         if (request.getStatus() != null) {
                              existingMaterial.setStatus(request.getStatus());
                         }

                         return materialRepository.save(materialMapper.toEntity(existingMaterial))
                                   .map(materialMapper::toDomain)
                                   .map(updatedMaterial -> {
                                        ProductResponse response = mapToResponse(updatedMaterial);
                                        response.setCategoryName(categoryEntity.getCategoryName());
                                        return response;
                                   });
                    });
     }

     @Override
     public Mono<Void> delete(String id) {
          return materialRepository.findById(id)
                    .switchIfEmpty(Mono.error(new CustomException("Producto no encontrado", "PRODUCT_NOT_FOUND", 404)))
                    .map(materialMapper::toDomain)
                    .flatMap(material -> {
                         material.setStatus(ProductStatus.INACTIVO);
                         return materialRepository.save(materialMapper.toEntity(material));
                    })
                    .then();
     }

     @Override
     public Flux<ProductResponse> findAllByStatus(String organizationId, ProductStatus status) {
          return materialRepository.findByOrganizationIdAndStatus(organizationId, status)
                    .map(materialMapper::toDomain)
                    .flatMap(this::enrichWithCategory);
     }

     @Override
     public Flux<ProductResponse> findByCategoryId(String categoryId, String organizationId) {
          return materialRepository.findByCategoryIdAndOrganizationId(categoryId, organizationId)
                    .map(materialMapper::toDomain)
                    .flatMap(this::enrichWithCategory);
     }

     @Override
     public Flux<ProductResponse> findByCategoryIdAndStatus(String categoryId, String organizationId,
               ProductStatus status) {
          return materialRepository.findByCategoryIdAndOrganizationIdAndStatus(categoryId, organizationId, status)
                    .map(materialMapper::toDomain)
                    .flatMap(this::enrichWithCategory);
     }

     @Override
     public Mono<ProductResponse> restore(String id) {
          return materialRepository.findById(id)
                    .switchIfEmpty(Mono.error(new CustomException("Producto no encontrado", "PRODUCT_NOT_FOUND", 404)))
                    .map(materialMapper::toDomain)
                    .flatMap(material -> {
                         material.setStatus(ProductStatus.ACTIVO);
                         return materialRepository.save(materialMapper.toEntity(material))
                                   .map(materialMapper::toDomain)
                                   .flatMap(this::enrichWithCategory);
                    });
     }

     private Mono<ProductResponse> enrichWithCategory(Material material) {
          return productCategoryRepository.findById(material.getCategoryId())
                    .map(categoryEntity -> {
                         ProductResponse response = mapToResponse(material);
                         response.setCategoryName(categoryEntity.getCategoryName());
                         return response;
                    })
                    .defaultIfEmpty(mapToResponse(material));
     }

     private Material mapToDomain(ProductRequest request) {
          return Material.builder()
                    .organizationId(request.getOrganizationId())
                    .productCode(request.getProductCode())
                    .productName(request.getProductName())
                    .categoryId(request.getCategoryId())
                    .unitOfMeasure(request.getUnitOfMeasure())
                    .minimumStock(request.getMinimumStock())
                    .maximumStock(request.getMaximumStock())
                    .currentStock(request.getCurrentStock())
                    .unitCost(request.getUnitCost())
                    .status(request.getStatus())
                    .build();
     }

     private ProductResponse mapToResponse(Material material) {
          return ProductResponse.builder()
                    .productId(material.getProductId())
                    .organizationId(material.getOrganizationId())
                    .productCode(material.getProductCode())
                    .productName(material.getProductName())
                    .categoryId(material.getCategoryId())
                    .unitOfMeasure(material.getUnitOfMeasure())
                    .minimumStock(material.getMinimumStock())
                    .maximumStock(material.getMaximumStock())
                    .currentStock(material.getCurrentStock())
                    .unitCost(material.getUnitCost())
                    .status(material.getStatus())
                    .createdAt(material.getCreatedAt())
                    .updatedAt(material.getUpdatedAt())
                    .build();
     }
}
