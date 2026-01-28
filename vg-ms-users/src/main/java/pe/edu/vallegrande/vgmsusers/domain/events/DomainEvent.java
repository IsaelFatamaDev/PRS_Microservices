package pe.edu.vallegrande.vgmsusers.domain.events;

import java.time.LocalDateTime;
import java.util.UUID;

// Evento base del dominio (parte del núcleo hexagonal)
public interface DomainEvent {
     UUID getEventId();

     LocalDateTime getOccurredOn();

     String getEventType();
}
