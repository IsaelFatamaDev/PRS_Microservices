package pe.edu.vallegrande.vgmsusers.infrastructure.validation;

import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.external.OrganizationClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.exception.custom.NotFoundException;
import reactor.core.publisher.Mono;

@Service
public class OrganizationValidationService {

    private final OrganizationClient organizationClient;

    public OrganizationValidationService(OrganizationClient organizationClient) {
        this.organizationClient = organizationClient;
    }

    /**
     * Valida si una organización existe.
     * Si existe, retorna Mono.empty().
     * Si no existe, retorna un error NotFoundException.
     */
    public Mono<Void> validateOrganizationExists(String organizationId) {
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.empty(); // O lanzar error si es obligatorio siempre
        }
        return organizationClient.getOrganizationById(organizationId)
                .flatMap(response -> {
                    if (!response.isStatus()) {
                        return Mono.error(new NotFoundException("Organización no encontrada: " + organizationId));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Valida si una organización existe y retorna los datos si es necesario.
     */
    public Mono<OrganizationClient.OrganizationData> validateAndGetOrganization(String organizationId) {
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.error(new NotFoundException("ID de organización no proporcionado"));
        }
        return organizationClient.getOrganizationById(organizationId)
                .flatMap(response -> {
                    if (!response.isStatus()) {
                        return Mono.error(new NotFoundException("Organización no encontrada: " + organizationId));
                    }
                    return Mono.just(response.getData());
                });
    }
}
