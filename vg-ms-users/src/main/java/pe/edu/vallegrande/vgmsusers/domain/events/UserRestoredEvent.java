package pe.edu.vallegrande.vgmsusers.domain.events;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class UserRestoredEvent implements DomainEvent {
    private final UUID eventId;
    private final LocalDateTime occurredOn;
    private final UUID userId;
    private final String username;

    @Override
    public String getEventType() {
        return "USER_RESTORED";
    }

    public static UserRestoredEvent from(UUID userId, String username) {
        return UserRestoredEvent.builder()
                .eventId(UUID.randomUUID())
                .occurredOn(LocalDateTime.now())
                .userId(userId)
                .username(username)
                .build();
    }
}
