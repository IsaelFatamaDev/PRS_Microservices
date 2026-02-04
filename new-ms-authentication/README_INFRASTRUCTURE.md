# 🔧 INFRASTRUCTURE LAYER - Capa de Infraestructura

> **Implementaciones concretas, configuraciones y adaptadores externos.**

## 📋 Principios

1. **Implementa interfaces del dominio**: Adapters Out implementan puertos de salida
2. **Expone APIs**: Adapters In (Controllers) exponen casos de uso
3. **Configura dependencias externas**: Keycloak, RabbitMQ, WebClient
4. **Maneja seguridad**: Headers del Gateway, filtros, contexto de seguridad

---

## 📂 Estructura

```
infrastructure/
├── adapters/
│   ├── in/
│   │   └── rest/
│   │       ├── AuthRest.java                       → [CLASS] @RestController @RequestMapping("/api/v1/auth")
│   │       │                                         @RequiredArgsConstructor @Slf4j @Tag(name = "Authentication")
│   │       └── GlobalExceptionHandler.java         → [CLASS] @RestControllerAdvice @Slf4j
│   │                                                 Maneja todas las excepciones de dominio y validación
│   └── out/
│       ├── external/
│       │   ├── KeycloakClientImpl.java             → [CLASS] @Component @Slf4j @RequiredArgsConstructor
│       │   │                                         implements IKeycloakClient
│       │   │                                         Usa keycloak-admin-client para Admin API
│       │   └── UserServiceClientImpl.java          → [CLASS] @Component @Slf4j @RequiredArgsConstructor
│       │                                             implements IUserServiceClient
│       │                                             Usa WebClient con Circuit Breaker
│       └── messaging/
│           └── UserEventListener.java              → [CLASS] @Component @Slf4j @RequiredArgsConstructor
│                                                     @RabbitListener(queues = "auth.user.*")
│                                                     Escucha eventos: created, deleted, restored, purged
├── config/
│   ├── KeycloakConfig.java                         → [CLASS] @Configuration
│   │                                                 @Bean Keycloak keycloakAdminClient()
│   │                                                 @Bean RealmResource realmResource()
│   ├── WebClientConfig.java                        → [CLASS] @Configuration
│   │                                                 @Bean WebClient webClient()
│   │                                                 @Bean WebClient keycloakWebClient()
│   ├── RabbitMQConfig.java                         → [CLASS] @Configuration
│   │                                                 @Bean Queue, Exchange, Binding
│   │                                                 @Bean Jackson2JsonMessageConverter
│   ├── Resilience4jConfig.java                     → [CLASS] @Configuration
│   │                                                 Circuit Breaker para Keycloak y Users service
│   └── SecurityConfig.java                         → [CLASS] @Configuration @EnableWebFluxSecurity
│                                                     @Bean SecurityWebFilterChain (permitAll, stateless, NO CORS)
│                                                     ⚠️ CORS se configura SOLO en el Gateway
│                                                     📝 Swagger detecta endpoints automáticamente (no necesita OpenApiConfig)
└── security/
    ├── AuthenticatedUser.java                      → [CLASS] @Data @Builder @NoArgsConstructor @AllArgsConstructor
    │                                                 DTO: userId, organizationId, roles, email
    │                                                 Métodos: isSuperAdmin(), isAdmin(), hasRole(String)
    ├── GatewayHeadersExtractor.java                → [CLASS] @Component
    │                                                 Extrae X-User-Id, X-Organization-Id, X-Roles de headers
    ├── GatewayHeadersFilter.java                   → [CLASS] @Component implements WebFilter
    │                                                 Inyecta AuthenticatedUser en Reactor Context
    └── SecurityContextAdapter.java                 → [CLASS] @Component implements ISecurityContext
                                                      Obtiene AuthenticatedUser del contexto reactivo
```

---

## 1️⃣ ADAPTERS IN - Controllers REST

### 📄 AuthRest.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsauthentication.application.dto.request.*;
import pe.edu.vallegrande.vgmsauthentication.application.dto.response.*;
import pe.edu.vallegrande.vgmsauthentication.application.mappers.AuthMapper;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.in.*;
import reactor.core.publisher.Mono;

