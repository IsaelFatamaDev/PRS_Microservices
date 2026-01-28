package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.UserNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.IUpdateUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateUserUseCaseImpl implements IUpdateUserUseCase {

     private final IUserRepository userRepository;
     private final IDomainEventPublisher domainEventPublisher;

     @Override
     public Mono<User> execute(UUID userId, User updatedUser) {
          return userRepository.findById(userId)
                    .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                    .flatMap(existing -> {
                         existing.updateWith(updatedUser); // Registra evento
                         return userRepository.save(existing);
                    })
                    .flatMap(this::publishDomainEvents)
                    .doOnSuccess(u -> log.info("User updated successfully: {}", userId))
                    .doOnError(e -> log.error("Error updating user: {}", userId, e));
     }

     private Mono<User> publishDomainEvents(User user) {
          return Flux.fromIterable(user.getDomainEvents())
                    .flatMap(event -> domainEventPublisher.publish(event)
                              .onErrorResume(e -> {
                                   log.warn("Failed to publish domain event: {}", e.getMessage());
                                   return Mono.empty();
                              }))
                    .then(Mono.fromRunnable(user::clearDomainEvents))
                    .thenReturn(user);
     }
}
