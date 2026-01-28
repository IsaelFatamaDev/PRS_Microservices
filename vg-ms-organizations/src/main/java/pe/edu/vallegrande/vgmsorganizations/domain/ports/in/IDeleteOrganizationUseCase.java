package pe.edu.vallegrande.vgmsorganizations.domain.ports.in;

import reactor.core.publisher.Mono;

// Puerto de entrada para eliminar organización
public interface IDeleteOrganizationUseCase {
     Mono<Void> softDelete(String id);

     Mono<Void> restore(String id);
}
