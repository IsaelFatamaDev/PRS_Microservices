package pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.mappers;

import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import pe.edu.vallegrande.vgmsnotification.domain.valueobjects.*;
import pe.edu.vallegrande.vgmsnotification.infrastructure.persistence.entities.NotificationDocument;

import java.time.LocalDateTime;

public class NotificationDomainMapper {
    
    public static NotificationDocument toDocument(Notification notification) {
        NotificationDocument document = new NotificationDocument();
        document.setId(notification.getId());
        document.setUserId(notification.getUserId());
        document.setChannel(notification.getChannel().name());
        document.setRecipient(notification.getRecipient());
        document.setType(notification.getType().name());
        document.setSubject(notification.getSubject());
        document.setMessage(notification.getMessage());
        document.setStatus(notification.getStatus().name());
        document.setPriority(notification.getPriority().name());
        document.setTemplateId(notification.getTemplateId());
        document.setTemplateParams(notification.getTemplateParams());
        document.setProviderName(notification.getProviderName());
        document.setProviderId(notification.getProviderId());
        document.setErrorMessage(notification.getErrorMessage());
        document.setRetryCount(notification.getRetryCount());
        document.setScheduledAt(notification.getScheduledAt());
        document.setSentAt(notification.getSentAt());
        document.setDeliveredAt(notification.getDeliveredAt());
        document.setReadAt(notification.getReadAt());
        document.setCreatedAt(notification.getCreatedAt());
        
        // Calcular expiresAt: 180 días desde creación
        if (notification.getCreatedAt() != null) {
            document.setExpiresAt(notification.getCreatedAt().plusDays(180));
        }
        
        return document;
    }
    
    public static Notification toDomain(NotificationDocument document) {
        return new Notification(
            document.getId(),
            document.getUserId(),
            NotificationChannel.valueOf(document.getChannel()),
            document.getRecipient(),
            NotificationType.valueOf(document.getType()),
            document.getSubject(),
            document.getMessage(),
            NotificationStatus.valueOf(document.getStatus()),
            NotificationPriority.valueOf(document.getPriority()),
            document.getTemplateId(),
            document.getTemplateParams(),
            document.getProviderName(),
            document.getProviderId(),
            document.getErrorMessage(),
            document.getRetryCount(),
            document.getScheduledAt(),
            document.getSentAt(),
            document.getDeliveredAt(),
            document.getReadAt(),
            document.getCreatedAt()
        );
    }
}
