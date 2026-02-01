package pe.edu.vallegrande.vgmsusers.infrastructure.client.external;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@lombok.extern.slf4j.Slf4j
public class FareClient {

    private final WebClient webClient;

    @Value("${external.ms-organizations.url}")
    private String organizationServiceUrl;

    public FareClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Mono<FareResponse> getFaresByZoneId(String zoneId) {
        String url = organizationServiceUrl + "/api/admin/fare/zone/" + zoneId;
        log.info("Fetching fares from URL: {}", url);

        return org.springframework.security.core.context.ReactiveSecurityContextHolder.getContext()
                .map(org.springframework.security.core.context.SecurityContext::getAuthentication)
                .filter(auth -> auth.getCredentials() instanceof org.springframework.security.oauth2.jwt.Jwt)
                .map(auth -> (org.springframework.security.oauth2.jwt.Jwt) auth.getCredentials())
                .map(org.springframework.security.oauth2.jwt.Jwt::getTokenValue)
                .flatMap(token -> webClient.get()
                        .uri(url)
                        .headers(h -> h.setBearerAuth(token))
                        .retrieve()
                        .bodyToMono(FareResponse.class))
                .switchIfEmpty(Mono.defer(() -> webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(FareResponse.class)))
                .doOnSuccess(response -> log.info("Fares fetched successfully for zone {}: {}", zoneId,
                        response.isSuccess()))
                .doOnError(e -> log.error("Error fetching fares for zone {}: {}", zoneId, e.getMessage()))
                .onErrorResume(e -> Mono.just(new FareResponse(false, "Error fetching fares", null)));
    }

    public static class FareResponse {
        private boolean success;
        private String message;
        private List<FareData> data;

        public FareResponse() {
        }

        public FareResponse(boolean success, String message, List<FareData> data) {
            this.success = success;
            this.message = message;
            this.data = data;
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public List<FareData> getData() {
            return data;
        }

        public void setData(List<FareData> data) {
            this.data = data;
        }
    }

    public static class FareData {
        @JsonProperty("id")
        private String id;

        @JsonProperty("fareCode")
        private String fareCode;

        @JsonProperty("zoneId")
        private String zoneId;

        @JsonProperty("zoneName")
        private String zoneName;

        @JsonProperty("fareName")
        private String fareName;

        @JsonProperty("fareType")
        private String fareType;

        @JsonProperty("fareDescription")
        private String fareDescription;

        @JsonProperty("fareAmount")
        private String fareAmount;

        @JsonProperty("status")
        private String status;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getFareCode() {
            return fareCode;
        }

        public void setFareCode(String fareCode) {
            this.fareCode = fareCode;
        }

        public String getZoneId() {
            return zoneId;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }

        public String getZoneName() {
            return zoneName;
        }

        public void setZoneName(String zoneName) {
            this.zoneName = zoneName;
        }

        public String getFareName() {
            return fareName;
        }

        public void setFareName(String fareName) {
            this.fareName = fareName;
        }

        public String getFareType() {
            return fareType;
        }

        public void setFareType(String fareType) {
            this.fareType = fareType;
        }

        public String getFareDescription() {
            return fareDescription;
        }

        public void setFareDescription(String fareDescription) {
            this.fareDescription = fareDescription;
        }

        public String getFareAmount() {
            return fareAmount;
        }

        public void setFareAmount(String fareAmount) {
            this.fareAmount = fareAmount;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}
