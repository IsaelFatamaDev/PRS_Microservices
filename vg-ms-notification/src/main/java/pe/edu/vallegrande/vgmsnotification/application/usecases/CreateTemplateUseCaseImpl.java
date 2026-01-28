package pe.edu.vallegrande.vgmsnotification.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.ICreateTemplateUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.ITemplateRepository;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class CreateTemplateUseCaseImpl implements ICreateTemplateUseCase {

    private final ITemplateRepository repository;
    private final IDomainEventPublisher eventPublisher;

    @Override
    public Mono<NotificationTemplate> execute(NotificationTemplate template) {
        return Mono.just(NotificationTemplate.createNew(template))
                .flatMap(repository::save)
                .flatMap(this::publishDomainEvents);
    }

    private Mono<NotificationTemplate> publishDomainEvents(NotificationTemplate template) {
        return Mono.when(
                template.getDomainEvents().stream()
                        .map(eventPublisher::publish)
                        .toList()
        ).then(Mono.fromRunnable(template::clearDomainEvents))
                .thenReturn(template);
    }
}
