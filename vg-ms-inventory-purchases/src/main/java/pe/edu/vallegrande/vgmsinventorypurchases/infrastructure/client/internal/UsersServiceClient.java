package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.client.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.external.UserDetailsResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class UsersServiceClient {

    @Qualifier("usersWebClient")
    private final WebClient webClient;

    /**
     * Obtiene los detalles del usuario con información completa
     * 
     * @param userId ID del usuario
     * @return Información completa del usuario
     */
    public Mono<UserDetailsResponse> getUserDetails(String userId) {
        log.info("Consultando información del usuario: {}", userId);

        return webClient
                .get()
                .uri("/internal/users/{userId}", userId)
                .retrieve()
                .bodyToMono(UserDetailsResponse.class)
                .timeout(Duration.ofSeconds(10))
                .doOnSuccess(response -> {
                    if (response != null && response.getSuccess()) {
                        log.info("Información del usuario {} obtenida exitosamente", userId);
                    } else {
                        log.warn("Respuesta no exitosa para usuario {}: {}",
                                userId, response != null ? response.getMessage() : "Respuesta nula");
                    }
                })
                .doOnError(WebClientResponseException.class, ex -> {
                    log.error("Error HTTP al consultar usuario {}: {} - {}",
                            userId, ex.getStatusCode(), ex.getResponseBodyAsString());
                })
                .doOnError(Exception.class, ex -> {
                    log.error("Error general al consultar usuario {}: {}", userId, ex.getMessage());
                })
                .onErrorResume(exception -> {
                    log.error("Fallo al obtener información del usuario {}: {}", userId, exception.getMessage());
                    // Retornar un objeto con información básica en caso de error
                    return Mono.just(UserDetailsResponse.builder()
                            .success(false)
                            .message("Error al obtener información del usuario")
                            .data(UserDetailsResponse.UserData.builder()
                                    .id(userId)
                                    .firstName("Usuario")
                                    .lastName("No encontrado")
                                    .build())
                            .build());
                });
    }

    /**
     * Verifica si el usuario pertenece a una organización específica
     * 
     * @param userId         ID del usuario
     * @param organizationId ID de la organización
     * @return true si el usuario pertenece a la organización, false en caso
     *         contrario
     */
    public Mono<Boolean> validateUserOrganization(String userId, String organizationId) {
        return getUserDetails(userId)
                .map(response -> {
                    if (response.getSuccess() && response.getData() != null
                            && response.getData().getOrganization() != null) {
                        String userOrgId = response.getData().getOrganization().getOrganizationId();
                        boolean matches = organizationId.equals(userOrgId);
                        log.info("Validación organización usuario {}: {} - Organizaciones: {} vs {}",
                                userId, matches, organizationId, userOrgId);
                        return matches;
                    }
                    log.warn("No se pudo validar la organización del usuario {}", userId);
                    return false;
                });
    }
}