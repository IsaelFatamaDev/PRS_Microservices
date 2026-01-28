package pe.edu.vallegrande.vgmsusers.application.usecases;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.IGetUserUseCase;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetUserUseCaseImpl implements IGetUserUseCase {

     private final IUserRepository userRepository;

     @Override
     public Mono<User> findById(UUID userId) {
          return userRepository.findById(userId)
                    .doOnSuccess(u -> log.info("User found: {}", userId))
                    .doOnError(e -> log.error("Error finding user by id: {}", userId, e));
     }

     @Override
     public Mono<User> findByUsername(String username) {
          return userRepository.findByUsername(username)
                    .doOnSuccess(u -> log.info("User found by username: {}", username))
                    .doOnError(e -> log.error("Error finding user by username: {}", username, e));
     }

     @Override
     public Flux<User> findAll() {
          return userRepository.findAll()
                    .doOnComplete(() -> log.info("All users retrieved"))
                    .doOnError(e -> log.error("Error retrieving all users", e));
     }

     @Override
     public Flux<User> findByRole(Role role) {
          return userRepository.findByRole(role)
                    .doOnComplete(() -> log.info("Users retrieved by role: {}", role))
                    .doOnError(e -> log.error("Error retrieving users by role: {}", role, e));
     }

     @Override
     public Flux<User> findByStatus(RecordStatus status) {
          return userRepository.findByStatus(status)
                    .doOnComplete(() -> log.info("Users retrieved by status: {}", status))
                    .doOnError(e -> log.error("Error retrieving users by status: {}", status, e));
     }

     @Override
     public Flux<User> findByOrganization(String organizationId) {
          return userRepository.findByOrganization(organizationId)
                    .doOnComplete(() -> log.info("Users retrieved by organization: {}", organizationId))
                    .doOnError(e -> log.error("Error retrieving users by organization: {}", organizationId, e));
     }
}
