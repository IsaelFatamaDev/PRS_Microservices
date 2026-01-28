package pe.edu.vallegrande.vgmsnotification.domain.events;

import lombok.*;
import pe.edu.vallegrande.vgmsnotification.domain.models.NotificationTemplate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateUpdatedEvent implements DomainEvent {
     private String eventId;
     private LocalDateTime occurredOn;
     private String templateId;
     private String code;
     private String updatedBy;

     @Override
     public String getEventType() {
          return "template.updated";
     }

     public static TemplateUpdatedEvent from(NotificationTemplate template) {
          return TemplateUpdatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .templateId(template.getId())
                    .code(template.getCode())
                    .updatedBy(template.getUpdatedBy())
                    .build();
     }
}
