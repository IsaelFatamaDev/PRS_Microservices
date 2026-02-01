package pe.edu.vallegrande.vgmsorganizations.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.ParameterDocument;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * Repositorio para ParameterDocument (MongoDB)
 * Usa Documents de infraestructura, NO modelos de dominio
 */
@Repository
public interface ParameterRepository extends ReactiveMongoRepository<ParameterDocument, String> {
    
    /**
     * Busca parámetros por ID de organización
     */
    Flux<ParameterDocument> findAllByOrganizationId(String organizationId);
    
    /**
     * Busca parámetros por múltiples IDs de organización
     */
    Flux<ParameterDocument> findAllByOrganizationIdIn(List<String> organizationIds);
    
    /**
     * Busca parámetros por estado
     */
    Flux<ParameterDocument> findAllByStatus(String status);
}
