package pe.edu.vallegrande.vgmsusers.application.events.publishers;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.application.events.UserCreatedEvent;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisherImpl implements IUserEventPublisher {

     private final RabbitTemplate rabbitTemplate;
     private final ObjectMapper objectMapper;

     @Value("${rabbitmq.exchange.users}")
     private String exchange;

     @Value("${rabbitmq.routing-key.user-created}")
     private String userCreatedRoutingKey;

     @Value("${rabbitmq.routing-key.user-updated}")
     private String userUpdatedRoutingKey;

     @Value("${rabbitmq.routing-key.user-deleted}")
     private String userDeletedRoutingKey;

     @Override
     public Mono<Void> publishUserCreated(User user) {
          return Mono.fromRunnable(() -> {
               try {
                    UserCreatedEvent event = mapToUserCreatedEvent(user);
                    String message = objectMapper.writeValueAsString(event);
                    rabbitTemplate.convertAndSend(exchange, userCreatedRoutingKey, message);
                    log.info("Published UserCreatedEvent for user: {}", user.getUserId());
               } catch (Exception e) {
                    log.error("Error publishing UserCreatedEvent", e);
                    throw new RuntimeException("Failed to publish event", e);
               }
          }).subscribeOn(Schedulers.boundedElastic()).then();
     }

     @Override
     public Mono<Void> publishUserUpdated(User user) {
          return Mono.fromRunnable(() -> {
               try {
                    UserCreatedEvent event = mapToUserCreatedEvent(user);
                    String message = objectMapper.writeValueAsString(event);
                    rabbitTemplate.convertAndSend(exchange, userUpdatedRoutingKey, message);
                    log.info("Published UserUpdatedEvent for user: {}", user.getUserId());
               } catch (Exception e) {
                    log.error("Error publishing UserUpdatedEvent", e);
                    throw new RuntimeException("Failed to publish event", e);
               }
          }).subscribeOn(Schedulers.boundedElastic()).then();
     }

     @Override
     public Mono<Void> publishUserDeleted(User user) {
          return Mono.fromRunnable(() -> {
               try {
                    UserCreatedEvent event = mapToUserCreatedEvent(user);
                    String message = objectMapper.writeValueAsString(event);
                    rabbitTemplate.convertAndSend(exchange, userDeletedRoutingKey, message);
                    log.info("Published UserDeletedEvent for user: {}", user.getUserId());
               } catch (Exception e) {
                    log.error("Error publishing UserDeletedEvent", e);
                    throw new RuntimeException("Failed to publish event", e);
               }
          }).subscribeOn(Schedulers.boundedElastic()).then();
     }

     private UserCreatedEvent mapToUserCreatedEvent(User user) {
          return UserCreatedEvent.builder()
                    .userId(user.getUserId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .phone(user.getPhone())
                    .role(user.getRole().name())
                    .organizationId(user.getOrganizationId())
                    .timestamp(LocalDateTime.now())
                    .build();
     }
}
