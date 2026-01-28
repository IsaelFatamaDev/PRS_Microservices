package pe.edu.vallegrande.vgmsnotification.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsnotification.domain.exceptions.TemplateNotFoundException;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.NotificationChannel;
import pe.edu.vallegrande.vgmsnotification.domain.ports.in.ISendNotificationUseCase;
import pe.edu.vallegrande.vgmsnotification.domain.ports.out.*;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class SendNotificationUseCaseImpl implements ISendNotificationUseCase {
    
    private final INotificationRepository notificationRepository;
    private final ITemplateRepository templateRepository;
    private final IWhatsAppService whatsAppService;
    private final ISmsService smsService;
    private final IEmailService emailService;
    private final IDomainEventPublisher eventPublisher;
    
    @Override
    public Mono<Notification> execute(Notification notification) {
        log.info("Sending notification: {} via {}", notification.getType(), notification.getChannel());
        
        return prepareNotification(notification)
                .flatMap(this::sendNotification)
                .flatMap(notificationRepository::save)
                .flatMap(this::publishDomainEvents);
    }
    
    private Mono<Notification> prepareNotification(Notification notification) {
        if (notification.getTemplateId() != null && notification.getMessage() == null) {
            return templateRepository.findByCode(notification.getTemplateId())
                    .switchIfEmpty(Mono.error(new TemplateNotFoundException(notification.getTemplateId())))
                    .map(template -> {
                        String renderedMessage = template.renderTemplate(notification.getTemplateParams());
                        notification.setMessage(renderedMessage);
                        if (template.getSubject() != null) {
                            notification.setSubject(template.getSubject());
                        }
                        return notification;
                    });
        }
        return Mono.just(notification);
    }
    
    private Mono<Notification> sendNotification(Notification notification) {
        NotificationChannel channel = notification.getChannel();
        String recipient = notification.getRecipient();
        String message = notification.getMessage();
        
        Mono<String> sendOperation = switch (channel) {
            case SMS -> sendSms(recipient, message);
            case WHATSAPP -> sendWhatsApp(recipient, message);
            case EMAIL -> sendEmail(notification);
            case IN_APP -> Mono.just("IN_APP");  // Solo se guarda en BD
        };
        
        return sendOperation
                .doOnSuccess(providerId -> {
                    notification.setProviderId(providerId);
                    notification.setProviderName(getProviderName(channel));
                    notification.markAsSent();
                })
                .onErrorResume(error -> {
                    log.error("Failed to send notification", error);
                    notification.markAsFailed(error.getMessage());
                    return Mono.just(notification);
                })
                .thenReturn(notification);
    }
    
    private Mono<String> sendSms(String recipient, String message) {
        return smsService.sendSms(recipient, message);
    }
    
    private Mono<String> sendWhatsApp(String recipient, String message) {
        return whatsAppService.sendMessage(recipient, message);
    }
    
    private Mono<String> sendEmail(Notification notification) {
        return emailService.sendEmail(
                notification.getRecipient(),
                notification.getSubject(),
                notification.getMessage()
        );
    }
    
    private String getProviderName(NotificationChannel channel) {
        return switch (channel) {
            case SMS -> "LOCAL_SMS_GATEWAY";
            case WHATSAPP -> "OWN_WHATSAPP_NUMBER";
            case EMAIL -> "SMTP_SERVER";
            case IN_APP -> "IN_APP";
        };
    }
    
    private Mono<Notification> publishDomainEvents(Notification notification) {
        return Mono.just(notification)
                .flatMap(n -> {
                    if (n.getDomainEvents().isEmpty()) {
                        return Mono.just(n);
                    }
                    
                    return Mono.when(
                            n.getDomainEvents().stream()
                                    .map(eventPublisher::publish)
                                    .toList()
                    ).then(Mono.fromRunnable(n::clearDomainEvents))
                            .thenReturn(n);
                });
    }
}
