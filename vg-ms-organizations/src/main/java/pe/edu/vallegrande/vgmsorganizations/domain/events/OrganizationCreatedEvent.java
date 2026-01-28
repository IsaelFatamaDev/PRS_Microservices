package pe.edu.vallegrande.vgmsorganizations.domain.events;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrganizationCreatedEvent implements DomainEvent {
     private final String eventId;
     private final LocalDateTime occurredOn;
     private final Organization organization;

     @Override
     public String getEventType() {
          return "ORGANIZATION_CREATED";
     }

     public static OrganizationCreatedEvent from(Organization organization) {
          return OrganizationCreatedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .organization(organization)
                    .build();
     }
}
