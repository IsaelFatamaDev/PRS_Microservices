package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import reactor.core.publisher.Mono;

// Puerto de entrada para actualizar organización
public interface IUpdateOrganizationUseCase {
     Mono<Organization> execute(String id, Organization organization);
}
