package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.UserNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.IDeleteUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IDomainEventPublisher;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeleteUserUseCaseImpl implements IDeleteUserUseCase {

     private final IUserRepository userRepository;
     private final IDomainEventPublisher domainEventPublisher;

     @Override
     public Mono<Void> softDelete(UUID userId) {
          return userRepository.findById(userId)
                    .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                    .flatMap(user -> {
                         user.markAsDeleted(); // Registra evento
                         return userRepository.save(user);
                    })
                    .flatMap(this::publishDomainEvents)
                    .doOnSuccess(v -> log.info("User soft deleted: {}", userId))
                    .doOnError(e -> log.error("Error soft deleting user: {}", userId, e));
     }

     @Override
     public Mono<Void> restore(UUID userId) {
          return userRepository.findById(userId)
                    .switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))
                    .flatMap(user -> {
                         user.markAsRestored(); // Registra evento
                         return userRepository.save(user);
                    })
                    .flatMap(this::publishDomainEvents)
                    .doOnSuccess(v -> log.info("User restored: {}", userId))
                    .doOnError(e -> log.error("Error restoring user: {}", userId, e));
     }

     private Mono<Void> publishDomainEvents(User user) {
          return Flux.fromIterable(user.getDomainEvents())
                    .flatMap(event -> domainEventPublisher.publish(event)
                              .onErrorResume(e -> {
                                   log.warn("Failed to publish domain event: {}", e.getMessage());
                                   return Mono.empty();
                              }))
                    .then(Mono.fromRunnable(user::clearDomainEvents))
                    .then();
     }
}log.warn("Failed to publish user deleted event: {}",e.getMessage());return Mono.empty();});}}
