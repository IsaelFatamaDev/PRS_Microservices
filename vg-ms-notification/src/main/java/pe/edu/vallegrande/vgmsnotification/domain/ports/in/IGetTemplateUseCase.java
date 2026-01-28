package pe.edu.vallegrande.vgmsnotification.domain.ports.in;

import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IGetTemplateUseCase {
     Mono<NotificationTemplate> findByCode(String code);

     Flux<NotificationTemplate> findByChannel(String channel);

     Flux<NotificationTemplate> findActive();
}
