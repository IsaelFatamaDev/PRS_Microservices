package pe.edu.vallegrande.vgmsnotification.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsnotification.domain.exceptions.NotificationNotFoundException;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IMarkAsReadUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.INotificationRepository;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarkAsReadUseCaseImpl implements IMarkAsReadUseCase {

    private final INotificationRepository repository;
    private final IDomainEventPublisher eventPublisher;

    @Override
    public Mono<Void> execute(String notificationId) {
        return repository.findById(notificationId)
                .switchIfEmpty(Mono.error(new NotificationNotFoundException(notificationId)))
                .doOnNext(notification -> notification.markAsRead())
                .flatMap(notification -> repository.save(notification)
                        .flatMap(saved -> publishDomainEvents(saved)))
                .then();
    }

    private Mono<Void> publishDomainEvents(pe.edu.vallegrande.vgmsnotification.domain.models.Notification notification) {
        return Mono.when(
                notification.getDomainEvents().stream()
                        .map(eventPublisher::publish)
                        .toList()
        ).then(Mono.fromRunnable(notification::clearDomainEvents));
    }
}
