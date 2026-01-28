package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Puerto de entrada para consultar calles
public interface IGetStreetUseCase {
     Mono<Street> findById(String id);

     Flux<Street> findAll();

     Flux<Street> findByZoneId(String zoneId);
}