/**
 * Controller REST para operaciones de autenticación.
 *
 * <p><b>Endpoints:</b></p>
 * <ul>
 *   <li>POST /api/v1/auth/login - Login con credenciales</li>
 *   <li>POST /api/v1/auth/refresh - Refrescar token</li>
 *   <li>POST /api/v1/auth/logout - Cerrar sesión</li>
 *   <li>GET  /api/v1/auth/validate - Validar token</li>
 *   <li>GET  /api/v1/auth/userinfo - Información del usuario</li>
 *   <li>POST /api/v1/auth/introspect - Introspección de token</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operaciones de autenticación OAuth2/OIDC")
public class AuthRest {

    private final ILoginUseCase loginUseCase;
    private final IRefreshTokenUseCase refreshTokenUseCase;
    private final ILogoutUseCase logoutUseCase;
    private final IValidateTokenUseCase validateTokenUseCase;

    // ═══════════════════════════════════════════════════════════════
    // LOGIN
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Autentica un usuario contra Keycloak usando OAuth2 Password Grant"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Login exitoso",
            content = @Content(schema = @Schema(implementation = LoginResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciales inválidas"
        ),
        @ApiResponse(
            responseCode = "503",
            description = "Keycloak no disponible"
        )
    })
    public Mono<ResponseEntity<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request
    ) {
        log.info("Login request for user: {}", request.getUsername());

        return loginUseCase.execute(AuthMapper.toCredentials(request))
            .map(AuthMapper::toLoginResponse)
            .map(ResponseEntity::ok);
    }

    // ═══════════════════════════════════════════════════════════════
    // REFRESH TOKEN
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/refresh")
    @Operation(
        summary = "Refrescar token",
        description = "Obtiene un nuevo access token usando el refresh token"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token refrescado",
            content = @Content(schema = @Schema(implementation = TokenResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Refresh token expirado o inválido"
        )
    })
    public Mono<ResponseEntity<TokenResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        log.debug("Refresh token request");

        return refreshTokenUseCase.execute(request.getRefreshToken(), request.getClientId())
            .map(AuthMapper::toTokenResponse)
            .map(ResponseEntity::ok);
    }

    // ═══════════════════════════════════════════════════════════════
    // LOGOUT
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/logout")
    @Operation(
        summary = "Cerrar sesión",
        description = "Invalida los tokens del usuario en Keycloak"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "204",
            description = "Logout exitoso"
        )
    })
    public Mono<ResponseEntity<Void>> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        log.info("Logout request");

        return logoutUseCase.execute(request.getRefreshToken(), request.getClientId())
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/logout/token")
    @Operation(
        summary = "Cerrar sesión con access token",
        description = "Cierra sesión usando el access token del header Authorization"
    )
    public Mono<ResponseEntity<Void>> logoutWithAccessToken(
            @Parameter(description = "Bearer token")
            @RequestHeader("Authorization") String authorization
    ) {
        String accessToken = extractToken(authorization);
        log.info("Logout request with access token");

        return logoutUseCase.executeWithAccessToken(accessToken)
            .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDATE TOKEN
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/validate")
    @Operation(
        summary = "Validar token",
        description = "Verifica si el access token es válido"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Token válido",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token inválido o expirado"
        )
    })
    public Mono<ResponseEntity<UserInfoResponse>> validateToken(
            @Parameter(description = "Bearer token")
            @RequestHeader("Authorization") String authorization
    ) {
        String accessToken = extractToken(authorization);
        log.debug("Validate token request");

        return validateTokenUseCase.execute(accessToken)
            .map(AuthMapper::toUserInfoResponse)
            .map(ResponseEntity::ok);
    }

    // ═══════════════════════════════════════════════════════════════
    // USER INFO
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/userinfo")
    @Operation(
        summary = "Obtener información del usuario",
        description = "Retorna información del usuario autenticado"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Información del usuario",
            content = @Content(schema = @Schema(implementation = UserInfoResponse.class))
        ),
        @ApiResponse(
            responseCode = "401",
            description = "No autenticado"
        )
    })
    public Mono<ResponseEntity<UserInfoResponse>> getUserInfo(
            @Parameter(description = "Bearer token")
            @RequestHeader("Authorization") String authorization
    ) {
        String accessToken = extractToken(authorization);

        return validateTokenUseCase.getUserInfo(accessToken)
            .map(AuthMapper::toUserInfoResponse)
            .map(ResponseEntity::ok);
    }

    // ═══════════════════════════════════════════════════════════════
    // INTROSPECT (RFC 7662)
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/introspect")
    @Operation(
        summary = "Introspección de token",
        description = "Endpoint de introspección según RFC 7662"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Resultado de introspección",
            content = @Content(schema = @Schema(implementation = IntrospectResponse.class))
        )
    })
    public Mono<ResponseEntity<IntrospectResponse>> introspect(
            @RequestParam String token,
            @RequestParam(defaultValue = "jass-web-client") String clientId,
            @RequestParam(required = false) String clientSecret
    ) {
        log.debug("Introspect request");

        return validateTokenUseCase.introspect(token, clientId, clientSecret)
            .map(AuthMapper::toIntrospectResponse)
            .map(ResponseEntity::ok);
    }

    // ═══════════════════════════════════════════════════════════════
    // HEALTH CHECK
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Verifica que el servicio esté activo")
    public Mono<ResponseEntity<String>> health() {
        return Mono.just(ResponseEntity.ok("OK"));
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private String extractToken(String authorization) {
        if (authorization != null && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }
        return authorization;
    }
}
```

---

### 📄 GlobalExceptionHandler.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.adapters.in.rest.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones.
 *
 * <p>Convierte excepciones de dominio a respuestas HTTP estándar.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════════════════════
    // EXCEPCIONES DE DOMINIO
    // ═══════════════════════════════════════════════════════════════

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleDomainException(DomainException ex) {
        log.warn("Domain exception: {} - {}", ex.getErrorCode(), ex.getMessage());

        return Mono.just(ResponseEntity
            .status(ex.getHttpStatus())
            .body(buildErrorResponse(ex.getErrorCode(), ex.getMessage(), ex.getHttpStatus())));
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleInvalidCredentials(InvalidCredentialsException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());

        return Mono.just(ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(buildErrorResponse(ex.getErrorCode(), ex.getMessage(), 401)));
    }

    @ExceptionHandler(TokenExpiredException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleTokenExpired(TokenExpiredException ex) {
        log.debug("Token expired: {}", ex.getMessage());

        return Mono.just(ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(buildErrorResponse(ex.getErrorCode(), ex.getMessage(), 401)));
    }

    @ExceptionHandler(TokenInvalidException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleTokenInvalid(TokenInvalidException ex) {
        log.debug("Invalid token: {}", ex.getMessage());

        return Mono.just(ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(buildErrorResponse(ex.getErrorCode(), ex.getMessage(), 401)));
    }

    @ExceptionHandler(KeycloakException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleKeycloakException(KeycloakException ex) {
        log.error("Keycloak error: {}", ex.getMessage(), ex);

        return Mono.just(ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(buildErrorResponse(ex.getErrorCode(), ex.getMessage(), 503)));
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDACIÓN
    // ═══════════════════════════════════════════════════════════════

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleValidation(WebExchangeBindException ex) {
        String errors = ex.getFieldErrors().stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        log.warn("Validation error: {}", errors);

        return Mono.just(ResponseEntity
            .badRequest()
            .body(buildErrorResponse("VALIDATION_ERROR", errors, 400)));
    }

    // ═══════════════════════════════════════════════════════════════
    // EXCEPCIONES GENÉRICAS
    // ═══════════════════════════════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<Map<String, Object>>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);

        return Mono.just(ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(buildErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                500
            )));
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    private Map<String, Object> buildErrorResponse(String code, String message, int status) {
        Map<String, Object> error = new HashMap<>();
        error.put("timestamp", LocalDateTime.now().toString());
        error.put("status", status);
        error.put("error", code);
        error.put("message", message);
        return error;
    }
}
```

---

## 2️⃣ ADAPTERS OUT - Clientes Externos

### 📄 KeycloakClientImpl.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.adapters.out.keycloak;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.InvalidCredentialsException;
import pe.edu.vallegrande.vgmsauthentication.domain.exceptions.KeycloakException;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.IKeycloakClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementación del cliente de Keycloak.
 *
 * <p>Maneja toda la comunicación con Keycloak:</p>
 * <ul>
 *   <li>Token Endpoint (login, refresh, logout)</li>
 *   <li>UserInfo Endpoint</li>
 *   <li>Admin API (gestión de usuarios)</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Component
public class KeycloakClientImpl implements IKeycloakClient {

    private final WebClient tokenWebClient;
    private final WebClient adminWebClient;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.admin.client-id:admin-cli}")
    private String adminClientId;

    @Value("${keycloak.admin.client-secret:}")
    private String adminClientSecret;

    public KeycloakClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${keycloak.auth-server-url}") String authServerUrl,
            @Value("${keycloak.realm}") String realm
    ) {
        String tokenUrl = authServerUrl + "/realms/" + realm;
        String adminUrl = authServerUrl + "/admin/realms/" + realm;

        this.tokenWebClient = webClientBuilder
            .baseUrl(tokenUrl)
            .build();

        this.adminWebClient = webClientBuilder
            .baseUrl(adminUrl)
            .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // TOKEN ENDPOINT
    // ═══════════════════════════════════════════════════════════════

    @Override
    @CircuitBreaker(name = "keycloak", fallbackMethod = "loginFallback")
    @Retry(name = "keycloak")
    public Mono<Map<String, Object>> getTokenWithPassword(
            String username,
            String password,
            String clientId
    ) {
        log.debug("Requesting token for user: {}", username);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        formData.add("username", username);
        formData.add("password", password);

        return tokenWebClient.post()
            .uri("/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .onStatus(
                status -> status.value() == 401,
                response -> Mono.error(new InvalidCredentialsException())
            )
            .onStatus(
                status -> status.is5xxServerError(),
                response -> Mono.error(KeycloakException.connectionError())
            )
            .bodyToMono(Map.class)
            .map(this::convertToStringObjectMap)
            .doOnSuccess(tokens -> log.debug("Token obtained successfully"))
            .doOnError(error -> log.warn("Token request failed: {}", error.getMessage()));
    }

    @SuppressWarnings("unused")
    private Mono<Map<String, Object>> loginFallback(
            String username,
            String password,
            String clientId,
            Throwable t
    ) {
        log.error("Keycloak circuit breaker opened for login. Error: {}", t.getMessage());
        return Mono.error(KeycloakException.connectionError());
    }

    @Override
    @CircuitBreaker(name = "keycloak")
    @Retry(name = "keycloak")
    public Mono<Map<String, Object>> refreshToken(String refreshToken, String clientId) {
        log.debug("Refreshing token");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("client_id", clientId);
        formData.add("refresh_token", refreshToken);

        return tokenWebClient.post()
            .uri("/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::convertToStringObjectMap);
    }

    @Override
    public Mono<Void> revokeToken(String refreshToken, String clientId) {
        log.debug("Revoking token");

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("refresh_token", refreshToken);

        return tokenWebClient.post()
            .uri("/protocol/openid-connect/logout")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(Void.class);
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDATION ENDPOINTS
    // ═══════════════════════════════════════════════════════════════

    @Override
    @CircuitBreaker(name = "keycloak")
    public Mono<Map<String, Object>> introspectToken(
            String token,
            String clientId,
            String clientSecret
    ) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", token);
        formData.add("client_id", clientId);
        if (clientSecret != null) {
            formData.add("client_secret", clientSecret);
        }

        return tokenWebClient.post()
            .uri("/protocol/openid-connect/token/introspect")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::convertToStringObjectMap);
    }

    @Override
    @CircuitBreaker(name = "keycloak")
    public Mono<Map<String, Object>> getUserInfo(String accessToken) {
        return tokenWebClient.get()
            .uri("/protocol/openid-connect/userinfo")
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::convertToStringObjectMap);
    }

    // ═══════════════════════════════════════════════════════════════
    // ADMIN API - Gestión de Usuarios
    // ═══════════════════════════════════════════════════════════════

    @Override
    @CircuitBreaker(name = "keycloak-admin")
    public Mono<String> createUser(
            String userId,
            String email,
            String firstName,
            String lastName,
            String password,
            String role
    ) {
        log.info("Creating user in Keycloak: {}", email);

        Map<String, Object> userRepresentation = new HashMap<>();
        userRepresentation.put("id", userId);
        userRepresentation.put("username", email);
        userRepresentation.put("email", email);
        userRepresentation.put("firstName", firstName);
        userRepresentation.put("lastName", lastName);
        userRepresentation.put("enabled", true);
        userRepresentation.put("emailVerified", true);

        // Credenciales
        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", password);
        credentials.put("temporary", true); // Debe cambiar en primer login

        userRepresentation.put("credentials", new Object[]{credentials});

        return getAdminToken()
            .flatMap(adminToken -> adminWebClient.post()
                .uri("/users")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(userRepresentation)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    String location = response.getHeaders().getFirst("Location");
                    return extractUserId(location);
                })
            )
            .flatMap(keycloakUserId -> assignRole(keycloakUserId, role).thenReturn(keycloakUserId));
    }

    @Override
    @CircuitBreaker(name = "keycloak-admin")
    public Mono<Void> updateUser(
            String userId,
            String email,
            String firstName,
            String lastName
    ) {
        log.info("Updating user in Keycloak: {}", userId);

        Map<String, Object> updates = new HashMap<>();
        if (email != null) {
            updates.put("email", email);
            updates.put("username", email);
        }
        if (firstName != null) updates.put("firstName", firstName);
        if (lastName != null) updates.put("lastName", lastName);

        return getAdminToken()
            .flatMap(adminToken -> adminWebClient.put()
                .uri("/users/{userId}", userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .retrieve()
                .bodyToMono(Void.class)
            );
    }

    @Override
    @CircuitBreaker(name = "keycloak-admin")
    public Mono<Void> disableUser(String userId) {
        log.info("Disabling user in Keycloak: {}", userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("enabled", false);

        return getAdminToken()
            .flatMap(adminToken -> adminWebClient.put()
                .uri("/users/{userId}", userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .retrieve()
                .bodyToMono(Void.class)
            );
    }

    @Override
    @CircuitBreaker(name = "keycloak-admin")
    public Mono<Void> enableUser(String userId) {
        log.info("Enabling user in Keycloak: {}", userId);

        Map<String, Object> updates = new HashMap<>();
        updates.put("enabled", true);

        return getAdminToken()
            .flatMap(adminToken -> adminWebClient.put()
                .uri("/users/{userId}", userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updates)
                .retrieve()
                .bodyToMono(Void.class)
            );
    }

    @Override
    @CircuitBreaker(name = "keycloak-admin")
    public Mono<Void> deleteUser(String userId) {
        log.info("Deleting user from Keycloak: {}", userId);

        return getAdminToken()
            .flatMap(adminToken -> adminWebClient.delete()
                .uri("/users/{userId}", userId)
                .header("Authorization", "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(Void.class)
            );
    }

    @Override
    @CircuitBreaker(name = "keycloak-admin")
    public Mono<Void> assignRole(String userId, String roleName) {
        log.info("Assigning role '{}' to user: {}", roleName, userId);

        return getAdminToken()
            .flatMap(adminToken ->
                // Primero obtener el rol
                adminWebClient.get()
                    .uri("/roles/{roleName}", roleName)
                    .header("Authorization", "Bearer " + adminToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMap(role ->
                        // Luego asignar al usuario
                        adminWebClient.post()
                            .uri("/users/{userId}/role-mappings/realm", userId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new Object[]{role})
                            .retrieve()
                            .bodyToMono(Void.class)
                    )
            );
    }

    @Override
    @CircuitBreaker(name = "keycloak-admin")
    public Mono<Void> removeRole(String userId, String roleName) {
        log.info("Removing role '{}' from user: {}", roleName, userId);

        return getAdminToken()
            .flatMap(adminToken ->
                adminWebClient.get()
                    .uri("/roles/{roleName}", roleName)
                    .header("Authorization", "Bearer " + adminToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .flatMap(role ->
                        adminWebClient.method(org.springframework.http.HttpMethod.DELETE)
                            .uri("/users/{userId}/role-mappings/realm", userId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(new Object[]{role})
                            .retrieve()
                            .bodyToMono(Void.class)
                    )
            );
    }

    @Override
    @CircuitBreaker(name = "keycloak-admin")
    public Mono<Void> resetPassword(String userId, String newPassword, boolean temporary) {
        log.info("Resetting password for user: {}", userId);

        Map<String, Object> credentials = new HashMap<>();
        credentials.put("type", "password");
        credentials.put("value", newPassword);
        credentials.put("temporary", temporary);

        return getAdminToken()
            .flatMap(adminToken -> adminWebClient.put()
                .uri("/users/{userId}/reset-password", userId)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(credentials)
                .retrieve()
                .bodyToMono(Void.class)
            );
    }

    // ═══════════════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Obtiene token de admin para la Admin API.
     */
    private Mono<String> getAdminToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", adminClientId);
        formData.add("client_secret", adminClientSecret);

        return tokenWebClient.post()
            .uri("/protocol/openid-connect/token")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(Map.class)
            .map(response -> (String) response.get("access_token"));
    }

    private String extractUserId(String location) {
        if (location == null) return null;
        String[] parts = location.split("/");
        return parts[parts.length - 1];
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToStringObjectMap(Map<?, ?> map) {
        Map<String, Object> result = new HashMap<>();
        map.forEach((key, value) -> result.put(key.toString(), value));
        return result;
    }
}
```

---

### 📄 UserServiceClientImpl.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.adapters.out.users;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.IUserServiceClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Implementación del cliente de vg-ms-users.
 *
 * <p>Comunica con el microservicio de usuarios para obtener
 * información adicional no almacenada en Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Component
public class UserServiceClientImpl implements IUserServiceClient {

    private final WebClient webClient;

    public UserServiceClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.users.url:http://vg-ms-users:8081}") String usersServiceUrl
    ) {
        this.webClient = webClientBuilder
            .baseUrl(usersServiceUrl)
            .build();
    }

    @Override
    @CircuitBreaker(name = "users-service", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "users-service")
    public Mono<UserInfo> getUserById(String userId) {
        log.debug("Getting user by ID: {}", userId);

        return webClient.get()
            .uri("/api/v1/users/{id}", userId)
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::mapToUserInfo);
    }

    @Override
    @CircuitBreaker(name = "users-service", fallbackMethod = "getUserByEmailFallback")
    @Retry(name = "users-service")
    public Mono<UserInfo> getUserByEmail(String email) {
        log.debug("Getting user by email: {}", email);

        return webClient.get()
            .uri("/api/v1/users/email/{email}", email)
            .retrieve()
            .bodyToMono(Map.class)
            .map(this::mapToUserInfo);
    }

    @Override
    @CircuitBreaker(name = "users-service")
    public Mono<Boolean> existsUser(String userId) {
        return webClient.head()
            .uri("/api/v1/users/{id}", userId)
            .retrieve()
            .toBodilessEntity()
            .map(response -> response.getStatusCode().is2xxSuccessful())
            .onErrorReturn(false);
    }

    // ═══════════════════════════════════════════════════════════════
    // FALLBACKS
    // ═══════════════════════════════════════════════════════════════

    @SuppressWarnings("unused")
    private Mono<UserInfo> getUserByIdFallback(String userId, Throwable t) {
        log.warn("Users service unavailable, using fallback for userId: {}", userId);
        return Mono.empty();
    }

    @SuppressWarnings("unused")
    private Mono<UserInfo> getUserByEmailFallback(String email, Throwable t) {
        log.warn("Users service unavailable, using fallback for email: {}", email);
        return Mono.empty();
    }

    // ═══════════════════════════════════════════════════════════════
    // MAPPERS
    // ═══════════════════════════════════════════════════════════════

    @SuppressWarnings("unchecked")
    private UserInfo mapToUserInfo(Map<?, ?> response) {
        Map<String, Object> data = (Map<String, Object>) response.get("data");
        if (data == null) {
            data = (Map<String, Object>) response;
        }

        return new UserInfo(
            getString(data, "id"),
            getString(data, "organizationId"),
            getString(data, "email"),
            getString(data, "firstName"),
            getString(data, "lastName"),
            getString(data, "role")
        );
    }

    private String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : null;
    }
}
```

