package pe.edu.vallegrande.vg_ms_claims_incidents.application.services;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.ComplaintCategoryDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ComplaintCategoryService {
    Flux<ComplaintCategoryDTO> findAll();

    Mono<ComplaintCategoryDTO> findById(String id);

    Mono<ComplaintCategoryDTO> save(ComplaintCategoryDTO complaintCategoryDTO);

    Mono<ComplaintCategoryDTO> update(String id, ComplaintCategoryDTO complaintCategoryDTO);

    Mono<Void> deleteById(String id);

    Mono<ComplaintCategoryDTO> restoreById(String id);
}