package pe.edu.vallegrande.vgmsusers.infrastructure.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsusers.domain.enums.RolesUsers;
import pe.edu.vallegrande.vgmsusers.domain.enums.UserStatus;
import pe.edu.vallegrande.vgmsusers.infrastructure.document.UserDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface UserRepository extends ReactiveMongoRepository<UserDocument, String> {

     // Búsquedas básicas
     @Query("{'userCode': ?0, 'deletedAt': null}")
     Mono<UserDocument> findByUserCodeAndDeletedAtIsNull(String userCode);

     @Query("{'_id': ?0, 'deletedAt': null}")
     Mono<UserDocument> findByIdAndDeletedAtIsNull(String id);

     // NUEVO: Búsqueda por username para MS-AUTHENTICATION
     @Query("{'username': ?0, 'deletedAt': null}")
     Mono<UserDocument> findByUsernameAndDeletedAtIsNull(String username);

     // Búsquedas por organización
     @Query("{'organizationId': ?0, 'deletedAt': null}")
     Flux<UserDocument> findByOrganizationIdAndDeletedAtIsNull(String organizationId, Pageable pageable);

     @Query("{'organizationId': ?0, 'deletedAt': null}")
     Flux<UserDocument> findByOrganizationIdAndDeletedAtIsNull(String organizationId);

     // NUEVO: Obtener TODOS los usuarios de una organización (activos e inactivos)
     @Query("{'organizationId': ?0}")
     Flux<UserDocument> findByOrganizationId(String organizationId);

     @Query(value = "{'organizationId': ?0, 'deletedAt': null}", count = true)
     Mono<Long> countByOrganizationIdAndDeletedAtIsNull(String organizationId);

     // Búsquedas por organización y estado
     @Query("{'organizationId': ?0, 'status': ?1, 'deletedAt': null}")
     Flux<UserDocument> findByOrganizationIdAndStatusAndDeletedAtIsNull(String organizationId, UserStatus status);

     @Query("{'organizationId': ?0, 'status': ?1}")
     Flux<UserDocument> findByOrganizationIdAndStatus(String organizationId, UserStatus status);

     // Búsquedas por rol (usando $in para buscar dentro del Set de roles)
     @Query("{'organizationId': ?0, 'roles': {'$in': [?1]}, 'deletedAt': null}")
     Flux<UserDocument> findByOrganizationIdAndRoleAndDeletedAtIsNull(String organizationId, RolesUsers role);

     // NUEVO: Búsqueda por rol global (sin filtrar por organización)
     @Query("{'roles': {'$in': [?0]}, 'deletedAt': null}")
     Flux<UserDocument> findByRoleAndDeletedAtIsNull(RolesUsers role);

     // Validaciones de existencia
     @Query(value = "{'personalInfo.documentNumber': ?0, 'deletedAt': null}", exists = true)
     Mono<Boolean> existsByPersonalInfoDocumentNumberAndDeletedAtIsNull(String documentNumber);

     @Query(value = "{'contact.email': ?0, 'deletedAt': null}", exists = true)
     Mono<Boolean> existsByContactEmailAndDeletedAtIsNull(String email);

     @Query(value = "{'contact.phone': ?0, 'deletedAt': null}", exists = true)
     Mono<Boolean> existsByContactPhoneAndDeletedAtIsNull(String phone);

     // Búsquedas adicionales
     @Query("{'contact.email': ?0, 'deletedAt': null}")
     Mono<UserDocument> findByContactEmailAndDeletedAtIsNull(String email);

     @Query("{'contact.phone': ?0, 'deletedAt': null}")
     Mono<UserDocument> findByContactPhoneAndDeletedAtIsNull(String phone);

     @Query("{'personalInfo.documentNumber': ?0, 'deletedAt': null}")
     Mono<UserDocument> findByPersonalInfoDocumentNumberAndDeletedAtIsNull(String documentNumber);

     // NUEVO: Contar usuarios SUPER_ADMIN
     @Query(value = "{'roles': {'$in': [?0]}, 'deletedAt': null}", count = true)
     Mono<Long> countByRolesContainingAndDeletedAtIsNull(RolesUsers role);
}