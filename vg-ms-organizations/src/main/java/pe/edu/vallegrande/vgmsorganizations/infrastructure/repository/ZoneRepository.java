package pe.edu.vallegrande.vgmsorganizations.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.ZoneDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Repositorio para ZoneDocument (MongoDB)
 * Usa Documents de infraestructura, NO modelos de dominio
 */
public interface ZoneRepository extends ReactiveMongoRepository<ZoneDocument, String> {
    
    /**
     * Busca zonas por ID de organización
     */
    Flux<ZoneDocument> findAllByOrganizationId(String organizationId);
    
    /**
     * Busca zonas por múltiples IDs de organización
     */
    Flux<ZoneDocument> findAllByOrganizationIdIn(List<String> organizationIds);
    
    /**
     * Busca una zona por zoneId y organizationId
     */
    Mono<ZoneDocument> findByZoneIdAndOrganizationId(String zoneId, String organizationId);
    
    /**
     * Busca la última zona por código (para generar siguiente código)
     */
    Mono<ZoneDocument> findFirstByOrderByZoneCodeDesc();
}