---

### 📄 UserEventListener.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.adapters.out.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsauthentication.application.events.external.*;
import pe.edu.vallegrande.vgmsauthentication.application.events.handlers.UserEventHandler;
import reactor.core.scheduler.Schedulers;

/**
 * Listener de eventos de usuario desde RabbitMQ.
 *
 * <p>Escucha eventos publicados por vg-ms-users y los procesa
 * para mantener sincronizado Keycloak.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final UserEventHandler userEventHandler;
    private final ObjectMapper objectMapper;

    // ═══════════════════════════════════════════════════════════════
    // LISTENERS
    // ═══════════════════════════════════════════════════════════════

    @RabbitListener(queues = "${rabbitmq.queues.user-created:auth.user.created}")
    public void handleUserCreated(String message) {
        log.info("Received user.created event");

        try {
            UserCreatedEvent event = objectMapper.readValue(message, UserCreatedEvent.class);
            userEventHandler.handleUserCreated(event)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    v -> log.info("User created event processed: {}", event.getUserId()),
                    error -> log.error("Error processing user created event: {}", error.getMessage())
                );
        } catch (Exception e) {
            log.error("Failed to parse user.created event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.user-updated:auth.user.updated}")
    public void handleUserUpdated(String message) {
        log.info("Received user.updated event");

        try {
            UserUpdatedEvent event = objectMapper.readValue(message, UserUpdatedEvent.class);
            userEventHandler.handleUserUpdated(event)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    v -> log.info("User updated event processed: {}", event.getUserId()),
                    error -> log.error("Error processing user updated event: {}", error.getMessage())
                );
        } catch (Exception e) {
            log.error("Failed to parse user.updated event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.user-deleted:auth.user.deleted}")
    public void handleUserDeleted(String message) {
        log.info("Received user.deleted event");

        try {
            UserDeletedEvent event = objectMapper.readValue(message, UserDeletedEvent.class);
            userEventHandler.handleUserDeleted(event)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    v -> log.info("User deleted event processed: {}", event.getUserId()),
                    error -> log.error("Error processing user deleted event: {}", error.getMessage())
                );
        } catch (Exception e) {
            log.error("Failed to parse user.deleted event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.user-restored:auth.user.restored}")
    public void handleUserRestored(String message) {
        log.info("Received user.restored event");

        try {
            UserRestoredEvent event = objectMapper.readValue(message, UserRestoredEvent.class);
            userEventHandler.handleUserRestored(event)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    v -> log.info("User restored event processed: {}", event.getUserId()),
                    error -> log.error("Error processing user restored event: {}", error.getMessage())
                );
        } catch (Exception e) {
            log.error("Failed to parse user.restored event: {}", e.getMessage());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.user-purged:auth.user.purged}")
    public void handleUserPurged(String message) {
        log.info("Received user.purged event");

        try {
            UserPurgedEvent event = objectMapper.readValue(message, UserPurgedEvent.class);
            userEventHandler.handleUserPurged(event)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    v -> log.info("User purged event processed: {}", event.getUserId()),
                    error -> log.error("Error processing user purged event: {}", error.getMessage())
                );
        } catch (Exception e) {
            log.error("Failed to parse user.purged event: {}", e.getMessage());
        }
    }
}
```

---

## 3️⃣ CONFIG - Configuraciones

### 📄 KeycloakConfig.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Keycloak.
 *
 * <p><b>Propiedades requeridas:</b></p>
 * <pre>
 * keycloak:
 *   auth-server-url: https://lab.vallegrande.edu.pe/jass/keycloak
 *   realm: sistema-jass
 *   admin:
 *     client-id: admin-cli
 *     client-secret: ${KEYCLOAK_ADMIN_SECRET}
 * </pre>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Configuration
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url}")
    private String authServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Bean
    public String keycloakIssuerUri() {
        return authServerUrl + "/realms/" + realm;
    }

    @Bean
    public String keycloakTokenUri() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/token";
    }

    @Bean
    public String keycloakJwksUri() {
        return authServerUrl + "/realms/" + realm + "/protocol/openid-connect/certs";
    }
}
```

---

### 📄 WebClientConfig.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuración de WebClient para comunicación HTTP reactiva.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Configuration
public class WebClientConfig {

    @Value("${webclient.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${webclient.timeout.read:10000}")
    private int readTimeout;

    @Value("${webclient.timeout.write:10000}")
    private int writeTimeout;

    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
            );

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
```

---

### 📄 RabbitMQConfig.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ.
 *
 * <p>Este microservicio SOLO escucha eventos, NO los publica.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.exchange.users:users.exchange}")
    private String usersExchange;

    @Value("${rabbitmq.queues.user-created:auth.user.created}")
    private String userCreatedQueue;

    @Value("${rabbitmq.queues.user-updated:auth.user.updated}")
    private String userUpdatedQueue;

    @Value("${rabbitmq.queues.user-deleted:auth.user.deleted}")
    private String userDeletedQueue;

    @Value("${rabbitmq.queues.user-restored:auth.user.restored}")
    private String userRestoredQueue;

    @Value("${rabbitmq.queues.user-purged:auth.user.purged}")
    private String userPurgedQueue;

    // ═══════════════════════════════════════════════════════════════
    // QUEUES
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(userCreatedQueue)
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "dlq.auth.user")
            .build();
    }

    @Bean
    public Queue userUpdatedQueue() {
        return QueueBuilder.durable(userUpdatedQueue)
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "dlq.auth.user")
            .build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(userDeletedQueue)
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "dlq.auth.user")
            .build();
    }

    @Bean
    public Queue userRestoredQueue() {
        return QueueBuilder.durable(userRestoredQueue)
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "dlq.auth.user")
            .build();
    }

    @Bean
    public Queue userPurgedQueue() {
        return QueueBuilder.durable(userPurgedQueue)
            .withArgument("x-dead-letter-exchange", "dlx.exchange")
            .withArgument("x-dead-letter-routing-key", "dlq.auth.user")
            .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // EXCHANGE
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public TopicExchange usersExchange() {
        return new TopicExchange(usersExchange);
    }

    // ═══════════════════════════════════════════════════════════════
    // BINDINGS
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder.bind(userCreatedQueue())
            .to(usersExchange())
            .with("user.created");
    }

    @Bean
    public Binding userUpdatedBinding() {
        return BindingBuilder.bind(userUpdatedQueue())
            .to(usersExchange())
            .with("user.updated");
    }

    @Bean
    public Binding userDeletedBinding() {
        return BindingBuilder.bind(userDeletedQueue())
            .to(usersExchange())
            .with("user.deleted");
    }

    @Bean
    public Binding userRestoredBinding() {
        return BindingBuilder.bind(userRestoredQueue())
            .to(usersExchange())
            .with("user.restored");
    }

    @Bean
    public Binding userPurgedBinding() {
        return BindingBuilder.bind(userPurgedQueue())
            .to(usersExchange())
            .with("user.purged");
    }

    // ═══════════════════════════════════════════════════════════════
    // CONVERTERS
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
```

---

### 📄 Resilience4jConfig.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de Resilience4j para Circuit Breaker y Retry.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Configuration
public class Resilience4jConfig {

    // ═══════════════════════════════════════════════════════════════
    // KEYCLOAK - Circuit Breaker
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public CircuitBreakerConfig keycloakCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            .build();
    }

    @Bean
    public RetryConfig keycloakRetryConfig() {
        return RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .retryExceptions(
                java.net.ConnectException.class,
                java.net.SocketTimeoutException.class,
                java.io.IOException.class
            )
            .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // USERS SERVICE - Circuit Breaker
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public CircuitBreakerConfig usersServiceCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            .failureRateThreshold(60)
            .waitDurationInOpenState(Duration.ofSeconds(20))
            .slidingWindowSize(5)
            .minimumNumberOfCalls(3)
            .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // TIME LIMITER
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public TimeLimiterConfig defaultTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(10))
            .cancelRunningFuture(true)
            .build();
    }
}
```

---

### 📄 SecurityConfig.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuración de seguridad mínima SIN OAuth2 y SIN CORS.
 *
 * <p><b>IMPORTANTE:</b></p>
 * <ul>
 *   <li>Este microservicio NO valida tokens JWT (lo hace el Gateway)</li>
 *   <li>CORS se configura ÚNICAMENTE en el Gateway</li>
 *   <li>La comunicación Gateway → MS es interna (red Docker)</li>
 *   <li>CSRF deshabilitado (API REST stateless)</li>
 * </ul>
 *
 * <p><b>¿Por qué no hay CORS aquí?</b></p>
 * <p>El frontend (Angular/React) solo habla con el Gateway.
 * El Gateway maneja CORS y reenvía las peticiones a este MS.
 * La comunicación interna no necesita CORS.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            // ═══════════════════════════════════════════════════════════════
            // NO CORS - Se configura en el Gateway, no aquí
            // ═══════════════════════════════════════════════════════════════
            .cors(ServerHttpSecurity.CorsSpec::disable)

            // CSRF deshabilitado - API REST stateless
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            // Todos los endpoints son públicos desde este MS
            // El Gateway ya validó el token antes de llegar aquí
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/api/v1/auth/**").permitAll()
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                .anyExchange().permitAll()
            )

            // Sin formularios ni HTTP Basic
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .build();
    }
}
```

---

## 4️⃣ SECURITY - Contexto de Seguridad

### 📄 AuthenticatedUser.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.security;

import lombok.Builder;
import lombok.Getter;

import java.util.Set;

/**
 * Representa un usuario autenticado extraído de los headers del Gateway.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Getter
@Builder
public class AuthenticatedUser {

    private final String userId;
    private final String email;
    private final String organizationId;
    private final Set<String> roles;

    /**
     * Verifica si el usuario tiene un rol específico.
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }

    /**
     * Verifica si el usuario tiene alguno de los roles especificados.
     */
    public boolean hasAnyRole(String... requiredRoles) {
        if (roles == null) return false;
        for (String role : requiredRoles) {
            if (roles.contains(role)) return true;
        }
        return false;
    }

    /**
     * Verifica si el usuario es administrador.
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN") || hasRole("ADMIN");
    }
}
```

---

### 📄 GatewayHeadersExtractor.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.security;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Extrae información de autenticación de los headers inyectados por el Gateway.
 *
 * <p><b>Headers esperados del Gateway:</b></p>
 * <ul>
 *   <li>X-User-Id: ID del usuario</li>
 *   <li>X-User-Email: Email del usuario</li>
 *   <li>X-User-Roles: Roles separados por coma</li>
 *   <li>X-Organization-Id: ID de la organización</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Component
public class GatewayHeadersExtractor {

    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_EMAIL = "X-User-Email";
    public static final String HEADER_USER_ROLES = "X-User-Roles";
    public static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";

    /**
     * Extrae el usuario autenticado de los headers.
     */
    public AuthenticatedUser extract(ServerHttpRequest request) {
        String userId = getHeader(request, HEADER_USER_ID);
        String email = getHeader(request, HEADER_USER_EMAIL);
        String organizationId = getHeader(request, HEADER_ORGANIZATION_ID);
        Set<String> roles = parseRoles(getHeader(request, HEADER_USER_ROLES));

        if (userId == null) {
            return null;
        }

        return AuthenticatedUser.builder()
            .userId(userId)
            .email(email)
            .organizationId(organizationId)
            .roles(roles)
            .build();
    }

    /**
     * Verifica si la petición está autenticada.
     */
    public boolean isAuthenticated(ServerHttpRequest request) {
        return getHeader(request, HEADER_USER_ID) != null;
    }

    private String getHeader(ServerHttpRequest request, String headerName) {
        return request.getHeaders().getFirst(headerName);
    }

    private Set<String> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return Collections.emptySet();
        }
        return Arrays.stream(rolesHeader.split(","))
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .collect(Collectors.toSet());
    }
}
```

---

### 📄 GatewayHeadersFilter.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * Filtro que procesa los headers del Gateway y los almacena en el contexto.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayHeadersFilter implements WebFilter {

    private final GatewayHeadersExtractor headersExtractor;

    public static final String AUTHENTICATED_USER_KEY = "authenticatedUser";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        AuthenticatedUser user = headersExtractor.extract(exchange.getRequest());

        if (user != null) {
            log.debug("Authenticated request from user: {}", user.getUserId());
            return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(AUTHENTICATED_USER_KEY, user));
        }

        return chain.filter(exchange);
    }
}
```

