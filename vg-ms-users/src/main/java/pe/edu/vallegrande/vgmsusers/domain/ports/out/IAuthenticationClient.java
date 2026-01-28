package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IAuthenticationClient {
     Mono<Void> createCredentials(UUID userId, String username, String password, String role);
}
