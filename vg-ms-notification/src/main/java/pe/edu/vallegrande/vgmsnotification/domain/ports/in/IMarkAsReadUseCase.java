package pe.edu.vallegrande.vgmsnotification.domain.ports.in;

import reactor.core.publisher.Mono;

public interface IMarkAsReadUseCase {
     Mono<Void> execute(String notificationId);
}
