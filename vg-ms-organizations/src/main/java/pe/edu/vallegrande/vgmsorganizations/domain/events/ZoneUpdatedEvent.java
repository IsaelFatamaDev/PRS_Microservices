package pe.edu.vallegrande.vgmsorganizations.domain.events;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ZoneUpdatedEvent implements DomainEvent {
     private final String eventId;
     private final LocalDateTime occurredOn;
     private final Zone previousState;
     private final Zone currentState;

     @Override
     public String getEventType() {
          return "ZONE_UPDATED";
     }

     public static ZoneUpdatedEvent from(Zone previousState, Zone currentState) {
          return ZoneUpdatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .previousState(previousState)
                    .currentState(currentState)
                    .build();
     }
}
