package pe.edu.vallegrande.vgmsorganizations.application.services;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.FareRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.FareResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FareService {

    /**
     * Crea una nueva tarifa
     */
    Mono<FareResponse> create(FareRequest request);

    /**
     * Obtiene todas las tarifas
     */
    Flux<FareResponse> findAll();

    /**
     * Obtiene una tarifa por ID
     */
    Mono<FareResponse> findById(String id);

    /**
     * Obtiene tarifas por zona
     */
    Flux<FareResponse> findByZoneId(String zoneId);

    /**
     * Actualiza una tarifa
     */
    Mono<FareResponse> update(String id, FareRequest request);

    /**
     * Elimina (desactiva) una tarifa
     */
    Mono<Void> delete(String id);

    /**
     * Restaura una tarifa eliminada
     */
    Mono<Void> restore(String id);
}
