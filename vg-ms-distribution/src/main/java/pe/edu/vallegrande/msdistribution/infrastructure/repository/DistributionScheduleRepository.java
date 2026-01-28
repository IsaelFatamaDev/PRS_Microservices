package pe.edu.vallegrande.msdistribution.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la gestión de Horarios de Distribución.
 * Acceso a datos para la colección 'schedule'.
 * 
 * @version 1.0
 */
@Repository
public interface DistributionScheduleRepository extends ReactiveMongoRepository<DistributionSchedule, String> {

    /**
     * Busca horarios por estado.
     * 
     * @param status Estado (ACTIVE/INACTIVE).
     * @return Flux de horarios.
     */
    Flux<DistributionSchedule> findAllByStatus(String status);

    /**
     * Verifica si existe un horario con el código dado.
     * 
     * @param scheduleCode Código de horario.
     * @return Mono boolean.
     */
    Mono<Boolean> existsByScheduleCode(String scheduleCode);

    /**
     * Obtiene el último horario ordenado por código descendente.
     * Usado para generación de autoincrementales.
     * 
     * @return Último horario.
     */
    Mono<DistributionSchedule> findTopByOrderByScheduleCodeDesc();
}
