package pe.edu.vallegrande.vg_ms_claims_incidents.application.services;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentResolutionDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncidentResolutionService {
    Flux<IncidentResolutionDTO> findAll();

    Mono<IncidentResolutionDTO> findById(String id);

    Mono<IncidentResolutionDTO> save(IncidentResolutionDTO incidentResolutionDTO);

    Mono<IncidentResolutionDTO> update(String id, IncidentResolutionDTO incidentResolutionDTO);

    Mono<Void> deleteById(String id);

    Flux<IncidentResolutionDTO> findByIncidentId(String incidentId);
}