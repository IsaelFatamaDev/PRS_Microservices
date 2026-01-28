package pe.edu.vallegrande.vg_ms_claims_incidents.application.services;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.ComplaintResponseDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ComplaintResponseService {
    Flux<ComplaintResponseDTO> findAll();

    Mono<ComplaintResponseDTO> findById(String id);

    Mono<ComplaintResponseDTO> save(ComplaintResponseDTO complaintResponseDTO);

    Mono<ComplaintResponseDTO> update(String id, ComplaintResponseDTO complaintResponseDTO);

    Mono<Void> deleteById(String id);
}