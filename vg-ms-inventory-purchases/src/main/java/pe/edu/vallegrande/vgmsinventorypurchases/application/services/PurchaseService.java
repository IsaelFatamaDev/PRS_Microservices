package pe.edu.vallegrande.vgmsinventorypurchases.application.services;

import pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.PurchaseStatus;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.PurchaseRequest;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.response.PurchaseResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface PurchaseService {

     Flux<PurchaseResponse> findAll();

     Flux<PurchaseResponse> findByOrganizationId(String organizationId);

     Mono<PurchaseResponse> findById(String purchaseId);

     Mono<PurchaseResponse> create(PurchaseRequest purchaseRequest);

     Mono<PurchaseResponse> update(String purchaseId, PurchaseRequest purchaseRequest);

     Mono<Void> delete(String purchaseId);

     Mono<PurchaseResponse> restore(String purchaseId);

     Flux<PurchaseResponse> findByStatus(String organizationId, PurchaseStatus status);

     Mono<PurchaseResponse> updateStatus(String purchaseId, PurchaseStatus status);
}
