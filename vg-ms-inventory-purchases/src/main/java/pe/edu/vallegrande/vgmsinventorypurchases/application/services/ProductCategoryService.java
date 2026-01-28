package pe.edu.vallegrande.vgmsinventorypurchases.application.services;

import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.GeneralStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.ProductCategoryRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.ProductCategoryResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductCategoryService {
     Flux<ProductCategoryResponse> findAll(String organizationId);

     Flux<ProductCategoryResponse> findAllByStatus(String organizationId, GeneralStatus status);

     Mono<ProductCategoryResponse> findById(String id);

     Mono<ProductCategoryResponse> create(ProductCategoryRequest request);

     Mono<ProductCategoryResponse> update(String id, ProductCategoryRequest request);

     Mono<Void> delete(String id);

     Mono<ProductCategoryResponse> restore(String id);
}
