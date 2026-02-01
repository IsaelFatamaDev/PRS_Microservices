package pe.edu.vallegrande.vgmsorganizations.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.StreetDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Repositorio para StreetDocument (MongoDB)
 * Usa Documents de infraestructura, NO modelos de dominio
 */
public interface StreetRepository extends ReactiveMongoRepository<StreetDocument, String> {
    
    /**
     * Busca calles por ID de zona
     */
    Flux<StreetDocument> findAllByZoneId(String zoneId);
    
    /**
     * Busca calles por múltiples IDs de zona
     */
    Flux<StreetDocument> findAllByZoneIdIn(List<String> zoneIds);
    
    /**
     * Busca la última calle por código (para generar siguiente código)
     */
    Mono<StreetDocument> findFirstByOrderByStreetCodeDesc();
}

