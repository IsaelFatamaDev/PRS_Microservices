package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.ProductStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.MaterialEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface MaterialRepository extends R2dbcRepository<MaterialEntity, String> {
     Flux<MaterialEntity> findByOrganizationId(String organizationId);

     Flux<MaterialEntity> findByOrganizationIdAndStatus(String organizationId, ProductStatus status);

     Flux<MaterialEntity> findByCategoryIdAndOrganizationId(String categoryId, String organizationId);

     Flux<MaterialEntity> findByCategoryIdAndOrganizationIdAndStatus(String categoryId, String organizationId,
               ProductStatus status);

     Mono<MaterialEntity> findByProductCodeAndOrganizationId(String productCode, String organizationId);

     Mono<Boolean> existsByProductCodeAndOrganizationId(String productCode, String organizationId);

     @org.springframework.data.r2dbc.repository.Modifying
     @org.springframework.data.r2dbc.repository.Query("UPDATE products SET current_stock = :newStock, updated_at = CURRENT_TIMESTAMP " +
               "WHERE product_id = :productId AND organization_id = :organizationId")
     Mono<Integer> updateCurrentStock(@org.springframework.data.repository.query.Param("productId") String productId,
               @org.springframework.data.repository.query.Param("organizationId") String organizationId,
               @org.springframework.data.repository.query.Param("newStock") Integer newStock);
}
