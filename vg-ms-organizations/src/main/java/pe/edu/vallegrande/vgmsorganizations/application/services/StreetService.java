package pe.edu.vallegrande.vgmsorganizations.application.services;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.StreetRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.StreetResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface StreetService {
    Mono<StreetResponse> create(StreetRequest request);
    Flux<StreetResponse> findAll();
    Mono<StreetResponse> findById(String id);
    Mono<StreetResponse> update(String id, StreetRequest request);
    Mono<Void> delete(String id);
    Mono<Void> restore(String id);
    Flux<StreetResponse> findByZoneId(String zoneId);
    Flux<StreetResponse> findByZoneIdIn(List<String> zoneIds);
}