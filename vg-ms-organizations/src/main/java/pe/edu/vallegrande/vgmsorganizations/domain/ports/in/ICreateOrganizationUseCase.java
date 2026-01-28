package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import reactor.core.publisher.Mono;

// Puerto de entrada para crear organización
public interface ICreateOrganizationUseCase {
     Mono<Organization> execute(Organization organization);
}
