package pe.edu.vallegrande.vgmsorganizations.domain.ports.out;

import reactor.core.publisher.Mono;

// Puerto de salida para cliente del servicio de usuarios
public interface IUserServiceClient {
     Mono<Boolean> userExists(String userId);

     Mono<Void> createAdmin(String organizationId, String username, String email);
}
