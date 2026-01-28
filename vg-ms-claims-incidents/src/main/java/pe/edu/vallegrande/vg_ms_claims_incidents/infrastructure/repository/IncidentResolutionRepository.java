package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.repository;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.IncidentResolutionDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IncidentResolutionRepository extends ReactiveMongoRepository<IncidentResolutionDocument, String> {
    Flux<IncidentResolutionDocument> findByIncidentId(String incidentId);
}