package pe.edu.vallegrande.vgmsusers.domain.events;

import lombok.Builder;
import lombok.Getter;
import pe.edu.vallegrande.vgmsusers.domain.models.User;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserUpdatedEvent implements DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final User user;
    private final User previousState;

    @Override
    public String getEventType() {
        return "USER_UPDATED";
    }

    public static UserUpdatedEvent from(User previousState, User currentState) {
        return UserUpdatedEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .previousState(previousState)
                .user(currentState)
                .build();
    }
}
