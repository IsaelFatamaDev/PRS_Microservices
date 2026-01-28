package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.PurchaseEntity;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;
import reactor.core.publisher.Flux;

@Repository
public interface PurchaseRepository extends R2dbcRepository<PurchaseEntity, String> {

    Flux<PurchaseEntity> findByRequestedByUserId(String userId);

    Flux<PurchaseEntity> findByOrganizationId(String organizationId);

    Flux<PurchaseEntity> findByOrganizationIdAndStatus(String organizationId, PurchaseStatus status);
}
