package pe.edu.vallegrande.vgmsinventorypurchases.application.services;

import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.ProductStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.ProductRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.ProductResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MaterialsService {
     Flux<ProductResponse> findAll(String organizationId);

     Flux<ProductResponse> findAllByStatus(String organizationId, ProductStatus status);

     Flux<ProductResponse> findByCategoryId(String categoryId, String organizationId);

     Flux<ProductResponse> findByCategoryIdAndStatus(String categoryId, String organizationId, ProductStatus status);

     Mono<ProductResponse> findById(String id);

     Mono<ProductResponse> create(ProductRequest request);

     Mono<ProductResponse> update(String id, ProductRequest request);

     Mono<Void> delete(String id);

     Mono<ProductResponse> restore(String id);
}
