package pe.edu.vallegrande.vgmsusers.domain.events;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsusers.domain.models.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserCreatedEvent implements DomainEvent {
     private final UUID eventId;
     private final LocalDateTime occurredOn;
     private final User user;

     @Override
     public String getEventType() {
          return "USER_CREATED";
     }

     public static UserCreatedEvent from(User user) {
          return UserCreatedEvent.builder()
                    .eventId(UUID.randomUUID())
                    .occurredOn(LocalDateTime.now())
                    .user(user)
                    .build();
     }
}
