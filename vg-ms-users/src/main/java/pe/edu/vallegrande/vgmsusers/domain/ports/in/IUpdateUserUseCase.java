package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface IUpdateUserUseCase {
     Mono<User> execute(UUID userId, User user);
}
