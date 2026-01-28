package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import pe.edu.vallegrande.vgmsusers.infrastructure.persistence.mappers.UserDomainMapper;
import pe.edu.vallegrande.vgmsusers.infrastructure.persistence.repositories.UserR2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

// SRP: Responsabilidad única - Adaptador de persistencia para usuarios
// DIP: Implementa interfaz del dominio, depende de abstracción
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements IUserRepository {

     private final UserR2dbcRepository r2dbcRepository;
     private final UserDomainMapper domainMapper;

     @Override
     public Mono<User> save(User user) {
          return r2dbcRepository.save(domainMapper.toEntity(user))
                    .map(domainMapper::toDomain);
     }

     @Override
     public Mono<User> findById(UUID userId) {
          return r2dbcRepository.findById(userId)
                    .map(domainMapper::toDomain);
     }

     @Override
     public Mono<User> findByUsername(String username) {
          return r2dbcRepository.findByUsername(username)
                    .map(domainMapper::toDomain);
     }

     @Override
     public Mono<User> findByDocumentNumber(String documentNumber) {
          return r2dbcRepository.findByDocumentNumber(documentNumber)
                    .map(domainMapper::toDomain);
     }

     @Override
     public Flux<User> findAll() {
          return r2dbcRepository.findAll()
                    .map(domainMapper::toDomain);
     }

     @Override
     public Flux<User> findByRole(Role role) {
          return r2dbcRepository.findByRole(role)
                    .map(domainMapper::toDomain);
     }

     @Override
     public Flux<User> findByStatus(RecordStatus status) {
          return r2dbcRepository.findByStatus(status)
                    .map(domainMapper::toDomain);
     }

     @Override
     public Flux<User> findByOrganization(String organizationId) {
          return r2dbcRepository.findByOrganization(organizationId)
                    .map(domainMapper::toDomain);
     }

     @Override
     public Mono<Boolean> existsByUsername(String username) {
          return r2dbcRepository.existsByUsername(username);
     }

     @Override
     public Mono<Boolean> existsByDocumentNumber(String documentNumber) {
          return r2dbcRepository.existsByDocumentNumber(documentNumber);
     }

     @Override
     public Mono<Void> deleteById(UUID userId) {
          return r2dbcRepository.deleteById(userId);
     }
}
