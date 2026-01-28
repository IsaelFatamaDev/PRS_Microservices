package pe.edu.vallegrande.vgmsnotification.application.usecases;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IGetNotificationUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.INotificationRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetNotificationUseCaseImpl implements IGetNotificationUseCase {

    private final INotificationRepository repository;

    @Override
    public Mono<Notification> findById(String id) {
        return repository.findById(id);
    }

    @Override
    public Flux<Notification> findByUserId(String userId) {
        return repository.findByUserId(userId);
    }

    @Override
    public Flux<Notification> findByStatus(String status) {
        return repository.findByStatus(status);
    }

    @Override
    public Flux<Notification> findUnreadByUserId(String userId) {
        return repository.findUnreadByUserId(userId);
    }
}
