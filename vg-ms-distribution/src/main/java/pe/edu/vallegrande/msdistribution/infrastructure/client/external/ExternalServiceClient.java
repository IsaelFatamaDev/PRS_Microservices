package pe.edu.vallegrande.msdistribution.infrastructure.client.external;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.msdistribution.infrastructure.client.external.dto.ExternalOrganization;
import pe.edu.vallegrande.msdistribution.infrastructure.client.external.dto.ExternalResponseDto;
import reactor.core.publisher.Mono;

/**
 * Cliente para consumir servicios externos.
 * Utiliza WebClient para comunicación no bloqueante.
 * 
 * @version 1.0
 */
@Service
public class ExternalServiceClient {

    private final WebClient organizationWebClient;

    /**
     * Inyección del cliente web configurado.
     * 
     * @param organizationWebClient Bean WebClient calificado.
     */
    public ExternalServiceClient(
            @Qualifier("organizationWebClient") WebClient organizationWebClient) {
        this.organizationWebClient = organizationWebClient;
    }

    /**
     * Obtiene una organización por ID desde el microservicio 'vg-ms-organization'.
     * Maneja errores registrándolos y retornando Mono.empty().
     * 
     * @param organizationId ID de la organización.
     * @return Mono con ExternalOrganization o vacío en caso de error.
     */
    public Mono<ExternalOrganization> getOrganizationById(String organizationId) {
        return organizationWebClient.get()
                .uri("/internal/organizations/" + organizationId)
                .retrieve()
                .bodyToMono(ExternalResponseDto.class)
                .map(response -> (ExternalOrganization) response.getData())
                .onErrorResume(e -> {
                    System.err.println("Error fetching organization " + organizationId
                            + ": " + e.getMessage());
                    return Mono.empty();
                });
    }
}