package pe.edu.vallegrande.vgmsorganizations.domain.events;

import java.time.LocalDateTime;

// Evento base del dominio
public interface DomainEvent {
     String getEventId();

     LocalDateTime getOccurredOn();

     String getEventType();
}
