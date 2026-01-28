package pe.edu.vallegrande.vg_ms_claims_incidents.application.services;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentDTO;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IncidentService {
    Flux<IncidentDTO> findAll();

    Mono<IncidentDTO> findById(String id);

    Mono<IncidentDTO> save(IncidentDTO incidentDTO);

    Mono<IncidentDTO> update(String id, IncidentDTO incidentDTO);

    Mono<Void> deleteById(String id);

    Mono<IncidentDTO> restoreById(String id);

    Flux<IncidentDTO> findByOrganizationId(String organizationId);

    Flux<IncidentDTO> findByIncidentTypeId(String incidentTypeId);

    Flux<IncidentDTO> findBySeverity(String severity);

    Flux<IncidentDTO> findByResolvedStatus(Boolean resolved);

    Flux<IncidentDTO> findByStatus(String status);

    Flux<IncidentDTO> findByRecordStatus(String recordStatus);

    Flux<IncidentDTO> findByIncidentCategory(String incidentCategory);

    Flux<IncidentDTO> findByZoneId(String zoneId);

    Flux<IncidentDTO> findByAssignedToUserId(String assignedToUserId);

    Flux<IncidentDTO> findByResolvedByUserId(String resolvedByUserId);

    Mono<IncidentDTO> resolveIncident(String id, IncidentDTO incidentDTO);

    Mono<IncidentDTO> completeIncident(String id, IncidentDTO incidentDTO);
}