package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IDeleteUserUseCase {
     Mono<Void> softDelete(UUID userId);

     Mono<Void> restore(UUID userId);
}
