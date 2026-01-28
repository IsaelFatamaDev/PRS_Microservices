package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IOrganizationClient;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationClientImpl implements IOrganizationClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${services.organizations.url}")
    private String organizationsUrl;

    @Override
    @CircuitBreaker(name = "organizationService", fallbackMethod = "validateOrganizationExistsFallback")
    @Retry(name = "organizationService")
    public Mono<Boolean> validateOrganizationExists(String organizationId) {
        return webClientBuilder.build()
                .get()
                .uri(organizationsUrl + "/api/organizations/{id}/exists", organizationId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(exists -> log.info("Organization exists: {}", exists))
                .doOnError(e -> log.error("Error validating organization: {}", organizationId, e));
    }

    @Override
    @CircuitBreaker(name = "organizationService", fallbackMethod = "validateZoneExistsFallback")
    @Retry(name = "organizationService")
    public Mono<Boolean> validateZoneExists(String zoneId) {
        return webClientBuilder.build()
                .get()
                .uri(organizationsUrl + "/api/zones/{id}/exists", zoneId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(exists -> log.info("Zone exists: {}", exists))
                .doOnError(e -> log.error("Error validating zone: {}", zoneId, e));
    }

    @Override
    @CircuitBreaker(name = "organizationService", fallbackMethod = "validateStreetExistsFallback")
    @Retry(name = "organizationService")
    public Mono<Boolean> validateStreetExists(String streetId) {
        return webClientBuilder.build()
                .get()
                .uri(organizationsUrl + "/api/streets/{id}/exists", streetId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .doOnSuccess(exists -> log.info("Street exists: {}", exists))
                .doOnError(e -> log.error("Error validating street: {}", streetId, e));
    }

    private Mono<Boolean> validateOrganizationExistsFallback(String organizationId, Exception e) {
        log.warn("Fallback: Assuming organization exists: {} due to: {}", organizationId, e.getMessage());
        return Mono.just(true);
    }

    private Mono<Boolean> validateZoneExistsFallback(String zoneId, Exception e) {
        log.warn("Fallback: Assuming zone exists: {} due to: {}", zoneId, e.getMessage());
        return Mono.just(true);
    }

    private Mono<Boolean> validateStreetExistsFallback(String streetId, Exception e) {
        log.warn("Fallback: Assuming street exists: {} due to: {}", streetId, e.getMessage());
        return Mono.just(true);
    }
}
