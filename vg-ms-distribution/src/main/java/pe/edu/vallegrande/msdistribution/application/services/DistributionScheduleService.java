package pe.edu.vallegrande.msdistribution.application.services;

import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionScheduleResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interfaz de servicio para la gestión de Horarios de Distribución.
 * Define la lógica para administrar los cronogramas/horarios de los puntos de
 * distribución.
 * 
 * @version 1.0
 */
public interface DistributionScheduleService {

    /**
     * Obtiene todos los horarios.
     * 
     * @return Flux con todos los horarios.
     */
    Flux<DistributionSchedule> getAll();

    /**
     * Obtiene todos los horarios activos.
     * 
     * @return Flux con horarios activos.
     */
    Flux<DistributionSchedule> getAllActive();

    /**
     * Obtiene todos los horarios inactivos.
     * 
     * @return Flux con horarios inactivos.
     */
    Flux<DistributionSchedule> getAllInactive();

    /**
     * Obtiene un horario por su identificador.
     * 
     * @param id ID del horario.
     * @return Mono con el horario encontrado.
     */
    Mono<DistributionSchedule> getById(String id);

    /**
     * Registra un nuevo horario de distribución.
     * 
     * @param request DTO con los datos del horario.
     * @return Mono con la respuesta del horario creado.
     */
    Mono<DistributionScheduleResponse> save(DistributionScheduleCreateRequest request);

    /**
     * Actualiza un horario específico (objeto dominio).
     * 
     * @param id       ID del horario.
     * @param schedule Objeto de dominio con datos actualizados.
     * @return Mono con el horario actualizado.
     */
    Mono<DistributionSchedule> update(String id, DistributionSchedule schedule);

    /**
     * Actualiza un horario mediante DTO.
     * 
     * @param id      ID del horario.
     * @param request DTO con nuevos datos.
     * @return Mono con la respuesta actualizada.
     */
    Mono<DistributionScheduleResponse> update(String id, DistributionScheduleCreateRequest request);

    /**
     * Elimina un horario (baja lógica).
     * 
     * @param id ID del horario a eliminar.
     * @return Mono<Void>.
     */
    Mono<Void> delete(String id);

    /**
     * Reactiva un horario.
     * 
     * @param id ID del horario.
     * @return Mono con el horario activado.
     */
    Mono<DistributionScheduleResponse> activate(String id);

    /**
     * Desactiva un horario.
     * 
     * @param id ID del horario.
     * @return Mono con el horario desactivado.
     */
    Mono<DistributionScheduleResponse> deactivate(String id);
}