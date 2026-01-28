package pe.edu.vallegrande.vgmsnotification.domain.ports.in;

import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import reactor.core.publisher.Mono;

public interface ISendNotificationUseCase {
     Mono<Notification> execute(Notification notification);
}
