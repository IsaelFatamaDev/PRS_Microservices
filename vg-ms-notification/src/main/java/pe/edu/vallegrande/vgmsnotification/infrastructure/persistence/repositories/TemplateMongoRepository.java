package pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.entities.TemplateDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface TemplateMongoRepository extends ReactiveMongoRepository<TemplateDocument, String> {
    Mono<TemplateDocument> findByCode(String code);
    Flux<TemplateDocument> findByChannel(String channel);
    Flux<TemplateDocument> findByStatus(String status);
    Mono<Boolean> existsByCode(String code);
}
