package pe.edu.vallegrande.vgmsusers.infrastructure.client.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.client.internal.AuthenticationClient;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.ApiResponse;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.CreateAccountRequestDto;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.request.UserCredentialRequest;
import pe.edu.vallegrande.vgmsusers.infrastructure.dto.response.CreateAccountResponse;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Component
public class AuthenticationClientImpl implements AuthenticationClient {

     private final WebClient.Builder webClientBuilder;

     @Value("${external.ms-authentication.url}")
     private String authenticationServiceUrl;

     @Value("${external.ms-authentication.timeout:10000}")
     private int timeout;

     @Value("${external.ms-authentication.retryAttempts:3}")
     private int retryAttempts;

     public AuthenticationClientImpl(WebClient.Builder webClientBuilder) {
          this.webClientBuilder = webClientBuilder;
     }

     @Override
     public Mono<ApiResponse<String>> registerUserInKeycloak(UserCredentialRequest request) {

          var createAccountRequest = new CreateAccountRequestDto(
                    request.getUserId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getOrganizationId(),
                    request.getTemporaryPassword(),
                    request.getRoles().stream().map(role -> role.name()).toArray(String[]::new));

          return webClientBuilder.build()
                    .post()
                    .uri(authenticationServiceUrl + "/api/auth/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createAccountRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<String>>() {
                    })
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofMillis(500))
                              .doAfterRetry(
                                        retrySignal -> {
                                        }))
                    .onErrorResume(error -> {
                         return Mono.just(ApiResponse.<String>builder()
                                   .success(false)
                                   .message("Error al registrar en servicio de autenticación: " + error.getMessage())
                                   .build());
                    })
                    .doOnSuccess(response -> {
                    });
     }

     @Override
     public Mono<ApiResponse<CreateAccountResponse>> createAccountWithFullResponse(UserCredentialRequest request) {

          var createAccountRequest = new CreateAccountRequestDto(
                    request.getUserId(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getEmail(),
                    request.getOrganizationId(),
                    request.getTemporaryPassword(),
                    request.getRoles().stream().map(role -> role.name()).toArray(String[]::new));

          return webClientBuilder.build()
                    .post()
                    .uri(authenticationServiceUrl + "/api/auth/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createAccountRequest)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<CreateAccountResponse>>() {
                    })
                    .timeout(Duration.ofMillis(timeout))
                    .retryWhen(Retry.backoff(retryAttempts, Duration.ofMillis(500))
                              .doAfterRetry(
                                        retrySignal -> {
                                        }))
                    .doOnError(error -> {
                    })
                    .onErrorResume(error -> {
                         return Mono.just(ApiResponse.<CreateAccountResponse>builder()
                                   .success(false)
                                   .message("MS-AUTHENTICATION no disponible")
                                   .build());
                    })
                    .doOnSuccess(response -> {
                    });
     }

     @Override
     public Mono<Boolean> isServiceAvailable() {
          return webClientBuilder.build()
                    .get()
                    .uri(authenticationServiceUrl + "/actuator/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(response -> true)
                    .timeout(Duration.ofMillis(2000))
                    .onErrorReturn(false);
     }
}
