package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.INotificationClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClientImpl implements INotificationClient {

        private final WebClient.Builder webClientBuilder;

        @Value("${services.notification.url}")
        private String notificationUrl;

        @Override
        @CircuitBreaker(name = "notificationService", fallbackMethod = "sendWhatsAppMessageFallback")
        @Retry(name = "notificationService")
        public Mono<Void> sendWhatsAppMessage(String phone, String message) {
                return webClientBuilder.build()
                                .post()
                                .uri(notificationUrl + "/api/notifications/whatsapp")
                                .bodyValue(Map.of(
                                                "phone", phone,
                                                "message", message))
                                .retrieve()
                                .bodyToMono(Void.class)
                                .doOnSuccess(v -> log.info("WhatsApp message sent to: {}", phone))
                                .doOnError(e -> log.error("Error sending WhatsApp message to: {}", phone, e));
        }

        @Override
        @CircuitBreaker(name = "notificationService", fallbackMethod = "sendWelcomeMessageFallback")
        @Retry(name = "notificationService")
        public Mono<Void> sendWelcomeMessage(UUID userId, String phone, String username) {
                String message = String.format("Bienvenido %s! Tu cuenta ha sido creada exitosamente.", username);
                return sendWhatsAppMessage(phone, message);
        }

        private Mono<Void> sendWhatsAppMessageFallback(String phone, String message, Exception e) {
                log.warn("Fallback: Cannot send WhatsApp to {} due to: {}", phone, e.getMessage());
                return Mono.empty();
        }

        private Mono<Void> sendWelcomeMessageFallback(UUID userId, String phone, String username, Exception e) {
                log.warn("Fallback: Cannot send welcome message to user {} due to: {}", userId, e.getMessage());
                return Mono.empty();
        }
}
