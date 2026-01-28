package pe.edu.vallegrande.vg_ms_claims_incidents.application.services;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentTypeDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncidentTypeService {
    Flux<IncidentTypeDTO> findAll();

    Mono<IncidentTypeDTO> findById(String id);

    Mono<IncidentTypeDTO> save(IncidentTypeDTO incidentTypeDTO);

    Mono<IncidentTypeDTO> update(String id, IncidentTypeDTO incidentTypeDTO);

    Mono<Void> deleteById(String id);

    Mono<IncidentTypeDTO> restoreById(String id);
}