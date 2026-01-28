package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

// Puerto de entrada para crear y gestionar zonas
public interface ICreateZoneUseCase {
     Mono<Zone> execute(Zone zone);

     Mono<Zone> updateZoneFee(String zoneId, BigDecimal newFee, String changedBy, String reason);
}
