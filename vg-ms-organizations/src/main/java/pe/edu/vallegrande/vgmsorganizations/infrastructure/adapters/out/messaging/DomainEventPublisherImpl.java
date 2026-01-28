package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.events.DomainEvent;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IDomainEventPublisher;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class DomainEventPublisherImpl implements IDomainEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String EXCHANGE = "organization.events";

    @Override
    public Mono<Void> publish(DomainEvent event) {
        return Mono.fromRunnable(() -> {
            try {
                String routingKey = determineRoutingKey(event.getEventType());
                String messageJson = objectMapper.writeValueAsString(event);

                rabbitTemplate.convertAndSend(EXCHANGE, routingKey, messageJson);
                log.info("Evento publicado: {} con routing key: {}", event.getEventType(), routingKey);
            } catch (JsonProcessingException e) {
                log.error("Error serializando evento: {}", e.getMessage());
                throw new RuntimeException("Error publicando evento", e);
            }
        });
    }

    private String determineRoutingKey(String eventType) {
        return switch (eventType) {
            case "OrganizationCreated" -> "organization.created";
            case "OrganizationUpdated" -> "organization.updated";
            case "OrganizationDeleted" -> "organization.deleted";
            case "ZoneCreated" -> "zone.created";
            case "ZoneUpdated" -> "zone.updated";
            case "ZoneFeeChanged" -> "zone.fee.changed";
            case "StreetCreated" -> "street.created";
            default -> "organization.unknown";
        };
    }
}
