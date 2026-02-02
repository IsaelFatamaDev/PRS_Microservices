# 📥 INFRASTRUCTURE LAYER - Capa de Infraestructura

> **Adaptadores externos. Implementa los contratos del dominio.**

## 📋 Principios

1. **Implementación de Puertos**: Cada adapter implementa una interfaz del dominio
2. **Detalles Técnicos**: Aquí vive la tecnología (R2DBC, WebClient, RabbitMQ)
3. **Configuración**: Beans de Spring, propiedades, etc.
4. **No Contiene Lógica de Negocio**: Solo traduce entre el mundo externo y el dominio

---

## 📂 Estructura

```
infrastructure/
├── adapters/
│   ├── in/rest/                     → Controladores REST (entrada)
│   │   ├── UserRest.java
│   │   └── GlobalExceptionHandler.java
│   └── out/
│       ├── persistence/             → Implementación repositorio
│       │   └── UserRepositoryImpl.java
│       ├── external/                → Clientes WebClient
│       │   ├── AuthenticationClientImpl.java
│       │   ├── OrganizationClientImpl.java
│       │   └── NotificationClientImpl.java
│       └── messaging/               → Publisher RabbitMQ
│           └── UserEventPublisherImpl.java
├── persistence/
│   ├── entities/
│   │   └── UserEntity.java
│   └── repositories/
│       └── UserR2dbcRepository.java
└── config/
    ├── R2dbcConfig.java           → Configuración R2DBC/Pool
    ├── WebClientConfig.java        → WebClient con Circuit Breaker
    ├── RabbitMQConfig.java         → Exchange y RabbitTemplate
    ├── Resilience4jConfig.java     → Circuit Breaker, Retry, TimeLimiter
    ├── SecurityConfig.java         → Seguridad WebFlux
    └── RequestContextFilter.java   → Propagación de headers
```

---

## 1️⃣ ADAPTERS IN - Controladores REST

### 📄 UserRest.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsusers.application.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.request.UpdateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.application.mappers.UserMapper;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.*;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Controlador REST para gestión de usuarios.
 *
 * <p>Expone los endpoints CRUD + operaciones especiales (restore, purge).</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "API para gestión de usuarios")
public class UserRest {

    private final ICreateUserUseCase createUserUseCase;
    private final IGetUserUseCase getUserUseCase;
    private final IUpdateUserUseCase updateUserUseCase;
    private final IDeleteUserUseCase deleteUserUseCase;
    private final IRestoreUserUseCase restoreUserUseCase;
    private final IPurgeUserUseCase purgeUserUseCase;
    private final UserMapper userMapper;

    // Constantes para mensajes
    private static final String USER_CREATED = "Usuario creado exitosamente";
    private static final String USER_FOUND = "Usuario obtenido exitosamente";
    private static final String USERS_FOUND = "Usuarios obtenidos exitosamente";
    private static final String USER_UPDATED = "Usuario actualizado exitosamente";
    private static final String USER_DELETED = "Usuario eliminado exitosamente";
    private static final String USER_RESTORED = "Usuario restaurado exitosamente";
    private static final String USER_PURGED = "Usuario eliminado permanentemente";

    // ═══════════════════════════════════════════════════════════════
    // CREATE
    // ═══════════════════════════════════════════════════════════════

