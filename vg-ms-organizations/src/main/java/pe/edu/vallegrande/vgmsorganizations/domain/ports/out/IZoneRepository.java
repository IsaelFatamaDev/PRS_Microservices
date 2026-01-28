package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Puerto de salida para repositorio de zonas
public interface IZoneRepository {
     Mono<Zone> save(Zone zone);

     Mono<Zone> findById(String id);

     Flux<Zone> findAll();

     Flux<Zone> findByOrganizationId(String organizationId);

     Flux<Zone> findByStatus(RecordStatus status);

     Mono<Boolean> existsByZoneCode(String zoneCode);

     Mono<Void> deleteById(String id);
}
