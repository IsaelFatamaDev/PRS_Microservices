package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IGetUserUseCase {
     Mono<User> findById(UUID userId);

     Mono<User> findByUsername(String username);

     Flux<User> findAll();

     Flux<User> findByRole(Role role);

     Flux<User> findByStatus(RecordStatus status);

     Flux<User> findByOrganization(String organizationId);
}
