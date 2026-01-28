package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.GeneralStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.ProductCategoryEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ProductCategoryRepository extends R2dbcRepository<ProductCategoryEntity, String> {
     Flux<ProductCategoryEntity> findByOrganizationId(String organizationId);

     Flux<ProductCategoryEntity> findByOrganizationIdAndStatus(String organizationId, GeneralStatus status);

     Mono<ProductCategoryEntity> findByCategoryCodeAndOrganizationId(String categoryCode, String organizationId);

     Mono<Boolean> existsByCategoryCodeAndOrganizationId(String categoryCode, String organizationId);
}
