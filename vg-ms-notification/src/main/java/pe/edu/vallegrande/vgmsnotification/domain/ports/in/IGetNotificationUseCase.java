package pe.edu.vallegrande.vgmsnotification.domain.ports.in;

import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetNotificationUseCase {
    Mono<Notification> findById(String id);
    Flux<Notification> findByUserId(String userId);
    Flux<Notification> findByStatus(String status);
    Flux<Notification> findUnreadByUserId(String userId);
}
