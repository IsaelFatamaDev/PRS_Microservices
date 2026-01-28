package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.repository;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document.IncidentDocument;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface IncidentRepository extends ReactiveMongoRepository<IncidentDocument, String> {
    Flux<IncidentDocument> findByOrganizationId(String organizationId);

    Flux<IncidentDocument> findByIncidentTypeId(String incidentTypeId);

    Flux<IncidentDocument> findBySeverity(String severity);

    Flux<IncidentDocument> findByResolved(Boolean resolved);

    Flux<IncidentDocument> findByStatus(String status);

    Flux<IncidentDocument> findByRecordStatus(String recordStatus);

    Flux<IncidentDocument> findByIncidentCategory(String incidentCategory);

    Flux<IncidentDocument> findByZoneId(String zoneId);

    Flux<IncidentDocument> findByAssignedToUserId(String assignedToUserId);

    Flux<IncidentDocument> findByResolvedByUserId(String resolvedByUserId);
}