    @PostMapping
    @Operation(
        summary = "Crear usuario",
        description = "Crea un nuevo usuario en el sistema"
    )
    @ApiResponses(value = {
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201",
            description = "Usuario creado exitosamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description = "Datos de entrada inválidos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409",
            description = "Ya existe un usuario con ese documento"
        )
    })
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> createUser(
            @Valid @RequestBody CreateUserRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        log.info("POST /users - Creating user with document: {}", request.getDocumentNumber());

        return createUserUseCase.execute(userMapper.toModel(request), userId)
            .map(user -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(userMapper.toResponse(user), USER_CREATED))
            );
    }

    // ═══════════════════════════════════════════════════════════════
    // READ
    // ═══════════════════════════════════════════════════════════════

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por ID")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserById(
            @Parameter(description = "ID del usuario")
            @PathVariable String id
    ) {
        log.info("GET /users/{}", id);

        return getUserUseCase.findById(id)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(userMapper.toResponse(user), USER_FOUND)
            ));
    }

    @GetMapping("/document/{documentNumber}")
    @Operation(summary = "Obtener usuario por número de documento")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserByDocument(
            @Parameter(description = "Número de documento")
            @PathVariable String documentNumber
    ) {
        log.info("GET /users/document/{}", documentNumber);

        return getUserUseCase.findByDocumentNumber(documentNumber)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(userMapper.toResponse(user), USER_FOUND)
            ));
    }

    @GetMapping
    @Operation(summary = "Listar usuarios activos")
    public Mono<ResponseEntity<ApiResponse<List<UserResponse>>>> getAllActiveUsers() {
        log.info("GET /users - Active users only");

        return getUserUseCase.findAllActive()
            .map(userMapper::toResponse)
            .collectList()
            .map(users -> ResponseEntity.ok(
                ApiResponse.success(users, USERS_FOUND)
            ));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los usuarios (incluye inactivos)")
    public Mono<ResponseEntity<ApiResponse<List<UserResponse>>>> getAllUsers() {
        log.info("GET /users/all - Including inactive");

        return getUserUseCase.findAll()
            .map(userMapper::toResponse)
            .collectList()
            .map(users -> ResponseEntity.ok(
                ApiResponse.success(users, USERS_FOUND)
            ));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar usuarios por organización")
    public Mono<ResponseEntity<ApiResponse<List<UserResponse>>>> getUsersByOrganization(
            @Parameter(description = "ID de la organización")
            @PathVariable String organizationId,
            @Parameter(description = "Incluir inactivos")
            @RequestParam(defaultValue = "false") boolean includeInactive
    ) {
        log.info("GET /users/organization/{} - includeInactive: {}", organizationId, includeInactive);

        var flux = includeInactive
            ? getUserUseCase.findByOrganizationId(organizationId)
            : getUserUseCase.findActiveByOrganizationId(organizationId);

        return flux
            .map(userMapper::toResponse)
            .collectList()
            .map(users -> ResponseEntity.ok(
                ApiResponse.success(users, USERS_FOUND)
            ));
    }

    // ═══════════════════════════════════════════════════════════════
    // UPDATE
    // ═══════════════════════════════════════════════════════════════

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> updateUser(
            @Parameter(description = "ID del usuario")
            @PathVariable String id,
            @Valid @RequestBody UpdateUserRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        log.info("PUT /users/{}", id);

        return updateUserUseCase.execute(id, userMapper.toModel(request), userId)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(userMapper.toResponse(user), USER_UPDATED)
            ));
    }

    // ═══════════════════════════════════════════════════════════════
    // DELETE (Soft Delete)
    // ═══════════════════════════════════════════════════════════════

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar usuario (soft delete)",
        description = "Marca el usuario como inactivo. Puede ser restaurado."
    )
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> deleteUser(
            @Parameter(description = "ID del usuario")
            @PathVariable String id,
            @Parameter(description = "Razón de eliminación")
            @RequestParam(required = false) String reason,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        log.info("DELETE /users/{} - Reason: {}", id, reason);

        return deleteUserUseCase.execute(id, userId, reason)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(userMapper.toResponse(user), USER_DELETED)
            ));
    }

    // ═══════════════════════════════════════════════════════════════
    // RESTORE
    // ═══════════════════════════════════════════════════════════════

    @PatchMapping("/{id}/restore")
    @Operation(
        summary = "Restaurar usuario",
        description = "Reactiva un usuario previamente eliminado (soft delete)"
    )
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> restoreUser(
            @Parameter(description = "ID del usuario")
            @PathVariable String id,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        log.info("PATCH /users/{}/restore", id);

        return restoreUserUseCase.execute(id, userId)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(userMapper.toResponse(user), USER_RESTORED)
            ));
    }

    // ═══════════════════════════════════════════════════════════════
    // PURGE (Hard Delete)
    // ═══════════════════════════════════════════════════════════════

    @DeleteMapping("/{id}/purge")
    @Operation(
        summary = "Eliminar usuario permanentemente (hard delete)",
        description = "⚠️ IRREVERSIBLE - Elimina el usuario de la base de datos"
    )
    public Mono<ResponseEntity<ApiResponse<Void>>> purgeUser(
            @Parameter(description = "ID del usuario")
            @PathVariable String id,
            @Parameter(description = "Razón de eliminación (requerida)")
            @RequestParam String reason,
            @RequestHeader(value = "X-User-Id", defaultValue = "system") String userId
    ) {
        log.warn("DELETE /users/{}/purge - Reason: {}", id, reason);

        return purgeUserUseCase.execute(id, userId, reason)
            .then(Mono.just(ResponseEntity.ok(
                ApiResponse.<Void>success(USER_PURGED)
            )));
    }
}
```

---

### 📄 GlobalExceptionHandler.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.in.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.vgmsusers.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsusers.application.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.DomainException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.ValidationException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Manejador global de excepciones.
 *
 * <p>Convierte excepciones de dominio y técnicas en respuestas HTTP estándar.</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ═══════════════════════════════════════════════════════════════
    // EXCEPCIONES DE DOMINIO
    // ═══════════════════════════════════════════════════════════════

    /**
     * Maneja todas las excepciones de dominio.
     */
    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleDomainException(DomainException ex) {
        log.error("Domain exception: {} - Code: {}", ex.getMessage(), ex.getErrorCode());

        ErrorMessage error = ErrorMessage.of(
            ex.getMessage(),
            ex.getErrorCode(),
            ex.getHttpStatus()
        );

        return Mono.just(
            ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getMessage(), error))
        );
    }

    /**
     * Maneja excepciones de validación con campo específico.
     */
    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationException(ValidationException ex) {
        log.error("Validation exception: {} - Field: {}", ex.getMessage(), ex.getField());

        ErrorMessage error = ErrorMessage.validation(
            ex.getField(),
            ex.getMessage(),
            ex.getErrorCode()
        );

        return Mono.just(
            ResponseEntity.badRequest()
                .body(ApiResponse.error("Error de validación", error))
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // VALIDACIONES (@Valid)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Maneja errores de validación de Jakarta Bean Validation.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationErrors(WebExchangeBindException ex) {
        log.error("Validation errors: {}", ex.getBindingResult().getErrorCount());

        List<ErrorMessage> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> ErrorMessage.validation(
                error.getField(),
                error.getDefaultMessage(),
                "VALIDATION_ERROR"
            ))
            .toList();

        return Mono.just(
            ResponseEntity.badRequest()
                .body(ApiResponse.error("Errores de validación", errors))
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // ERRORES DE SERVICIOS EXTERNOS (WebClient)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Maneja errores de comunicación con servicios externos.
     */
    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleWebClientException(WebClientResponseException ex) {
        log.error("External service error: {} - Status: {}", ex.getMessage(), ex.getStatusCode());

        ErrorMessage error = ErrorMessage.of(
            "Error de comunicación con servicio externo",
            "EXTERNAL_SERVICE_ERROR",
            ex.getStatusCode().value()
        );

        return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error("Servicio externo no disponible", error))
        );
    }

    // ═══════════════════════════════════════════════════════════════
    // ERRORES INESPERADOS
    // ═══════════════════════════════════════════════════════════════

    /**
     * Maneja cualquier excepción no capturada.
     */
    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericError(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorMessage error = ErrorMessage.of(
            "Error interno del servidor",
            "INTERNAL_SERVER_ERROR",
            500
        );

        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error del servidor", error))
        );
    }
}
```

