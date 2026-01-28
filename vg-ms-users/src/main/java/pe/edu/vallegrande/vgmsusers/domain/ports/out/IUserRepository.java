package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IUserRepository {
     Mono<User> save(User user);

     Mono<User> findById(UUID userId);

     Mono<User> findByUsername(String username);

     Mono<User> findByDocumentNumber(String documentNumber);

     Flux<User> findAll();

     Flux<User> findByRole(Role role);

     Flux<User> findByStatus(RecordStatus status);

     Flux<User> findByOrganization(String organizationId);

     Mono<Boolean> existsByUsername(String username);

     Mono<Boolean> existsByDocumentNumber(String documentNumber);

     Mono<Void> deleteById(UUID userId);
}
