package pe.edu.vallegrande.vgmsusers.infrastructure.client.external;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Service
@Slf4j
public class NotificationClient {

    private final WebClient webClient;

    @Value("${external.ms-notifications.url}")
    private String notificationUrl;

    public NotificationClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<Void> sendWelcomeMessage(String number, String username, String password, String clientName) {
        String url = notificationUrl + "/welcome";

        Map<String, String> body = Map.of(
                "number", number,
                "username", username,
                "password", password,
                "clientName", clientName);

        return webClient.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .doOnSuccess(response -> log.info("Welcome message sent successfully to {}", number))
                .doOnError(error -> log.error("Error sending welcome message to {}: {}", number, error.getMessage()))
                .then();
    }
}
