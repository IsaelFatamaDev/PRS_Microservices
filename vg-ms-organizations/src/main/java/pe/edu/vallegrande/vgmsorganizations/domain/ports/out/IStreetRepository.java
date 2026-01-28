package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Puerto de salida para repositorio de calles
public interface IStreetRepository {
     Mono<Street> save(Street street);

     Mono<Street> findById(String id);

     Flux<Street> findAll();

     Flux<Street> findByZoneId(String zoneId);

     Mono<Void> deleteById(String id);
}
