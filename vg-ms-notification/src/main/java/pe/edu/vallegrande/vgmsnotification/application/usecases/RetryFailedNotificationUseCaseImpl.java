package pe.edu.vallegrande.vgmsnotification.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsnotification.domain.exceptions.NotificationNotFoundException;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.IRetryFailedNotificationUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.ISendNotificationUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.INotificationRepository;
import reactor.core.publisher.Mono;

/**
 * Caso de uso para reintentar notificaciones fallidas
 * Valida si la notificación puede ser reintentada según su prioridad
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RetryFailedNotificationUseCaseImpl implements IRetryFailedNotificationUseCase {

    private final INotificationRepository notificationRepository;
    private final ISendNotificationUseCase sendNotificationUseCase;

    @Override
    public Mono<Notification> execute(String notificationId) {
        return notificationRepository.findById(notificationId)
            .switchIfEmpty(Mono.error(new NotificationNotFoundException(notificationId)))
            .flatMap(notification -> {
                // Validar si puede reintentar
                if (!notification.canRetry()) {
                    log.warn("Notification {} cannot be retried (max retries reached)", notificationId);
                    return Mono.error(new RuntimeException("Max retries reached for notification"));
                }

                // Incrementar contador de reintentos
                notification.incrementRetry();

                log.info("Retrying notification {}, attempt {}",
                    notificationId, notification.getRetryCount());

                // Reintentar envío
                return sendNotificationUseCase.execute(notification);
            });
    }
}
