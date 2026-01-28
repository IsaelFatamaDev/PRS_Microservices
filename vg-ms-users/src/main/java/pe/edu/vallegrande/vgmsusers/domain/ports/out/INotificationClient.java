package pe.edu.vallegrande.vgmsusers.domain.ports.out;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface INotificationClient {
     Mono<Void> sendWhatsAppMessage(String phone, String message);

     Mono<Void> sendWelcomeMessage(UUID userId, String phone, String username);
}
