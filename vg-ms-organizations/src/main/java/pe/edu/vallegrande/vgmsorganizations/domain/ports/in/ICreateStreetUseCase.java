package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import reactor.core.publisher.Mono;

// Puerto de entrada para crear calles
public interface ICreateStreetUseCase {
     Mono<Street> execute(Street street);
}
