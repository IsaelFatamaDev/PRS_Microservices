package pe.edu.vallegrande.vg_ms_payment.infrastructure.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Parameter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    @Qualifier("internalWebClient")
    private final WebClient internalWebClient;

    @Value("${microservices.users.url}")
    private String userServiceUrl;

    @Value("${microservices.organization.url}")
    private String organizationServiceUrl;

    /**
     * Obtiene la información completa de un usuario por su ID usando WebClient interno
     * @param userId ID del usuario
     * @param organizationId ID de la organización para obtener datos completos
     * @return Mono con la información del usuario
     */
    public Mono<UserResponse> getUserByIdAutoAuth(String userId, String organizationId) {
        log.info("Obteniendo información completa del usuario con ID: {} y organizationId: {}", userId, organizationId);

        return internalWebClient.get()
                .uri(userServiceUrl + "/clients/{userId}?organizationId={organizationId}", userId, organizationId)
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                    response -> {
                        log.error("Error del servidor (5xx) al obtener usuario {}: {}", userId, response.statusCode());
                        return Mono.error(new RuntimeException("Servicio de usuarios no disponible: " + response.statusCode()));
                    })
                .bodyToMono(InternalApiResponse.class)
                .map(InternalApiResponse::getData)
                .doOnNext(user -> log.info("Usuario completo obtenido: {}", user.getUserId()))
                .onErrorResume(error -> {
                    log.error("Error obteniendo usuario completo: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Obtiene la información completa de un usuario por su ID usando WebClient interno (método de compatibilidad)
     * @param userId ID del usuario
     * @return Mono con la información del usuario
     */
    public Mono<UserResponse> getUserByIdAutoAuth(String userId) {
        log.info("Obteniendo información del usuario con ID: {} (método de compatibilidad)", userId);

        return internalWebClient.get()
                .uri(userServiceUrl + "/users/{userId}", userId)
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                    response -> {
                        log.error("Error del servidor (5xx) al obtener usuario {}: {}", userId, response.statusCode());
                        return Mono.error(new RuntimeException("Servicio de usuarios no disponible: " + response.statusCode()));
                    })
                .bodyToMono(InternalApiResponse.class)
                .map(InternalApiResponse::getData)
                .doOnNext(user -> log.info("Usuario obtenido: {}", user))
                .onErrorResume(error -> {
                    log.error("Error obteniendo usuario: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Obtiene la información de una organización por su ID usando WebClient interno
     * @param organizationId ID de la organización
     * @return Mono con la información de la organización
     */
    public Mono<OrganizationInfo> getOrganizationByIdAutoAuth(String organizationId) {
        log.info("Obteniendo información de la organización con ID: {}", organizationId);

        return internalWebClient.get()
                .uri(organizationServiceUrl + "/organizations/{organizationId}", organizationId)
                .retrieve()
                .onStatus(status -> status.is5xxServerError(),
                    response -> {
                        log.error("Error del servidor (5xx) al obtener organización {}: {}", organizationId, response.statusCode());
                        return Mono.error(new RuntimeException("Servicio de organización no disponible: " + response.statusCode()));
                    })
                .bodyToMono(OrganizationApiResponse.class)
                .map(OrganizationApiResponse::getData)
                .doOnNext(org -> log.info("Organización obtenida: {}", org))
                .onErrorResume(error -> {
                    log.error("Error obteniendo organización: {}", error.getMessage());
                    return Mono.empty();
                });
    }

    /**
     * Obtiene solo el logo base64 de una organización
     * @param organizationId ID de la organización
     * @return Mono con el logo en formato base64
     */
    public Mono<String> getOrganizationLogo(String organizationId) {
        log.info("Obteniendo logo de la organización con ID: {}", organizationId);

        return getOrganizationByIdAutoAuth(organizationId)
                .map(OrganizationInfo::getLogo)
                .doOnNext(logo -> log.info("Logo obtenido para organización {}", organizationId))
                .onErrorResume(error -> {
                    log.error("Error obteniendo logo de organización {}: {}", organizationId, error.getMessage());
                    return Mono.empty();
                });
    }



    // Clases para mapear la respuesta del microservicio de usuarios
    public static class InternalApiResponse {

        private boolean success;
        private UserResponse data;
        private String message;

        // Getters y setters
        public boolean isSuccess() { return success; }

        public void setSuccess(boolean success) { this.success = success; }

        public UserResponse getData() { return data; }

        public void setData(UserResponse data) { this.data = data; }

        public String getMessage() { return message; }

        public void setMessage(String message) { this.message = message; }
    }

    public static class OrganizationApiResponse {

        private boolean success;
        private OrganizationInfo data;
        private String message;

        // Getters y setters
        public boolean isSuccess() { return success; }

        public void setSuccess(boolean success) { this.success = success; }

        public OrganizationInfo getData() { return data; }

        public void setData(OrganizationInfo data) { this.data = data; }

        public String getMessage() { return message; }

        public void setMessage(String message) { this.message = message; }
    }

    public static class UserResponse {

        private String id;
        private String firstName;
        private String lastName;
        private String documentNumber;
        private String email;
        private String phone;
        private String address;
        private String fareAmount;
        private OrganizationInfo organization;
        private WaterBoxAssignmentInfo waterBoxAssignment;

        // Getters y setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getUserId() { return id; } // Alias para compatibilidad
        public void setUserId(String userId) { this.id = userId; }

        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getDocumentNumber() { return documentNumber; }
        public void setDocumentNumber(String documentNumber) { this.documentNumber = documentNumber; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }

        public String getAddress() { return address; }
        public void setAddress(String address) { this.address = address; }

        public String getFareAmount() { return fareAmount; }
        public void setFareAmount(String fareAmount) { this.fareAmount = fareAmount; }

        public OrganizationInfo getOrganization() { return organization; }
        public void setOrganization(OrganizationInfo organization) { this.organization = organization; }

        public WaterBoxAssignmentInfo getWaterBoxAssignment() { return waterBoxAssignment; }
        public void setWaterBoxAssignment(WaterBoxAssignmentInfo waterBoxAssignment) { this.waterBoxAssignment = waterBoxAssignment; }

        public String getFullName() {
            return firstName + " " + lastName;
        }

        public String getOrganizationId() {
            return organization != null ? organization.getOrganizationId() : null;
        }
    }

    public static class OrganizationInfo {
        private String organizationId;
        private String organizationName;
        private String logo;

        // Getters y setters
        public String getOrganizationId() { return organizationId; }
        public void setOrganizationId(String organizationId) { this.organizationId = organizationId; }

        public String getOrganizationName() { return organizationName; }
        public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

        public String getLogo() { return logo; }
        public void setLogo(String logo) { this.logo = logo; }

    }

    public static class WaterBoxAssignmentInfo {
        private Integer id;
        private Integer waterBoxId;
        private String boxCode;

        // Getters y setters
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }

        public Integer getWaterBoxId() { return waterBoxId; }
        public void setWaterBoxId(Integer waterBoxId) { this.waterBoxId = waterBoxId; }

        public String getBoxCode() { return boxCode; }
        public void setBoxCode(String boxCode) { this.boxCode = boxCode; }
    }
}