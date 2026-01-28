package pe.edu.vallegrande.vgmsorganizations.domain.events;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrganizationUpdatedEvent implements DomainEvent {
     private final String eventId;
     private final LocalDateTime occurredOn;
     private final Organization previousState;
     private final Organization currentState;

     @Override
     public String getEventType() {
          return "ORGANIZATION_UPDATED";
     }

     public static OrganizationUpdatedEvent from(Organization previousState, Organization currentState) {
          return OrganizationUpdatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .previousState(previousState)
                    .currentState(currentState)
                    .build();
     }
}