---

## 2️⃣ ADAPTERS OUT - Implementaciones de Puertos

### 📁 persistence/

#### 📄 UserRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsusers.application.mappers.UserMapper;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserRepository;
import pe.edu.vallegrande.vgmsusers.infrastructure.persistence.repositories.UserR2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación del repositorio de usuarios usando R2DBC.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements IUserRepository {

    private final UserR2dbcRepository r2dbcRepository;
    private final UserMapper userMapper;

    @Override
    public Mono<User> save(User user) {
        log.debug("Saving user: {}", user.getDocumentNumber());
        return r2dbcRepository.save(userMapper.toEntity(user))
            .map(userMapper::toModel);
    }

    @Override
    public Mono<User> update(User user) {
        log.debug("Updating user: {}", user.getId());
        return r2dbcRepository.save(userMapper.toEntity(user))
            .map(userMapper::toModel);
    }

    @Override
    public Mono<User> findById(String id) {
        log.debug("Finding user by ID: {}", id);
        return r2dbcRepository.findById(id)
            .map(userMapper::toModel);
    }

    @Override
    public Mono<User> findByDocumentNumber(String documentNumber) {
        log.debug("Finding user by document: {}", documentNumber);
        return r2dbcRepository.findByDocumentNumber(documentNumber)
            .map(userMapper::toModel);
    }

    @Override
    public Flux<User> findAll() {
        log.debug("Finding all users");
        return r2dbcRepository.findAll()
            .map(userMapper::toModel);
    }

    @Override
    public Flux<User> findByRecordStatus(RecordStatus status) {
        log.debug("Finding users by status: {}", status);
        return r2dbcRepository.findByRecordStatus(status.name())
            .map(userMapper::toModel);
    }

    @Override
    public Flux<User> findByOrganizationId(String organizationId) {
        log.debug("Finding users by organization: {}", organizationId);
        return r2dbcRepository.findByOrganizationId(organizationId)
            .map(userMapper::toModel);
    }

    @Override
    public Flux<User> findByOrganizationIdAndRecordStatus(String organizationId, RecordStatus status) {
        log.debug("Finding users by organization {} and status {}", organizationId, status);
        return r2dbcRepository.findByOrganizationIdAndRecordStatus(organizationId, status.name())
            .map(userMapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByDocumentNumber(String documentNumber) {
        log.debug("Checking if exists by document: {}", documentNumber);
        return r2dbcRepository.existsByDocumentNumber(documentNumber);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        log.warn("Physically deleting user: {}", id);
        return r2dbcRepository.deleteById(id);
    }
}
```

---

### 📁 external/

#### 📄 AuthenticationClientImpl.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.ExternalServiceException;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IAuthenticationClient;
import reactor.core.publisher.Mono;

/**
 * Cliente WebClient para el servicio de autenticación.
 *
 * <p>Incluye Circuit Breaker, Retry y Time Limiter para resiliencia.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationClientImpl implements IAuthenticationClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${webclient.services.authentication.base-url}")
    private String authServiceUrl;

    private static final String SERVICE_NAME = "authenticationService";

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "createUserFallback")
    @Retry(name = SERVICE_NAME)
    @TimeLimiter(name = SERVICE_NAME)
    public Mono<String> createUser(
            String userId,
            String email,
            String firstName,
            String lastName,
            String role
    ) {
        log.info("Creating user in authentication service: {}", userId);

        return webClientBuilder.build()
            .post()
            .uri(authServiceUrl + "/api/v1/auth/register")
            .bodyValue(new RegisterRequest(userId, email, firstName, lastName, role))
            .retrieve()
            .bodyToMono(RegisterResponse.class)
            .map(RegisterResponse::keycloakId)
            .doOnSuccess(id -> log.info("User created in Keycloak: {}", id))
            .doOnError(e -> log.error("Failed to create user in auth service: {}", e.getMessage()));
    }

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "disableUserFallback")
    @Retry(name = SERVICE_NAME)
    public Mono<Void> disableUser(String userId) {
        log.info("Disabling user in authentication service: {}", userId);

        return webClientBuilder.build()
            .patch()
            .uri(authServiceUrl + "/api/v1/auth/users/{userId}/disable", userId)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.info("User disabled in Keycloak: {}", userId));
    }

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "enableUserFallback")
    @Retry(name = SERVICE_NAME)
    public Mono<Void> enableUser(String userId) {
        log.info("Enabling user in authentication service: {}", userId);

        return webClientBuilder.build()
            .patch()
            .uri(authServiceUrl + "/api/v1/auth/users/{userId}/enable", userId)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.info("User enabled in Keycloak: {}", userId));
    }

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "deleteUserFallback")
    @Retry(name = SERVICE_NAME)
    public Mono<Void> deleteUser(String userId) {
        log.warn("Deleting user from authentication service: {}", userId);

        return webClientBuilder.build()
            .delete()
            .uri(authServiceUrl + "/api/v1/auth/users/{userId}", userId)
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.warn("User deleted from Keycloak: {}", userId));
    }

    // ═══════════════════════════════════════════════════════════════
    // FALLBACKS
    // ═══════════════════════════════════════════════════════════════

    private Mono<String> createUserFallback(
            String userId, String email, String firstName, String lastName, String role, Throwable t
    ) {
        log.error("Circuit breaker: createUser fallback for {}: {}", userId, t.getMessage());
        return Mono.error(new ExternalServiceException("Authentication", t));
    }

    private Mono<Void> disableUserFallback(String userId, Throwable t) {
        log.error("Circuit breaker: disableUser fallback for {}: {}", userId, t.getMessage());
        return Mono.error(new ExternalServiceException("Authentication", t));
    }

    private Mono<Void> enableUserFallback(String userId, Throwable t) {
        log.error("Circuit breaker: enableUser fallback for {}: {}", userId, t.getMessage());
        return Mono.error(new ExternalServiceException("Authentication", t));
    }

    private Mono<Void> deleteUserFallback(String userId, Throwable t) {
        log.error("Circuit breaker: deleteUser fallback for {}: {}", userId, t.getMessage());
        return Mono.error(new ExternalServiceException("Authentication", t));
    }

    // ═══════════════════════════════════════════════════════════════
    // DTOs INTERNOS
    // ═══════════════════════════════════════════════════════════════

    private record RegisterRequest(
        String userId,
        String email,
        String firstName,
        String lastName,
        String role
    ) {}

    private record RegisterResponse(String keycloakId) {}
}
```

---

#### 📄 OrganizationClientImpl.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IOrganizationClient;
import reactor.core.publisher.Mono;

/**
 * Cliente WebClient para el servicio de organizaciones.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationClientImpl implements IOrganizationClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${webclient.services.organization.base-url}")
    private String organizationServiceUrl;

    private static final String SERVICE_NAME = "organizationService";

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "existsOrganizationFallback")
    @Retry(name = SERVICE_NAME)
    @TimeLimiter(name = SERVICE_NAME)
    public Mono<Boolean> existsOrganization(String organizationId) {
        log.debug("Checking if organization exists: {}", organizationId);

        return webClientBuilder.build()
            .get()
            .uri(organizationServiceUrl + "/api/v1/organizations/{id}/exists", organizationId)
            .retrieve()
            .bodyToMono(Boolean.class)
            .defaultIfEmpty(false);
    }

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "existsZoneFallback")
    @Retry(name = SERVICE_NAME)
    @TimeLimiter(name = SERVICE_NAME)
    public Mono<Boolean> existsZone(String organizationId, String zoneId) {
        log.debug("Checking if zone {} exists in organization {}", zoneId, organizationId);

        return webClientBuilder.build()
            .get()
            .uri(organizationServiceUrl + "/api/v1/organizations/{orgId}/zones/{zoneId}/exists",
                organizationId, zoneId)
            .retrieve()
            .bodyToMono(Boolean.class)
            .defaultIfEmpty(false);
    }

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "existsStreetFallback")
    @Retry(name = SERVICE_NAME)
    @TimeLimiter(name = SERVICE_NAME)
    public Mono<Boolean> existsStreet(String zoneId, String streetId) {
        log.debug("Checking if street {} exists in zone {}", streetId, zoneId);

        return webClientBuilder.build()
            .get()
            .uri(organizationServiceUrl + "/api/v1/zones/{zoneId}/streets/{streetId}/exists",
                zoneId, streetId)
            .retrieve()
            .bodyToMono(Boolean.class)
            .defaultIfEmpty(false);
    }

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "validateHierarchyFallback")
    @Retry(name = SERVICE_NAME)
    @TimeLimiter(name = SERVICE_NAME)
    public Mono<Boolean> validateHierarchy(String organizationId, String zoneId, String streetId) {
        log.debug("Validating hierarchy: org={}, zone={}, street={}", organizationId, zoneId, streetId);

        return existsOrganization(organizationId)
            .flatMap(orgExists -> {
                if (!orgExists) {
                    return Mono.just(false);
                }
                return existsZone(organizationId, zoneId);
            })
            .flatMap(zoneExists -> {
                if (!zoneExists) {
                    return Mono.just(false);
                }
                return existsStreet(zoneId, streetId);
            });
    }

    // ═══════════════════════════════════════════════════════════════
    // FALLBACKS - Devuelven true para no bloquear operaciones
    // ═══════════════════════════════════════════════════════════════

    private Mono<Boolean> existsOrganizationFallback(String organizationId, Throwable t) {
        log.warn("Organization service unavailable, assuming exists: {}", organizationId);
        return Mono.just(true); // Permitir continuar si el servicio no está disponible
    }

    private Mono<Boolean> existsZoneFallback(String organizationId, String zoneId, Throwable t) {
        log.warn("Organization service unavailable, assuming zone exists: {}", zoneId);
        return Mono.just(true);
    }

    private Mono<Boolean> existsStreetFallback(String zoneId, String streetId, Throwable t) {
        log.warn("Organization service unavailable, assuming street exists: {}", streetId);
        return Mono.just(true);
    }

    private Mono<Boolean> validateHierarchyFallback(String orgId, String zoneId, String streetId, Throwable t) {
        log.warn("Organization service unavailable, skipping hierarchy validation");
        return Mono.just(true);
    }
}
```

---

#### 📄 NotificationClientImpl.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.external;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.INotificationClient;
import reactor.core.publisher.Mono;

/**
 * Cliente WebClient para el servicio de notificaciones.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationClientImpl implements INotificationClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${webclient.services.notification.base-url}")
    private String notificationServiceUrl;

    private static final String SERVICE_NAME = "notificationService";

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "sendWelcomeMessageFallback")
    @Retry(name = SERVICE_NAME)
    public Mono<Void> sendWelcomeMessage(String phone, String firstName, String organizationName) {
        log.info("Sending welcome message to: {}", phone);

        return webClientBuilder.build()
            .post()
            .uri(notificationServiceUrl + "/api/v1/notifications/whatsapp")
            .bodyValue(new WhatsAppMessage(
                phone,
                "welcome",
                new WelcomeParams(firstName, organizationName)
            ))
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.info("Welcome message sent to: {}", phone))
            .doOnError(e -> log.warn("Failed to send welcome message: {}", e.getMessage()));
    }

    @Override
    @CircuitBreaker(name = SERVICE_NAME, fallbackMethod = "sendProfileUpdatedNotificationFallback")
    @Retry(name = SERVICE_NAME)
    public Mono<Void> sendProfileUpdatedNotification(String phone, String firstName) {
        log.info("Sending profile updated notification to: {}", phone);

        return webClientBuilder.build()
            .post()
            .uri(notificationServiceUrl + "/api/v1/notifications/whatsapp")
            .bodyValue(new WhatsAppMessage(
                phone,
                "profile_updated",
                new ProfileUpdatedParams(firstName)
            ))
            .retrieve()
            .bodyToMono(Void.class)
            .doOnSuccess(v -> log.info("Profile updated notification sent to: {}", phone));
    }

    // ═══════════════════════════════════════════════════════════════
    // FALLBACKS - Las notificaciones no deben bloquear la operación principal
    // ═══════════════════════════════════════════════════════════════

    private Mono<Void> sendWelcomeMessageFallback(String phone, String firstName, String org, Throwable t) {
        log.warn("Failed to send welcome message to {}, will retry later: {}", phone, t.getMessage());
        // TODO: Encolar para reintento posterior
        return Mono.empty();
    }

    private Mono<Void> sendProfileUpdatedNotificationFallback(String phone, String firstName, Throwable t) {
        log.warn("Failed to send profile updated notification to {}: {}", phone, t.getMessage());
        return Mono.empty();
    }

    // ═══════════════════════════════════════════════════════════════
    // DTOs INTERNOS
    // ═══════════════════════════════════════════════════════════════

    private record WhatsAppMessage(String phone, String template, Object params) {}
    private record WelcomeParams(String firstName, String organizationName) {}
    private record ProfileUpdatedParams(String firstName) {}
}
```

---

### 📁 messaging/

#### 📄 UserEventPublisherImpl.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.application.events.*;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.domain.ports.out.IUserEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Implementación del publicador de eventos usando RabbitMQ.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventPublisherImpl implements IUserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    private static final String EXCHANGE = "user.events";

    @Override
    public Mono<Void> publishUserCreated(User user, String createdBy) {
        return Mono.fromRunnable(() -> {
            UserCreatedEvent event = UserCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .userId(user.getId())
                .organizationId(user.getOrganizationId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .documentNumber(user.getDocumentNumber())
                .role(user.getRole().name())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();

            rabbitTemplate.convertAndSend(EXCHANGE, "user.created", event);
            log.info("Published USER_CREATED event for userId: {}", user.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishUserUpdated(User user, Map<String, Object> changedFields, String updatedBy) {
        return Mono.fromRunnable(() -> {
            UserUpdatedEvent event = UserUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .userId(user.getId())
                .organizationId(user.getOrganizationId())
                .changedFields(changedFields)
                .updatedBy(updatedBy)
                .correlationId(MDC.get("correlationId"))
                .build();

            rabbitTemplate.convertAndSend(EXCHANGE, "user.updated", event);
            log.info("Published USER_UPDATED event for userId: {}", user.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishUserDeleted(String userId, String organizationId, String reason, String deletedBy) {
        return Mono.fromRunnable(() -> {
            UserDeletedEvent event = UserDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .organizationId(organizationId)
                .previousStatus("ACTIVE")
                .reason(reason)
                .deletedBy(deletedBy)
                .correlationId(MDC.get("correlationId"))
                .build();

            rabbitTemplate.convertAndSend(EXCHANGE, "user.deleted", event);
            log.info("Published USER_DELETED event for userId: {}", userId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishUserRestored(String userId, String organizationId, String restoredBy) {
        return Mono.fromRunnable(() -> {
            UserRestoredEvent event = UserRestoredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .userId(userId)
                .organizationId(organizationId)
                .previousStatus("INACTIVE")
                .restoredBy(restoredBy)
                .correlationId(MDC.get("correlationId"))
                .build();

            rabbitTemplate.convertAndSend(EXCHANGE, "user.restored", event);
            log.info("Published USER_RESTORED event for userId: {}", userId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishUserPurged(User user, String reason, String purgedBy) {
        return Mono.fromRunnable(() -> {
            UserPurgedEvent event = UserPurgedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .userId(user.getId())
                .organizationId(user.getOrganizationId())
                .email(user.getEmail())
                .documentNumber(user.getDocumentNumber())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .reason(reason)
                .purgedBy(purgedBy)
                .correlationId(MDC.get("correlationId"))
                .build();

            rabbitTemplate.convertAndSend(EXCHANGE, "user.purged", event);
            log.warn("Published USER_PURGED event for userId: {}", user.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
```

---

## 3️⃣ PERSISTENCE - Entidades y Repositorios

### 📄 UserEntity.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Entidad JPA para persistencia con R2DBC.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {

    @Id
    private String id;

    @Column("organization_id")
    private String organizationId;

    @Column("record_status")
    private String recordStatus;

    @Column("created_at")
    private LocalDateTime createdAt;

    @Column("created_by")
    private String createdBy;

    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("updated_by")
    private String updatedBy;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("document_type")
    private String documentType;

    @Column("document_number")
    private String documentNumber;

    @Column("email")
    private String email;

    @Column("phone")
    private String phone;

    @Column("address")
    private String address;

    @Column("zone_id")
    private String zoneId;

    @Column("street_id")
    private String streetId;

    @Column("role")
    private String role;
}
```

---

### 📄 UserR2dbcRepository.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.persistence.repositories;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio R2DBC para usuarios.
 */
@Repository
public interface UserR2dbcRepository extends R2dbcRepository<UserEntity, String> {

    /**
     * Busca usuario por número de documento.
     */
    Mono<UserEntity> findByDocumentNumber(String documentNumber);

    /**
     * Verifica si existe por número de documento.
     */
    Mono<Boolean> existsByDocumentNumber(String documentNumber);

    /**
     * Lista usuarios por estado.
     */
    Flux<UserEntity> findByRecordStatus(String recordStatus);

    /**
     * Lista usuarios por organización.
     */
    Flux<UserEntity> findByOrganizationId(String organizationId);

    /**
     * Lista usuarios por organización y estado.
     */
    Flux<UserEntity> findByOrganizationIdAndRecordStatus(String organizationId, String recordStatus);
}
```

---

## 4️⃣ CONFIG - Configuraciones

### 📄 R2dbcConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.r2dbc.connection.R2dbcTransactionManager;
import org.springframework.transaction.ReactiveTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de R2DBC y transacciones reactivas.
 */
@Configuration
@EnableR2dbcRepositories(basePackages = "pe.edu.vallegrande.vgmsusers.infrastructure.persistence.repositories")
@EnableR2dbcAuditing
@EnableTransactionManagement
public class R2dbcConfig {

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
```

---

### 📄 WebClientConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuración de WebClient con timeouts.
 */
@Configuration
public class WebClientConfig {

    @Value("${webclient.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${webclient.read-timeout:10000}")
    private int readTimeout;

    @Value("${webclient.write-timeout:10000}")
    private int writeTimeout;

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
            );

        // Aumentar buffer para respuestas grandes
        ExchangeStrategies strategies = ExchangeStrategies.builder()
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
            .build();

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .exchangeStrategies(strategies);
    }
}
```

---

### 📄 RabbitMQConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de RabbitMQ.
 *
 * <p><b>IMPORTANTE:</b> Exchanges, Queues y Bindings se definen AQUÍ, no en YAML.</p>
 * <p>En application.yml solo va: host, port, username, password, publisher-confirm-type.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Configuration
public class RabbitMQConfig {

    // ═══════════════════════════════════════════════════════════════
    // CONSTANTES - Exchange centralizado para todo el sistema
    // ═══════════════════════════════════════════════════════════════

    public static final String EXCHANGE_NAME = "jass.events";

    // Routing Keys para eventos de usuario
    public static final String USER_CREATED_KEY = "user.created";
    public static final String USER_UPDATED_KEY = "user.updated";
    public static final String USER_DELETED_KEY = "user.deleted";
    public static final String USER_RESTORED_KEY = "user.restored";
    public static final String USER_PURGED_KEY = "user.purged";

    // ═══════════════════════════════════════════════════════════════
    // EXCHANGE - Topic Exchange compartido por todos los microservicios
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public TopicExchange jassEventsExchange() {
        return ExchangeBuilder
            .topicExchange(EXCHANGE_NAME)
            .durable(true)
            .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // MESSAGE CONVERTER - JSON serialization
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ═══════════════════════════════════════════════════════════════
    // RABBIT TEMPLATE - Configurado con el exchange y converter
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            MessageConverter jsonMessageConverter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter);
        template.setExchange(EXCHANGE_NAME);
        return template;
    }
}
```

