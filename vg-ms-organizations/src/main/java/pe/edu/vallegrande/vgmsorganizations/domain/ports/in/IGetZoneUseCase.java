package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Puerto de entrada para consultar zonas
public interface IGetZoneUseCase {
     Mono<Zone> findById(String id);

     Flux<Zone> findAll();

     Flux<Zone> findByOrganizationId(String organizationId);
}
