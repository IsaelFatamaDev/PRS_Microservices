package pe.edu.vallegrande.vgmsorganizations.application.services;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.ZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ZoneResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ZoneService {
    Mono<ZoneResponse> create(ZoneRequest request);
    Flux<ZoneResponse> findAll();
    Mono<ZoneResponse> findById(String id);
    Mono<ZoneResponse> update(String id, ZoneRequest request);
    Mono<Void> delete(String id);
    Mono<Void> restore(String id);
    Flux<ZoneResponse> findByOrganizationId(String organizationId);
    Mono<ZoneResponse> findByZoneIdAndOrganizationId(String zoneId, String organizationId);
}