---

### 📄 SecurityContextAdapter.java

```java
package pe.edu.vallegrande.vgmsauthentication.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsauthentication.domain.ports.out.ISecurityContext;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Set;

/**
 * Adaptador que implementa ISecurityContext usando el contexto reactivo.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Component
public class SecurityContextAdapter implements ISecurityContext {

    @Override
    public Mono<String> getCurrentUserId() {
        return getAuthenticatedUser()
            .map(AuthenticatedUser::getUserId);
    }

    @Override
    public Mono<String> getCurrentOrganizationId() {
        return getAuthenticatedUser()
            .map(AuthenticatedUser::getOrganizationId);
    }

    @Override
    public Mono<Set<String>> getCurrentUserRoles() {
        return getAuthenticatedUser()
            .map(AuthenticatedUser::getRoles)
            .defaultIfEmpty(Collections.emptySet());
    }

    @Override
    public Mono<Boolean> isAuthenticated() {
        return getAuthenticatedUser()
            .map(user -> true)
            .defaultIfEmpty(false);
    }

    @Override
    public Mono<String> getCurrentUserEmail() {
        return getAuthenticatedUser()
            .map(AuthenticatedUser::getEmail);
    }

    private Mono<AuthenticatedUser> getAuthenticatedUser() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(GatewayHeadersFilter.AUTHENTICATED_USER_KEY)) {
                return Mono.just(ctx.get(GatewayHeadersFilter.AUTHENTICATED_USER_KEY));
            }
            return Mono.empty();
        });
    }
}
```

