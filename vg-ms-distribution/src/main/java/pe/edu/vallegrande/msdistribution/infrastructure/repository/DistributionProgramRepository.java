package pe.edu.vallegrande.msdistribution.infrastructure.repository;

import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import pe.edu.vallegrande.msdistribution.infrastructure.document.DistributionProgramDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la gestión de Programas de Distribución.
 * Utiliza Spring Data MongoDB Reactive.
 * 
 * @version 1.0
 */
@Repository
public interface DistributionProgramRepository extends ReactiveMongoRepository<DistributionProgramDocument, String> {

    /**
     * Busca programas por estado.
     * 
     * @param status Estado a filtrar (ACTIVE, INACTIVE).
     * @return Flux de programas.
     */
    Flux<DistributionProgramDocument> findAllByStatus(String status);

    /**
     * Busca el primer programa por código, proyectando solo campos clave.
     * 
     * @param programCode Código del programa.
     * @return Mono con el programa encontrado (campos limitados) o vacío.
     */
    @Query(value = "{'programCode': ?0}", fields = "{'id': 1, 'programCode': 1, 'status': 1}")
    Mono<DistributionProgramDocument> findFirstByProgramCode(String programCode);

    /**
     * Obtiene el último programa creado ordenado por código descendente.
     * Útil para generar el siguiente código correlativo.
     * 
     * @return Mono con el último programa.
     */
    Mono<DistributionProgramDocument> findFirstByOrderByProgramCodeDesc();

    /**
     * Busca programas por ID de organización.
     * 
     * @param organizationId ID de la organización.
     * @return Flux de programas.
     */
    Flux<DistributionProgramDocument> findByOrganizationId(String organizationId);

    /**
     * Verifica si existe un programa con el código dado.
     * 
     * @param programCode Código del programa.
     * @return Mono terminando en true/false.
     */
    @Query(value = "{'programCode': ?0}", exists = true)
    Mono<Boolean> existsByProgramCode(String programCode);

    /**
     * Cuenta el número de programas con un estado específico.
     * Optimizado para contar sin cargar documentos.
     * 
     * @param status Estado a contar.
     * @return Mono con la cantidad.
     */
    @Query(value = "{'status': ?0}", count = true)
    Mono<Long> countByStatus(String status);

    /**
     * Obtiene todos los programas con proyección ligera (campos básicos).
     * Optimizado para listados masivos.
     * 
     * @return Flux de programas con proyección.
     */
    @Query(value = "{}", fields = "{'id': 1, 'programCode': 1, 'status': 1, 'organizationId': 1, 'programDate': 1}")
    Flux<DistributionProgramDocument> findAllLight();
}
