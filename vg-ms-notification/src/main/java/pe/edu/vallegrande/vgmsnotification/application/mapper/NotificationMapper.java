package pe.edu.vallegrande.vgmsnotification.application.mapper;

import pe.edu.vallegrande.vgmsnotification.application.dto.notification.NotificationResponse;
import pe.edu.vallegrande.vgmsnotification.application.dto.notification.SendNotificationRequest;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import pe.edu.vallegrande.vgmsnotification.domain.models.valueobjects.*;

import java.time.LocalDateTime;
import java.util.UUID;

public class NotificationMapper {
    
    public static Notification toDomain(SendNotificationRequest request) {
        return Notification.builder()
                .id(UUID.randomUUID().toString())
                .userId(request.getUserId())
                .channel(NotificationChannel.valueOf(request.getChannel()))
                .recipient(request.getRecipient())
                .type(NotificationType.valueOf(request.getType()))
                .subject(request.getSubject())
                .message(request.getMessage())
                .status(NotificationStatus.PENDING)
                .priority(request.getPriority() != null ? 
                        NotificationPriority.valueOf(request.getPriority()) : NotificationPriority.NORMAL)
                .templateId(request.getTemplateCode())
                .templateParams(request.getTemplateParams())
                .retryCount(0)
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .build();
    }
    
    public static NotificationResponse toResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .channel(notification.getChannel().name())
                .recipient(notification.getRecipient())
                .type(notification.getType().name())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .status(notification.getStatus().name())
                .priority(notification.getPriority() != null ? notification.getPriority().name() : null)
                .templateId(notification.getTemplateId())
                .templateParams(notification.getTemplateParams())
                .providerName(notification.getProviderName())
                .providerId(notification.getProviderId())
                .errorMessage(notification.getErrorMessage())
                .retryCount(notification.getRetryCount())
                .scheduledAt(notification.getScheduledAt())
                .sentAt(notification.getSentAt())
                .deliveredAt(notification.getDeliveredAt())
                .readAt(notification.getReadAt())
                .createdAt(notification.getCreatedAt())
                .createdBy(notification.getCreatedBy())
                .build();
    }
}
