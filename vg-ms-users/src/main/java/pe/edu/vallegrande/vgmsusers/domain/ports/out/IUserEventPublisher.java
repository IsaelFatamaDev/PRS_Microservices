package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import pe.edu.vallegrande.vgmsusers.domain.models.User;
import reactor.core.publisher.Mono;

public interface IUserEventPublisher {
     Mono<Void> publishUserCreated(User user);

     Mono<Void> publishUserUpdated(User user);

     Mono<Void> publishUserDeleted(User user);
}
