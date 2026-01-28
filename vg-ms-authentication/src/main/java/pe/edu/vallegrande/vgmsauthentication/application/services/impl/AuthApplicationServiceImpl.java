package pe.edu.vallegrande.vgmsauthentication.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.ChangePasswordRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.CreateAccountRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.FirstPasswordChangeRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.LoginRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.ApiResponse;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.AuthResponse;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.CreateAccountResponse;
import pe.edu.vallegrande.vgmsauthentication.application.services.AuthApplicationService;
import pe.edu.vallegrande.vgmsauthentication.application.services.UserIntegrationService;
import pe.edu.vallegrande.vgmsauthentication.application.services.KeycloakDomainService;
import pe.edu.vallegrande.vgmsauthentication.domain.models.Username;

import reactor.core.publisher.Mono;

import jakarta.annotation.PostConstruct;

/**
 * Implementación del servicio de aplicación para autenticación
 */
@Service
@Slf4j
public class AuthApplicationServiceImpl implements AuthApplicationService {

     private final KeycloakDomainService keycloakDomainService;
     private final UserIntegrationService userIntegrationService;

     // Constructor explícito (PRS Standard - No usar @RequiredArgsConstructor)
     public AuthApplicationServiceImpl(KeycloakDomainService keycloakDomainService,
               UserIntegrationService userIntegrationService) {
          this.keycloakDomainService = keycloakDomainService;
          this.userIntegrationService = userIntegrationService;
     }

     @PostConstruct
     public void init() {
          // Configurar Keycloak al iniciar la aplicación
          keycloakDomainService.initializeKeycloakIfNeeded()
                    .then(keycloakDomainService.configureTokenMappers()) // Configurar mappers
                    .subscribe(
                              unused -> log.info("Keycloak y mappers configurados exitosamente"),
                              error -> log.error("Error configurando Keycloak: {}", error.getMessage()));
     }

     @Override
     public Mono<ApiResponse<CreateAccountResponse>> createAccount(CreateAccountRequest request) {
          // GENERAR username automáticamente basado en firstName y lastName
          Username username = Username.fromNames(request.getFirstName(), request.getLastName());

          log.info("Creando cuenta para usuario: {} con username generado: {}", request.getUserId(),
                    username.getValue());

          return keycloakDomainService.createUserAccount(
                    username.getValue(),
                    request.getEmail(),
                    request.getFirstName(),
                    request.getLastName(),
                    request.getTemporaryPassword())
                    .flatMap(userCreationResult -> {
                         // SIMPLE: Usar método original (es lo suficientemente rápido)
                         return keycloakDomainService.assignRoles(username.getValue(), request.getRoles())
                                   .then(keycloakDomainService.updateUserAttributes(
                                             username.getValue(),
                                             request.getOrganizationId(),
                                             request.getUserId()))
                                   .then(Mono.just(ApiResponse.success(
                                             "Cuenta creada exitosamente",
                                             CreateAccountResponse.builder()
                                                       .userId(userCreationResult.keycloakUserId())
                                                       .username(username.getValue())
                                                       .temporaryPassword(userCreationResult.temporaryPassword())
                                                       .accountEnabled(true)
                                                       .message("Usuario creado exitosamente. Credenciales generadas para primer acceso.")
                                                       .build())));
                    })
                    .onErrorResume(error -> {
                         log.error("Error creando cuenta para usuario {}: {}", request.getUserId(), error.getMessage());
                         return Mono.just(ApiResponse.error("Error creando cuenta: " + error.getMessage()));
                    });
     }

     @Override
     public Mono<ApiResponse<AuthResponse>> login(LoginRequest request) {
          log.info("Procesando login para username: {}", request.getUsername());

          return userIntegrationService.getUserByUsername(request.getUsername())
                    .flatMap(msUserInfo -> {
                         return keycloakDomainService.updateUserAttributes(
                                   request.getUsername(),
                                   msUserInfo.organizationId(),
                                   msUserInfo.id())
                                   .then(keycloakDomainService.authenticateUser(request.getUsername(),
                                             request.getPassword()))
                                   .flatMap(tokenInfo -> {
                                        return keycloakDomainService.getUserInfoFromToken(tokenInfo.accessToken())
                                                  .map(keycloakUserInfo -> {
                                                       AuthResponse authResponse = AuthResponse.builder()
                                                                 .accessToken(tokenInfo.accessToken())
                                                                 .refreshToken(tokenInfo.refreshToken())
                                                                 .expiresIn(tokenInfo.expiresIn())
                                                                 .tokenType(tokenInfo.tokenType())
                                                                 .userInfo(buildUserInfoFromKeycloakAndMsUsers(
                                                                           keycloakUserInfo, msUserInfo,
                                                                           request.getUsername()))
                                                                 .build();

                                                       return ApiResponse.success("Login exitoso", authResponse);
                                                  });
                                   });
                    })
                    .onErrorMap(error -> {
                         if (error.getMessage().contains("Credenciales inválidas")) {
                              return error;
                         }
                         log.error(" Login falló - Error interno para {}: {}",
                                   request.getUsername(), error.getMessage());
                         return new RuntimeException(
                                   "Error en proceso de login: " + error.getMessage());
                    })
                    .onErrorResume(error -> {
                         log.warn("Login fallido para {}: {}", request.getUsername(), error.getMessage());
                         return Mono.just(ApiResponse.error("Credenciales inválidas"));
                    });
     }

