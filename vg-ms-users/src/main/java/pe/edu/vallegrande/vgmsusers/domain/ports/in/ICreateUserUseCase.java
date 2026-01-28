package pe.edu.vallegrande.vgmsusers.domain.ports.in;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Mono;

public interface ICreateUserUseCase {
     Mono<User> execute(User user, String password);
}
