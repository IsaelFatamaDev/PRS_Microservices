package pe.edu.vallegrande.vgmsinventorypurchases.application.services;

import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.SupplierStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.SupplierRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.SupplierResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SupplierService {
     Flux<SupplierResponse> findAll(String organizationId);

     Flux<SupplierResponse> findAllByStatus(String organizationId, SupplierStatus status);

     Mono<SupplierResponse> findById(String id);

     Mono<SupplierResponse> create(SupplierRequest request);

     Mono<SupplierResponse> update(String id, SupplierRequest request);

     Mono<Void> delete(String id);

     Mono<SupplierResponse> restore(String id);
}
