package pe.edu.vallegrande.vgmsnotification.domain.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreatedEvent implements DomainEvent {
     private String eventId;
     private LocalDateTime occurredOn;
     private String notificationId;
     private String userId;
     private String channel;
     private String type;
     private String recipient;

     @Override
     public String getEventType() {
          return "notification.created";
     }

     public static NotificationCreatedEvent from(Notification notification) {
          return NotificationCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .notificationId(notification.getId())
                    .userId(notification.getUserId())
                    .channel(notification.getChannel().name())
                    .type(notification.getType().name())
                    .recipient(notification.getRecipient())
                    .build();
     }
}
