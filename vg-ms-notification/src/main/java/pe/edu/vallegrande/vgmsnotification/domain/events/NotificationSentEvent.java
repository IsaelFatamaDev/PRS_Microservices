package pe.edu.vallegrande.vgmsnotification.domain.events;

import lombok.*;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSentEvent implements DomainEvent {
     private String eventId;
     private LocalDateTime occurredOn;
     private String notificationId;
     private String userId;
     private String channel;
     private String providerName;

     @Override
     public String getEventType() {
          return "notification.sent";
     }

     public static NotificationSentEvent from(Notification notification) {
          return NotificationSentEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .notificationId(notification.getId())
                    .userId(notification.getUserId())
                    .channel(notification.getChannel().name())
                    .providerName(notification.getProviderName())
                    .build();
     }
}