---

## 📋 Resumen de Endpoints

| Método | Endpoint | Descripción | Auth |
|--------|----------|-------------|------|
| POST | `/api/v1/auth/login` | Login con credenciales | No |
| POST | `/api/v1/auth/refresh` | Refrescar token | No |
| POST | `/api/v1/auth/logout` | Cerrar sesión | No |
| POST | `/api/v1/auth/logout/token` | Logout con access token | Bearer |
| GET | `/api/v1/auth/validate` | Validar token | Bearer |
| GET | `/api/v1/auth/userinfo` | Información del usuario | Bearer |
| POST | `/api/v1/auth/introspect` | Introspección RFC 7662 | No |
| GET | `/api/v1/auth/health` | Health check | No |

---

## 📋 Properties (application.yml)

```yaml
spring:
  application:
    name: vg-ms-authentication
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

# Keycloak Configuration
keycloak:
  auth-server-url: ${KEYCLOAK_URL:http://localhost:9090}
  realm: ${KEYCLOAK_REALM:sistema-jass}
  admin:
    client-id: ${KEYCLOAK_ADMIN_CLIENT_ID:admin-cli}
    client-secret: ${KEYCLOAK_ADMIN_CLIENT_SECRET:}

# Services URLs
services:
  users:
    url: ${USERS_SERVICE_URL:http://vg-ms-users:8081}

# RabbitMQ
spring.rabbitmq:
  host: ${RABBITMQ_HOST:localhost}
  port: ${RABBITMQ_PORT:5672}
  username: ${RABBITMQ_USER:guest}
  password: ${RABBITMQ_PASS:guest}

rabbitmq:
  exchange:
    users: users.exchange
  queues:
    user-created: auth.user.created
    user-updated: auth.user.updated
    user-deleted: auth.user.deleted
    user-restored: auth.user.restored
    user-purged: auth.user.purged

# WebClient Timeouts
webclient:
  timeout:
    connect: 5000
    read: 10000
    write: 10000

# Resilience4j
resilience4j:
  circuitbreaker:
    instances:
      keycloak:
        failureRateThreshold: 50
        waitDurationInOpenState: 30s
        slidingWindowSize: 10
      keycloak-admin:
        failureRateThreshold: 50
        waitDurationInOpenState: 60s
        slidingWindowSize: 5
      users-service:
        failureRateThreshold: 60
        waitDurationInOpenState: 20s
  retry:
    instances:
      keycloak:
        maxAttempts: 3
        waitDuration: 500ms
      users-service:
        maxAttempts: 2
        waitDuration: 300ms

# Server
server:
  port: ${PORT:8082}

# Eureka
eureka:
  client:
    service-url:
      defaultZone: ${EUREKA_URL:http://localhost:8761/eureka}
  instance:
    prefer-ip-address: true
```

