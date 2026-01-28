package pe.edu.vallegrande.vgmsnotification.domain.events;

import lombok.*;
import pe.edu.vallegrande.vgmsnotification.domain.models.Notification;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationReadEvent implements DomainEvent {
     private String eventId;
     private LocalDateTime occurredOn;
     private String notificationId;
     private String userId;

     @Override
     public String getEventType() {
          return "notification.read";
     }

     public static NotificationReadEvent from(Notification notification) {
          return NotificationReadEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .notificationId(notification.getId())
                    .userId(notification.getUserId())
                    .build();
     }
}
