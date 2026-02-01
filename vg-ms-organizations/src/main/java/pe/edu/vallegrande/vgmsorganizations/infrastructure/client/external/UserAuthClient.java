package pe.edu.vallegrande.vgmsorganizations.infrastructure.client.external;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.CreateAdminRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.CreateAdminResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.ValidateUserRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.ValidateUserResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.msusers.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.msusers.MsUsersUserCreationResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.msusers.MsUsersUserInfo;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom.ExternalServiceException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

/**
 * Cliente para comunicarse con el microservicio MS-USERS
 * Maneja las operaciones de creación de administradores y validación de usuarios
 */
@Component
public class UserAuthClient {

    private final WebClient webClient;
    private final String createAdminEndpoint;
    private final String validateEndpoint;

    public UserAuthClient(
            @Qualifier("msUsersWebClient") WebClient webClient,
            @Value("${microservices.users.endpoints.create-admin:/internal/organizations/{organizationId}/create-admin}") String createAdminEndpoint,
            @Value("${microservices.users.endpoints.validate:/internal/users/validate}") String validateEndpoint) {
        this.webClient = webClient;
        this.createAdminEndpoint = createAdminEndpoint != null && !createAdminEndpoint.trim().isEmpty() 
            ? createAdminEndpoint 
            : "/internal/organizations/{organizationId}/create-admin";
        this.validateEndpoint = validateEndpoint != null && !validateEndpoint.trim().isEmpty() 
            ? validateEndpoint 
            : "/internal/users/validate";
    }

    /**
     * Obtiene los administradores de una organización
     */
    public Flux<MsUsersUserInfo> getAdminsByOrganizationId(String organizationId) {
        // Validar parámetro
        if (organizationId == null || organizationId.trim().isEmpty()) {
            return Flux.empty();
        }
        
        String url = "/internal/organizations/" + organizationId + "/admins";
        
        return webClient
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<MsUsersUserInfo>>>() {})
                .flatMapMany(apiResponse -> {
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        return Flux.fromIterable(apiResponse.getData());
                    }
                    return Flux.empty();
                })
                .timeout(Duration.ofSeconds(10))
                .onErrorResume(Exception.class, ex -> Flux.empty());
    }

    /**
     * Crea un usuario administrador en el servicio MS-USERS
     */
    public Mono<CreateAdminResponse> createAdmin(CreateAdminRequest request, String organizationId) {
        // Validar parámetros
        if (request == null || organizationId == null || organizationId.trim().isEmpty()) {
            return Mono.error(new ExternalServiceException("Invalid request parameters"));
        }
        
        String url = createAdminEndpoint.replace("{organizationId}", organizationId);

        return webClient
                .post()
                .uri(url)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ApiResponse<MsUsersUserCreationResponse>>() {})
                .flatMap(apiResponse -> {
                    if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                        MsUsersUserInfo userInfo = apiResponse.getData().getUserInfo();
                        
                        if (userInfo != null && (userInfo.getId() != null || userInfo.getUserCode() != null)) {
                            String userIdToUse = userInfo.getId() != null ? userInfo.getId() : userInfo.getUserCode();
                            
                            return Mono.just(CreateAdminResponse.builder()
                                    .userId(userIdToUse)
                                    .firstName(userInfo.getFirstName())
                                    .lastName(userInfo.getLastName())
                                    .name(userInfo.getName() != null ? userInfo.getName() 
                                            : (userInfo.getFirstName() + " " + userInfo.getLastName()))
                                    .email(userInfo.getEmail())
                                    .documentType(userInfo.getDocumentType())
                                    .documentNumber(userInfo.getDocumentNumber())
                                    .phone(userInfo.getPhone())
                                    .address(userInfo.getAddress())
                                    .role(userInfo.getRole())
                                    .organizationId(userInfo.getOrganizationId())
                                    .status(userInfo.getStatus())
                                    .success(apiResponse.isSuccess())
                                    .message(apiResponse.getMessage())
                                    .build());
                        }
                        
                        return Mono.error(new ExternalServiceException(
                                "MS-USERS returned success but missing user info"));
                    }
                    
                    String errorMessage = apiResponse != null && apiResponse.getMessage() != null 
                            ? apiResponse.getMessage() 
                            : "MS-USERS returned failure";
                    
                    return Mono.error(new ExternalServiceException(errorMessage));
                })
                .timeout(Duration.ofSeconds(30))
                .onErrorResume(WebClientResponseException.class, ex -> 
                        Mono.error(mapWebClientException(ex)))
                .onErrorResume(Exception.class, ex -> 
                        Mono.error(new ExternalServiceException("Failed to create admin user: " + ex.getMessage())));
    }

    /**
     * Valida la existencia de un usuario en el servicio MS-USERS
     */
    public Mono<ValidateUserResponse> validateUser(ValidateUserRequest request) {
        if (request == null) {
            return Mono.error(new ExternalServiceException("ValidateUserRequest cannot be null"));
        }
        
        return webClient
                .post()
                .uri(validateEndpoint)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(ValidateUserResponse.class)
                .timeout(Duration.ofSeconds(10))
                .retryWhen(Retry.backoff(2, Duration.ofMillis(500))
                        .filter(throwable -> !(throwable instanceof WebClientResponseException.BadRequest)))
                .onErrorMap(WebClientResponseException.class, this::mapWebClientException)
                .onErrorMap(Exception.class, ex -> new ExternalServiceException(
                        "Failed to validate user: " + ex.getMessage()));
    }

    /**
     * Valida la existencia de un usuario por email
     */
    public Mono<ValidateUserResponse> validateUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Mono.error(new ExternalServiceException("Email cannot be null or empty"));
        }
        
        ValidateUserRequest request = new ValidateUserRequest();
        request.setEmail(email);
        return validateUser(request);
    }

    /**
     * Valida la existencia de un usuario por ID
     */
    public Mono<ValidateUserResponse> validateUserById(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Mono.error(new ExternalServiceException("User ID cannot be null or empty"));
        }
        
        ValidateUserRequest request = new ValidateUserRequest();
        request.setUserId(userId);
        return validateUser(request);
    }

    /**
     * Mapea las excepciones de WebClient a excepciones personalizadas
     */
    private ExternalServiceException mapWebClientException(WebClientResponseException ex) {
        String errorMessage = switch (ex.getStatusCode().value()) {
            case 400 -> "Bad request to MS-USERS service";
            case 404 -> "MS-USERS service endpoint not found";
            case 500 -> "MS-USERS service internal error";
            case 503 -> "MS-USERS service unavailable";
            default -> "MS-USERS service returned error: " + ex.getStatusCode();
        };

        return new ExternalServiceException(errorMessage + ": " + ex.getResponseBodyAsString());
    }
}