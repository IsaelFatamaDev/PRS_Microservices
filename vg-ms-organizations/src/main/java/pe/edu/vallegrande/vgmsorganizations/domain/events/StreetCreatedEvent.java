package pe.edu.vallegrande.vgmsorganizations.domain.events;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class StreetCreatedEvent implements DomainEvent {
     private final String eventId;
     private final LocalDateTime occurredOn;
     private final Street street;

     @Override
     public String getEventType() {
          return "STREET_CREATED";
     }

     public static StreetCreatedEvent from(Street street) {
          return StreetCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .street(street)
                    .build();
     }
}
