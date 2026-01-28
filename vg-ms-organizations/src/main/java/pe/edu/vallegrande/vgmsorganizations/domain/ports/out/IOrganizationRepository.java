package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Puerto de salida para repositorio de organizaciones
public interface IOrganizationRepository {
     Mono<Organization> save(Organization organization);

     Mono<Organization> findById(String id);

     Mono<Organization> findByRuc(String ruc);

     Flux<Organization> findAll();

     Flux<Organization> findByStatus(RecordStatus status);

     Flux<Organization> findByRegion(String region);

     Mono<Boolean> existsByRuc(String ruc);

     Mono<Void> deleteById(String id);
}
