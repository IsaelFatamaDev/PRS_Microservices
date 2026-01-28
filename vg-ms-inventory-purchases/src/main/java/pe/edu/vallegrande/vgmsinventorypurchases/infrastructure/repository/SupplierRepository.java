package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.SupplierStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.SupplierEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SupplierRepository extends R2dbcRepository<SupplierEntity, String> {
     Flux<SupplierEntity> findByOrganizationId(String organizationId);

     Flux<SupplierEntity> findByOrganizationIdAndStatus(String organizationId, SupplierStatus status);

     Mono<SupplierEntity> findBySupplierCodeAndOrganizationId(String supplierCode, String organizationId);

     Mono<Boolean> existsBySupplierCodeAndOrganizationId(String supplierCode, String organizationId);
}
