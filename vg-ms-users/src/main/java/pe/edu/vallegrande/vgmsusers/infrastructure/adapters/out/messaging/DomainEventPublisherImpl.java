package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.domain.events.*;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IDomainEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class DomainEventPublisherImpl implements IDomainEventPublisher {

     private final RabbitTemplate rabbitTemplate;
     private final ObjectMapper objectMapper;

     @Override
     public Mono<Void> publish(DomainEvent event) {
          return Mono.fromRunnable(() -> {
               try {
                    String routingKey = getRoutingKey(event);
                    String message = objectMapper.writeValueAsString(event);

                    rabbitTemplate.convertAndSend(
                              "users.events.exchange",
                              routingKey,
                              message);

                    log.info("Domain event published: {} - EventId: {}",
                              event.getEventType(), event.getEventId());
               } catch (Exception e) {
                    log.error("Error publishing domain event: {}", event.getEventType(), e);
                    throw new RuntimeException("Failed to publish domain event", e);
               }
          })
                    .subscribeOn(Schedulers.boundedElastic())
                    .then();
     }

     private String getRoutingKey(DomainEvent event) {
          return switch (event) {
               case UserCreatedEvent e -> "user.created";
               case UserUpdatedEvent e -> "user.updated";
               case UserDeletedEvent e -> "user.deleted";
               case UserRestoredEvent e -> "user.restored";
               default -> "user.unknown";
          };
     }
}
