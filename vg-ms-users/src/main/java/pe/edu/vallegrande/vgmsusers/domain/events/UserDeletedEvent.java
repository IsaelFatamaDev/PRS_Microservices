package pe.edu.vallegrande.vgmsusers.domain.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserDeletedEvent implements DomainEvent {
     private final UUID eventId;
     private final LocalDateTime occurredOn;
     private final UUID userId;
     private final String username;

     @Override
     public String getEventType() {
          return "USER_DELETED";
     }

     public static UserDeletedEvent from(UUID userId, String username) {
          return UserDeletedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .occurredOn(LocalDateTime.now())
                    .userId(userId)
                    .username(username)
                    .build();
     }
}
