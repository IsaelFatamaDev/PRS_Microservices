package pe.edu.vallegrande.vgmsnotification.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base interface for all domain events
 */
public interface DomainEvent {
     String getEventId();

     LocalDateTime getOccurredOn();

     String getEventType();
}
