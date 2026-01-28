package pe.edu.vallegrande.msdistribution.application.services;

import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionProgramResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Interfaz de servicio para la gestión de Programas de Distribución.
 * Define las operaciones CRUD y lógicas de negocio relacionadas con la
 * programación de distribución.
 * 
 * @version 1.0
 */
public interface DistributionProgramService {

    /**
     * Obtiene todos los programas de distribución.
     * 
     * @return Flux con todos los programas encontrados.
     */
    Flux<DistributionProgramResponse> getAll();

    /**
     * Obtiene un programa de distribución por su ID.
     * 
     * @param id Identificador único del programa.
     * @return Mono con el programa encontrado o vacío si no existe.
     */
    Mono<DistributionProgramResponse> getById(String id);

    /**
     * Guarda un nuevo programa de distribución.
     * 
     * @param request Datos de creación del programa.
     * @return Mono con el programa creado y persistido.
     */
    Mono<DistributionProgramResponse> save(DistributionProgramCreateRequest request);

    /**
     * Actualiza un programa existente reemplazando todos sus datos.
     * 
     * @param id      Identificador del programa a actualizar.
     * @param request Nuevos datos para el programa.
     * @return Mono con el programa actualizado.
     */
    Mono<DistributionProgramResponse> update(String id, DistributionProgramCreateRequest request);

    /**
     * Actualiza parcialmente un programa existente.
     * 
     * @param id      Identificador del programa.
     * @param request Datos a actualizar.
     * @return Mono con el programa actualizado.
     */
    Mono<DistributionProgramResponse> updatePartial(String id,
            pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramUpdateRequest request);

    /**
     * Elimina lógicamente un programa de distribución (cambio de estado).
     * 
     * @param id Identificador del programa.
     * @return Mono<Void> indicando finalización.
     */
    Mono<Void> delete(String id);

    /**
     * Reactiva un programa de distribución previamente inactivado.
     * 
     * @param id Identificador del programa.
     * @return Mono con el programa reactivado.
     */
    Mono<DistributionProgramResponse> activate(String id);

    /**
     * Desactiva un programa de distribución.
     * 
     * @param id Identificador del programa.
     * @return Mono con el programa desactivado.
     */
    Mono<DistributionProgramResponse> desactivate(String id);

    /**
     * Elimina físicamente un registro de la base de datos.
     * Uso con precaución.
     * 
     * @param id Identificador del programa.
     * @return Mono<Void> indicando finalización.
     */
    Mono<Void> physicalDelete(String id);

    /**
     * Obtiene programas filtrados por ID de organización.
     * 
     * @param organizationId Identificador de la organización.
     * @return Flux con los programas de la organización.
     */
    Flux<DistributionProgramResponse> getByOrganizationId(String organizationId);

    /**
     * Obtiene todos los programas activos (con estado 'A' o equivalente).
     * 
     * @return Flux con programas activos.
     */
    Flux<DistributionProgramResponse> getAllActive(); // CON_AGUA

    /**
     * Obtiene todos los programas inactivos.
     * 
     * @return Flux con programas inactivos.
     */
    Flux<DistributionProgramResponse> getAllInactive(); // SIN_AGUA

    /**
     * Cuenta el número total de programas activos.
     * 
     * @return Mono con el conteo (Long).
     */
    Mono<Long> countActive(); // CON_AGUA

    /**
     * Cuenta el número total de programas inactivos.
     * 
     * @return Mono con el conteo (Long).
     */
    Mono<Long> countInactive(); // SIN_AGUA

    /**
     * Actualiza el estado de entrega de agua de un programa.
     * 
     * @param id                  Identificador del programa.
     * @param waterDeliveryStatus Nuevo estado de entrega.
     * @param observations        Observaciones del cambio.
     * @param confirmedBy         Usuario que confirma el cambio.
     * @return Mono con el programa actualizado.
     */
    Mono<DistributionProgramResponse> updateWaterDeliveryStatus(String id, String waterDeliveryStatus,
            String observations, String confirmedBy);

    /**
     * Obtiene programas filtrados por estado de entrega de agua.
     * 
     * @param waterDeliveryStatus Estado de entrega a filtrar.
     * @return Flux con los programas coincidentes.
     */
    Flux<DistributionProgramResponse> getByWaterDeliveryStatus(String waterDeliveryStatus);

    /**
     * Cuenta programas por estado de entrega de agua.
     * 
     * @param waterDeliveryStatus Estado a contar.
     * @return Mono con el conteo.
     */
    Mono<Long> countByWaterDeliveryStatus(String waterDeliveryStatus);

    /**
     * Obtiene programas que tienen suministro de agua (CON_AGUA).
     * 
     * @return Flux con programas con agua.
     */
    Flux<DistributionProgramResponse> getProgramsWithWater(); // CON_AGUA

    /**
     * Obtiene programas sin suministro de agua (SIN_AGUA).
     * 
     * @return Flux con programas sin agua.
     */
    Flux<DistributionProgramResponse> getProgramsWithoutWater(); // SIN_AGUA

    /**
     * Marca un programa como "Con Agua".
     * 
     * @param id           Identificador del programa.
     * @param observations Observaciones opcionales.
     * @param confirmedBy  Usuario confirmador.
     * @return Mono con el programa actualizado.
     */
    Mono<DistributionProgramResponse> markWithWater(String id, String observations, String confirmedBy); // Marca
                                                                                                         // CON_AGUA

    /**
     * Marca un programa como "Sin Agua".
     * 
     * @param id           Identificador del programa.
     * @param observations Observaciones opcionales.
     * @param confirmedBy  Usuario confirmador.
     * @return Mono con el programa actualizado.
     */
    Mono<DistributionProgramResponse> markWithoutWater(String id, String observations, String confirmedBy); // Marca
                                                                                                            // SIN_AGUA
}