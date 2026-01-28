package pe.edu.vallegrande.vgmsorganizations.domain.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class OrganizationDeletedEvent implements DomainEvent {
     private final String eventId;
     private final LocalDateTime occurredOn;
     private final String organizationId;
     private final String organizationName;

     @Override
     public String getEventType() {
          return "ORGANIZATION_DELETED";
     }

     public static OrganizationDeletedEvent from(String organizationId, String organizationName) {
          return OrganizationDeletedEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .occurredOn(LocalDateTime.now())
                    .organizationId(organizationId)
                    .organizationName(organizationName)
                    .build();
     }
}
