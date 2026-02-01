package pe.edu.vallegrande.vgmsorganizations.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.OrganizationDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio para OrganizationDocument (MongoDB)
 * Usa Documents de infraestructura, NO modelos de dominio
 */
public interface OrganizationRepository extends ReactiveMongoRepository<OrganizationDocument, String> {

    /**
     * Busca organizaciones por estado
     */
    Flux<OrganizationDocument> findAllByStatus(String status);
    
    /**
     * Busca organizaciones por organizationId
     */
    Flux<OrganizationDocument> findByOrganizationId(String organizationId);
    
    /**
     * Busca la última organización por código (para generar siguiente código)
     * Optimizado para solo obtener el último registro sin cargar todos
     */
    Mono<OrganizationDocument> findFirstByOrganizationCodeIsNotNullOrderByOrganizationCodeDesc();
}
