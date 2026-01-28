package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.events.DomainEvent;
import reactor.core.publisher.Mono;

// Puerto de salida para publicar eventos de dominio
public interface IDomainEventPublisher {
     Mono<Void> publish(DomainEvent event);
}
