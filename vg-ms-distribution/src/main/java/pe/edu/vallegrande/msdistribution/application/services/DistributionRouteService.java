package pe.edu.vallegrande.msdistribution.application.services;

import pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionRouteResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interfaz de servicio para la gestión de Rutas de Distribución.
 * Define las operaciones CRUD y lógicas de acceso a rutas.
 * 
 * @version 1.0
 */
public interface DistributionRouteService {

    /**
     * Obtiene todas las rutas registradas.
     * 
     * @return Flux con la lista de rutas.
     */
    Flux<DistributionRoute> getAll();

    /**
     * Obtiene todas las rutas activas.
     * 
     * @return Flux con rutas activas.
     */
    Flux<DistributionRoute> getAllActive();

    /**
     * Obtiene todas las rutas inactivas.
     * 
     * @return Flux con rutas inactivas.
     */
    Flux<DistributionRoute> getAllInactive();

    /**
     * Obtiene una ruta por su ID.
     * 
     * @param id Identificador de la ruta.
     * @return Mono con la ruta encontrada.
     */
    Mono<DistributionRoute> getById(String id);

    /**
     * Crea una nueva ruta de distribución.
     * 
     * @param request Datos de creación de la ruta.
     * @return Mono con la respuesta de la ruta creada.
     */
    Mono<DistributionRouteResponse> save(DistributionRouteCreateRequest request);

    /**
     * Actualiza una ruta existente (objeto completo).
     * 
     * @param id    Identificador de la ruta.
     * @param route Objeto ruta con los datos actualizados.
     * @return Mono con la ruta actualizada.
     */
    Mono<DistributionRoute> update(String id, DistributionRoute route);

    /**
     * Actualiza una ruta existente usando un DTO de creación.
     * 
     * @param id      Identificador de la ruta.
     * @param request Datos actualizados.
     * @return Mono con la respuesta de la actualización.
     */
    Mono<DistributionRouteResponse> update(String id, DistributionRouteCreateRequest request);

    /**
     * Actualiza parcialmente una ruta.
     * 
     * @param id      Identificador de la ruta.
     * @param request Datos parciales a actualizar.
     * @return Mono con la respuesta actualizada.
     */
    Mono<DistributionRouteResponse> updatePartial(String id,
            pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteUpdateRequest request);

    /**
     * Elimina una ruta (baja lógica).
     * 
     * @param id Identificador de la ruta.
     * @return Mono<Void>.
     */
    Mono<Void> delete(String id);

    /**
     * Reactiva una ruta eliminada.
     * 
     * @param id Identificador de la ruta.
     * @return Mono con la respuesta de la ruta activada.
     */
    Mono<DistributionRouteResponse> activate(String id);

    /**
     * Desactiva una ruta.
     * 
     * @param id Identificador de la ruta.
     * @return Mono con la respuesta de la ruta desactivada.
     */
    Mono<DistributionRouteResponse> deactivate(String id);
}