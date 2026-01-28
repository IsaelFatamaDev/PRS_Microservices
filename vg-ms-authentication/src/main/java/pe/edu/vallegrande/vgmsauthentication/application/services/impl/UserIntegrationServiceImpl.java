package pe.edu.vallegrande.vgmsauthentication.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsauthentication.application.services.UserIntegrationService;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.ApiResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de integración con microservicio de usuarios
 */
@Service
@Slf4j
public class UserIntegrationServiceImpl implements UserIntegrationService {

     private final WebClient.Builder webClientBuilder;

     // Constructor explícito (PRS Standard - No usar @RequiredArgsConstructor)
     public UserIntegrationServiceImpl(WebClient.Builder webClientBuilder) {
          this.webClientBuilder = webClientBuilder;
     }

     @Value("${app.services.users.url}")
     private String usersServiceUrl;

     @Value("${app.services.users.timeout:5000}")
     private int timeout;

     @Override
     public Mono<UserInfo> getUserByUsername(String username) {
          log.debug("Buscando usuario por username: {}", username);

          return webClientBuilder.build()
                    .get()
                    .uri(usersServiceUrl + "/common/user/username/{username}", username)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .cast(ApiResponse.class)
                    .filter(ApiResponse::isSuccess)
                    .map(response -> mapToUserInfo(response.getData()))
                    .doOnSuccess(userInfo -> log.debug("Usuario encontrado: {}", userInfo.userCode()))
                    .doOnError(
                              error -> log.error("Error buscando usuario por username {}: {}", username,
                                        error.getMessage()));
     }

     @Override
     public Mono<UserInfo> getUserById(String userId) {
          log.debug("Buscando usuario por ID: {}", userId);

          return webClientBuilder.build()
                    .get()
                    .uri(usersServiceUrl + "/common/user/id/{id}", userId)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .filter(ApiResponse::isSuccess)
                    .map(response -> mapToUserInfo(response.getData()))
                    .doOnSuccess(userInfo -> log.debug("Usuario encontrado: {}", userInfo.userCode()))
                    .doOnError(error -> log.error("Error buscando usuario por ID {}: {}", userId, error.getMessage()));
     }

     @Override
     public Mono<UserInfo> getUserByCode(String userCode) {
          log.debug("Buscando usuario por código: {}", userCode);

          return webClientBuilder.build()
                    .get()
                    .uri(usersServiceUrl + "/common/user/code/{userCode}/basic", userCode)
                    .retrieve()
                    .bodyToMono(ApiResponse.class)
                    .timeout(Duration.ofMillis(timeout))
                    .filter(ApiResponse::isSuccess)
                    .map(response -> mapToUserInfo(response.getData()))
                    .doOnSuccess(userInfo -> log.debug("Usuario encontrado: {}", userInfo.userCode()))
                    .doOnError(error -> log.error("Error buscando usuario por código {}: {}", userCode,
                              error.getMessage()));
     }

     @Override
     public Mono<Boolean> validateUserExists(String email) {
          log.debug("Validando existencia de usuario: {}", email);

          return getUserByUsername(email)
                    .map(UserInfo::isActive)
                    .onErrorReturn(false)
                    .doOnNext(exists -> log.debug("Usuario {} existe y está activo: {}", email, exists));
     }

     /**
      * Mapea la respuesta del API a UserInfo
      */
     private UserInfo mapToUserInfo(Object data) {
          if (data instanceof Map<?, ?> map) {
               return new UserInfo(
                         (String) map.get("id"),
                         (String) map.get("userCode"),
                         (String) map.get("firstName"),
                         (String) map.get("lastName"),
                         (String) map.get("email"),
                         (String) map.get("phone"),
                         (String) map.get("organizationId"),
                         (String) map.get("streetId"),
                         (String) map.get("zoneId"),
                         convertToRoleSet(map.get("roles")),
                         (String) map.get("status"),
                         parseLocalDateTime(map.get("createdAt")),
                         parseLocalDateTime(map.get("updatedAt")));
          }
          throw new IllegalArgumentException("No se pudo mapear la respuesta a UserInfo");
     }

     /**
      * Convierte roles (ArrayList o cualquier Collection) a Set<String>
      */
     private Set<String> convertToRoleSet(Object rolesObj) {
          if (rolesObj == null) {
               return Set.of();
          }

          if (rolesObj instanceof Collection<?> collection) {
               return collection.stream()
                         .map(Object::toString)
                         .collect(Collectors.toSet());
          }

          // Si es un solo rol como String
          if (rolesObj instanceof String singleRole) {
               return Set.of(singleRole);
          }

          log.warn("Tipo de roles desconocido: {}", rolesObj.getClass());
          return Set.of();
     }

     /**
      * Parsea fecha desde diferentes formatos
      */
     private java.time.LocalDateTime parseLocalDateTime(Object dateObj) {
          if (dateObj == null)
               return null;

          if (dateObj instanceof String dateStr) {
               try {
                    return java.time.LocalDateTime.parse(dateStr);
               } catch (Exception e) {
                    log.warn("No se pudo parsear fecha: {}", dateStr);
                    return null;
               }
          }

          return null;
     }
}
