package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities.StreetDocument;
import reactor.core.publisher.Flux;

public interface StreetMongoRepository extends ReactiveMongoRepository<StreetDocument, String> {

    Flux<StreetDocument> findByZoneId(String zoneId);
}
