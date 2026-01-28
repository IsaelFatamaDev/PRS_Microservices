package pe.edu.vallegrande.vgmsauthentication.infrastructure.rest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.ChangePasswordRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.CreateAccountRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.FirstPasswordChangeRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.request.LoginRequest;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.ApiResponse;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.AuthResponse;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.CreateAccountResponse;
import pe.edu.vallegrande.vgmsauthentication.application.services.AuthApplicationService;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.utils.AuthorizationHeaderExtractor;
import reactor.core.publisher.Mono;

/**
 * REST Controller para operaciones de autenticación
 */
@RestController
@RequestMapping("/api/auth")
@Validated
@Slf4j
public class AuthRest {

     private final AuthApplicationService authApplicationService;

     // Constructor explícito (PRS Standard - No usar @RequiredArgsConstructor)
     public AuthRest(AuthApplicationService authApplicationService) {
          this.authApplicationService = authApplicationService;
     }

     /**
      * Registrar nuevo usuario (endpoint público)
      * POST /api/auth/register
      */
     @PostMapping("/register")
     public Mono<ResponseEntity<ApiResponse<CreateAccountResponse>>> register(
               @Valid @RequestBody CreateAccountRequest request) {
          log.info("Registro de nuevo usuario - email: {}", request.getEmail());

          return authApplicationService.createAccount(request)
                    .map(response -> {
                         if (response.isSuccess()) {
                              log.info("Usuario registrado exitosamente - username: {}", response.getData().username());
                         } else {
                              log.warn("Error registrando usuario: {}", response.getMessage());
                         }
                         return ResponseEntity.status(HttpStatus.CREATED).body(response);
                    });
     }

     /**
      * Crear cuenta desde MS-users
      * POST /api/auth/accounts
      */
     @PostMapping("/accounts")
     public Mono<ResponseEntity<ApiResponse<CreateAccountResponse>>> createAccount(
               @Valid @RequestBody CreateAccountRequest request) {
          log.info("Creando cuenta para usuario: {} - email: {}", request.getUserId(), request.getEmail());

          return authApplicationService.createAccount(request)
                    .map(response -> {
                         if (response.isSuccess()) {
                              log.info("Cuenta creada exitosamente - username generado: {} - contraseña temporal: {}",
                                        response.getData().username(), response.getData().temporaryPassword());
                         } else {
                              log.warn("Error creando cuenta: {}", response.getMessage());
                         }
                         return ResponseEntity.status(HttpStatus.CREATED).body(response);
                    });
     }

     /**
      * Login de usuario
      * POST /api/auth/login
      */
     @PostMapping("/login")
     public Mono<ResponseEntity<ApiResponse<AuthResponse>>> login(@Valid @RequestBody LoginRequest request) {
          log.info("Intento de login para username: {}", request.getUsername());

          return authApplicationService.login(request)
                    .map(response -> {
                         if (response.isSuccess()) {
                              log.info("Login exitoso para: {}", request.getUsername());
                         } else {
                              log.warn("Login fallido para: {}", request.getUsername());
                         }
                         return ResponseEntity.ok(response);
                    });
     }

     /**
      * Refresh token
      * POST /api/auth/refresh
      */
     @PostMapping("/refresh")
     public Mono<ResponseEntity<ApiResponse<AuthResponse>>> refreshToken(
               @RequestHeader("Authorization") String authHeader) {
          String refreshToken = AuthorizationHeaderExtractor.extractToken(authHeader);
          log.debug("Refrescando token");

          return authApplicationService.refreshToken(refreshToken)
                    .map(ResponseEntity::ok);
     }

     /**
      * Validar token
      * GET /api/auth/validate
      */
     @GetMapping("/validate")
     public Mono<ResponseEntity<ApiResponse<Boolean>>> validateToken(
               @RequestHeader("Authorization") String authHeader) {
          String accessToken = AuthorizationHeaderExtractor.extractToken(authHeader);

          return authApplicationService.validateToken(accessToken)
                    .map(ResponseEntity::ok);
     }

     /**
      * Obtener usuario actual
      * GET /api/auth/me
      */
     @GetMapping("/me")
     public Mono<ResponseEntity<ApiResponse<AuthResponse.UserInfo>>> getCurrentUser(
               @RequestHeader("Authorization") String authHeader) {
          String accessToken = AuthorizationHeaderExtractor.extractToken(authHeader);

          return authApplicationService.getCurrentUser(accessToken)
                    .map(ResponseEntity::ok);
     }

     /**
      * Logout
      * POST /api/auth/logout
      */
     @PostMapping("/logout")
     public Mono<ResponseEntity<ApiResponse<Void>>> logout(@RequestHeader("Authorization") String authHeader) {
          String accessToken = AuthorizationHeaderExtractor.extractToken(authHeader);
          log.info("Cerrando sesión");

          return authApplicationService.logout(accessToken)
                    .map(ResponseEntity::ok);
     }

     /**
      * Cambiar contraseña
      * PUT /api/auth/change-password
      */
     @PutMapping("/change-password")
     public Mono<ResponseEntity<ApiResponse<String>>> changePassword(
               @RequestHeader("Authorization") String authHeader,
               @Valid @RequestBody ChangePasswordRequest request) {
          String accessToken = AuthorizationHeaderExtractor.extractToken(authHeader);
          log.info("Solicitud de cambio de contraseña");

          return authApplicationService.changePassword(accessToken, request)
                    .map(ResponseEntity::ok);
     }

     /**
      * Health check
      * GET /api/auth/health
      */
     @GetMapping("/health")
     public Mono<ResponseEntity<ApiResponse<String>>> health() {
          return Mono.just(
                    ResponseEntity.ok(ApiResponse.success("MS-Authentication funcionando correctamente", "v2.0.0")));
     }

     /**
      * Primer cambio de contraseña (para contraseñas temporales)
      * POST /api/auth/first-password-change
      */
     @PostMapping("/first-password-change")
     public Mono<ResponseEntity<ApiResponse<String>>> firstPasswordChange(
               @Valid @RequestBody FirstPasswordChangeRequest request) {
          log.info("Solicitud de primer cambio de contraseña para usuario: {}", request.getUsername());

          // Este endpoint permite cambiar contraseñas temporales sin autenticación previa
          return authApplicationService.firstPasswordChange(request)
                    .map(ResponseEntity::ok);
     }

     /**
      * NUEVO: Renovar contraseña temporal (generar nueva cuando expire)
      * POST /api/auth/renew-temporary-password
      */
     @PostMapping("/renew-temporary-password")
     public Mono<ResponseEntity<ApiResponse<String>>> renewTemporaryPassword(@RequestParam String username) {
          log.info("Renovando contraseña temporal para usuario: {}", username);
          return authApplicationService.renewTemporaryPassword(username)
                    .map(newPassword -> ResponseEntity.ok(ApiResponse.success(
                              "Nueva contraseña temporal generada. Password: " + newPassword,
                              newPassword)))
                    .onErrorResume(error -> {
                         log.error("Error renovando contraseña temporal para {}: {}", username, error.getMessage());
                         return Mono.just(ResponseEntity.badRequest().body(
                                   ApiResponse.error("Error renovando contraseña temporal: " + error.getMessage())));
                    });
     }

}
