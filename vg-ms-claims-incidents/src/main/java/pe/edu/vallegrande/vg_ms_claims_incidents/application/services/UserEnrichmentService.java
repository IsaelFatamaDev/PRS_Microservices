package pe.edu.vallegrande.vg_ms_claims_incidents.application.services;

import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.UserDTO;
import reactor.core.publisher.Mono;
import java.util.List;

public interface UserEnrichmentService {
    
    /**
     * Obtener usuario por ID
     */
    Mono<UserDTO> getUserById(String userId, String organizationId);
    
    /**
     * Obtener usuario por username
     */
    Mono<UserDTO> getUserByUsername(String username, String organizationId);
    
    /**
     * Obtener usuario con datos de respaldo
     */
    Mono<UserDTO> getUserWithFallback(String userId, String organizationId, String fallbackUsername);
    
    /**
     * Obtener múltiples usuarios
     */
    Mono<List<UserDTO>> getMultipleUsers(List<String> userIds, String organizationId);
    
    /**
     * Verificar disponibilidad del servicio
     */
    Mono<Boolean> isUserServiceAvailable();
    
    /**
     * Test de integración
     */
    Mono<List<UserDTO>> testUserIntegration(String organizationId);
}
