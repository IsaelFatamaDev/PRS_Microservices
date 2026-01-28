package pe.edu.vallegrande.vgmsnotification.domain.events;

import lombok.*;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateCreatedEvent implements DomainEvent {
     private String eventId;
     private LocalDateTime occurredOn;
     private String templateId;
     private String code;
     private String channel;

     @Override
     public String getEventType() {
          return "template.created";
     }

     public static TemplateCreatedEvent from(NotificationTemplate template) {
          return TemplateCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .templateId(template.getId())
                    .code(template.getCode())
                    .channel(template.getChannel().name())
                    .build();
     }
}
