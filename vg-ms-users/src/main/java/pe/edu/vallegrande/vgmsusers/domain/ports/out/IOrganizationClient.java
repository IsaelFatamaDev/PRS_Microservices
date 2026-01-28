package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import reactor.core.publisher.Mono;

public interface IOrganizationClient {
     Mono<Boolean> validateOrganizationExists(String organizationId);

     Mono<Boolean> validateZoneExists(String zoneId);

     Mono<Boolean> validateStreetExists(String streetId);
}
