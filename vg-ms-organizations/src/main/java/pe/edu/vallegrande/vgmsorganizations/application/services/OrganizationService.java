package pe.edu.vallegrande.vgmsorganizations.application.services;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.OrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationStatsResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationWithAdminsResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OrganizationService {
    // Métodos originales (mantienen funcionalidad completa)
    Mono<OrganizationResponse> create(OrganizationRequest request);
    Flux<OrganizationResponse> findAll();
    Mono<OrganizationResponse> findById(String id);
    Mono<OrganizationResponse> update(String id, OrganizationRequest request);
    Mono<Void> delete(String id);
    Mono<Void> restore(String id);
    Flux<OrganizationWithAdminsResponse> getOrganizationsWithAdmins();
    
    // Métodos optimizados adicionales (para mejor rendimiento)
    Flux<OrganizationStatsResponse> findAllLight();  // Sin zonas/calles
    Mono<OrganizationResponse> findByIdLight(String id);  // Sin zonas/calles

    // Métodos con estadísticas
    Flux<OrganizationStatsResponse> findAllWithStats();  // Con contadores de zonas/calles
    Mono<OrganizationStatsResponse> findByIdWithStats(String id);  // Con contadores
    
    // Nuevo método para obtener datos específicos de una organización
    Flux<OrganizationResponse> findByOrganizationId(String organizationId);
}