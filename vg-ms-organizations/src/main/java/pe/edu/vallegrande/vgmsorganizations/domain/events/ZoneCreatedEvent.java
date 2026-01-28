package pe.edu.vallegrande.vgmsorganizations.domain.events;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ZoneCreatedEvent implements DomainEvent {
     private final String eventId;
     private final LocalDateTime occurredOn;
     private final Zone zone;

     @Override
     public String getEventType() {
          return "ZONE_CREATED";
     }

     public static ZoneCreatedEvent from(Zone zone) {
          return ZoneCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .zone(zone)
                    .build();
     }
}
