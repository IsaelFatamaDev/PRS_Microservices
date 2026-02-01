package pe.edu.vallegrande.vgmsorganizations.application.services;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.ParameterRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ParameterResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ParameterService {
    Mono<ParameterResponse> create(ParameterRequest request);
    Flux<ParameterResponse> findAll();
    Mono<ParameterResponse> findById(String id);
    Mono<ParameterResponse> update(String id, ParameterRequest request);
    Mono<Void> delete(String id);
    Mono<Void> restore(String id);
    Flux<ParameterResponse> findByOrganizationId(String organizationId);
}