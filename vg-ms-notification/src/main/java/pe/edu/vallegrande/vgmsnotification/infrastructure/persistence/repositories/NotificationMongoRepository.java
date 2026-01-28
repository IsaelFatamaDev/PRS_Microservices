package pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.entities.NotificationDocument;
import reactor.core.publisher.Flux;

public interface NotificationMongoRepository extends ReactiveMongoRepository<NotificationDocument, String> {
    Flux<NotificationDocument> findByUserId(String userId);
    Flux<NotificationDocument> findByStatus(String status);
    Flux<NotificationDocument> findByUserIdAndStatus(String userId, String status);
}
