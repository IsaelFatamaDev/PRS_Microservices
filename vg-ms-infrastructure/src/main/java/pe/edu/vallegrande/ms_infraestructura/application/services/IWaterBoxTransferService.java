package pe.edu.vallegrande.ms_infraestructura.application.services;

import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxTransferRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxTransferResponse;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IWaterBoxTransferService {
    Flux<WaterBoxTransferResponse> getAll();
    Mono<WaterBoxTransferResponse> getById(Long id);
    Mono<WaterBoxTransferResponse> save(WaterBoxTransferRequest request);
}