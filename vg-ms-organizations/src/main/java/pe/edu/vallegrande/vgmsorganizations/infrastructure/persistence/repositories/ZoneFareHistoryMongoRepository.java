package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities.ZoneFareHistoryDocument;
import reactor.core.publisher.Flux;

public interface ZoneFareHistoryMongoRepository extends ReactiveMongoRepository<ZoneFareHistoryDocument, String> {

    Flux<ZoneFareHistoryDocument> findByZoneId(String zoneId);
}
