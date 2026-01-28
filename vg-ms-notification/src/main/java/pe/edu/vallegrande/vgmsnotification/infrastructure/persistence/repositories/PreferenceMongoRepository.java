package pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.entities.PreferenceDocument;
import reactor.core.publisher.Mono;

public interface PreferenceMongoRepository extends ReactiveMongoRepository<PreferenceDocument, String> {
    Mono<PreferenceDocument> findByUserId(String userId);
}
