package pe.edu.vallegrande.vg_ms_claims_incidents.application.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.UserEnrichmentService;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.client.UserApiClient;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.UserDTO;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserEnrichmentServiceImpl implements UserEnrichmentService {

    private final UserApiClient userApiClient;

    @Override
    public Mono<UserDTO> getUserById(String userId, String organizationId) {
        log.info("Servicio: Obteniendo usuario por ID: {} de org: {}", userId, organizationId);
        return userApiClient.getUserById(userId, organizationId);
    }

    @Override
    public Mono<UserDTO> getUserByUsername(String username, String organizationId) {
        log.info("Servicio: Obteniendo usuario por username: {} de org: {}", username, organizationId);
        return userApiClient.getUserByUsername(username, organizationId);
    }

    @Override
    public Mono<UserDTO> getUserWithFallback(String userId, String organizationId, String fallbackUsername) {
        log.info("Servicio: Obteniendo usuario con fallback: {} de org: {} ({})", userId, organizationId, fallbackUsername);
        return userApiClient.getUserByIdWithFallback(userId, organizationId, fallbackUsername);
    }

    @Override
    public Mono<List<UserDTO>> getMultipleUsers(List<String> userIds, String organizationId) {
        log.info("Servicio: Obteniendo múltiples usuarios de org: {}: {}", organizationId, userIds);
        return userApiClient.getUsersByIds(userIds, organizationId);
    }

    @Override
    public Mono<Boolean> isUserServiceAvailable() {
        log.info("Servicio: Verificando disponibilidad del servicio de usuarios");
        return userApiClient.checkHealth();
    }

    @Override
    public Mono<List<UserDTO>> testUserIntegration(String organizationId) {
        log.info("Servicio: Probando integración con organizationId: {}", organizationId);
        return userApiClient.testIntegration(organizationId);
    }
}
