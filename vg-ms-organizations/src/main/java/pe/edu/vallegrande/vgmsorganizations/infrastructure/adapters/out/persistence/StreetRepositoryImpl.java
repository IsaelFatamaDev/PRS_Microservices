package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.StreetNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.mappers.StreetDomainMapper;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.StreetMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class StreetRepositoryImpl implements IStreetRepository {

     private final StreetMongoRepository mongoRepository;

     @Override
     public Mono<Street> save(Street street) {
          return Mono.just(street)
                    .map(StreetDomainMapper::toDocument)
                    .flatMap(mongoRepository::save)
                    .map(StreetDomainMapper::toDomain);
     }

     @Override
     public Mono<Street> findById(String id) {
          return mongoRepository.findById(id)
                    .map(StreetDomainMapper::toDomain)
                    .switchIfEmpty(Mono.error(new StreetNotFoundException("Calle no encontrada: " + id)));
     }

     @Override
     public Flux<Street> findAll() {
          return mongoRepository.findAll()
                    .map(StreetDomainMapper::toDomain);
     }

     @Override
     public Flux<Street> findByZoneId(String zoneId) {
          return mongoRepository.findByZoneId(zoneId)
                    .map(StreetDomainMapper::toDomain);
     }

     @Override
     public Mono<Void> deleteById(String id) {
          return mongoRepository.deleteById(id);
     }
}
