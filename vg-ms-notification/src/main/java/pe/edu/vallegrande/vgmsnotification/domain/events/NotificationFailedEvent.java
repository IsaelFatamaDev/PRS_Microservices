package pe.edu.vallegrande.vgmsnotification.domain.events;

import lombok.*;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationFailedEvent implements DomainEvent {
     private String eventId;
     private LocalDateTime occurredOn;
     private String notificationId;
     private String userId;
     private String channel;
     private String errorMessage;
     private Integer retryCount;

     @Override
     public String getEventType() {
          return "notification.failed";
     }

     public static NotificationFailedEvent from(Notification notification, String errorMessage) {
          return NotificationFailedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .notificationId(notification.getId())
                    .userId(notification.getUserId())
                    .channel(notification.getChannel().name())
                    .errorMessage(errorMessage)
                    .retryCount(notification.getRetryCount())
                    .build();
     }
}
