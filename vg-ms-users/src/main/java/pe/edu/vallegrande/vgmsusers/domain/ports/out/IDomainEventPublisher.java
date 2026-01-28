package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import pe.edu.vallegrande.vgmsusers.domain.events.DomainEvent;
import reactor.core.publisher.Mono;

// Puerto de salida para publicar eventos de dominio (hexagonal)
public interface IDomainEventPublisher {
     Mono<Void> publish(DomainEvent event);
}
