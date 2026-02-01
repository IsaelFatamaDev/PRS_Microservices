package pe.edu.vallegrande.vgmsorganizations.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.domain.models.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    Mono<User> findByEmail(String email);
    Flux<User> findAllByStatus(String status);
    Mono<Boolean> existsByEmail(String email);
    Flux<User> findByOrganizationId(String organizationId);
}
