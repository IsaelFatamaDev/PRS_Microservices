package pe.edu.vallegrande.msdistribution.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la gestión de Rutas de Distribución.
 * Acceso a datos para la colección 'route'.
 * 
 * @version 1.0
 */
@Repository
public interface DistributionRouteRepository extends ReactiveMongoRepository<DistributionRoute, String> {

    /**
     * Busca rutas por estado.
     * 
     * @param status Estado (ACTIVE/INACTIVE).
     * @return Flux de rutas.
     */
    Flux<DistributionRoute> findAllByStatus(String status);

    /**
     * Verifica si existe una ruta con el código especificado.
     * 
     * @param routeCode Código de ruta.
     * @return Mono boolean.
     */
    Mono<Boolean> existsByRouteCode(String routeCode);

    /**
     * Obtiene la última ruta creada por orden de código descendente.
     * Usado para generación de autoincrementales.
     * 
     * @return Última ruta.
     */
    Mono<DistributionRoute> findTopByOrderByRouteCodeDesc();
}
