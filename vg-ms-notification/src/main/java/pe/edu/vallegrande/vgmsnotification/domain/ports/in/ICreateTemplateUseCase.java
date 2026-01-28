package pe.edu.vallegrande.vgmsnotification.domain.ports.in;

import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import reactor.core.publisher.Mono;

public interface ICreateTemplateUseCase {
     Mono<NotificationTemplate> execute(NotificationTemplate template);
}
