package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities.ZoneDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ZoneMongoRepository extends ReactiveMongoRepository<ZoneDocument, String> {

    Flux<ZoneDocument> findByOrganizationId(String organizationId);

    Flux<ZoneDocument> findByStatus(String status);

    Mono<Boolean> existsByZoneCode(String zoneCode);
}
