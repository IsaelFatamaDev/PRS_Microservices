package pe.edu.vallegrande.vg_ms_claims_incidents.application.services;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.ComplaintDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ComplaintService {
    Flux<ComplaintDTO> findAll();

    Mono<ComplaintDTO> findById(String id);

    Mono<ComplaintDTO> save(ComplaintDTO complaintDTO);

    Mono<ComplaintDTO> update(String id, ComplaintDTO complaintDTO);

    Mono<Void> deleteById(String id);

    Mono<ComplaintDTO> restoreById(String id);
}