> **📌 Nota:** El exchange `jass.events` es compartido por todos los microservicios del sistema. Cada microservicio publica eventos con su routing key específica (ej: `user.created`, `payment.completed`, `claim.opened`).

---

### 📄 SecurityConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Configuración de seguridad.
 *
 * <p>En desarrollo se permite todo. En producción se configura JWT.</p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                // Endpoints públicos
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // TODO: En producción, configurar JWT
                .anyExchange().permitAll()
            )
            .build();
    }
}
```

---

### 📄 Resilience4jConfig.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración programática de Resilience4j.
 *
 * <p>Esta clase complementa la configuración YAML con beans personalizados.
 * Los valores del YAML tienen precedencia sobre estos defaults.</p>
 *
 * <p><strong>¿Por qué tener clase además de YAML?</strong></p>
 * <ul>
 *   <li>El YAML define valores específicos por instancia</li>
 *   <li>Esta clase define la configuración base/default</li>
 *   <li>Permite crear configuraciones programáticas dinámicas</li>
 *   <li>Útil para registrar métricas y health indicators</li>
 * </ul>
 */
@Configuration
public class Resilience4jConfig {

    /**
     * Configuración base para Circuit Breaker.
     */
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
            // Ventana deslizante de 10 llamadas
            .slidingWindowSize(10)
            .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
            // Mínimo 5 llamadas antes de calcular tasa de fallos
            .minimumNumberOfCalls(5)
            // Umbral de fallos: 50%
            .failureRateThreshold(50)
            // Tiempo en estado OPEN antes de pasar a HALF_OPEN
            .waitDurationInOpenState(Duration.ofSeconds(10))
            // Llamadas permitidas en estado HALF_OPEN
            .permittedNumberOfCallsInHalfOpenState(3)
            // Transición automática de OPEN a HALF_OPEN
            .automaticTransitionFromOpenToHalfOpenEnabled(true)
            // Excepciones que cuentan como fallo
            .recordExceptions(
                java.io.IOException.class,
                java.util.concurrent.TimeoutException.class,
                org.springframework.web.reactive.function.client.WebClientResponseException.class
            )
            // Excepciones que NO cuentan como fallo
            .ignoreExceptions(
                pe.edu.vallegrande.vgmsusers.domain.exceptions.NotFoundException.class,
                pe.edu.vallegrande.vgmsusers.domain.exceptions.ValidationException.class
            )
            .build();
    }

    /**
     * Registry de Circuit Breakers con la configuración base.
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig config) {
        return CircuitBreakerRegistry.of(config);
    }

    /**
     * Configuración base para Retry.
     */
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            // Backoff exponencial: 500ms, 1s, 2s
            .intervalFunction(io.github.resilience4j.core.IntervalFunction
                .ofExponentialBackoff(Duration.ofMillis(500), 2))
            // Solo reintentar en estas excepciones
            .retryExceptions(
                java.io.IOException.class,
                java.util.concurrent.TimeoutException.class
            )
            // No reintentar en estas (errores de negocio)
            .ignoreExceptions(
                pe.edu.vallegrande.vgmsusers.domain.exceptions.DomainException.class
            )
            .build();
    }

    /**
     * Registry de Retries con la configuración base.
     */
    @Bean
    public RetryRegistry retryRegistry(RetryConfig config) {
        return RetryRegistry.of(config);
    }

    /**
     * Configuración base para Time Limiter.
     */
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
            .timeoutDuration(Duration.ofSeconds(5))
            .cancelRunningFuture(true)
            .build();
    }

    /**
     * Registry de Time Limiters con la configuración base.
     */
    @Bean
    public TimeLimiterRegistry timeLimiterRegistry(TimeLimiterConfig config) {
        return TimeLimiterRegistry.of(config);
    }
}
```

