package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Puerto de entrada para consultar organizaciones
public interface IGetOrganizationUseCase {
     Mono<Organization> findById(String id);

     Mono<Organization> findByRuc(String ruc);

     Flux<Organization> findAll();

     Flux<Organization> findByStatus(RecordStatus status);

     Flux<Organization> findByRegion(String region);
}
