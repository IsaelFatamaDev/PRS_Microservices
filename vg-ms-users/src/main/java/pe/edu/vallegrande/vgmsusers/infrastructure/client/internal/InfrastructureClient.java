package pe.edu.vallegrande.vgmsusers.infrastructure.client.internal;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class InfrastructureClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${external.ms-infrastructure.url}")
    private String infrastructureUrl;

    @Value("${external.ms-infrastructure.timeout:3000}")
    private long timeout;

    public InfrastructureClient(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<WaterBoxAssignmentResponse> getActiveWaterBoxAssignmentByUserId(String userId) {

        return webClientBuilder.build()
                .get()
                .uri(infrastructureUrl + "/internal/users/{userId}/water-box-assignment", userId)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response -> {
                    return Mono.empty();
                })
                .onStatus(HttpStatusCode::is5xxServerError, response -> {
                    return Mono.empty();
                })
                .bodyToMono(WaterBoxAssignmentResponse.class)
                .timeout(Duration.ofMillis(timeout))
                .doOnSuccess(response -> {
                })
                .doOnError(error -> {
                })
                .onErrorResume(error -> {
                    return Mono.empty();
                });
    }

    @Data
    public static class WaterBoxAssignmentResponse {
        private Long id;
        private Long waterBoxId;
        private String userId;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Double monthlyFee;
        private String status;
        private LocalDateTime createdAt;
        private Long transferId;
        private String boxCode;
        private String boxType;
    }
}
