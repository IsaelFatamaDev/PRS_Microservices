package pe.edu.vallegrande.ms_infraestructura.application.services;

import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IWaterBoxService {
    Flux<WaterBoxResponse> getAllActive();
    Flux<WaterBoxResponse> getAllInactive();
    Mono<WaterBoxResponse> getById(Long id);
    Mono<WaterBoxResponse> save(WaterBoxRequest request);
    Mono<WaterBoxResponse> update(Long id, WaterBoxRequest request);
    Mono<Void> delete(Long id); // Soft delete
    Mono<WaterBoxResponse> restore(Long id); // Restore soft deleted
}