---

## 🔑 Diagrama de Comunicación

### Arquitectura Completa

```
                                  ┌─────────────────────────────────────────┐
                                  │            EXTERNAL CLIENTS              │
                                  │  (Web App, Mobile App, Postman, etc.)   │
                                  └───────────────────┬─────────────────────┘
                                                      │
                                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              vg-ms-gateway (8080)                           │
│  ┌───────────────────────────────────────────────────────────────────────┐  │
│  │  • Validates JWT tokens using JWKS from Keycloak                      │  │
│  │  • Injects headers: X-User-Id, X-User-Roles, X-Organization-Id        │  │
│  │  • Routes requests to microservices                                   │  │
│  └───────────────────────────────────────────────────────────────────────┘  │
└───────────────────────────────────────┬─────────────────────────────────────┘
                                        │
            ┌───────────────────────────┼───────────────────────────┐
            │                           │                           │
            ▼                           ▼                           ▼
┌───────────────────┐     ┌───────────────────┐     ┌───────────────────┐
│ vg-ms-authentication│     │   vg-ms-users     │     │  Other Services   │
│     (8082)        │     │     (8081)        │     │  (8083, 8084...)  │
├───────────────────┤     ├───────────────────┤     ├───────────────────┤
│ • Login           │     │ • CRUD Users      │     │ • Business Logic  │
│ • Refresh         │     │ • Publish Events  │     │ • Read X-Headers  │
│ • Logout          │     │                   │     │                   │
│ • Validate        │     │                   │     │                   │
└─────────┬─────────┘     └─────────┬─────────┘     └───────────────────┘
          │                         │
          │                         │
          ▼                         ▼
┌───────────────────┐     ┌───────────────────┐
│    Keycloak       │     │    RabbitMQ       │
│     (9090)        │     │     (5672)        │
├───────────────────┤     ├───────────────────┤
│ • Token Endpoint  │     │ • users.exchange  │
│ • UserInfo        │◄────│ • Routing Keys:   │
│ • Admin API       │     │   - user.created  │
│ • JWKS            │     │   - user.deleted  │
└───────────────────┘     │   - user.restored │
                          │   - user.purged   │
                          └───────────────────┘
```

### Flujo de Comunicación Entre Servicios

```
┌─────────────────┐      ┌─────────────────────┐      ┌───────────────┐
│   vg-ms-users   │      │ vg-ms-authentication│      │   Keycloak    │
│   (port 8081)   │      │    (port 8082)      │      │  (port 9090)  │
└────────┬────────┘      └──────────┬──────────┘      └───────┬───────┘
         │                          │                         │
         │   RabbitMQ Events        │                         │
         │ ────────────────────────>│                         │
         │   (user.created, etc)    │                         │
         │                          │                         │
         │                          │  POST /token            │
         │                          │ ───────────────────────>│
         │                          │                         │
         │                          │  GET /userinfo          │
         │   GET /api/v1/users      │ ───────────────────────>│
         │<─────────────────────────│                         │
         │   (enrich login info)    │                         │
         │                          │  Admin API              │
         │                          │ ───────────────────────>│
         │                          │  (create/update user)   │
         │                          │                         │
```

