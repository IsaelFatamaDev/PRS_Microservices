package pe.edu.vallegrande.vgmsusers.domain.validators;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IOrganizationClient;
import reactor.core.publisher.Mono;

// SRP: Responsabilidad única - Validar existencia de organización
@Component
@RequiredArgsConstructor
public class OrganizationValidator {

    private final IOrganizationClient organizationClient;

    // Validar que la organización exista
    public Mono<Void> validateOrganizationExists(String organizationId) {
        return organizationClient.validateOrganizationExists(organizationId)
                .flatMap(exists -> exists
                        ? Mono.empty()
                        : Mono.error(new OrganizationNotFoundException("Organization not found: " + organizationId)));
    }

    // Validar organización del usuario
    public Mono<Void> validateUserOrganization(User user) {
        return validateOrganizationExists(user.getOrganizationId());
    }
}
