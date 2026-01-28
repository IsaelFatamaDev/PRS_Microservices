package pe.edu.vallegrande.vgmsnotification.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IGetTemplateUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.ITemplateRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetTemplateUseCaseImpl implements IGetTemplateUseCase {

    private final ITemplateRepository repository;

    @Override
    public Mono<NotificationTemplate> findByCode(String code) {
        return repository.findByCode(code);
    }

    @Override
    public Flux<NotificationTemplate> findByChannel(String channel) {
        return repository.findByChannel(channel);
    }

    @Override
    public Flux<NotificationTemplate> findActive() {
        return repository.findByStatus("ACTIVE");
    }
}