### Diagrama de Capas de Infraestructura

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                         🔧 INFRASTRUCTURE LAYER                              │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                         ADAPTERS IN (REST)                              │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  AuthRest.java                                                  │  │ │
│  │  │    POST /api/v1/auth/login                                      │  │ │
│  │  │    POST /api/v1/auth/refresh                                    │  │ │
│  │  │    POST /api/v1/auth/logout                                     │  │ │
│  │  │    GET  /api/v1/auth/validate                                   │  │ │
│  │  │    GET  /api/v1/auth/userinfo                                   │  │ │
│  │  │    POST /api/v1/auth/introspect                                 │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  │  ┌─────────────────────────────────────────────────────────────────┐  │ │
│  │  │  GlobalExceptionHandler.java                                    │  │ │
│  │  │    - DomainException → HTTP Response                            │  │ │
│  │  │    - InvalidCredentialsException → 401                          │  │ │
│  │  │    - KeycloakException → 503                                    │  │ │
│  │  └─────────────────────────────────────────────────────────────────┘  │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                       ADAPTERS OUT (EXTERNAL)                           │ │
│  │  ┌──────────────────────┐  ┌──────────────────────────────────────┐   │ │
│  │  │  KeycloakClientImpl  │  │  UserServiceClientImpl               │   │ │
│  │  ├──────────────────────┤  ├──────────────────────────────────────┤   │ │
│  │  │ • getTokenWithPassword│ │ • getUserById(id)                    │   │ │
│  │  │ • refreshToken       │  │ • getUserByEmail(email)              │   │ │
│  │  │ • revokeToken        │  │ • existsUser(id)                     │   │ │
│  │  │ • introspectToken    │  └──────────────────────────────────────┘   │ │
│  │  │ • getUserInfo        │                                             │ │
│  │  │ • createUser         │  ┌──────────────────────────────────────┐   │ │
│  │  │ • updateUser         │  │  UserEventListener                   │   │ │
│  │  │ • disableUser        │  ├──────────────────────────────────────┤   │ │
│  │  │ • enableUser         │  │ @RabbitListener(queues)              │   │ │
│  │  │ • deleteUser         │  │ • handleUserCreated                  │   │ │
│  │  │ • assignRole         │  │ • handleUserUpdated                  │   │ │
│  │  │ • removeRole         │  │ • handleUserDeleted                  │   │ │
│  │  │ • resetPassword      │  │ • handleUserRestored                 │   │ │
│  │  └──────────────────────┘  │ • handleUserPurged                   │   │ │
│  │                            └──────────────────────────────────────┘   │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                           SECURITY                                      │ │
│  │  ┌──────────────────────────────────────────────────────────────────┐ │ │
│  │  │  GatewayHeadersFilter → GatewayHeadersExtractor                  │ │ │
│  │  │    │                                                             │ │ │
│  │  │    ▼                                                             │ │ │
│  │  │  AuthenticatedUser → SecurityContextAdapter (ISecurityContext)   │ │ │
│  │  │                                                                  │ │ │
│  │  │  Headers esperados del Gateway:                                  │ │ │
│  │  │    • X-User-Id: UUID del usuario                                 │ │ │
│  │  │    • X-User-Email: email@example.com                             │ │ │
│  │  │    • X-User-Roles: ADMIN,USER                                    │ │ │
│  │  │    • X-Organization-Id: UUID de organización                     │ │ │
│  │  └──────────────────────────────────────────────────────────────────┘ │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
│  ┌────────────────────────────────────────────────────────────────────────┐ │
│  │                          CONFIG                                         │ │
│  │  ┌────────────────────┐ ┌─────────────────┐ ┌────────────────────────┐ │ │
│  │  │ KeycloakConfig     │ │ WebClientConfig │ │ RabbitMQConfig         │ │ │
│  │  │   - authServerUrl  │ │   - timeouts    │ │   - exchange           │ │ │
│  │  │   - realm          │ │   - @LoadBalanced│ │  - queues             │ │ │
│  │  │   - adminCredentials│ └─────────────────┘ │   - bindings           │ │ │
│  │  └────────────────────┘                     └────────────────────────┘ │ │
│  │  ┌────────────────────┐ ┌───────────────────┐                         │ │
│  │  │ SecurityConfig     │ │ Resilience4jConfig│  📝 Swagger detecta     │ │
│  │  │   - SIN OAuth2     │ │   - circuitBreaker│     endpoints auto      │ │
│  │  │   - permitAll()    │ │   - retry         │     (no OpenApiConfig)  │ │
│  │  │   - NO CORS        │ │   - timeLimiter   │                         │ │
│  │  └────────────────────┘ └───────────────────┘                         │ │
│  └────────────────────────────────────────────────────────────────────────┘ │
│                                                                             │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 9️⃣ CONFIGURACIÓN YAML - Archivos de Propiedades

### 📄 application.yml (Base - Configuración común)

```yaml
# ═══════════════════════════════════════════════════════════════════════════
# CONFIGURACIÓN BASE - vg-ms-authentication
# ═══════════════════════════════════════════════════════════════════════════
# Este archivo contiene configuración común para todos los perfiles.
# Las propiedades específicas de entorno se definen en application-{profile}.yml

server:
  port: 8082

spring:
  application:
    name: vg-ms-authentication

  # ─────────────────────────────────────────────────────────────────────────
  # JACKSON - Serialización JSON
  # ─────────────────────────────────────────────────────────────────────────
  jackson:
    serialization:
      write-dates-as-timestamps: false
      indent-output: false
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null
    date-format: "yyyy-MM-dd'T'HH:mm:ss.SSSZ"

  # ─────────────────────────────────────────────────────────────────────────
  # WEBFLUX
  # ─────────────────────────────────────────────────────────────────────────
  webflux:
    base-path: /api/v1

# ═══════════════════════════════════════════════════════════════════════════
# ACTUATOR - Health Checks & Métricas
# ═══════════════════════════════════════════════════════════════════════════
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers,circuitbreakerevents
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      show-components: when-authorized
  health:
    circuitbreakers:
      enabled: true

# ═══════════════════════════════════════════════════════════════════════════
# SPRINGDOC - OpenAPI/Swagger
# ═══════════════════════════════════════════════════════════════════════════
springdoc:
     api-docs:
          path: /v3/api-docs
          enabled: true
     swagger-ui:
          path: /swagger-ui.html
          enabled: true
          try-it-out-enabled: true
          operations-sorter: method

# ═══════════════════════════════════════════════════════════════════════════
# RESILIENCE4J - Circuit Breaker (Configuración común)
# ═══════════════════════════════════════════════════════════════════════════
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-type: COUNT_BASED
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        automatic-transition-from-open-to-half-open-enabled: true
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 50
    instances:
      keycloakService:
        base-config: default
        wait-duration-in-open-state: 15s
      usersService:
        base-config: default
        wait-duration-in-open-state: 10s

  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
    instances:
      keycloakService:
        base-config: default
        max-attempts: 3
      usersService:
        base-config: default
        max-attempts: 2

  timelimiter:
    configs:
      default:
        timeout-duration: 5s
        cancel-running-future: true
    instances:
      keycloakService:
        base-config: default
        timeout-duration: 10s
      usersService:
        base-config: default
        timeout-duration: 5s

# ═══════════════════════════════════════════════════════════════════════════
# LOGGING (Configuración base)
# ═══════════════════════════════════════════════════════════════════════════
logging:
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  level:
    root: INFO
    pe.edu.vallegrande: INFO
    org.springframework.web: INFO
    org.springframework.security: INFO
```

---

### 📄 application-dev.yml (Desarrollo Local con Docker)