     @Override
     public Mono<ApiResponse<AuthResponse>> refreshToken(String refreshToken) {
          log.debug("Refrescando token");

          return keycloakDomainService.refreshToken(refreshToken)
                    .map(tokenInfo -> {
                         AuthResponse authResponse = AuthResponse.builder()
                                   .accessToken(tokenInfo.accessToken())
                                   .refreshToken(tokenInfo.refreshToken())
                                   .expiresIn(tokenInfo.expiresIn())
                                   .tokenType(tokenInfo.tokenType())
                                   .build();

                         return ApiResponse.success("Token refrescado", authResponse);
                    })
                    .onErrorResume(error -> {
                         log.error("Error refrescando token: {}", error.getMessage());
                         return Mono.just(ApiResponse.error("Error refrescando token"));
                    });
     }

     @Override
     public Mono<ApiResponse<Boolean>> validateToken(String accessToken) {
          return keycloakDomainService.validateToken(accessToken)
                    .map(isValid -> ApiResponse.success("Token validado", isValid))
                    .onErrorReturn(ApiResponse.error("Token inválido"));
     }

     @Override
     public Mono<ApiResponse<AuthResponse.UserInfo>> getCurrentUser(String accessToken) {
          // Por simplicidad, retornamos error - en producción necesitaríamos
          // decodificar el JWT token para obtener la información del usuario
          log.warn("Implementar getCurrentUser decodificando JWT token");
          return Mono.just(ApiResponse.error("Funcionalidad getCurrentUser no implementada"));
     }

     @Override
     public Mono<ApiResponse<Void>> logout(String accessToken) {
          // En un sistema stateless con JWT, el logout es responsabilidad del cliente
          // que debe descartar el token. Opcionalmente se podría implementar
          // una blacklist de tokens en Keycloak
          log.info("Logout solicitado - token debe ser descartado por el cliente");
          return Mono.just(ApiResponse.success("Sesión cerrada exitosamente"));
     }

     /**
      * Construye información del usuario combinando datos de Keycloak y MS-users
      */
     private AuthResponse.UserInfo buildUserInfoFromKeycloakAndMsUsers(
               KeycloakDomainService.UserInfo keycloakUserInfo,
               UserIntegrationService.UserInfo msUserInfo,
               String username) {
          return AuthResponse.UserInfo.builder()
                    .userId(msUserInfo.id())
                    .username(username)
                    .email(keycloakUserInfo.email())
                    .firstName(keycloakUserInfo.givenName())
                    .lastName(keycloakUserInfo.familyName())
                    .organizationId(msUserInfo.organizationId())
                    .roles(msUserInfo.roles())
                    .mustChangePassword(false)
                    .lastLogin(java.time.LocalDateTime.now())
                    .build();
     }

     @Override
     public Mono<ApiResponse<String>> changePassword(String accessToken, ChangePasswordRequest request) {
          log.info("Procesando cambio de contraseña");

          // Validar que las nuevas contraseñas coincidan
          if (!request.getNewPassword().equals(request.getConfirmPassword())) {
               return Mono.just(ApiResponse.error("Las contraseñas no coinciden"));
          }

          // Obtener username del token y luego cambiar contraseña
          return keycloakDomainService.getUserInfoFromToken(accessToken)
                    .flatMap(userInfo -> keycloakDomainService.changePassword(userInfo.preferredUsername(),
                              request.getCurrentPassword(), request.getNewPassword()))
                    .then(Mono.fromCallable(() -> {
                         log.info(" Contraseña cambiada exitosamente");
                         return ApiResponse.success("Contraseña actualizada exitosamente", "OK");
                    }))
                    .onErrorResume(error -> {
                         log.error(" Error cambiando contraseña: {}", error.getMessage());
                         return Mono.just(
                                   ApiResponse.<String>error("Error al cambiar la contraseña: " + error.getMessage()));
                    });
     }

     @Override
     public Mono<ApiResponse<String>> firstPasswordChange(FirstPasswordChangeRequest request) {
          log.info("🔧 Procesando primer cambio de contraseña para usuario: {}", request.getUsername());

          // Validar que las nuevas contraseñas coincidan
          if (!request.getNewPassword().equals(request.getConfirmPassword())) {
               return Mono.just(ApiResponse.error("Las contraseñas no coinciden"));
          }

          // Primero autenticar con la contraseña temporal para obtener un token
          return keycloakDomainService.authenticateUser(request.getUsername(), request.getTemporaryPassword())
                    .flatMap(authResult -> {
                         if (authResult == null || authResult.accessToken() == null) {
                              return Mono.just(ApiResponse.<String>error("Contraseña temporal inválida"));
                         }

                         // Cambiar la contraseña usando el token obtenido
                         return keycloakDomainService.changePassword(
                                   authResult.accessToken(),
                                   request.getTemporaryPassword(),
                                   request.getNewPassword()).then(Mono.fromCallable(() -> {
                                        log.info(" Primer cambio de contraseña exitoso para: {}",
                                                  request.getUsername());
                                        return ApiResponse.success(
                                                  "Contraseña actualizada exitosamente. Ya puedes hacer login con tu nueva contraseña.",
                                                  "OK");
                                   }));
                    })
                    .onErrorResume(error -> {
                         log.error(" Error en primer cambio de contraseña para {}: {}", request.getUsername(),
                                   error.getMessage());
                         return Mono.just(
                                   ApiResponse.<String>error("Error al cambiar la contraseña: " + error.getMessage()));
                    });
     }

     @Override
     public Mono<String> renewTemporaryPassword(String username) {
          log.info("🔄 Renovando contraseña temporal para usuario: {}", username);

          return keycloakDomainService.renewTemporaryPassword(username)
                    .doOnSuccess(newPassword -> {
                         log.info(" Nueva contraseña temporal generada para usuario: {}", username);
                    })
                    .onErrorMap(error -> {
                         log.error(" Error renovando contraseña temporal para {}: {}", username, error.getMessage());
                         return new RuntimeException("Error renovando contraseña temporal: " + error.getMessage());
                    });
     }
}
