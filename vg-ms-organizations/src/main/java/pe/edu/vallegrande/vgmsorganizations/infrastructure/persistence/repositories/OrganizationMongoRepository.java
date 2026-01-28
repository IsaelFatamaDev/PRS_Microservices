package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities.OrganizationDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationMongoRepository extends ReactiveMongoRepository<OrganizationDocument, String> {

    Mono<OrganizationDocument> findByRuc(String ruc);

    Flux<OrganizationDocument> findByStatus(String status);

    Flux<OrganizationDocument> findByRegion(String region);

    Mono<Boolean> existsByRuc(String ruc);
}