```yaml
# ═══════════════════════════════════════════════════════════════════════════
# CONFIGURACIÓN DEV - Docker Local
# ═══════════════════════════════════════════════════════════════════════════
# Perfil: dev
# Ejecutar con: ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
# O en Docker: SPRING_PROFILES_ACTIVE=dev

spring:
  config:
    activate:
      on-profile: dev

# ═══════════════════════════════════════════════════════════════════════════
# KEYCLOAK - Configuración Local
# ═══════════════════════════════════════════════════════════════════════════
keycloak:
  auth-server-url: http://localhost:9090
  realm: sistema-jass
  client-id: jass-backend
  client-secret: ${KEYCLOAK_CLIENT_SECRET:your-client-secret-here}
  admin:
    username: admin
    password: ${KEYCLOAK_ADMIN_PASSWORD:admin}
  # Endpoints OAuth2/OIDC (calculados desde auth-server-url + realm)
  token-endpoint: ${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/token
  userinfo-endpoint: ${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/userinfo
  logout-endpoint: ${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/logout
  introspect-endpoint: ${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/token/introspect
  jwks-uri: ${keycloak.auth-server-url}/realms/${keycloak.realm}/protocol/openid-connect/certs
  admin-api-url: ${keycloak.auth-server-url}/admin/realms/${keycloak.realm}

# ═══════════════════════════════════════════════════════════════════════════
# RABBITMQ - Local
# ═══════════════════════════════════════════════════════════════════════════
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated

# ═══════════════════════════════════════════════════════════════════════════
# SERVICIOS EXTERNOS - URLs Locales
# ═══════════════════════════════════════════════════════════════════════════
services:
  users:
    url: http://localhost:8081
    timeout: 5000

# ═══════════════════════════════════════════════════════════════════════════
# LOGGING - Más detallado en desarrollo
# ═══════════════════════════════════════════════════════════════════════════
logging:
  level:
    root: INFO
    pe.edu.vallegrande: DEBUG
    org.springframework.web: DEBUG
    org.springframework.security: DEBUG
    org.keycloak: DEBUG
    io.github.resilience4j: DEBUG
    org.springframework.amqp: DEBUG

# ═══════════════════════════════════════════════════════════════════════════
# SPRINGDOC - Habilitado en dev
# ═══════════════════════════════════════════════════════════════════════════
springdoc:
  swagger-ui:
    enabled: true
    try-it-out-enabled: true
```

---

### 📄 application-prod.yml (Producción - Valle Grande)

```yaml
# ═══════════════════════════════════════════════════════════════════════════
# CONFIGURACIÓN PROD - Valle Grande Cloud
# ═══════════════════════════════════════════════════════════════════════════
# Perfil: prod
# URL Keycloak: https://lab.vallegrande.edu.pe/jass/keycloak
# Realm: sistema-jass

spring:
  config:
    activate:
      on-profile: prod

# ═══════════════════════════════════════════════════════════════════════════
# KEYCLOAK - Producción Valle Grande
# ═══════════════════════════════════════════════════════════════════════════
keycloak:
  auth-server-url: https://lab.vallegrande.edu.pe/jass/keycloak
  realm: sistema-jass
  client-id: jass-backend
  client-secret: ${KEYCLOAK_CLIENT_SECRET}
  admin:
    username: ${KEYCLOAK_ADMIN_USERNAME:admin}
    password: ${KEYCLOAK_ADMIN_PASSWORD}
  # Endpoints OAuth2/OIDC (producción)
  token-endpoint: https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/token
  userinfo-endpoint: https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/userinfo
  logout-endpoint: https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/logout
  introspect-endpoint: https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/token/introspect
  jwks-uri: https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/certs
  revocation-endpoint: https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/revoke
  admin-api-url: https://lab.vallegrande.edu.pe/jass/keycloak/admin/realms/sistema-jass

# ═══════════════════════════════════════════════════════════════════════════
# RABBITMQ - Producción
# ═══════════════════════════════════════════════════════════════════════════
spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:rabbitmq}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    publisher-confirm-type: correlated
    publisher-returns: true

# ═══════════════════════════════════════════════════════════════════════════
# SERVICIOS EXTERNOS - URLs de Producción
# ═══════════════════════════════════════════════════════════════════════════
services:
  users:
    url: ${USERS_SERVICE_URL:http://vg-ms-users:8081}
    timeout: 10000

# ═══════════════════════════════════════════════════════════════════════════
# LOGGING - Menos verboso en producción
# ═══════════════════════════════════════════════════════════════════════════
logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
    org.springframework.web: WARN
    org.springframework.security: WARN
    org.keycloak: WARN
    io.github.resilience4j: INFO

# ═══════════════════════════════════════════════════════════════════════════
# SPRINGDOC - Deshabilitado en producción (o habilitado con auth)
# ═══════════════════════════════════════════════════════════════════════════
springdoc:
  swagger-ui:
    enabled: false
  api-docs:
    enabled: false

# ═══════════════════════════════════════════════════════════════════════════
# RESILIENCE4J - Ajustes de producción (más tolerante)
# ═══════════════════════════════════════════════════════════════════════════
resilience4j:
  circuitbreaker:
    instances:
      keycloakService:
        wait-duration-in-open-state: 30s
        sliding-window-size: 20
      usersService:
        wait-duration-in-open-state: 20s
        sliding-window-size: 15

  timelimiter:
    instances:
      keycloakService:
        timeout-duration: 15s
      usersService:
        timeout-duration: 10s
```

---

## 🔟 RESUMEN DE ENDPOINTS KEYCLOAK

### DEV (Local Docker)

| Endpoint | URL |
|----------|-----|
| **Base URL** | `http://localhost:9090` |
| **Realm** | `sistema-jass` |
| **Token** | `http://localhost:9090/realms/sistema-jass/protocol/openid-connect/token` |
| **Userinfo** | `http://localhost:9090/realms/sistema-jass/protocol/openid-connect/userinfo` |
| **Logout** | `http://localhost:9090/realms/sistema-jass/protocol/openid-connect/logout` |
| **Introspect** | `http://localhost:9090/realms/sistema-jass/protocol/openid-connect/token/introspect` |
| **JWKS** | `http://localhost:9090/realms/sistema-jass/protocol/openid-connect/certs` |
| **Admin API** | `http://localhost:9090/admin/realms/sistema-jass` |

### PROD (Valle Grande)

| Endpoint | URL |
|----------|-----|
| **Base URL** | `https://lab.vallegrande.edu.pe/jass/keycloak` |
| **Realm** | `sistema-jass` |
| **Token** | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/token` |
| **Userinfo** | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/userinfo` |
| **Logout** | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/logout` |
| **Introspect** | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/token/introspect` |
| **JWKS** | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/certs` |
| **Revocation** | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/revoke` |
| **Admin API** | `https://lab.vallegrande.edu.pe/jass/keycloak/admin/realms/sistema-jass` |

---

## 1️⃣1️⃣ VARIABLES DE ENTORNO

### Variables requeridas en producción

```bash
# Keycloak
KEYCLOAK_CLIENT_SECRET=your-client-secret
KEYCLOAK_ADMIN_USERNAME=admin
KEYCLOAK_ADMIN_PASSWORD=your-secure-password

# RabbitMQ
RABBITMQ_HOST=rabbitmq
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=your-user
RABBITMQ_PASSWORD=your-password
RABBITMQ_VHOST=/

# Servicios
USERS_SERVICE_URL=http://vg-ms-users:8081

# Spring
SPRING_PROFILES_ACTIVE=prod
```

### Ejemplo docker-compose para desarrollo

```yaml
version: "3.9"

services:
  vg-ms-authentication:
    build: .
    container_name: vg-ms-authentication
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      KEYCLOAK_CLIENT_SECRET: your-client-secret
    depends_on:
      - keycloak
      - rabbitmq
    networks:
      - jass-network

  keycloak:
    image: quay.io/keycloak/keycloak:26.0.8
    container_name: keycloak
    ports:
      - "9090:8080"
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: keycloak123!
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_HTTP_ENABLED: "true"
      KC_JAVA_OPTS: "-Xms128m -Xmx512m"
    command: ["start"]
    depends_on:
      - postgres
    networks:
      - jass-network

  postgres:
    image: postgres:15-alpine
    container_name: keycloak-db
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: keycloak123!
    volumes:
      - keycloak_pgdata:/var/lib/postgresql/data
    networks:
      - jass-network

  rabbitmq:
    image: rabbitmq:3.12-management-alpine
    container_name: rabbitmq
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
    networks:
      - jass-network

volumes:
  keycloak_pgdata:

networks:
  jass-network:
    driver: bridge
```
