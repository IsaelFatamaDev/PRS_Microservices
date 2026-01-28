package pe.edu.vallegrande.ms_water_quality.infrastructure.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.ms_water_quality.infrastructure.client.dto.ExternalOrganization;
import pe.edu.vallegrande.ms_water_quality.infrastructure.client.dto.ExternalUser;
import pe.edu.vallegrande.ms_water_quality.infrastructure.client.dto.UserApiResponse;
import pe.edu.vallegrande.ms_water_quality.infrastructure.dto.ResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Service
public class ExternalServiceClient {

    private final WebClient userWebClient;

    public ExternalServiceClient(
            @Qualifier("userWebClient") WebClient userWebClient,
            @Qualifier("organizationWebClient") WebClient organizationWebClient) {
        this.userWebClient = userWebClient;
    }

    public Flux<ExternalUser> getAdminsByOrganization(String organizationId) {
        return userWebClient.get()
                .uri("/internal/organizations/{organizationId}/admins", organizationId)
                .retrieve()
                .bodyToMono(UserApiResponse.class)
                .flatMapMany(response -> {
                    if (response == null || response.getData() == null) return Flux.empty();
                    return Flux.fromIterable(response.getData());
                })
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .onErrorResume(WebClientResponseException.class, e -> Flux.empty())
                .onErrorResume(e -> Flux.empty());
    }

    public Mono<ExternalUser> getUserById(String userId) {
        return userWebClient.get()
                .uri("/api/users/{userId}", userId)
                .retrieve()
                .bodyToMono(ResponseDto.class)
                .flatMap(response -> {
                    if (response == null || response.getData() == null) return Mono.empty();
                    Object data = response.getData();
                    if (data instanceof ExternalUser user) return Mono.just(user);
                    return Mono.empty();
                })
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .onErrorResume(WebClientResponseException.class, e -> Mono.empty())
                .onErrorResume(e -> Mono.empty());
    }

    public Mono<ExternalOrganization> getOrganizationById(String organizationId) {
        return getAdminsByOrganization(organizationId)
                .next()
                .flatMap(adminUser -> {
                    if (adminUser == null || adminUser.getOrganization() == null) return Mono.empty();
                    return Mono.just(adminUser.getOrganization());
                })
                .retryWhen(Retry.backoff(2, Duration.ofMillis(300)))
                .onErrorResume(WebClientResponseException.class, e -> Mono.empty())
                .onErrorResume(e -> Mono.empty());
    }
}
