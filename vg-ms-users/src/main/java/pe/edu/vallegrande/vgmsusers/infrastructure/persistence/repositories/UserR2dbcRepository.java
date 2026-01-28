package pe.edu.vallegrande.vgmsusers.infrastructure.persistence.repositories;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;
import pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserR2dbcRepository extends R2dbcRepository<UserEntity, UUID> {

     Mono<UserEntity> findByUsername(String username);

     Mono<UserEntity> findByDocumentNumber(String documentNumber);

     Flux<UserEntity> findByRole(Role role);

     Flux<UserEntity> findByStatus(RecordStatus status);

     @Query("SELECT * FROM users WHERE organization_id = :organizationId")
     Flux<UserEntity> findByOrganization(String organizationId);

     Mono<Boolean> existsByUsername(String username);

     Mono<Boolean> existsByDocumentNumber(String documentNumber);
}
