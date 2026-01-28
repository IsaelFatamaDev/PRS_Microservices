package pe.edu.vallegrande.ms_infraestructura.application.services;

import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxAssignmentRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IWaterBoxAssignmentService {

    Flux<WaterBoxAssignmentResponse> getAllActive();

    Flux<WaterBoxAssignmentResponse> getAllInactive();

    Mono<WaterBoxAssignmentResponse> getById(Long id);

    Mono<WaterBoxAssignmentResponse> save(WaterBoxAssignmentRequest request);

    Mono<WaterBoxAssignmentResponse> update(Long id, WaterBoxAssignmentRequest request);

    Mono<Void> delete(Long id); // Soft delete

    Mono<WaterBoxAssignmentResponse> restore(Long id); // Restore soft deleted

    // Método para obtener la asignación activa de un usuario
    Mono<WaterBoxAssignmentResponse> getActiveAssignmentByUserId(String userId);
}