---

### 📄 RequestContextFilter.java

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

/**
 * WebFilter para propagar contexto de request en WebFlux reactivo.
 *
 * <p>Funcionalidades:</p>
 * <ul>
 *   <li>Genera/propaga correlationId para trazabilidad distribuida</li>
 *   <li>Extrae userId del header X-User-Id para auditoría</li>
 *   <li>Configura MDC para logging contextual</li>
 *   <li>Propaga contexto a través de Reactor Context</li>
 * </ul>
 *
 * <p><strong>NOTA:</strong> CORS se maneja en el Gateway, NO aquí.</p>
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestContextFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String CORRELATION_ID_KEY = "correlationId";
    public static final String USER_ID_KEY = "userId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        // Obtener o generar correlationId
        String correlationId = request.getHeaders().getFirst(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }

        // Obtener userId del header (propagado por Gateway)
        String userId = request.getHeaders().getFirst(USER_ID_HEADER);
        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }

        // Valores finales para usar en lambdas
        final String finalCorrelationId = correlationId;
        final String finalUserId = userId;

        // Log de inicio de request
        log.debug("Request: {} {} - correlationId: {}, userId: {}",
            request.getMethod(), request.getPath(), finalCorrelationId, finalUserId);

        // Agregar correlationId al response header
        exchange.getResponse().getHeaders().add(CORRELATION_ID_HEADER, finalCorrelationId);

        return chain.filter(exchange)
            // Configurar MDC para logging (solo en el hilo actual)
            .contextWrite(ctx -> {
                MDC.put(CORRELATION_ID_KEY, finalCorrelationId);
                MDC.put(USER_ID_KEY, finalUserId);
                return ctx;
            })
            // Propagar en Reactor Context (disponible en toda la cadena reactiva)
            .contextWrite(Context.of(
                CORRELATION_ID_KEY, finalCorrelationId,
                USER_ID_KEY, finalUserId
            ))
            // Limpiar MDC al terminar
            .doFinally(signalType -> {
                MDC.remove(CORRELATION_ID_KEY);
                MDC.remove(USER_ID_KEY);
            });
    }
}
```

---

## 5️⃣ RESOURCES - Configuraciones YAML

### 📄 application.yml

```yaml
spring:
  application:
    name: vg-ms-users
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}

  webflux:
    base-path: /api/v1

server:
  port: 8081
  shutdown: graceful

spring.lifecycle:
  timeout-per-shutdown-phase: 30s

springdoc:
  api-docs:
    path: /v3/api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    try-it-out-enabled: true
    operations-sorter: method

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,circuitbreakers
  endpoint:
    health:
      show-details: always
  health:
    circuitbreakers:
      enabled: true
    db:
      enabled: true
    rabbit:
      enabled: true

logging:
  level:
    root: INFO
    pe.edu.vallegrande: INFO
```

---

### 📄 application-dev.yml

```yaml
spring:
  # NOTA: CORS se maneja en vg-ms-gateway, NO aquí

  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: SISTEMAJASS
    pool:
      initial-size: 5
      max-size: 20

  flyway:
    enabled: true
    url: jdbc:postgresql://localhost:5432/sistemajass
    user: sistemajass_user
    password: SISTEMAJASS
    locations: classpath:db/migration

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    publisher-confirm-type: correlated

webclient:
  connect-timeout: 5000
  read-timeout: 10000
  write-timeout: 10000
  services:
    authentication:
      base-url: http://localhost:8082
    organization:
      base-url: http://localhost:8083
    notification:
      base-url: http://localhost:8090

resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        base-config: default

  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 500ms
        enable-exponential-backoff: true
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        base-config: default

  timelimiter:
    configs:
      default:
        timeout-duration: 5s
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        timeout-duration: 10s

logging:
  level:
    pe.edu.vallegrande: DEBUG
    org.springframework.r2dbc: DEBUG
```

---

### 📄 application-prod.yml

> **⚠️ NOTA**: En producción, las credenciales se inyectan via variables de entorno o secrets.

```yaml
spring:
  # CORS se maneja SOLO en vg-ms-gateway

  r2dbc:
    url: r2dbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${DB_NAME:sistemajass}
    username: ${DB_USER}
    password: ${DB_PASSWORD}
    pool:
      initial-size: 10
      max-size: 50
      max-idle-time: 30m

  flyway:
    enabled: true
    url: jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${DB_NAME:sistemajass}
    user: ${DB_USER}
    password: ${DB_PASSWORD}
    locations: classpath:db/migration

  rabbitmq:
    host: ${RABBITMQ_HOST:rabbitmq}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USER}
    password: ${RABBITMQ_PASSWORD}
    publisher-confirm-type: correlated
    publisher-returns: true

webclient:
  connect-timeout: 3000
  read-timeout: 5000
  write-timeout: 5000
  services:
    # En Docker Compose, usar nombres de servicio
    authentication:
      base-url: http://vg-ms-authentication:8082
    organization:
      base-url: http://vg-ms-organizations:8083
    notification:
      base-url: http://vg-ms-notifications:8090

# Configuración más estricta en producción
resilience4j:
  circuitbreaker:
    configs:
      default:
        register-health-indicator: true
        sliding-window-size: 20
        minimum-number-of-calls: 10
        wait-duration-in-open-state: 30s
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 80
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        base-config: default
        # Notificaciones pueden ser más lentas
        slow-call-duration-threshold: 5s

  retry:
    configs:
      default:
        max-attempts: 3
        wait-duration: 1s
        enable-exponential-backoff: true
        exponential-backoff-multiplier: 2
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        base-config: default

  timelimiter:
    configs:
      default:
        timeout-duration: 3s
    instances:
      authenticationService:
        base-config: default
      organizationService:
        base-config: default
      notificationService:
        timeout-duration: 10s

# Logging en producción: menos verbose
logging:
  level:
    root: WARN
    pe.edu.vallegrande: INFO
    org.springframework.r2dbc: WARN

# Swagger deshabilitado en producción
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

---

### 📄 db/migration/V1__create_users_table.sql

```sql
-- =====================================================
-- USERS TABLE - vg-ms-users
-- =====================================================

CREATE TABLE IF NOT EXISTS users (
    -- Primary Key
    id VARCHAR(36) PRIMARY KEY,

    -- Foreign Keys (referencias a otros microservicios)
    organization_id VARCHAR(36) NOT NULL,
    zone_id VARCHAR(36) NOT NULL,
    street_id VARCHAR(36) NOT NULL,

    -- Auditoría
    record_status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(36),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(36),

    -- Datos del usuario
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(150) NOT NULL,
    document_type VARCHAR(10) NOT NULL,
    document_number VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    phone VARCHAR(20),
    address VARCHAR(250) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'CLIENT',

    -- Constraints
    CONSTRAINT uk_users_document_number UNIQUE (document_number),
    CONSTRAINT chk_users_document_type CHECK (document_type IN ('DNI', 'RUC', 'CE')),
    CONSTRAINT chk_users_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_users_role CHECK (role IN ('SUPER_ADMIN', 'ADMIN', 'CLIENT')),
    CONSTRAINT chk_users_contact CHECK (email IS NOT NULL OR phone IS NOT NULL)
);

-- Índices
CREATE INDEX idx_users_organization_id ON users(organization_id);
CREATE INDEX idx_users_zone_id ON users(zone_id);
CREATE INDEX idx_users_record_status ON users(record_status);
CREATE INDEX idx_users_document_number ON users(document_number);
CREATE INDEX idx_users_role ON users(role);

-- Comentarios
COMMENT ON TABLE users IS 'Tabla de usuarios del sistema JASS Digital';
COMMENT ON COLUMN users.record_status IS 'ACTIVE = activo, INACTIVE = eliminado lógicamente';
COMMENT ON CONSTRAINT chk_users_contact ON users IS 'Al menos email o phone debe tener valor';
```

---

## ✅ Resumen de la Capa de Infraestructura

| Componente | Cantidad | Descripción |
|------------|----------|-------------|
| REST Controllers | 2 clases | UserRest + GlobalExceptionHandler |
| Repository Impl | 1 clase | UserRepositoryImpl |
| External Clients | 3 clases | Auth, Organization, Notification |
| Event Publisher | 1 clase | UserEventPublisherImpl |
| Entities | 1 clase | UserEntity |
| R2DBC Repository | 1 interface | UserR2dbcRepository |
| Configs | 6 clases | R2DBC, WebClient, RabbitMQ, Resilience4j, Security, RequestContextFilter |

> **⚠️ NOTA IMPORTANTE:** CORS se configura **SOLO en el Gateway** (`vg-ms-gateway`), NO en microservicios individuales.
| SQL Migrations | 1 archivo | V1__create_users_table.sql |

---

## 📦 pom.xml - Dependencias Principales

```xml
<dependencies>
    <!-- Spring WebFlux -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webflux</artifactId>
    </dependency>

    <!-- R2DBC PostgreSQL -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-r2dbc</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>r2dbc-postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- Flyway -->
    <dependency>
        <groupId>org.flywaydb</groupId>
        <artifactId>flyway-core</artifactId>
    </dependency>
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>

    <!-- RabbitMQ -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-amqp</artifactId>
    </dependency>

    <!-- Resilience4j -->
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-spring-boot3</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.resilience4j</groupId>
        <artifactId>resilience4j-reactor</artifactId>
    </dependency>

    <!-- Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- OpenAPI / Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>

    <!-- Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

---

## 🎯 Resumen Final del Microservicio

| Capa | Clases | Responsabilidad |
|------|--------|-----------------|
| **Domain** | 14 | Modelos, Ports, Exceptions |
| **Application** | 18 | UseCases, DTOs, Mappers, Events |
| **Infrastructure** | 15 | REST, Repository, Clients, Config |
| **TOTAL** | **47** | Microservicio completo |

---

**¡Documentación completa del microservicio vg-ms-users!** 🎉
