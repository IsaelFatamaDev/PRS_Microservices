package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.domain.events.DomainEvent;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.ICreateUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.*;
import pe.edu.vallegrande.vgmsusers.domain.validators.OrganizationValidator;
import pe.edu.vallegrande.vgmsusers.domain.validators.UserUniquenessValidator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

// SRP: Responsabilidad única - Coordinar la creación de usuarios
// DIP: Depende de abstracciones (interfaces y validadores), no de implementaciones concretas
@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements ICreateUserUseCase {

     private final IUserRepository userRepository;
     private final IAuthenticationClient authenticationClient;
     private final IDomainEventPublisher domainEventPublisher;
     private final UserUniquenessValidator uniquenessValidator;
     private final OrganizationValidator organizationValidator;

     @Override
     public Mono<User> execute(User user, String password) {
          user.setUserId(UUID.randomUUID());
          User newUser = User.createNew(user); // Registra evento

          return uniquenessValidator.validateUserDoesNotExist(newUser)
                    .then(organizationValidator.validateUserOrganization(newUser))
                    .then(userRepository.save(newUser))
                    .flatMap(savedUser -> createAuthCredentials(savedUser, password)
                              .thenReturn(savedUser))
                    .flatMap(this::publishDomainEvents)
                    .doOnSuccess(u -> log.info("User created successfully: {}", u.getUserId()))
                    .doOnError(e -> log.error("Error creating user", e));
     }

     // Crear credenciales de autenticación
     private Mono<Void> createAuthCredentials(User user, String password) {
          return authenticationClient.createCredentials(
                    user.getUserId(),
                    user.getUsername(),
                    password,
                    user.getRole().name());
     }

     // Publicar todos los eventos de dominio registrados
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
