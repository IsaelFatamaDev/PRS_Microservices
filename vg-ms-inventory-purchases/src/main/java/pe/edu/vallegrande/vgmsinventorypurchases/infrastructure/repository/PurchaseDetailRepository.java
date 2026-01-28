package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.PurchaseDetailEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface PurchaseDetailRepository extends R2dbcRepository<PurchaseDetailEntity, String> {

    Flux<PurchaseDetailEntity> findByProductId(String productId);

    Flux<PurchaseDetailEntity> findByPurchaseId(String purchaseId);

    Mono<Void> deleteByPurchaseId(String purchaseId);
}
