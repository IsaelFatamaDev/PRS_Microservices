package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import pe.edu.vallegrande.vgmsorganizations.domain.models.ZoneFareHistory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Puerto de salida para repositorio de historial de tarifas
public interface IZoneFareHistoryRepository {
     Mono<ZoneFareHistory> save(ZoneFareHistory history);

     Flux<ZoneFareHistory> findByZoneId(String zoneId);
}
