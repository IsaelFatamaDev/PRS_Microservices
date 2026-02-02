# 📂 ESTRUCTURA DETALLADA MAESTRA - 11 MICROSERVICIOS

Este documento contiene la **ESTRUCTURA COMPLETA Y DEFINITIVA** de TODOS los microservicios del proyecto JASS Digital, con cada archivo, cada clase, cada configuración definida.

> **📌 NOTA IMPORTANTE**: Cada microservicio es INDEPENDIENTE y tiene sus propias clases base (no hay paquete compartido entre servicios).

---

# 📋 ÍNDICE

1. [Estándar de Excepciones](#estandar-excepciones)
2. [Estándar de Eventos](#estandar-eventos)
3. [Estructura por Microservicio](#estructura-microservicios)

---

# 🔥 ESTÁNDAR DE EXCEPCIONES {#estandar-excepciones}

## 📁 Ubicación Obligatoria

**TODAS las excepciones de dominio deben estar en:**

```
domain/exceptions/
```

**El GlobalExceptionHandler debe estar en:**

```
infrastructure/adapters/in/rest/GlobalExceptionHandler.java
```

---

## 🎯 Jerarquía de Excepciones Base

Cada microservicio **DEBE** tener esta estructura base:

```text
domain/exceptions/
├── DomainException.java              → [ABSTRACT] Clase base para TODAS las excepciones
├── NotFoundException.java            → [CLASS] HTTP 404 - Recurso no encontrado
├── BusinessRuleException.java        → [CLASS] HTTP 400 - Regla de negocio violada
├── ValidationException.java          → [CLASS] HTTP 400 - Error de validación
├── UnauthorizedException.java        → [CLASS] HTTP 401 - No autenticado
├── ForbiddenException.java           → [CLASS] HTTP 403 - Sin permisos
├── ConflictException.java            → [CLASS] HTTP 409 - Conflicto (duplicados)
├── ExternalServiceException.java     → [CLASS] HTTP 503 - Servicio externo no disponible
└── [Específicas del dominio]         → Ej: InsufficientBalanceException, WaterBoxAlreadyAssignedException
```

---

## 📝 Implementación de Clase Base

```java
// domain/exceptions/DomainException.java
package pe.edu.vallegrande.[microservicio].domain.exceptions;

/**
 * Clase base abstracta para todas las excepciones de dominio.
 * Proporciona código de error y mensaje consistentes.
 */
public abstract class DomainException extends RuntimeException {

    private final String errorCode;
    private final int httpStatus;

    protected DomainException(String message, String errorCode, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    protected DomainException(String message, String errorCode, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }
}
```

---

## 📝 Excepciones Específicas

```java
// domain/exceptions/NotFoundException.java
public class NotFoundException extends DomainException {
    public NotFoundException(String resource, String id) {
        super(
            String.format("%s con ID '%s' no encontrado", resource, id),
            "RESOURCE_NOT_FOUND",
            404
        );
    }

    public NotFoundException(String message) {
        super(message, "RESOURCE_NOT_FOUND", 404);
    }
}

// domain/exceptions/BusinessRuleException.java
public class BusinessRuleException extends DomainException {
    public BusinessRuleException(String message) {
        super(message, "BUSINESS_RULE_VIOLATION", 400);
    }

    public BusinessRuleException(String message, String errorCode) {
        super(message, errorCode, 400);
    }
}

// domain/exceptions/ConflictException.java
public class ConflictException extends DomainException {
    public ConflictException(String message) {
        super(message, "RESOURCE_CONFLICT", 409);
    }
}

// domain/exceptions/ExternalServiceException.java
public class ExternalServiceException extends DomainException {
    public ExternalServiceException(String serviceName) {
        super(
            String.format("Servicio '%s' no disponible temporalmente", serviceName),
            "EXTERNAL_SERVICE_UNAVAILABLE",
            503
        );
    }

    public ExternalServiceException(String serviceName, Throwable cause) {
        super(
            String.format("Error comunicándose con servicio '%s'", serviceName),
            "EXTERNAL_SERVICE_ERROR",
            503,
            cause
        );
    }
}
```

---

## 📊 Excepciones por Microservicio

| Microservicio | Excepciones Específicas |
|---------------|------------------------|
| **users** | `UserNotFoundException`, `InvalidContactException`, `DuplicateDocumentException` |
| **authentication** | `InvalidCredentialsException`, `KeycloakException`, `TokenExpiredException` |
| **organizations** | `OrganizationNotFoundException`, `ZoneNotFoundException`, `StreetNotFoundException`, `DuplicateFareException` |
| **commercial** | `ReceiptNotFoundException`, `PaymentNotFoundException`, `DebtNotFoundException`, `InsufficientBalanceException`, `DuplicatePaymentException` |
| **water-quality** | `TestingPointNotFoundException`, `QualityTestNotFoundException`, `InvalidMeasurementException` |
| **distribution** | `ProgramNotFoundException`, `RouteNotFoundException`, `ScheduleConflictException` |
| **inventory** | `SupplierNotFoundException`, `MaterialNotFoundException`, `InsufficientStockException` |
| **claims** | `ComplaintNotFoundException`, `IncidentNotFoundException`, `InvalidTransitionException` |
| **infrastructure** | `WaterBoxNotFoundException`, `WaterBoxAlreadyAssignedException`, `InvalidTransferException` |

---

## 🎛️ GlobalExceptionHandler Estándar

```java
// infrastructure/adapters/in/rest/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ══════════════════════════════════════════════════════════════
    // EXCEPCIONES DE DOMINIO
    // ══════════════════════════════════════════════════════════════

    @ExceptionHandler(DomainException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleDomainException(DomainException ex) {
        log.error("Domain exception: {} - Code: {}", ex.getMessage(), ex.getErrorCode());

        ErrorMessage error = ErrorMessage.builder()
            .message(ex.getMessage())
            .errorCode(ex.getErrorCode())
            .status(ex.getHttpStatus())
            .build();

        return Mono.just(
            ResponseEntity.status(ex.getHttpStatus())
                .body(ApiResponse.error(ex.getMessage(), error))
        );
    }

    // ══════════════════════════════════════════════════════════════
    // VALIDACIONES (@Valid)
    // ══════════════════════════════════════════════════════════════

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationErrors(WebExchangeBindException ex) {
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

    // ══════════════════════════════════════════════════════════════
    // ERRORES DE SERVICIOS EXTERNOS (WebClient)
    // ══════════════════════════════════════════════════════════════

    @ExceptionHandler(WebClientResponseException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleWebClientException(WebClientResponseException ex) {
        log.error("External service error: {} - Status: {}", ex.getMessage(), ex.getStatusCode());

        ErrorMessage error = ErrorMessage.builder()
            .message("Error de comunicación con servicio externo")
            .errorCode("EXTERNAL_SERVICE_ERROR")
            .status(ex.getStatusCode().value())
            .build();

        return Mono.just(
            ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(ApiResponse.error("Servicio externo no disponible", error))
        );
    }

    // ══════════════════════════════════════════════════════════════
    // ERROR GENÉRICO (Catch-all)
    // ══════════════════════════════════════════════════════════════

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericError(Exception ex) {
        log.error("Unexpected error", ex);

        ErrorMessage error = ErrorMessage.builder()
            .message("Error interno del servidor")
            .errorCode("INTERNAL_SERVER_ERROR")
            .status(500)
            .build();

        return Mono.just(
            ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Error del servidor", error))
        );
    }
}
```

---

# 📬 ESTÁNDAR DE EVENTOS {#estandar-eventos}

## 📁 Ubicación Obligatoria

### Estructura según Arquitectura Hexagonal

```
domain/
├── ports/out/
│   └── I[Entidad]EventPublisher.java    → INTERFAZ (El dominio define QUÉ eventos publicar)

application/
├── events/                               → CLASES DTO de eventos (Data Transfer Objects)
│   ├── [Entidad]CreatedEvent.java
│   ├── [Entidad]UpdatedEvent.java
│   ├── [Entidad]DeletedEvent.java       → Eliminación LÓGICA (soft delete)
│   ├── [Entidad]RestoredEvent.java      → Restauración de eliminación lógica
│   └── [Entidad]PurgedEvent.java        → Eliminación FÍSICA (hard delete)

infrastructure/
├── adapters/out/
│   └── messaging/
│       └── [Entidad]EventPublisherImpl.java  → IMPLEMENTACIÓN (Infra define CÓMO publicar)
├── messaging/
│   └── listeners/
│       └── [Entidad]EventListener.java       → LISTENERS de eventos EXTERNOS
```

### ⚠️ REGLA IMPORTANTE

| Componente | Ubicación | Razón |
|------------|-----------|-------|
| **Interfaz** `IEventPublisher` | `domain/ports/out/` | El dominio define el contrato |
| **DTOs de eventos** | `application/events/` | Son objetos de transferencia |
| **Implementación** `EventPublisherImpl` | `infrastructure/adapters/out/messaging/` | RabbitMQ es detalle de infraestructura |
| **Listeners** (eventos externos) | `infrastructure/messaging/listeners/` | Consumir es detalle de infraestructura |

---

## 🎯 Tipos de Eventos CRUD Completo

### 1️⃣ Eventos de CREACIÓN

```java
// application/events/UserCreatedEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCreatedEvent {
    private String eventId;
    private String eventType = "USER_CREATED";
    private LocalDateTime timestamp;

    // Datos del recurso creado
    private String userId;
    private String email;
    private String firstName;
    private String lastName;
    private String organizationId;
    private String role;

    // Metadata
    private String createdBy;
    private String correlationId;
}
```

### 2️⃣ Eventos de ACTUALIZACIÓN

```java
// application/events/UserUpdatedEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdatedEvent {
    private String eventId;
    private String eventType = "USER_UPDATED";
    private LocalDateTime timestamp;

    // Identificador
    private String userId;

    // Campos modificados (solo los que cambiaron)
    private Map<String, Object> changedFields;

    // Valores anteriores (para auditoría)
    private Map<String, Object> previousValues;

    // Metadata
    private String updatedBy;
    private String correlationId;
}
```

### 3️⃣ Eventos de ELIMINACIÓN LÓGICA (Soft Delete)

```java
// application/events/UserDeletedEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDeletedEvent {
    private String eventId;
    private String eventType = "USER_DELETED";        // Eliminación LÓGICA
    private LocalDateTime timestamp;

    // Identificador
    private String userId;
    private String organizationId;

    // Información del estado anterior
    private String previousStatus;                     // ACTIVE -> INACTIVE

    // Razón de eliminación (opcional)
    private String reason;

    // Metadata
    private String deletedBy;
    private String correlationId;
}
```

### 4️⃣ Eventos de RESTAURACIÓN

```java
// application/events/UserRestoredEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRestoredEvent {
    private String eventId;
    private String eventType = "USER_RESTORED";
    private LocalDateTime timestamp;

    // Identificador
    private String userId;
    private String organizationId;

    // Información de restauración
    private String previousStatus;                     // INACTIVE -> ACTIVE
    private LocalDateTime deletedAt;                   // Cuándo fue eliminado

    // Metadata
    private String restoredBy;
    private String correlationId;
}
```

### 5️⃣ Eventos de ELIMINACIÓN FÍSICA (Hard Delete / Purge)

```java
// application/events/UserPurgedEvent.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPurgedEvent {
    private String eventId;
    private String eventType = "USER_PURGED";         // Eliminación FÍSICA
    private LocalDateTime timestamp;

    // Identificador del recurso eliminado permanentemente
    private String userId;
    private String organizationId;

    // Snapshot de datos eliminados (para auditoría)
    private String email;
    private String documentNumber;

    // Razón de purga (requerida para auditoría)
    private String reason;

    // Metadata
    private String purgedBy;
    private String correlationId;

    // Flag para indicar si se deben eliminar datos relacionados
    private boolean cascadeDelete;
}
```

---

## 📤 Publisher de Eventos

```java
// infrastructure/adapters/out/messaging/UserEventPublisherImpl.java
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisherImpl implements IUserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    // Exchange centralizado para todo el sistema JASS
    private static final String EXCHANGE = "jass.events";

    @Override
    public void publishUserCreated(User user, String createdBy) {
        UserCreatedEvent event = UserCreatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .userId(user.getId())
            .email(user.getEmail())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .organizationId(user.getOrganizationId())
            .role(user.getRole().name())
            .createdBy(createdBy)
            .correlationId(MDC.get("correlationId"))
            .build();

        rabbitTemplate.convertAndSend(EXCHANGE, "user.created", event);
        log.info("Published USER_CREATED event for userId: {}", user.getId());
    }

    @Override
    public void publishUserUpdated(User user, Map<String, Object> changes, String updatedBy) {
        UserUpdatedEvent event = UserUpdatedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .userId(user.getId())
            .changedFields(changes)
            .updatedBy(updatedBy)
            .correlationId(MDC.get("correlationId"))
            .build();

        rabbitTemplate.convertAndSend(EXCHANGE, "user.updated", event);
        log.info("Published USER_UPDATED event for userId: {}", user.getId());
    }

    @Override
    public void publishUserDeleted(String userId, String organizationId, String reason, String deletedBy) {
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
        log.info("Published USER_DELETED (soft) event for userId: {}", userId);
    }

    @Override
    public void publishUserRestored(String userId, String organizationId, String restoredBy) {
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
    }

    @Override
    public void publishUserPurged(User user, String reason, String purgedBy, boolean cascadeDelete) {
        UserPurgedEvent event = UserPurgedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .userId(user.getId())
            .organizationId(user.getOrganizationId())
            .email(user.getEmail())
            .documentNumber(user.getDocumentNumber())
            .reason(reason)
            .purgedBy(purgedBy)
            .cascadeDelete(cascadeDelete)
            .correlationId(MDC.get("correlationId"))
            .build();

        rabbitTemplate.convertAndSend(EXCHANGE, "user.purged", event);
        log.warn("Published USER_PURGED (hard delete) event for userId: {}", user.getId());
    }
}
```

---

## 📥 Listener de Eventos Externos

```java
// infrastructure/messaging/listeners/UserEventListener.java
@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventListener {

    private final IKeycloakClient keycloakClient;

    @RabbitListener(queues = "authentication.user.created")
    public void handleUserCreated(UserCreatedEvent event) {
        log.info("Received USER_CREATED event: {}", event.getUserId());

        keycloakClient.createUser(event.getEmail(), event.getFirstName(), event.getLastName())
            .doOnSuccess(v -> log.info("User created in Keycloak: {}", event.getUserId()))
            .doOnError(e -> log.error("Failed to create user in Keycloak", e))
            .subscribe();
    }

    @RabbitListener(queues = "authentication.user.deleted")
    public void handleUserDeleted(UserDeletedEvent event) {
        log.info("Received USER_DELETED event: {}", event.getUserId());

        // Deshabilitar usuario en Keycloak (soft delete)
        keycloakClient.disableUser(event.getUserId())
            .doOnSuccess(v -> log.info("User disabled in Keycloak: {}", event.getUserId()))
            .subscribe();
    }

    @RabbitListener(queues = "authentication.user.restored")
    public void handleUserRestored(UserRestoredEvent event) {
        log.info("Received USER_RESTORED event: {}", event.getUserId());

        // Rehabilitar usuario en Keycloak
        keycloakClient.enableUser(event.getUserId())
            .doOnSuccess(v -> log.info("User enabled in Keycloak: {}", event.getUserId()))
            .subscribe();
    }

    @RabbitListener(queues = "authentication.user.purged")
    public void handleUserPurged(UserPurgedEvent event) {
        log.warn("Received USER_PURGED event: {}", event.getUserId());

        // Eliminar usuario permanentemente de Keycloak
        keycloakClient.deleteUser(event.getUserId())
            .doOnSuccess(v -> log.warn("User permanently deleted from Keycloak: {}", event.getUserId()))
            .subscribe();
    }
}
```

---

## 📊 Matriz de Eventos por Microservicio

> **⚠️ IMPORTANTE**: Todos los microservicios usan el **exchange centralizado `jass.events`**.
> Los routing keys diferencian el tipo de evento.

| Microservicio | Eventos que PUBLICA | Routing Keys |
|---------------|---------------------|---------------|
| **users** | Created, Updated, Deleted, Restored, Purged | `user.created`, `user.updated`, `user.deleted`, `user.restored`, `user.purged` |
| **authentication** | (Solo escucha) | - |
| **organizations** | Created, Updated, Deleted, Restored | `organization.created`, `organization.updated`, etc. |
| **commercial** | PaymentCreated, ReceiptGenerated, ServiceCutScheduled | `payment.created`, `receipt.generated`, `service-cut.scheduled` |
| **claims** | ComplaintCreated, IncidentCreated, StatusChanged | `complaint.created`, `incident.created`, `status.changed` |
| **infrastructure** | WaterBoxAssigned, WaterBoxTransferred | `waterbox.assigned`, `waterbox.transferred` |

| Microservicio | Eventos que ESCUCHA | Acción |
|---------------|---------------------|--------|
| **authentication** | `user.created`, `user.deleted`, `user.restored`, `user.purged` | Sincronizar Keycloak |
| **infrastructure** | `service-cut.scheduled` | Actualizar estado water-box |
| **notification** | `user.*`, `payment.*`, `complaint.*`, `incident.*` | Enviar WhatsApp/Email |

---

## ⚙️ Configuración RabbitMQ

> **📌 NOTA**: Exchanges, Queues y Bindings se configuran en Java, **NO en YAML**.
> En application.yml solo va: host, port, username, password, publisher-confirm-type.

```java
// infrastructure/config/RabbitMQConfig.java
@Configuration
public class RabbitMQConfig {

    // ══════════════════════════════════════════════════════════════
    // EXCHANGE CENTRALIZADO - Compartido por todos los microservicios
    // ══════════════════════════════════════════════════════════════

    public static final String EXCHANGE_NAME = "jass.events";

    @Bean
    public TopicExchange jassEventsExchange() {
        return ExchangeBuilder
            .topicExchange(EXCHANGE_NAME)
            .durable(true)
            .build();
    }

    // ══════════════════════════════════════════════════════════════
    // QUEUES (para este servicio como consumidor)
    // Ejemplo: vg-ms-authentication escuchando eventos de users
    // ══════════════════════════════════════════════════════════════

    @Bean
    public Queue authenticationUserCreatedQueue() {
        return new Queue("authentication.user.created", true);
    }

    @Bean
    public Queue authenticationUserDeletedQueue() {
        return new Queue("authentication.user.deleted", true);
    }

    @Bean
    public Queue authenticationUserRestoredQueue() {
        return new Queue("authentication.user.restored", true);
    }

    @Bean
    public Queue authenticationUserPurgedQueue() {
        return new Queue("authentication.user.purged", true);
    }

    // ══════════════════════════════════════════════════════════════
    // BINDINGS
    // ══════════════════════════════════════════════════════════════

    @Bean
    public Binding bindingUserCreated() {
        return BindingBuilder
            .bind(authenticationUserCreatedQueue())
            .to(jassEventsExchange())
            .with("user.created");
    }

    @Bean
    public Binding bindingUserDeleted() {
        return BindingBuilder
            .bind(authenticationUserDeletedQueue())
            .to(jassEventsExchange())
            .with("user.deleted");
    }

    @Bean
    public Binding bindingUserRestored() {
        return BindingBuilder
            .bind(authenticationUserRestoredQueue())
            .to(jassEventsExchange())
            .with("user.restored");
    }

    @Bean
    public Binding bindingUserPurged() {
        return BindingBuilder
            .bind(authenticationUserPurgedQueue())
            .to(jassEventsExchange())
            .with("user.purged");
    }

    // ══════════════════════════════════════════════════════════════
    // MESSAGE CONVERTER (JSON)
    // ══════════════════════════════════════════════════════════════

    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}
```

---

# 📦 ESTRUCTURA POR MICROSERVICIO {#estructura-microservicios}

---

## 1. 📦 vg-ms-users {#estructura-users}

```text
vg-ms-users/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsusers/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── User.java                           → [CLASS] extends BaseEntity
│   │   │   │   │                                         Campos: firstName, lastName,
│   │   │   │   │                                         documentType, documentNumber,
│   │   │   │   │                                         email (OPCIONAL), phone (OPCIONAL),
│   │   │   │   │                                         address, zoneId, streetId, role
│   │   │   │   └── valueobjects/
│   │   │   │       ├── Role.java                       → [ENUM] SUPER_ADMIN, ADMIN, CLIENT
│   │   │   │       ├── DocumentType.java               → [ENUM] DNI, RUC, CE
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateUserUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IGetUserUseCase.java            → [INTERFACE]
│   │   │   │   │   ├── IUpdateUserUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IDeleteUserUseCase.java         → [INTERFACE] Soft delete
│   │   │   │   │   ├── IRestoreUserUseCase.java        → [INTERFACE] Restaurar
│   │   │   │   │   └── IPurgeUserUseCase.java          → [INTERFACE] Hard delete
│   │   │   │   └── out/
│   │   │   │       ├── IUserRepository.java            → [INTERFACE] Reactivo (Mono/Flux)
│   │   │   │       ├── IAuthenticationClient.java      → [INTERFACE] Crear usuario en Keycloak
│   │   │   │       ├── IOrganizationClient.java        → [INTERFACE] Validar org/zona/calle
│   │   │   │       ├── INotificationClient.java        → [INTERFACE] Enviar WhatsApp
│   │   │   │       └── IUserEventPublisher.java        → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ValidationException.java            → [CLASS] HTTP 400
│   │   │       ├── ConflictException.java              → [CLASS] HTTP 409
│   │   │       ├── ExternalServiceException.java       → [CLASS] HTTP 503
│   │   │       ├── UserNotFoundException.java          → [CLASS] extends NotFoundException
│   │   │       ├── DuplicateDocumentException.java     → [CLASS] extends ConflictException
│   │   │       └── InvalidContactException.java        → [CLASS] Al menos email O phone requerido
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateUserUseCaseImpl.java          → [CLASS] @Service
│   │   │   │   ├── GetUserUseCaseImpl.java             → [CLASS] @Service
│   │   │   │   ├── UpdateUserUseCaseImpl.java          → [CLASS] @Service
│   │   │   │   ├── DeleteUserUseCaseImpl.java          → [CLASS] @Service (Soft delete)
│   │   │   │   ├── RestoreUserUseCaseImpl.java         → [CLASS] @Service (Restaurar)
│   │   │   │   └── PurgeUserUseCaseImpl.java           → [CLASS] @Service (Hard delete)
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateUserRequest.java          → [CLASS] @Valid
│   │   │   │   │   └── UpdateUserRequest.java          → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       └── UserResponse.java               → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   └── UserMapper.java                     → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ DTOs DE EVENTOS (solo clases de datos)
│   │   │       ├── UserCreatedEvent.java               → [CLASS] Evento creación
│   │   │       ├── UserUpdatedEvent.java               → [CLASS] Evento actualización
│   │   │       ├── UserDeletedEvent.java               → [CLASS] Evento eliminación LÓGICA
│   │   │       ├── UserRestoredEvent.java              → [CLASS] Evento restauración
│   │   │       └── UserPurgedEvent.java                → [CLASS] Evento eliminación FÍSICA
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── UserRest.java               → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   └── UserRepositoryImpl.java     → [CLASS] @Repository
│   │       │       ├── external/
│   │       │       │   ├── AuthenticationClientImpl.java → [CLASS] @Component
│   │       │       │   ├── OrganizationClientImpl.java → [CLASS] @Component
│   │       │       │   └── NotificationClientImpl.java → [CLASS] @Component
│   │       │       └── messaging/                      → ⚠️ IMPLEMENTACIÓN DEL PUBLISHER
│   │       │           └── UserEventPublisherImpl.java → [CLASS] Implementa IUserEventPublisher
│   │       ├── messaging/                              → ⚠️ LISTENERS DE EVENTOS EXTERNOS
│   │       │   └── listeners/
│   │       │       └── (vacío - users no escucha eventos externos)
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   └── UserEntity.java                 → [CLASS] @Table("users")
│   │       │   └── repositories/
│   │       │       └── UserR2dbcRepository.java        → [INTERFACE] R2dbcRepository
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── WebClientConfig.java                → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component WebFilter
│   │
│   └── resources/
│       ├── application.yml                             → Base común
│       ├── application-dev.yml                         → Docker local
│       ├── application-prod.yml                        → Docker Compose VPC
│       └── db/migration/
│           └── V1__create_users_table.sql              → SQL Script
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## 2. 📦 vg-ms-authentication {#estructura-authentication}

> **⚠️ IMPORTANTE**: Este servicio es un **PROXY a Keycloak**. NO guarda passwords en base de datos.

```text
vg-ms-authentication/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsauthentication/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   └── UserCredentials.java                → [CLASS] DTO temporal (NO persiste)
│   │   │   │                                             username, password (solo para request)
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ILoginUseCase.java              → [INTERFACE]
│   │   │   │   │   ├── IRegisterUserUseCase.java       → [INTERFACE] Crea usuario en Keycloak
│   │   │   │   │   └── IRefreshTokenUseCase.java       → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IKeycloakClient.java            → [INTERFACE] Admin API Keycloak
│   │   │   │       └── IUserServiceClient.java         → [INTERFACE] WebClient a vg-ms-users
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ExternalServiceException.java       → [CLASS] HTTP 503
│   │   │       ├── InvalidCredentialsException.java    → [CLASS] Login fallido
│   │   │       ├── KeycloakException.java              → [CLASS] Error comunicación Keycloak
│   │   │       └── TokenExpiredException.java          → [CLASS] Token JWT expirado
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── LoginUseCaseImpl.java               → [CLASS] @Service (delega a Keycloak)
│   │   │   │   ├── RegisterUserUseCaseImpl.java        → [CLASS] @Service
│   │   │   │   └── RefreshTokenUseCaseImpl.java        → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java               → [CLASS] { username, password }
│   │   │   │   │   ├── RegisterUserRequest.java        → [CLASS]
│   │   │   │   │   └── RefreshTokenRequest.java        → [CLASS]
│   │   │   │   └── response/
│   │   │   │       └── LoginResponse.java              → [CLASS] { accessToken, refreshToken, expiresIn }
│   │   │   ├── security/
│   │   │   │   └── JwtValidator.java                   → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ SOLO EVENTOS EXTERNOS (no publica)
│   │   │       └── external/                           → DTOs de eventos que ESCUCHA
│   │   │           ├── UserCreatedEvent.java           → [CLASS] DTO del evento externo
│   │   │           ├── UserDeletedEvent.java           → [CLASS] DTO del evento externo
│   │   │           ├── UserRestoredEvent.java          → [CLASS] DTO del evento externo
│   │   │           └── UserPurgedEvent.java            → [CLASS] DTO del evento externo
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── AuthRest.java               → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       └── external/
│   │       │           ├── KeycloakClientImpl.java     → [CLASS] @Component (Admin API)
│   │       │           └── UserServiceClientImpl.java  → [CLASS] @Component
│   │       ├── messaging/                              → ⚠️ LISTENERS DE EVENTOS EXTERNOS
│   │       │   └── listeners/
│   │       │       └── UserEventListener.java          → [CLASS] @RabbitListener
│   │       │           │                                 Escucha: user.created → Crear en Keycloak
│   │       │           │                                 Escucha: user.deleted → Deshabilitar en Keycloak
│   │       │           │                                 Escucha: user.restored → Habilitar en Keycloak
│   │       │           │                                 Escucha: user.purged → Eliminar de Keycloak
│   │       └── config/
│   │           ├── KeycloakConfig.java                 → [CLASS] Keycloak Admin Client
│   │           ├── WebClientConfig.java                → [CLASS]
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration (Queues y Bindings)
│   │           ├── Resilience4jConfig.java             → [CLASS] Circuit Breaker
│   │           └── SecurityConfig.java                 → [CLASS]
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

**NOTAS**:

- ❌ NO hay tabla `credentials` ni PostgreSQL
- ✅ TODA la autenticación se maneja en Keycloak
- ✅ Este servicio solo CONSULTA y CREA usuarios en Keycloak via Admin API
- ✅ ESCUCHA eventos de `jass.events` (routing keys: `user.*`) para sincronizar Keycloak

---

## 3. 📦 vg-ms-organizations {#estructura-organizations}

```text
vg-ms-organizations/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsorganizations/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Organization.java                   → [CLASS] Organización/JASS
│   │   │   │   ├── Zone.java                           → [CLASS] Zonas geográficas
│   │   │   │   ├── Street.java                         → [CLASS] Calles por zona
│   │   │   │   ├── Fare.java                           → [CLASS] Tarifas
│   │   │   │   ├── Parameter.java                      → [CLASS] Parámetros de configuración
│   │   │   │   └── valueobjects/
│   │   │   │       ├── OrganizationType.java           → [ENUM] JASS, JAAS, OMSABAR
│   │   │   │       ├── FareType.java                   → [ENUM] MONTHLY_FEE, INSTALLATION_FEE, etc.
│   │   │   │       ├── StreetType.java                 → [ENUM] JR, AV, CALLE, PASAJE
│   │   │   │       ├── ParameterType.java              → [ENUM] BILLING_DAY, GRACE_PERIOD, etc.
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateOrganizationUseCase.java → [INTERFACE]
│   │   │   │   │   ├── IUpdateOrganizationUseCase.java → [INTERFACE]
│   │   │   │   │   ├── IDeleteOrganizationUseCase.java → [INTERFACE] Soft delete
│   │   │   │   │   ├── IRestoreOrganizationUseCase.java → [INTERFACE] Restaurar
│   │   │   │   │   ├── ICreateZoneUseCase.java         → [INTERFACE]
│   │   │   │   │   └── ICreateStreetUseCase.java       → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IOrganizationRepository.java    → [INTERFACE] Reactive
│   │   │   │       ├── IZoneRepository.java            → [INTERFACE]
│   │   │   │       ├── IStreetRepository.java          → [INTERFACE]
│   │   │   │       └── IOrganizationEventPublisher.java → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ConflictException.java              → [CLASS] HTTP 409
│   │   │       ├── OrganizationNotFoundException.java  → [CLASS]
│   │   │       ├── ZoneNotFoundException.java          → [CLASS]
│   │   │       ├── StreetNotFoundException.java        → [CLASS]
│   │   │       ├── FareNotFoundException.java          → [CLASS]
│   │   │       └── DuplicateFareException.java         → [CLASS] Tarifa ya existe
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateOrganizationUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   ├── UpdateOrganizationUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   ├── DeleteOrganizationUseCaseImpl.java  → [CLASS] @Service (Soft delete)
│   │   │   │   ├── RestoreOrganizationUseCaseImpl.java → [CLASS] @Service
│   │   │   │   ├── CreateZoneUseCaseImpl.java          → [CLASS] @Service
│   │   │   │   └── CreateStreetUseCaseImpl.java        → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS]
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS]
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateOrganizationRequest.java  → [CLASS]
│   │   │   │   │   ├── CreateZoneRequest.java          → [CLASS]
│   │   │   │   │   └── CreateStreetRequest.java        → [CLASS]
│   │   │   │   └── response/
│   │   │   │       ├── OrganizationResponse.java       → [CLASS]
│   │   │   │       ├── ZoneResponse.java               → [CLASS]
│   │   │   │       └── StreetResponse.java             → [CLASS]
│   │   │   ├── mappers/
│   │   │   │   ├── OrganizationMapper.java             → [CLASS] @Component
│   │   │   │   ├── ZoneMapper.java                     → [CLASS] @Component
│   │   │   │   └── StreetMapper.java                   → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ DTOs DE EVENTOS (solo clases de datos)
│   │   │       ├── OrganizationCreatedEvent.java       → [CLASS]
│   │   │       ├── OrganizationUpdatedEvent.java       → [CLASS]
│   │   │       ├── OrganizationDeletedEvent.java       → [CLASS] Soft delete
│   │   │       ├── OrganizationRestoredEvent.java      → [CLASS] Restauración
│   │   │       ├── ZoneCreatedEvent.java               → [CLASS]
│   │   │       └── StreetCreatedEvent.java             → [CLASS]
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── OrganizationRest.java       → [CLASS] @RestController
│   │       │   │       ├── ZoneRest.java               → [CLASS] @RestController
│   │       │   │       ├── StreetRest.java             → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   └── OrganizationRepositoryImpl.java → [CLASS] @Repository
│   │       │       └── messaging/                      → ⚠️ IMPLEMENTACIÓN PUBLISHER
│   │       │           └── OrganizationEventPublisherImpl.java → [CLASS] Implementa IOrganizationEventPublisher
│   │       ├── messaging/                              → ⚠️ LISTENERS (si aplica)
│   │       │   └── listeners/
│   │       │       └── (vacío - organizations no escucha eventos externos)
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── OrganizationDocument.java       → [CLASS] @Document("organizations")
│   │       │   │   ├── ZoneDocument.java               → [CLASS] @Document("zones")
│   │       │   │   ├── StreetDocument.java             → [CLASS] @Document("streets")
│   │       │   │   ├── FareDocument.java               → [CLASS] @Document("fares")
│   │       │   │   └── ParameterDocument.java          → [CLASS] @Document("parameters")
│   │       │   └── repositories/
│   │       │       ├── OrganizationMongoRepository.java → [INTERFACE] ReactiveMongoRepository
│   │       │       ├── ZoneMongoRepository.java        → [INTERFACE]
│   │       │       ├── StreetMongoRepository.java      → [INTERFACE]
│   │       │       ├── FareMongoRepository.java        → [INTERFACE]
│   │       │       └── ParameterMongoRepository.java   → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 4. 📦 vg-ms-commercial-operations {#estructura-commercial}

> **Responsabilidad**: Facturación (Recibos), Pagos, Deudas, Cortes de Servicio, Caja Chica.

```text
vg-ms-commercial-operations/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmscommercial/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Receipt.java                        → [CLASS] Recibo mensual (PRINCIPAL)
│   │   │   │   ├── ReceiptDetail.java                  → [CLASS] Detalles del recibo
│   │   │   │   ├── Payment.java                        → [CLASS] Pago principal
│   │   │   │   ├── PaymentDetail.java                  → [CLASS] Detalles/desglose del pago
│   │   │   │   ├── Debt.java                           → [CLASS] Deuda pendiente
│   │   │   │   ├── ServiceCut.java                     → [CLASS] Cortes de servicio
│   │   │   │   ├── PettyCash.java                      → [CLASS] Caja chica
│   │   │   │   ├── PettyCashMovement.java              → [CLASS] Movimientos de caja
│   │   │   │   └── valueobjects/
│   │   │   │       ├── ConceptType.java                → [ENUM] MONTHLY_FEE, INSTALLATION_FEE, etc.
│   │   │   │       ├── ReceiptStatus.java              → [ENUM] PENDING, PAID, OVERDUE, CANCELLED
│   │   │   │       ├── PaymentType.java                → [ENUM] MONTHLY_FEE, INSTALLATION_FEE, etc.
│   │   │   │       ├── PaymentMethod.java              → [ENUM] CASH, BANK_TRANSFER, CARD, YAPE, PLIN
│   │   │   │       ├── PaymentStatus.java              → [ENUM] PENDING, COMPLETED, CANCELLED, FAILED
│   │   │   │       ├── DebtStatus.java                 → [ENUM] PENDING, PARTIAL, PAID, CANCELLED
│   │   │   │       ├── CutReason.java                  → [ENUM] NON_PAYMENT, MAINTENANCE, USER_REQUEST
│   │   │   │       ├── CutStatus.java                  → [ENUM] PENDING, EXECUTED, RECONNECTED, CANCELLED
│   │   │   │       ├── MovementType.java               → [ENUM] IN, OUT, ADJUSTMENT
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateReceiptUseCase.java      → [INTERFACE]
│   │   │   │   │   ├── IGetReceiptUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── ICancelReceiptUseCase.java      → [INTERFACE] Soft delete
│   │   │   │   │   ├── ICreatePaymentUseCase.java      → [INTERFACE]
│   │   │   │   │   ├── IGetPaymentUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── ICancelPaymentUseCase.java      → [INTERFACE] Soft delete
│   │   │   │   │   ├── ICreateDebtUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IGetDebtUseCase.java            → [INTERFACE]
│   │   │   │   │   ├── ICreateServiceCutUseCase.java   → [INTERFACE]
│   │   │   │   │   ├── IGetServiceCutUseCase.java      → [INTERFACE]
│   │   │   │   │   ├── ICreatePettyCashUseCase.java    → [INTERFACE]
│   │   │   │   │   ├── IGetPettyCashUseCase.java       → [INTERFACE]
│   │   │   │   │   ├── IRegisterMovementUseCase.java   → [INTERFACE]
│   │   │   │   │   └── IGetPettyCashBalanceUseCase.java → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IReceiptRepository.java         → [INTERFACE]
│   │   │   │       ├── IPaymentRepository.java         → [INTERFACE]
│   │   │   │       ├── IDebtRepository.java            → [INTERFACE]
│   │   │   │       ├── IServiceCutRepository.java      → [INTERFACE]
│   │   │   │       ├── IPettyCashRepository.java       → [INTERFACE]
│   │   │   │       ├── IUserServiceClient.java         → [INTERFACE] WebClient a vg-ms-users
│   │   │   │       ├── IInfrastructureClient.java      → [INTERFACE] WebClient a vg-ms-infrastructure
│   │   │   │       ├── INotificationClient.java        → [INTERFACE] WhatsApp/Email
│   │   │   │       └── ICommercialEventPublisher.java  → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ConflictException.java              → [CLASS] HTTP 409
│   │   │       ├── ExternalServiceException.java       → [CLASS] HTTP 503
│   │   │       ├── ReceiptNotFoundException.java       → [CLASS]
│   │   │       ├── PaymentNotFoundException.java       → [CLASS]
│   │   │       ├── DebtNotFoundException.java          → [CLASS]
│   │   │       ├── ServiceCutNotFoundException.java    → [CLASS]
│   │   │       ├── PettyCashNotFoundException.java     → [CLASS]
│   │   │       ├── DuplicatePaymentException.java      → [CLASS] Pago duplicado
│   │   │       └── InsufficientBalanceException.java   → [CLASS] Saldo insuficiente
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── receipt/
│   │   │   │   │   ├── CreateReceiptUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   │   ├── GetReceiptUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   │   ├── CancelReceiptUseCaseImpl.java   → [CLASS] @Service (Soft delete)
│   │   │   │   │   └── GenerateMonthlyReceiptsUseCaseImpl.java → [CLASS] @Service (Job)
│   │   │   │   ├── payment/
│   │   │   │   │   ├── CreatePaymentUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   │   ├── GetPaymentUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   │   ├── CancelPaymentUseCaseImpl.java   → [CLASS] @Service (Soft delete)
│   │   │   │   │   └── ProcessPaymentUseCaseImpl.java  → [CLASS] @Service (Actualiza deudas)
│   │   │   │   ├── debt/
│   │   │   │   │   ├── CreateDebtUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   │   ├── GetDebtUseCaseImpl.java         → [CLASS] @Service
│   │   │   │   │   └── UpdateDebtStatusUseCaseImpl.java → [CLASS] @Service
│   │   │   │   ├── servicecut/
│   │   │   │   │   ├── CreateServiceCutUseCaseImpl.java → [CLASS] @Service
│   │   │   │   │   ├── GetServiceCutUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   │   └── ExecuteServiceCutUseCaseImpl.java → [CLASS] @Service
│   │   │   │   └── pettycash/
│   │   │   │       ├── CreatePettyCashUseCaseImpl.java → [CLASS] @Service
│   │   │   │       ├── GetPettyCashUseCaseImpl.java    → [CLASS] @Service
│   │   │   │       ├── RegisterMovementUseCaseImpl.java → [CLASS] @Service
│   │   │   │       └── GetPettyCashBalanceUseCaseImpl.java → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateReceiptRequest.java       → [CLASS] @Valid
│   │   │   │   │   ├── CreatePaymentRequest.java       → [CLASS] @Valid
│   │   │   │   │   ├── CreateDebtRequest.java          → [CLASS] @Valid
│   │   │   │   │   ├── CreateServiceCutRequest.java    → [CLASS] @Valid
│   │   │   │   │   ├── CreatePettyCashRequest.java     → [CLASS] @Valid
│   │   │   │   │   └── RegisterMovementRequest.java    → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       ├── ReceiptResponse.java            → [CLASS] DTO (incluye detalles)
│   │   │   │       ├── PaymentResponse.java            → [CLASS] DTO (incluye detalles)
│   │   │   │       ├── DebtResponse.java               → [CLASS] DTO
│   │   │   │       ├── ServiceCutResponse.java         → [CLASS] DTO
│   │   │   │       ├── PettyCashResponse.java          → [CLASS] DTO
│   │   │   │       └── PettyCashMovementResponse.java  → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   ├── ReceiptMapper.java                  → [CLASS] @Component
│   │   │   │   ├── PaymentMapper.java                  → [CLASS] @Component
│   │   │   │   ├── DebtMapper.java                     → [CLASS] @Component
│   │   │   │   ├── ServiceCutMapper.java               → [CLASS] @Component
│   │   │   │   ├── PettyCashMapper.java                → [CLASS] @Component
│   │   │   │   └── PettyCashMovementMapper.java        → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ DTOs DE EVENTOS (solo clases de datos)
│   │   │       ├── ReceiptGeneratedEvent.java          → [CLASS]
│   │   │       ├── ReceiptCancelledEvent.java          → [CLASS] Soft delete
│   │   │       ├── PaymentCreatedEvent.java            → [CLASS]
│   │   │       ├── PaymentCompletedEvent.java          → [CLASS]
│   │   │       ├── PaymentCancelledEvent.java          → [CLASS] Soft delete
│   │   │       ├── DebtCreatedEvent.java               → [CLASS]
│   │   │       ├── DebtPaidEvent.java                  → [CLASS]
│   │   │       ├── ServiceCutScheduledEvent.java       → [CLASS]
│   │   │       ├── ServiceCutExecutedEvent.java        → [CLASS]
│   │   │       ├── ServiceReconnectedEvent.java        → [CLASS]
│   │   │       └── PettyCashMovementEvent.java         → [CLASS]
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── ReceiptRest.java            → [CLASS] @RestController
│   │       │   │       ├── PaymentRest.java            → [CLASS] @RestController
│   │       │   │       ├── DebtRest.java               → [CLASS] @RestController
│   │       │   │       ├── ServiceCutRest.java         → [CLASS] @RestController
│   │       │   │       ├── PettyCashRest.java          → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   ├── ReceiptRepositoryImpl.java  → [CLASS] @Repository
│   │       │       │   ├── PaymentRepositoryImpl.java  → [CLASS] @Repository
│   │       │       │   ├── DebtRepositoryImpl.java     → [CLASS] @Repository
│   │       │       │   ├── ServiceCutRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   └── PettyCashRepositoryImpl.java → [CLASS] @Repository
│   │       │       ├── external/
│   │       │       │   ├── UserServiceClientImpl.java  → [CLASS] @Component
│   │       │       │   ├── InfrastructureClientImpl.java → [CLASS] @Component
│   │       │       │   └── NotificationClientImpl.java → [CLASS] @Component
│   │       │       └── messaging/                      → ⚠️ IMPLEMENTACIÓN PUBLISHER
│   │       │           └── CommercialEventPublisherImpl.java → [CLASS] Implementa ICommercialEventPublisher
│   │       ├── messaging/                              → ⚠️ LISTENERS (si aplica)
│   │       │   └── listeners/
│   │       │       └── (vacío - commercial no escucha eventos externos)
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── ReceiptEntity.java              → [CLASS] @Table("receipts")
│   │       │   │   ├── ReceiptDetailEntity.java        → [CLASS] @Table("receipt_details")
│   │       │   │   ├── PaymentEntity.java              → [CLASS] @Table("payments")
│   │       │   │   ├── PaymentDetailEntity.java        → [CLASS] @Table("payment_details")
│   │       │   │   ├── DebtEntity.java                 → [CLASS] @Table("debts")
│   │       │   │   ├── ServiceCutEntity.java           → [CLASS] @Table("service_cuts")
│   │       │   │   ├── PettyCashEntity.java            → [CLASS] @Table("petty_cash")
│   │       │   │   └── PettyCashMovementEntity.java    → [CLASS] @Table("petty_cash_movements")
│   │       │   └── repositories/
│   │       │       ├── ReceiptR2dbcRepository.java     → [INTERFACE] R2dbcRepository
│   │       │       ├── ReceiptDetailR2dbcRepository.java → [INTERFACE]
│   │       │       ├── PaymentR2dbcRepository.java     → [INTERFACE]
│   │       │       ├── PaymentDetailR2dbcRepository.java → [INTERFACE]
│   │       │       ├── DebtR2dbcRepository.java        → [INTERFACE]
│   │       │       ├── ServiceCutR2dbcRepository.java  → [INTERFACE]
│   │       │       ├── PettyCashR2dbcRepository.java   → [INTERFACE]
│   │       │       └── PettyCashMovementR2dbcRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           ├── WebClientConfig.java                → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_payments_table.sql
│           ├── V2__create_payment_details_table.sql
│           ├── V3__create_debts_table.sql
│           ├── V4__create_receipts_table.sql
│           ├── V5__create_receipt_details_table.sql
│           ├── V6__create_service_cuts_table.sql
│           ├── V7__create_petty_cash_table.sql
│           └── V8__create_petty_cash_movements_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 5. 📦 vg-ms-water-quality {#estructura-water-quality}

```text
vg-ms-water-quality/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmswaterquality/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── TestingPoint.java                   → [CLASS] Puntos de muestreo
│   │   │   │   ├── QualityTest.java                    → [CLASS] Pruebas de calidad
│   │   │   │   └── valueobjects/
│   │   │   │       ├── PointType.java                  → [ENUM] RESERVOIR, TAP, WELL, SOURCE
│   │   │   │       ├── TestType.java                   → [ENUM] CHLORINE, PH, TURBIDITY, BACTERIOLOGICAL, CHEMICAL
│   │   │   │       ├── TestResult.java                 → [ENUM] APPROVED, REJECTED, REQUIRES_TREATMENT
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateTestingPointUseCase.java → [INTERFACE]
│   │   │   │   │   ├── IGetTestingPointUseCase.java    → [INTERFACE]
│   │   │   │   │   ├── IUpdateTestingPointUseCase.java → [INTERFACE]
│   │   │   │   │   ├── IDeleteTestingPointUseCase.java → [INTERFACE] Soft delete
│   │   │   │   │   ├── IRestoreTestingPointUseCase.java → [INTERFACE]
│   │   │   │   │   ├── ICreateQualityTestUseCase.java  → [INTERFACE]
│   │   │   │   │   ├── IGetQualityTestUseCase.java     → [INTERFACE]
│   │   │   │   │   └── IUpdateQualityTestUseCase.java  → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── ITestingPointRepository.java    → [INTERFACE]
│   │   │   │       ├── IQualityTestRepository.java     → [INTERFACE]
│   │   │   │       └── IWaterQualityEventPublisher.java → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ConflictException.java              → [CLASS] HTTP 409
│   │   │       ├── TestingPointNotFoundException.java  → [CLASS]
│   │   │       ├── QualityTestNotFoundException.java   → [CLASS]
│   │   │       └── InvalidTestResultException.java     → [CLASS] Resultado fuera de rango
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── testingpoint/
│   │   │   │   │   ├── CreateTestingPointUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── GetTestingPointUseCaseImpl.java     → [CLASS] @Service
│   │   │   │   │   ├── UpdateTestingPointUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── DeleteTestingPointUseCaseImpl.java  → [CLASS] @Service (Soft delete)
│   │   │   │   │   └── RestoreTestingPointUseCaseImpl.java → [CLASS] @Service
│   │   │   │   └── qualitytest/
│   │   │   │       ├── CreateQualityTestUseCaseImpl.java   → [CLASS] @Service
│   │   │   │       ├── GetQualityTestUseCaseImpl.java      → [CLASS] @Service
│   │   │   │       └── UpdateQualityTestUseCaseImpl.java   → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateTestingPointRequest.java  → [CLASS] @Valid
│   │   │   │   │   ├── UpdateTestingPointRequest.java  → [CLASS] @Valid
│   │   │   │   │   ├── CreateQualityTestRequest.java   → [CLASS] @Valid
│   │   │   │   │   └── UpdateQualityTestRequest.java   → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       ├── TestingPointResponse.java       → [CLASS] DTO
│   │   │   │       └── QualityTestResponse.java        → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   ├── TestingPointMapper.java             → [CLASS] @Component
│   │   │   │   └── QualityTestMapper.java              → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ DTOs DE EVENTOS (solo clases de datos)
│   │   │       ├── TestingPointCreatedEvent.java       → [CLASS]
│   │   │       ├── TestingPointUpdatedEvent.java       → [CLASS]
│   │   │       ├── TestingPointDeletedEvent.java       → [CLASS] Soft delete
│   │   │       ├── TestingPointRestoredEvent.java      → [CLASS]
│   │   │       ├── QualityTestCreatedEvent.java        → [CLASS]
│   │   │       ├── QualityTestUpdatedEvent.java        → [CLASS]
│   │   │       ├── QualityTestCompletedEvent.java      → [CLASS] Cuando se aprueba/rechaza
│   │   │       └── QualityAlertEvent.java              → [CLASS] Cuando resultado es malo
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── TestingPointRest.java       → [CLASS] @RestController
│   │       │   │       ├── QualityTestRest.java        → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   ├── TestingPointRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   └── QualityTestRepositoryImpl.java  → [CLASS] @Repository
│   │       │       └── messaging/                      → ⚠️ IMPLEMENTACIÓN PUBLISHER
│   │       │           └── WaterQualityEventPublisherImpl.java → [CLASS] Implementa IWaterQualityEventPublisher
│   │       ├── messaging/                              → ⚠️ LISTENERS (si aplica)
│   │       │   └── listeners/
│   │       │       └── (vacío - water-quality no escucha eventos externos)
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── TestingPointDocument.java       → [CLASS] @Document("testing_points")
│   │       │   │   └── QualityTestDocument.java        → [CLASS] @Document("quality_tests")
│   │       │   └── repositories/
│   │       │       ├── TestingPointMongoRepository.java → [INTERFACE]
│   │       │       └── QualityTestMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 6. 📦 vg-ms-distribution {#estructura-distribution}

```text
vg-ms-distribution/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsdistribution/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── DistributionProgram.java            → [CLASS] Programa de distribución
│   │   │   │   ├── DistributionRoute.java              → [CLASS] Rutas de distribución
│   │   │   │   ├── DistributionSchedule.java           → [CLASS] Horarios de distribución
│   │   │   │   └── valueobjects/
│   │   │   │       ├── DayOfWeek.java                  → [ENUM] MONDAY, TUESDAY, WEDNESDAY, etc.
│   │   │   │       ├── DistributionStatus.java         → [ENUM] ACTIVE, INACTIVE, SUSPENDED
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateProgramUseCase.java      → [INTERFACE]
│   │   │   │   │   ├── IGetProgramUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IUpdateProgramUseCase.java      → [INTERFACE]
│   │   │   │   │   ├── IDeleteProgramUseCase.java      → [INTERFACE] Soft delete
│   │   │   │   │   ├── IRestoreProgramUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── ICreateRouteUseCase.java        → [INTERFACE]
│   │   │   │   │   ├── IGetRouteUseCase.java           → [INTERFACE]
│   │   │   │   │   ├── ICreateScheduleUseCase.java     → [INTERFACE]
│   │   │   │   │   └── IGetScheduleUseCase.java        → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IProgramRepository.java         → [INTERFACE]
│   │   │   │       ├── IRouteRepository.java           → [INTERFACE]
│   │   │   │       ├── IScheduleRepository.java        → [INTERFACE]
│   │   │   │       └── IDistributionEventPublisher.java → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ConflictException.java              → [CLASS] HTTP 409
│   │   │       ├── ProgramNotFoundException.java       → [CLASS]
│   │   │       ├── RouteNotFoundException.java         → [CLASS]
│   │   │       ├── ScheduleNotFoundException.java      → [CLASS]
│   │   │       └── ScheduleConflictException.java      → [CLASS] Horario superpuesto
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── program/
│   │   │   │   │   ├── CreateProgramUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   │   ├── GetProgramUseCaseImpl.java      → [CLASS] @Service
│   │   │   │   │   ├── UpdateProgramUseCaseImpl.java   → [CLASS] @Service
│   │   │   │   │   ├── DeleteProgramUseCaseImpl.java   → [CLASS] @Service (Soft delete)
│   │   │   │   │   └── RestoreProgramUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   ├── route/
│   │   │   │   │   ├── CreateRouteUseCaseImpl.java     → [CLASS] @Service
│   │   │   │   │   └── GetRouteUseCaseImpl.java        → [CLASS] @Service
│   │   │   │   └── schedule/
│   │   │   │       ├── CreateScheduleUseCaseImpl.java  → [CLASS] @Service
│   │   │   │       └── GetScheduleUseCaseImpl.java     → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateProgramRequest.java       → [CLASS] @Valid
│   │   │   │   │   ├── UpdateProgramRequest.java       → [CLASS] @Valid
│   │   │   │   │   ├── CreateRouteRequest.java         → [CLASS] @Valid
│   │   │   │   │   └── CreateScheduleRequest.java      → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       ├── ProgramResponse.java            → [CLASS] DTO
│   │   │   │       ├── RouteResponse.java              → [CLASS] DTO
│   │   │   │       └── ScheduleResponse.java           → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   ├── ProgramMapper.java                  → [CLASS] @Component
│   │   │   │   ├── RouteMapper.java                    → [CLASS] @Component
│   │   │   │   └── ScheduleMapper.java                 → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ DTOs DE EVENTOS (solo clases de datos)
│   │   │       ├── ProgramCreatedEvent.java            → [CLASS]
│   │   │       ├── ProgramUpdatedEvent.java            → [CLASS]
│   │   │       ├── ProgramDeletedEvent.java            → [CLASS] Soft delete
│   │   │       ├── ProgramRestoredEvent.java           → [CLASS]
│   │   │       ├── RouteCreatedEvent.java              → [CLASS]
│   │   │       ├── ScheduleCreatedEvent.java           → [CLASS]
│   │   │       └── DistributionScheduledEvent.java     → [CLASS] Cuando se programa
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── ProgramRest.java            → [CLASS] @RestController
│   │       │   │       ├── RouteRest.java              → [CLASS] @RestController
│   │       │   │       ├── ScheduleRest.java           → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   ├── ProgramRepositoryImpl.java  → [CLASS] @Repository
│   │       │       │   ├── RouteRepositoryImpl.java    → [CLASS] @Repository
│   │       │       │   └── ScheduleRepositoryImpl.java → [CLASS] @Repository
│   │       │       └── messaging/                      → ⚠️ IMPLEMENTACIÓN PUBLISHER
│   │       │           └── DistributionEventPublisherImpl.java → [CLASS] Implementa IDistributionEventPublisher
│   │       ├── messaging/                              → ⚠️ LISTENERS (si aplica)
│   │       │   └── listeners/
│   │       │       └── (vacío - distribution no escucha eventos externos)
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── DistributionProgramDocument.java → [CLASS] @Document("distribution_programs")
│   │       │   │   ├── DistributionRouteDocument.java  → [CLASS] @Document("distribution_routes")
│   │       │   │   └── DistributionScheduleDocument.java → [CLASS] @Document("distribution_schedules")
│   │       │   └── repositories/
│   │       │       ├── DistributionProgramMongoRepository.java → [INTERFACE]
│   │       │       ├── DistributionRouteMongoRepository.java → [INTERFACE]
│   │       │       └── DistributionScheduleMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 7. 📦 vg-ms-inventory-purchases {#estructura-inventory}

```text
vg-ms-inventory-purchases/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsinventory/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Supplier.java                       → [CLASS] Proveedores
│   │   │   │   ├── Material.java                       → [CLASS] Materiales/Productos
│   │   │   │   ├── ProductCategory.java                → [CLASS] Categorías de productos
│   │   │   │   ├── Purchase.java                       → [CLASS] Orden de compra
│   │   │   │   ├── PurchaseDetail.java                 → [CLASS] Detalle de compra (líneas)
│   │   │   │   ├── InventoryMovement.java              → [CLASS] Kardex/movimientos
│   │   │   │   └── valueobjects/
│   │   │   │       ├── MovementType.java               → [ENUM] IN, OUT, ADJUSTMENT
│   │   │   │       ├── PurchaseStatus.java             → [ENUM] PENDING, RECEIVED, CANCELLED
│   │   │   │       ├── Unit.java                       → [ENUM] UNIT, METERS, KG, LITERS
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateSupplierUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IGetSupplierUseCase.java        → [INTERFACE]
│   │   │   │   │   ├── IUpdateSupplierUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IDeleteSupplierUseCase.java     → [INTERFACE] Soft delete
│   │   │   │   │   ├── IRestoreSupplierUseCase.java    → [INTERFACE]
│   │   │   │   │   ├── ICreateMaterialUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IGetMaterialUseCase.java        → [INTERFACE]
│   │   │   │   │   ├── IUpdateMaterialUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IDeleteMaterialUseCase.java     → [INTERFACE] Soft delete
│   │   │   │   │   ├── IRestoreMaterialUseCase.java    → [INTERFACE]
│   │   │   │   │   ├── ICreatePurchaseUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IGetPurchaseUseCase.java        → [INTERFACE]
│   │   │   │   │   ├── ICancelPurchaseUseCase.java     → [INTERFACE] Soft delete
│   │   │   │   │   ├── IRegisterMovementUseCase.java   → [INTERFACE]
│   │   │   │   │   └── IGetInventoryBalanceUseCase.java → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── ISupplierRepository.java        → [INTERFACE]
│   │   │   │       ├── IMaterialRepository.java        → [INTERFACE]
│   │   │   │       ├── IPurchaseRepository.java        → [INTERFACE]
│   │   │   │       ├── IInventoryMovementRepository.java → [INTERFACE]
│   │   │   │       └── IInventoryEventPublisher.java   → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ConflictException.java              → [CLASS] HTTP 409
│   │   │       ├── SupplierNotFoundException.java      → [CLASS]
│   │   │       ├── MaterialNotFoundException.java      → [CLASS]
│   │   │       ├── PurchaseNotFoundException.java      → [CLASS]
│   │   │       ├── InsufficientStockException.java     → [CLASS] Stock insuficiente
│   │   │       └── DuplicateMaterialCodeException.java → [CLASS] Código duplicado
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── supplier/
│   │   │   │   │   ├── CreateSupplierUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── GetSupplierUseCaseImpl.java     → [CLASS] @Service
│   │   │   │   │   ├── UpdateSupplierUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── DeleteSupplierUseCaseImpl.java  → [CLASS] @Service (Soft delete)
│   │   │   │   │   └── RestoreSupplierUseCaseImpl.java → [CLASS] @Service
│   │   │   │   ├── material/
│   │   │   │   │   ├── CreateMaterialUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── GetMaterialUseCaseImpl.java     → [CLASS] @Service
│   │   │   │   │   ├── UpdateMaterialUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── DeleteMaterialUseCaseImpl.java  → [CLASS] @Service (Soft delete)
│   │   │   │   │   └── RestoreMaterialUseCaseImpl.java → [CLASS] @Service
│   │   │   │   ├── purchase/
│   │   │   │   │   ├── CreatePurchaseUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── GetPurchaseUseCaseImpl.java     → [CLASS] @Service
│   │   │   │   │   ├── CancelPurchaseUseCaseImpl.java  → [CLASS] @Service (Soft delete)
│   │   │   │   │   └── ReceivePurchaseUseCaseImpl.java → [CLASS] @Service (Genera movimiento)
│   │   │   │   └── inventory/
│   │   │   │       ├── RegisterMovementUseCaseImpl.java → [CLASS] @Service
│   │   │   │       └── GetInventoryBalanceUseCaseImpl.java → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateSupplierRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── UpdateSupplierRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── CreateMaterialRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── UpdateMaterialRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── CreatePurchaseRequest.java      → [CLASS] @Valid
│   │   │   │   │   └── RegisterMovementRequest.java    → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       ├── SupplierResponse.java           → [CLASS] DTO
│   │   │   │       ├── MaterialResponse.java           → [CLASS] DTO
│   │   │   │       ├── PurchaseResponse.java           → [CLASS] DTO
│   │   │   │       ├── InventoryMovementResponse.java  → [CLASS] DTO
│   │   │   │       └── InventoryBalanceResponse.java   → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   ├── SupplierMapper.java                 → [CLASS] @Component
│   │   │   │   ├── MaterialMapper.java                 → [CLASS] @Component
│   │   │   │   ├── PurchaseMapper.java                 → [CLASS] @Component
│   │   │   │   └── InventoryMovementMapper.java        → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ DTOs DE EVENTOS (solo clases de datos)
│   │   │       ├── SupplierCreatedEvent.java           → [CLASS]
│   │   │       ├── SupplierUpdatedEvent.java           → [CLASS]
│   │   │       ├── SupplierDeletedEvent.java           → [CLASS] Soft delete
│   │   │       ├── SupplierRestoredEvent.java          → [CLASS]
│   │   │       ├── MaterialCreatedEvent.java           → [CLASS]
│   │   │       ├── MaterialUpdatedEvent.java           → [CLASS]
│   │   │       ├── MaterialDeletedEvent.java           → [CLASS] Soft delete
│   │   │       ├── MaterialRestoredEvent.java          → [CLASS]
│   │   │       ├── PurchaseCreatedEvent.java           → [CLASS]
│   │   │       ├── PurchaseReceivedEvent.java          → [CLASS] Cuando se recibe
│   │   │       ├── PurchaseCancelledEvent.java         → [CLASS] Soft delete
│   │   │       ├── InventoryMovementEvent.java         → [CLASS] Kardex IN/OUT
│   │   │       └── LowStockAlertEvent.java             → [CLASS] Stock mínimo
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── SupplierRest.java           → [CLASS] @RestController
│   │       │   │       ├── MaterialRest.java           → [CLASS] @RestController
│   │       │   │       ├── PurchaseRest.java           → [CLASS] @RestController
│   │       │   │       ├── InventoryMovementRest.java  → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   ├── SupplierRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   ├── MaterialRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   ├── PurchaseRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   └── InventoryMovementRepositoryImpl.java → [CLASS] @Repository
│   │       │       └── messaging/                      → ⚠️ IMPLEMENTACIÓN PUBLISHER
│   │       │           └── InventoryEventPublisherImpl.java → [CLASS] Implementa IInventoryEventPublisher
│   │       ├── messaging/                              → ⚠️ LISTENERS (si aplica)
│   │       │   └── listeners/
│   │       │       └── (vacío - inventory no escucha eventos externos)
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── SupplierEntity.java             → [CLASS] @Table("suppliers")
│   │       │   │   ├── MaterialEntity.java             → [CLASS] @Table("materials")
│   │       │   │   ├── ProductCategoryEntity.java      → [CLASS] @Table("product_categories")
│   │       │   │   ├── PurchaseEntity.java             → [CLASS] @Table("purchases")
│   │       │   │   ├── PurchaseDetailEntity.java       → [CLASS] @Table("purchase_details")
│   │       │   │   └── InventoryMovementEntity.java    → [CLASS] @Table("inventory_movements")
│   │       │   └── repositories/
│   │       │       ├── SupplierR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── MaterialR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── ProductCategoryR2dbcRepository.java → [INTERFACE]
│   │       │       ├── PurchaseR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── PurchaseDetailR2dbcRepository.java → [INTERFACE]
│   │       │       └── InventoryMovementR2dbcRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_suppliers_table.sql
│           ├── V2__create_materials_table.sql
│           ├── V3__create_product_categories_table.sql
│           ├── V4__create_purchases_table.sql
│           ├── V5__create_purchase_details_table.sql
│           └── V6__create_inventory_movements_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 8. 📦 vg-ms-claims-incidents {#estructura-claims}

```text
vg-ms-claims-incidents/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsclaims/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── Complaint.java                      → [CLASS] Quejas de clientes
│   │   │   │   ├── ComplaintCategory.java              → [CLASS] Categorías de quejas
│   │   │   │   ├── ComplaintResponse.java              → [CLASS] Respuestas a quejas
│   │   │   │   ├── Incident.java                       → [CLASS] Incidentes de infraestructura
│   │   │   │   ├── IncidentType.java                   → [CLASS] Tipos de incidentes
│   │   │   │   ├── IncidentResolution.java             → [CLASS] Resoluciones de incidentes
│   │   │   │   └── valueobjects/
│   │   │   │       ├── ComplaintPriority.java          → [ENUM] LOW, MEDIUM, HIGH, URGENT
│   │   │   │       ├── ComplaintStatus.java            → [ENUM] RECEIVED, IN_PROGRESS, RESOLVED, CLOSED
│   │   │   │       ├── ResponseType.java               → [ENUM] INVESTIGACION, SOLUCION, SEGUIMIENTO
│   │   │   │       ├── IncidentSeverity.java           → [ENUM] LOW, MEDIUM, HIGH, CRITICAL
│   │   │   │       ├── IncidentStatus.java             → [ENUM] REPORTED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED
│   │   │   │       ├── ResolutionType.java             → [ENUM] REPARACION_TEMPORAL, REPARACION_COMPLETA, REEMPLAZO
│   │   │   │       ├── MaterialUsed.java               → [VALUE OBJECT] Embedded: productId, quantity, unit, unitCost
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateComplaintUseCase.java    → [INTERFACE]
│   │   │   │   │   ├── IGetComplaintUseCase.java       → [INTERFACE]
│   │   │   │   │   ├── IUpdateComplaintUseCase.java    → [INTERFACE]
│   │   │   │   │   ├── ICloseComplaintUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── ICreateIncidentUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IGetIncidentUseCase.java        → [INTERFACE]
│   │   │   │   │   ├── IUpdateIncidentUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IAssignIncidentUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IResolveIncidentUseCase.java    → [INTERFACE]
│   │   │   │   │   └── ICloseIncidentUseCase.java      → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IComplaintRepository.java       → [INTERFACE]
│   │   │   │       ├── IIncidentRepository.java        → [INTERFACE]
│   │   │   │       ├── IUserServiceClient.java         → [INTERFACE] WebClient a vg-ms-users
│   │   │   │       ├── IInfrastructureClient.java      → [INTERFACE] WebClient a vg-ms-infrastructure
│   │   │   │       └── IClaimsEventPublisher.java      → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ConflictException.java              → [CLASS] HTTP 409
│   │   │       ├── ComplaintNotFoundException.java     → [CLASS]
│   │   │       ├── IncidentNotFoundException.java      → [CLASS]
│   │   │       ├── ComplaintAlreadyClosedException.java → [CLASS]
│   │   │       └── IncidentAlreadyResolvedException.java → [CLASS]
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── complaint/
│   │   │   │   │   ├── CreateComplaintUseCaseImpl.java → [CLASS] @Service
│   │   │   │   │   ├── GetComplaintUseCaseImpl.java    → [CLASS] @Service
│   │   │   │   │   ├── UpdateComplaintUseCaseImpl.java → [CLASS] @Service
│   │   │   │   │   ├── AddResponseUseCaseImpl.java     → [CLASS] @Service
│   │   │   │   │   └── CloseComplaintUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   └── incident/
│   │   │   │       ├── CreateIncidentUseCaseImpl.java  → [CLASS] @Service
│   │   │   │       ├── GetIncidentUseCaseImpl.java     → [CLASS] @Service
│   │   │   │       ├── UpdateIncidentUseCaseImpl.java  → [CLASS] @Service
│   │   │   │       ├── AssignIncidentUseCaseImpl.java  → [CLASS] @Service
│   │   │   │       ├── ResolveIncidentUseCaseImpl.java → [CLASS] @Service
│   │   │   │       └── CloseIncidentUseCaseImpl.java   → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateComplaintRequest.java     → [CLASS] @Valid
│   │   │   │   │   ├── UpdateComplaintRequest.java     → [CLASS] @Valid
│   │   │   │   │   ├── AddResponseRequest.java         → [CLASS] @Valid
│   │   │   │   │   ├── CreateIncidentRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── UpdateIncidentRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── AssignIncidentRequest.java      → [CLASS] @Valid
│   │   │   │   │   └── ResolveIncidentRequest.java     → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       ├── ComplaintResponse.java          → [CLASS] DTO
│   │   │   │       ├── ComplaintDetailResponse.java    → [CLASS] DTO (con respuestas)
│   │   │   │       ├── IncidentResponse.java           → [CLASS] DTO
│   │   │   │       └── IncidentDetailResponse.java     → [CLASS] DTO (con resolución)
│   │   │   ├── mappers/
│   │   │   │   ├── ComplaintMapper.java                → [CLASS] @Component
│   │   │   │   └── IncidentMapper.java                 → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ DTOs DE EVENTOS (solo clases de datos)
│   │   │       ├── ComplaintCreatedEvent.java          → [CLASS]
│   │   │       ├── ComplaintUpdatedEvent.java          → [CLASS]
│   │   │       ├── ComplaintResponseAddedEvent.java    → [CLASS]
│   │   │       ├── ComplaintClosedEvent.java           → [CLASS]
│   │   │       ├── IncidentCreatedEvent.java           → [CLASS]
│   │   │       ├── IncidentAssignedEvent.java          → [CLASS]
│   │   │       ├── IncidentUpdatedEvent.java           → [CLASS]
│   │   │       ├── IncidentResolvedEvent.java          → [CLASS]
│   │   │       ├── IncidentClosedEvent.java            → [CLASS]
│   │   │       └── UrgentIncidentAlertEvent.java       → [CLASS] Severidad CRITICAL
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── ComplaintRest.java          → [CLASS] @RestController
│   │       │   │       ├── IncidentRest.java           → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   ├── ComplaintRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   └── IncidentRepositoryImpl.java → [CLASS] @Repository
│   │       │       ├── external/
│   │       │       │   ├── UserServiceClientImpl.java  → [CLASS] @Component
│   │       │       │   └── InfrastructureClientImpl.java → [CLASS] @Component
│   │       │       └── messaging/                      → ⚠️ IMPLEMENTACIÓN PUBLISHER
│   │       │           └── ClaimsEventPublisherImpl.java → [CLASS] Implementa IClaimsEventPublisher
│   │       ├── messaging/                              → ⚠️ LISTENERS (si aplica)
│   │       │   └── listeners/
│   │       │       └── (vacío - claims no escucha eventos externos)
│   │       ├── persistence/
│   │       │   ├── documents/
│   │       │   │   ├── ComplaintDocument.java          → [CLASS] @Document("complaints")
│   │       │   │   ├── ComplaintCategoryDocument.java  → [CLASS] @Document("complaint_categories")
│   │       │   │   ├── ComplaintResponseDocument.java  → [CLASS] @Document("complaint_responses")
│   │       │   │   ├── IncidentDocument.java           → [CLASS] @Document("incidents")
│   │       │   │   ├── IncidentTypeDocument.java       → [CLASS] @Document("incident_types")
│   │       │   │   └── IncidentResolutionDocument.java → [CLASS] @Document("incident_resolutions")
│   │       │   └── repositories/
│   │       │       ├── ComplaintMongoRepository.java   → [INTERFACE]
│   │       │       ├── ComplaintCategoryMongoRepository.java → [INTERFACE]
│   │       │       ├── ComplaintResponseMongoRepository.java → [INTERFACE]
│   │       │       ├── IncidentMongoRepository.java    → [INTERFACE]
│   │       │       ├── IncidentTypeMongoRepository.java → [INTERFACE]
│   │       │       └── IncidentResolutionMongoRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── MongoConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component (Lee headers)
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 9. 📦 vg-ms-infrastructure {#estructura-infrastructure}

```text
vg-ms-infrastructure/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsinfrastructure/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── WaterBox.java                       → [CLASS] Caja de agua principal
│   │   │   │   ├── WaterBoxAssignment.java             → [CLASS] Asignación de caja a usuario
│   │   │   │   ├── WaterBoxTransfer.java               → [CLASS] Transferencia entre usuarios
│   │   │   │   └── valueobjects/
│   │   │   │       ├── BoxType.java                    → [ENUM] RESIDENTIAL, COMMERCIAL, COMMUNAL, INSTITUTIONAL
│   │   │   │       ├── AssignmentStatus.java           → [ENUM] ACTIVE, INACTIVE, TRANSFERRED
│   │   │   │       └── RecordStatus.java               → [ENUM] ACTIVE, INACTIVE, SUSPENDED
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateWaterBoxUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IGetWaterBoxUseCase.java        → [INTERFACE]
│   │   │   │   │   ├── IUpdateWaterBoxUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── IDeleteWaterBoxUseCase.java     → [INTERFACE] Soft delete
│   │   │   │   │   ├── IRestoreWaterBoxUseCase.java    → [INTERFACE]
│   │   │   │   │   ├── IAssignWaterBoxUseCase.java     → [INTERFACE]
│   │   │   │   │   ├── ITransferWaterBoxUseCase.java   → [INTERFACE]
│   │   │   │   │   ├── ISuspendWaterBoxUseCase.java    → [INTERFACE] Corte de servicio
│   │   │   │   │   └── IReconnectWaterBoxUseCase.java  → [INTERFACE] Reconexión
│   │   │   │   └── out/
│   │   │   │       ├── IWaterBoxRepository.java        → [INTERFACE]
│   │   │   │       ├── IWaterBoxAssignmentRepository.java → [INTERFACE]
│   │   │   │       ├── IWaterBoxTransferRepository.java → [INTERFACE]
│   │   │   │       └── IInfrastructureEventPublisher.java → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/                             → ⚠️ UBICACIÓN CORRECTA
│   │   │       ├── DomainException.java                → [ABSTRACT] Clase base
│   │   │       ├── NotFoundException.java              → [CLASS] HTTP 404
│   │   │       ├── BusinessRuleException.java          → [CLASS] HTTP 400
│   │   │       ├── ConflictException.java              → [CLASS] HTTP 409
│   │   │       ├── WaterBoxNotFoundException.java      → [CLASS]
│   │   │       ├── AssignmentNotFoundException.java    → [CLASS]
│   │   │       ├── WaterBoxAlreadyAssignedException.java → [CLASS]
│   │   │       ├── WaterBoxAlreadySuspendedException.java → [CLASS]
│   │   │       └── TransferNotAllowedException.java    → [CLASS] Transferencia inválida
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── waterbox/
│   │   │   │   │   ├── CreateWaterBoxUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── GetWaterBoxUseCaseImpl.java     → [CLASS] @Service
│   │   │   │   │   ├── UpdateWaterBoxUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   │   ├── DeleteWaterBoxUseCaseImpl.java  → [CLASS] @Service (Soft delete)
│   │   │   │   │   ├── RestoreWaterBoxUseCaseImpl.java → [CLASS] @Service
│   │   │   │   │   ├── SuspendWaterBoxUseCaseImpl.java → [CLASS] @Service (Corte)
│   │   │   │   │   └── ReconnectWaterBoxUseCaseImpl.java → [CLASS] @Service
│   │   │   │   ├── assignment/
│   │   │   │   │   └── AssignWaterBoxUseCaseImpl.java  → [CLASS] @Service
│   │   │   │   └── transfer/
│   │   │   │       └── TransferWaterBoxUseCaseImpl.java → [CLASS] @Service
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (Wrapper)
│   │   │   │   │   ├── PageResponse.java               → [CLASS] ✅ ESTÁNDAR (Paginación)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (Errores)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateWaterBoxRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── UpdateWaterBoxRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── AssignWaterBoxRequest.java      → [CLASS] @Valid
│   │   │   │   │   ├── TransferWaterBoxRequest.java    → [CLASS] @Valid
│   │   │   │   │   └── SuspendWaterBoxRequest.java     → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       ├── WaterBoxResponse.java           → [CLASS] DTO
│   │   │   │       ├── WaterBoxDetailResponse.java     → [CLASS] DTO (con asignaciones)
│   │   │   │       ├── WaterBoxAssignmentResponse.java → [CLASS] DTO
│   │   │   │       └── WaterBoxTransferResponse.java   → [CLASS] DTO
│   │   │   ├── mappers/
│   │   │   │   ├── WaterBoxMapper.java                 → [CLASS] @Component
│   │   │   │   ├── WaterBoxAssignmentMapper.java       → [CLASS] @Component
│   │   │   │   └── WaterBoxTransferMapper.java         → [CLASS] @Component
│   │   │   └── events/                                 → ⚠️ DTOs DE EVENTOS (solo clases de datos)
│   │   │       ├── WaterBoxCreatedEvent.java           → [CLASS]
│   │   │       ├── WaterBoxUpdatedEvent.java           → [CLASS]
│   │   │       ├── WaterBoxDeletedEvent.java           → [CLASS] Soft delete
│   │   │       ├── WaterBoxRestoredEvent.java          → [CLASS]
│   │   │       ├── WaterBoxAssignedEvent.java          → [CLASS]
│   │   │       ├── WaterBoxTransferredEvent.java       → [CLASS]
│   │   │       ├── WaterBoxSuspendedEvent.java         → [CLASS] Corte de servicio
│   │   │       ├── WaterBoxReconnectedEvent.java       → [CLASS] Reconexión
│   │   │       └── external/                           → DTOs de eventos que ESCUCHA
│   │   │           ├── ServiceCutScheduledEvent.java   → [CLASS] De commercial-operations
│   │   │           └── IncidentCreatedEvent.java       → [CLASS] De claims-incidents
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   └── rest/
│   │       │   │       ├── WaterBoxRest.java           → [CLASS] @RestController
│   │       │   │       ├── WaterBoxAssignmentRest.java → [CLASS] @RestController
│   │       │   │       ├── WaterBoxTransferRest.java   → [CLASS] @RestController
│   │       │   │       └── GlobalExceptionHandler.java → [CLASS] @RestControllerAdvice
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   ├── WaterBoxRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   ├── WaterBoxAssignmentRepositoryImpl.java → [CLASS] @Repository
│   │       │       │   └── WaterBoxTransferRepositoryImpl.java → [CLASS] @Repository
│   │       │       └── messaging/                      → ⚠️ IMPLEMENTACIÓN PUBLISHER
│   │       │           └── InfrastructureEventPublisherImpl.java → [CLASS] Implementa IInfrastructureEventPublisher
│   │       ├── messaging/                              → ⚠️ LISTENERS DE EVENTOS EXTERNOS
│   │       │   └── listeners/
│   │       │       ├── ServiceCutEventListener.java    → [CLASS] @RabbitListener
│   │       │       │                                     Escucha: service-cut.scheduled → Suspender caja
│   │       │       │                                     Escucha: service-cut.reconnected → Reconectar
│   │       │       └── IncidentEventListener.java      → [CLASS] @RabbitListener
│   │       │                                             Escucha: incident.created → Actualizar estado caja
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── WaterBoxEntity.java             → [CLASS] @Table("water_boxes")
│   │       │   │   ├── WaterBoxAssignmentEntity.java   → [CLASS] @Table("water_box_assignments")
│   │       │   │   └── WaterBoxTransferEntity.java     → [CLASS] @Table("water_box_transfers")
│   │       │   └── repositories/
│   │       │       ├── WaterBoxR2dbcRepository.java    → [INTERFACE]
│   │       │       ├── WaterBoxAssignmentR2dbcRepository.java → [INTERFACE]
│   │       │       └── WaterBoxTransferR2dbcRepository.java → [INTERFACE]
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│   │           └── RequestContextFilter.java           → [CLASS] @Component
│   │
│   └── resources/
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-prod.yml
│       └── db/migration/
│           ├── V1__create_water_boxes_table.sql
│           ├── V2__create_water_box_assignments_table.sql
│           └── V3__create_water_box_transfers_table.sql
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 10. 📦 vg-ms-notification {#estructura-notification}

> **⚠️ IMPORTANTE**: Este servicio es en **Node.js/TypeScript**. Es el HUB de notificaciones.
> **ESCUCHA TODOS los eventos** de otros microservicios para enviar WhatsApp/Email/SMS.

```text
vg-ms-notification/
├── src/
│   ├── index.ts                                        → [FILE] Express server + RabbitMQ consumer
│   ├── routes/
│   │   ├── whatsapp.routes.ts                          → [FILE] Rutas WhatsApp
│   │   ├── email.routes.ts                             → [FILE] Rutas Email
│   │   └── sms.routes.ts                               → [FILE] Rutas SMS
│   ├── controllers/
│   │   ├── whatsapp.controller.ts                      → [FILE] Lógica envío WhatsApp
│   │   ├── email.controller.ts                         → [FILE] Lógica envío Email
│   │   └── sms.controller.ts                           → [FILE] Lógica envío SMS
│   ├── services/
│   │   ├── whatsapp.service.ts                         → [FILE] Twilio WhatsApp API
│   │   ├── email.service.ts                            → [FILE] SendGrid/Nodemailer
│   │   └── sms.service.ts                              → [FILE] Twilio SMS API
│   ├── messaging/                                      → ⚠️ LISTENERS DE TODOS LOS EVENTOS
│   │   ├── consumer.ts                                 → [FILE] RabbitMQ Consumer principal
│   │   └── handlers/
│   │       ├── user.handler.ts                         → [FILE] Escucha: user.created, user.updated, user.deleted
│   │       ├── payment.handler.ts                      → [FILE] Escucha: payment.completed, payment.cancelled
│   │       ├── receipt.handler.ts                      → [FILE] Escucha: receipt.generated, receipt.overdue
│   │       ├── service-cut.handler.ts                  → [FILE] Escucha: service-cut.scheduled, service-cut.executed
│   │       ├── complaint.handler.ts                    → [FILE] Escucha: complaint.created, complaint.resolved
│   │       ├── incident.handler.ts                     → [FILE] Escucha: incident.created, urgent-incident.alert
│   │       ├── quality.handler.ts                      → [FILE] Escucha: quality-test.completed, quality.alert
│   │       └── distribution.handler.ts                 → [FILE] Escucha: distribution.scheduled
│   ├── templates/                                      → [FOLDER] Plantillas de mensajes
│   │   ├── whatsapp/
│   │   │   ├── welcome.template.ts                     → [FILE] Bienvenida nuevo usuario
│   │   │   ├── payment-confirmation.template.ts        → [FILE] Confirmación de pago
│   │   │   ├── receipt-generated.template.ts           → [FILE] Nuevo recibo generado
│   │   │   ├── overdue-reminder.template.ts            → [FILE] Recordatorio de mora
│   │   │   ├── service-cut-warning.template.ts         → [FILE] Aviso de corte programado
│   │   │   ├── service-cut-executed.template.ts        → [FILE] Notificación de corte ejecutado
│   │   │   ├── complaint-received.template.ts          → [FILE] Queja recibida
│   │   │   ├── complaint-resolved.template.ts          → [FILE] Queja resuelta
│   │   │   └── quality-alert.template.ts               → [FILE] Alerta de calidad de agua
│   │   └── email/
│   │       ├── welcome.template.hbs                    → [FILE] Template Handlebars
│   │       ├── receipt.template.hbs                    → [FILE] Recibo mensual
│   │       └── statement.template.hbs                  → [FILE] Estado de cuenta
│   ├── middlewares/
│   │   ├── auth.middleware.ts                          → [FILE] Validación headers JWT
│   │   └── error.middleware.ts                         → [FILE] Manejo de errores global
│   ├── config/
│   │   ├── twilio.config.ts                            → [FILE] Configuración Twilio
│   │   ├── sendgrid.config.ts                          → [FILE] Configuración SendGrid
│   │   └── rabbitmq.config.ts                          → [FILE] Configuración RabbitMQ
│   ├── exceptions/                                     → ⚠️ EXCEPCIONES PERSONALIZADAS
│   │   ├── DomainError.ts                              → [CLASS] Clase base de errores
│   │   ├── NotificationFailedError.ts                  → [CLASS] Error al enviar notificación
│   │   ├── TemplateNotFoundError.ts                    → [CLASS] Plantilla no encontrada
│   │   └── InvalidRecipientError.ts                    → [CLASS] Destinatario inválido
│   └── types/
│       ├── notification.types.ts                       → [FILE] TypeScript interfaces
│       ├── events.types.ts                             → [FILE] DTOs de eventos recibidos
│       └── templates.types.ts                          → [FILE] Tipos de plantillas
│
├── package.json
├── tsconfig.json
├── .env.example
├── Dockerfile
└── README.md
```

**Eventos que ESCUCHA vg-ms-notification** (todos del exchange `jass.events`):

| Routing Key                | Acción                              |
|----------------------------|-------------------------------------|
| user.created               | Enviar WhatsApp de bienvenida       |
| user.deleted               | Enviar email de despedida           |
| payment.completed          | Enviar confirmación de pago         |
| payment.cancelled          | Notificar pago anulado              |
| receipt.generated          | Enviar recibo mensual               |
| receipt.overdue            | Enviar recordatorio de mora         |
| service-cut.scheduled      | Enviar aviso de corte programado    |
| service-cut.executed       | Notificar corte ejecutado           |
| service.reconnected        | Notificar reconexión                |
| complaint.created          | Confirmar recepción de queja        |
| complaint.resolved         | Notificar resolución                |
| incident.created           | Notificar incidente reportado       |
| urgent-incident.alert      | Alerta urgente a administradores    |
| quality-test.completed     | Informe de calidad disponible       |
| quality.alert              | Alerta de calidad de agua           |
| distribution.scheduled     | Notificar horario de distribución   |

---

## 11. 📦 vg-ms-gateway {#estructura-gateway}

> **Nota**: El Gateway NO maneja excepciones de dominio ni eventos.
> Solo enruta peticiones y valida JWT.

```text
vg-ms-gateway/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsgateway/
│   │   ├── config/
│   │   │   ├── GatewayConfig.java                      → [CLASS] @Configuration (Routes)
│   │   │   ├── SecurityConfig.java                     → [CLASS] ResourceServer JWT
│   │   │   └── Resilience4jConfig.java                 → [CLASS] Circuit Breaker global
│   │   │   └── ⚠️ CORS se configura en application.yml (spring.cloud.gateway.globalcors)
│   │   ├── filters/
│   │   │   ├── AuthenticationFilter.java               → [CLASS] Pre-filter JWT validation
│   │   │   ├── TenantFilter.java                       → [CLASS] Extract organization_id
│   │   │   ├── RateLimitFilter.java                    → [CLASS] Redis Rate Limiter
│   │   │   └── LoggingFilter.java                      → [CLASS] Request/Response logging
│   │   ├── exceptions/
│   │   │   ├── GatewayErrorResponse.java               → [CLASS] Formato de error del gateway
│   │   │   └── GlobalErrorHandler.java                 → [CLASS] @ControllerAdvice
│   │   └── GatewayApplication.java                     → [CLASS] @SpringBootApplication
│   │
│   └── resources/
│       ├── application.yml                             → [CONFIG] Routes Definition
│       ├── application-dev.yml
│       └── application-prod.yml
│
├── Dockerfile
├── pom.xml
└── README.md
```

---

## 📝 NOTAS FINALES

### Tecnologías por Microservicio

| Microservicio              | Base de Datos | Stack Principal                  |
|----------------------------|---------------|----------------------------------|
| vg-ms-users                | PostgreSQL    | Spring WebFlux + R2DBC           |
| vg-ms-authentication       | Keycloak      | Spring WebFlux + Keycloak Admin  |
| vg-ms-organizations        | MongoDB       | Spring WebFlux + Reactive Mongo  |
| vg-ms-commercial-operations| PostgreSQL    | Spring WebFlux + R2DBC           |
| vg-ms-water-quality        | MongoDB       | Spring WebFlux + Reactive Mongo  |
| vg-ms-distribution         | MongoDB       | Spring WebFlux + Reactive Mongo  |
| vg-ms-inventory-purchases  | PostgreSQL    | Spring WebFlux + R2DBC           |
| vg-ms-claims-incidents     | MongoDB       | Spring WebFlux + Reactive Mongo  |
| vg-ms-infrastructure       | PostgreSQL    | Spring WebFlux + R2DBC           |
| vg-ms-notification         | N/A           | Node.js + Express + Twilio       |
| vg-ms-gateway              | N/A           | Spring Cloud Gateway             |

### Principios de Arquitectura

1. **Hexagonal Architecture** (Ports & Adapters) en TODOS los servicios Java
2. **Clean Architecture** con separación domain/application/infrastructure
3. **Reactive Programming** con Reactor (Mono/Flux)
4. **Event-Driven** con RabbitMQ para comunicación asíncrona
5. **Multi-Tenancy** con organization_id en headers
6. **Security** con JWT validation en Gateway
7. **Excepciones en domain/exceptions/** - Nunca en infrastructure
8. **Eventos CRUD completos**: Created, Updated, Deleted (soft), Restored, Purged (hard)
