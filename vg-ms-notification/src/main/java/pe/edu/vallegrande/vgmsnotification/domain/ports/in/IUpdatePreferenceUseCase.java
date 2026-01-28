package pe.edu.vallegrande.vgmsnotification.domain.ports.in;

import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationPreference;
import reactor.core.publisher.Mono;

public interface IUpdatePreferenceUseCase {
     Mono<NotificationPreference> execute(NotificationPreference preference);
}
