package pe.edu.vallegrande.vgmsorganizations.infrastructure.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.FareDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FareRepository extends ReactiveMongoRepository<FareDocument, String> {

    /**
     * Busca tarifas por ID de zona
     */
    Flux<FareDocument> findAllByZoneId(String zoneId);

    /**
     * Busca tarifas por lista de IDs de zona
     */
    Flux<FareDocument> findAllByZoneIdIn(java.util.List<String> zoneIds);

    /**
     * Busca tarifas por estado
     */
    Flux<FareDocument> findAllByStatus(String status);

    /**
     * Busca la última tarifa por código (para generar siguiente código)
     */
    Mono<FareDocument> findFirstByOrderByFareCodeDesc();

    /**
     * Busca tarifas por ID de zona y estado
     */
    Flux<FareDocument> findAllByZoneIdAndStatus(String zoneId, String status);
}
