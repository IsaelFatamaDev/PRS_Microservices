package pe.edu.vallegrande.vgmsinventorypurchases.application.services.impl;

import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsinventorypurchases.application.services.ProductCategoryService;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.GeneralStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.ProductCategory;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.ProductCategoryRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.ProductCategoryResponse;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.exception.CustomException;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper.ProductCategoryMapper;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository.ProductCategoryRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class ProductCategoryServiceImpl implements ProductCategoryService {

     private final ProductCategoryRepository productCategoryRepository;
     private final ProductCategoryMapper productCategoryMapper;

     public ProductCategoryServiceImpl(ProductCategoryRepository productCategoryRepository,
               ProductCategoryMapper productCategoryMapper) {
          this.productCategoryRepository = productCategoryRepository;
          this.productCategoryMapper = productCategoryMapper;
     }

     @Override
     public Flux<ProductCategoryResponse> findAll(String organizationId) {
          return productCategoryRepository.findByOrganizationId(organizationId)
                    .map(productCategoryMapper::toDomain)
                    .map(this::mapToResponse);
     }

     @Override
     public Mono<ProductCategoryResponse> findById(String id) {
          return productCategoryRepository.findById(id)
                    .map(productCategoryMapper::toDomain)
                    .map(this::mapToResponse)
                    .switchIfEmpty(
                              Mono.error(new CustomException("Categoría no encontrada", "CATEGORY_NOT_FOUND", 404)));
     }

     @Override
     public Mono<ProductCategoryResponse> create(ProductCategoryRequest request) {
          return productCategoryRepository
                    .existsByCategoryCodeAndOrganizationId(request.getCategoryCode(), request.getOrganizationId())
                    .flatMap(exists -> {
                         if (Boolean.TRUE.equals(exists)) {
                              return Mono.error(new CustomException("Ya existe una categoría con este código",
                                        "CATEGORY_CODE_EXISTS", 400));
                         }

                         ProductCategory category = mapToDomain(request);
                         category.setCreatedAt(LocalDateTime.now());

                         if (category.getStatus() == null) {
                              category.setStatus(GeneralStatus.ACTIVO);
                         }

                         return productCategoryRepository.save(productCategoryMapper.toEntity(category))
                                   .map(productCategoryMapper::toDomain)
                                   .map(this::mapToResponse);
                    });
     }

     @Override
     public Mono<ProductCategoryResponse> update(String id, ProductCategoryRequest request) {
          return productCategoryRepository.findById(id)
                    .switchIfEmpty(
                              Mono.error(new CustomException("Categoría no encontrada", "CATEGORY_NOT_FOUND", 404)))
                    .map(productCategoryMapper::toDomain)
                    .flatMap(existingCategory -> {
                         if (!existingCategory.getCategoryCode().equals(request.getCategoryCode())) {
                              return productCategoryRepository
                                        .existsByCategoryCodeAndOrganizationId(request.getCategoryCode(),
                                                  request.getOrganizationId())
                                        .flatMap(exists -> {
                                             if (Boolean.TRUE.equals(exists)) {
                                                  return Mono.error(new CustomException(
                                                            "Ya existe una categoría con este código",
                                                            "CATEGORY_CODE_EXISTS", 400));
                                             }
                                             return updateCategory(existingCategory, request);
                                        });
                         }
                         return updateCategory(existingCategory, request);
                    });
     }

     private Mono<ProductCategoryResponse> updateCategory(ProductCategory existingCategory,
               ProductCategoryRequest request) {
          existingCategory.setCategoryCode(request.getCategoryCode());
          existingCategory.setCategoryName(request.getCategoryName());
          existingCategory.setDescription(request.getDescription());

          if (request.getStatus() != null) {
               existingCategory.setStatus(request.getStatus());
          }

          return productCategoryRepository.save(productCategoryMapper.toEntity(existingCategory))
                    .map(productCategoryMapper::toDomain)
                    .map(this::mapToResponse);
     }

     @Override
     public Mono<Void> delete(String id) {
          return productCategoryRepository.findById(id)
                    .switchIfEmpty(
                              Mono.error(new CustomException("Categoría no encontrada", "CATEGORY_NOT_FOUND", 404)))
                    .map(productCategoryMapper::toDomain)
                    .flatMap(category -> {
                         category.setStatus(GeneralStatus.INACTIVO);
                         return productCategoryRepository.save(productCategoryMapper.toEntity(category));
                    })
                    .then();
     }

     @Override
     public Flux<ProductCategoryResponse> findAllByStatus(String organizationId, GeneralStatus status) {
          return productCategoryRepository.findByOrganizationIdAndStatus(organizationId, status)
                    .map(productCategoryMapper::toDomain)
                    .map(this::mapToResponse);
     }

     @Override
     public Mono<ProductCategoryResponse> restore(String id) {
          return productCategoryRepository.findById(id)
                    .switchIfEmpty(
                              Mono.error(new CustomException("Categoría no encontrada", "CATEGORY_NOT_FOUND", 404)))
                    .map(productCategoryMapper::toDomain)
                    .flatMap(category -> {
                         category.setStatus(GeneralStatus.ACTIVO);
                         return productCategoryRepository.save(productCategoryMapper.toEntity(category))
                                   .map(productCategoryMapper::toDomain)
                                   .map(this::mapToResponse);
                    });
     }

     private ProductCategory mapToDomain(ProductCategoryRequest request) {
          return ProductCategory.builder()
                    .organizationId(request.getOrganizationId())
                    .categoryCode(request.getCategoryCode())
                    .categoryName(request.getCategoryName())
                    .description(request.getDescription())
                    .status(request.getStatus())
                    .build();
     }

     private ProductCategoryResponse mapToResponse(ProductCategory category) {
          return ProductCategoryResponse.builder()
                    .categoryId(category.getCategoryId())
                    .organizationId(category.getOrganizationId())
                    .categoryCode(category.getCategoryCode())
                    .categoryName(category.getCategoryName())
                    .description(category.getDescription())
                    .status(category.getStatus())
                    .createdAt(category.getCreatedAt())
                    .build();
     }
}
