# 📚 CLEAN CODE Y BUENAS PRÁCTICAS APLICADAS - vg-ms-users

## ✅ ARQUITECTURA HEXAGONAL + DDD

### 🏗️ Separación de Capas

```
Domain (Núcleo de negocio - Sin dependencias externas)
    ↓
Application (Casos de uso - Orquestación)
    ↓
Infrastructure (Detalles técnicos - Adaptadores)
```

#### **1. Domain Layer - Lógica de Negocio Pura**

- ✅ **Models con comportamiento**: `User.java` tiene métodos `activate()`, `deactivate()`, `isActive()`, `getFullName()`
- ✅ **Value Objects inmutables**: `Role`, `DocumentType`, `RecordStatus` (enums)
- ✅ **Ports (Interfaces)**: Define contratos sin implementación
  - `ICreateUserUseCase`, `IGetUserUseCase`, `IUpdateUserUseCase`, `IDeleteUserUseCase`
  - `IUserRepository`, `IAuthenticationClient`, `IOrganizationClient`, `IUserEventPublisher`
- ✅ **Excepciones personalizadas**: `UserNotFoundException`, `DuplicateUserException`, `OrganizationNotFoundException`
- ✅ **Sin dependencias de Spring/Persistencia**: Domain NO conoce R2DBC ni Spring

#### **2. Application Layer - Casos de Uso**

- ✅ **Use Cases con Single Responsibility**:
  - `CreateUserUseCaseImpl`: Solo crea usuarios + validaciones + eventos
  - `GetUserUseCaseImpl`: Solo consultas
  - `UpdateUserUseCaseImpl`: Solo actualiza + eventos
  - `DeleteUserUseCaseImpl`: Soft delete + restore + eventos
- ✅ **Validaciones en Use Cases**:
  - `validateUserDoesNotExist()`: Verifica username y documento duplicados
  - `validateOrganization()`: Verifica que la organización exista
- ✅ **Manejo de errores reactivo**: `switchIfEmpty()`, `onErrorResume()`
- ✅ **Logging estructurado**: `@Slf4j` con logs en puntos críticos
- ✅ **DTOs separados**: Request, Response, Common
  - `CreateUserRequest` con `@Valid`
  - `UpdateUserRequest` (campos opcionales)
  - `UserResponse` (sin password)
  - `ApiResponse<T>` genérico
  - `ErrorMessage` para validaciones

#### **3. Infrastructure Layer - Detalles Técnicos**

- ✅ **Adapters In (REST)**:
  - `UserRest`: Implementa API RESTful con todos los endpoints
  - Usa `@Valid` para validación automática
  - Retorna `Mono/Flux` reactivos
  - Maneja códigos HTTP correctos (201, 404, 204)
- ✅ **Adapters Out (Persistencia + Externos)**:
  - `UserRepositoryImpl`: Implementa `IUserRepository` usando R2DBC
  - `AuthenticationClientImpl`: WebClient + Circuit Breaker
  - `OrganizationClientImpl`: WebClient + Circuit Breaker
  - `NotificationClientImpl`: WebClient + Circuit Breaker
- ✅ **Entities separadas del Domain**: `UserEntity` con anotaciones R2DBC

---

## ✅ MAPPERS - COMUNICACIÓN ENTRE CAPAS

### 🔄 UserMapper (Responsabilidad Única: Convertir entre representaciones)

```java
@Component
public class UserMapper {
    // DTO → Domain (API → Lógica de negocio)
    User toDomain(CreateUserRequest request)
    User updateDomain(User existing, UpdateUserRequest request)

    // Domain → DTO (Lógica de negocio → API)
    UserResponse toResponse(User user)

    // Domain ↔ Entity (Lógica de negocio ↔ Persistencia)
    UserEntity toEntity(User user)
    User toDomain(UserEntity entity)
}
```

**Por qué es correcto:**

- ✅ **Centraliza todas las conversiones** en un solo lugar
- ✅ **Evita duplicación de código**: `UserRepositoryImpl` usa el mapper
- ✅ **Single Responsibility**: Solo convierte, no tiene lógica de negocio
- ✅ **Facilita testing**: Mock del mapper en tests
- ✅ **Clean Code**: Nombres descriptivos (`toDomain`, `toEntity`, `toResponse`)

**Antes (❌ MAL)**:

```java
// UserRepositoryImpl tenía métodos privados toEntity() y toDomain()
private UserEntity toEntity(User user) { ... }  // DUPLICACIÓN
private User toDomain(UserEntity entity) { ... } // VIOLACIÓN SRP
```

**Ahora (✅ BIEN)**:

```java
// UserRepositoryImpl DELEGA al mapper
return r2dbcRepository.save(mapper.toEntity(user))
    .map(mapper::toDomain);
```

---

## ✅ CLEAN CODE - PRINCIPIOS APLICADOS

### 📐 SOLID Principles

#### **S - Single Responsibility Principle**

- ✅ `CreateUserUseCaseImpl`: Solo crea usuarios
- ✅ `UserMapper`: Solo convierte entre representaciones
- ✅ `UserEventPublisherImpl`: Solo publica eventos
- ✅ `GlobalExceptionHandler`: Solo maneja excepciones

#### **O - Open/Closed Principle**

- ✅ `IUserRepository` es una interfaz: Abierto para extensión, cerrado para modificación
- ✅ Nuevos casos de uso NO modifican código existente

#### **L - Liskov Substitution Principle**

- ✅ `UserRepositoryImpl implements IUserRepository`: Cumple el contrato
- ✅ Todas las implementaciones de clients cumplen sus interfaces

#### **I - Interface Segregation Principle**

- ✅ Interfaces pequeñas y específicas:
  - `ICreateUserUseCase`: Solo 1 método `execute()`
  - `IGetUserUseCase`: Solo consultas
  - `IUserEventPublisher`: Solo 3 métodos de publicación

#### **D - Dependency Inversion Principle**

- ✅ Use Cases dependen de **interfaces** (`IUserRepository`), no de implementaciones
- ✅ Spring inyecta implementaciones vía `@RequiredArgsConstructor`

### 🎯 Otros Principios Clean Code

#### **DRY (Don't Repeat Yourself)**

- ✅ **Mapper centralizado**: No repetimos conversiones
- ✅ **ApiResponse genérico**: `ApiResponse<T>` reutilizable
- ✅ **Métodos auxiliares**: `mapToUserCreatedEvent()` en publisher

#### **KISS (Keep It Simple, Stupid)**

- ✅ **Métodos cortos**: Máximo 20 líneas por método
- ✅ **Una responsabilidad por método**
- ✅ **Nombres descriptivos**: `validateUserDoesNotExist()`, `publishEvent()`

#### **YAGNI (You Aren't Gonna Need It)**

- ✅ **Solo lo necesario**: No hay código "por si acaso"
- ✅ **Features implementadas**: CRUD + soft delete + restore + eventos

#### **Fail Fast**

- ✅ **Validaciones tempranas**: En `CreateUserUseCaseImpl`

  ```java
  validateUserDoesNotExist(user)
      .then(validateOrganization(user))
      .then(userRepository.save(user))
  ```

- ✅ **Excepciones descriptivas**: `DuplicateUserException("Username already exists: " + username)`

---

## ✅ MANEJO DE EXCEPCIONES

### 🛡️ Excepciones Personalizadas (Domain Layer)

```java
// Excepciones de dominio - SIN dependencias de Spring
public class UserNotFoundException extends RuntimeException { }
public class DuplicateUserException extends RuntimeException { }
public class OrganizationNotFoundException extends RuntimeException { }
```

**Por qué es correcto:**

- ✅ **Expresan errores de negocio**, no técnicos
- ✅ **Viven en Domain Layer**: No dependen de infraestructura
- ✅ **Nombres descriptivos**: Comunican el problema claramente

### 🌐 Global Exception Handler (Infrastructure Layer)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleUserNotFoundException() {
        return Mono.just(ResponseEntity.status(404).body(...));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleDuplicateUserException() {
        return Mono.just(ResponseEntity.status(409).body(...));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ApiResponse<List<ErrorMessage>>>> handleValidationException() {
        // Convierte errores de validación @Valid en respuesta estructurada
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericException() {
        return Mono.just(ResponseEntity.status(500).body(...));
    }
}
```

**Ventajas:**

- ✅ **Centralizado**: Todas las excepciones en un solo lugar
- ✅ **Consistencia**: Todas las respuestas tienen el mismo formato `ApiResponse<T>`
- ✅ **HTTP Status correcto**: 404, 409, 400, 500
- ✅ **Validaciones estructuradas**: Convierte `@Valid` en `List<ErrorMessage>`
- ✅ **Reactivo**: Retorna `Mono<ResponseEntity<>>`

### 🔄 Manejo de Errores Reactivos

```java
// En Use Cases
.switchIfEmpty(Mono.error(new UserNotFoundException("User not found: " + userId)))

// En Event Publisher
.onErrorResume(e -> {
    log.warn("Failed to publish event: {}", e.getMessage());
    return Mono.empty(); // NO fallar el flujo principal por evento
})

// En Circuit Breaker
@CircuitBreaker(name = "authenticationService", fallbackMethod = "createCredentialsFallback")
private Mono<Void> createCredentialsFallback(UUID userId, ..., Exception e) {
    log.warn("Fallback: Cannot create credentials due to: {}", e.getMessage());
    return Mono.empty();
}
```

---

## ✅ EVENTOS Y MENSAJERÍA ASÍNCRONA

### 📨 Publicación de Eventos con RabbitMQ

```java
@Component
public class UserEventPublisherImpl implements IUserEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> publishUserCreated(User user) {
        return Mono.fromRunnable(() -> {
            try {
                UserCreatedEvent event = mapToUserCreatedEvent(user);
                String message = objectMapper.writeValueAsString(event);
                rabbitTemplate.convertAndSend(exchange, userCreatedRoutingKey, message);
                log.info("Published UserCreatedEvent for user: {}", user.getUserId());
            } catch (Exception e) {
                log.error("Error publishing UserCreatedEvent", e);
                throw new RuntimeException("Failed to publish event", e);
            }
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
```

**Por qué es correcto:**

- ✅ **Asíncrono no bloqueante**: `subscribeOn(Schedulers.boundedElastic())`
- ✅ **Serialización a JSON**: `ObjectMapper.writeValueAsString()`
- ✅ **Logging detallado**: Success y error
- ✅ **Separación de concerns**: Publisher implementa interfaz del domain

### 🔧 Configuración RabbitMQ Profesional

```java
@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange usersExchange() { ... }

    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(userCreatedQueue).build();
    }

    @Bean
    public Binding userCreatedBinding() {
        return BindingBuilder.bind(userCreatedQueue())
            .to(usersExchange())
            .with(userCreatedRoutingKey);
    }

    @Bean
    public Jackson2JsonMessageConverter messageConverter() { ... }
}
```

**Ventajas:**

- ✅ **Queues duraderas**: Mensajes persisten entre reinicios
- ✅ **TopicExchange**: Routing basado en patterns
- ✅ **Jackson JSON**: Serialización automática
- ✅ **Bindings declarativos**: Spring crea automáticamente

### 📬 Eventos Publicados

| Evento | Exchange | Routing Key | Cuándo |
|--------|----------|-------------|--------|
| `UserCreatedEvent` | users.exchange | users.created | Después de crear usuario |
| `UserUpdatedEvent` | users.exchange | users.updated | Después de actualizar |
| `UserDeletedEvent` | users.exchange | users.deleted | Después de soft delete |

---

## ✅ CIRCUIT BREAKER Y RESILIENCE

### 🔌 Circuit Breaker con Resilience4j

```java
@Component
public class AuthenticationClientImpl implements IAuthenticationClient {

    @CircuitBreaker(name = "authenticationService", fallbackMethod = "createCredentialsFallback")
    @Retry(name = "authenticationService")
    public Mono<Void> createCredentials(UUID userId, ...) {
        return webClientBuilder.build()
            .post()
            .uri(authenticationUrl + "/api/auth/credentials")
            .bodyValue(...)
            .retrieve()
            .bodyToMono(Void.class);
    }

    private Mono<Void> createCredentialsFallback(UUID userId, ..., Exception e) {
        log.warn("Fallback: Cannot create credentials due to: {}", e.getMessage());
        return Mono.empty(); // Degradación graceful
    }
}
```

**Patrón implementado:**

- ✅ **Circuit Breaker**: Protege contra fallos en servicios externos
- ✅ **Retry**: 3 intentos con 1s de espera
- ✅ **Fallback**: Degradación graceful sin romper el flujo
- ✅ **Logging**: Registra cada fallback para monitoreo

### ⚙️ Configuración Resilience4j

```yaml
resilience4j:
  circuitbreaker:
    instances:
      authenticationService:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 30s
  retry:
    instances:
      authenticationService:
        max-attempts: 3
        wait-duration: 1s
```

**Estados del Circuit Breaker:**

1. **CLOSED**: Normal, llama al servicio
2. **OPEN**: Detectó 50%+ fallos, llama al fallback
3. **HALF_OPEN**: Prueba si el servicio se recuperó

---

## ✅ VALIDACIONES

### 🛡️ Validaciones en Use Cases (Lógica de Negocio)

```java
private Mono<Void> validateUserDoesNotExist(User user) {
    return userRepository.existsByUsername(user.getUsername())
        .flatMap(exists -> exists
            ? Mono.error(new DuplicateUserException("Username already exists"))
            : Mono.empty())
        .then(userRepository.existsByDocumentNumber(user.getDocumentNumber())
            .flatMap(exists -> exists
                ? Mono.error(new DuplicateUserException("Document already exists"))
                : Mono.empty()));
}
```

### ✅ Validaciones en DTOs (Formato)

```java
@Data
public class CreateUserRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    @Email(message = "Email must be valid")
    private String email;

    @NotNull(message = "Role is required")
    private Role role;
}
```

**Controller maneja `@Valid`:**

```java
@PostMapping
public Mono<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
    // Spring valida automáticamente
    // GlobalExceptionHandler convierte errores en ApiResponse
}
```

---

## ✅ LOGGING

```java
@Slf4j
public class CreateUserUseCaseImpl {

    @Override
    public Mono<User> execute(User user, String password) {
        return validateUserDoesNotExist(user)
            .then(userRepository.save(user))
            .doOnSuccess(u -> log.info("User created successfully: {}", u.getUserId()))
            .doOnError(e -> log.error("Error creating user", e));
    }
}
```

**Niveles usados correctamente:**

- `log.info()`: Operaciones exitosas
- `log.warn()`: Fallbacks, eventos no críticos
- `log.error()`: Errores que requieren atención
- `log.debug()`: Información de depuración (en dev)

---

## 📊 RESUMEN DE BUENAS PRÁCTICAS

| Principio | Implementación | ✅ |
|-----------|----------------|---|
| **Arquitectura Hexagonal** | Domain → Application → Infrastructure | ✅ |
| **DDD** | Entidades con comportamiento, Value Objects | ✅ |
| **SOLID** | Interfaces, SRP, DIP | ✅ |
| **Clean Code** | Nombres descriptivos, métodos cortos | ✅ |
| **Mappers** | Conversión centralizada entre capas | ✅ |
| **Excepciones Personalizadas** | UserNotFoundException, DuplicateUserException | ✅ |
| **Global Exception Handler** | Manejo centralizado, respuestas consistentes | ✅ |
| **Circuit Breaker** | Resilience4j + Fallback | ✅ |
| **Eventos Asíncronos** | RabbitMQ + UserCreatedEvent | ✅ |
| **Validaciones** | @Valid en DTOs + lógica en Use Cases | ✅ |
| **Logging** | @Slf4j con niveles correctos | ✅ |
| **Reactivo** | Mono/Flux, no bloqueante | ✅ |
| **Testing Ready** | Interfaces mockeables, inyección de dependencias | ✅ |

---

## 🎓 CONCLUSIÓN

Este microservicio **vg-ms-users** implementa las **mejores prácticas profesionales** de:

- ✅ **Arquitectura Hexagonal** completa
- ✅ **Domain-Driven Design** con entidades ricas
- ✅ **Clean Code** en cada capa
- ✅ **SOLID Principles** aplicados rigurosamente
- ✅ **Manejo robusto de excepciones** (custom + global handler)
- ✅ **Eventos asíncronos** con RabbitMQ
- ✅ **Resiliencia** con Circuit Breaker
- ✅ **Validaciones en múltiples niveles**
- ✅ **Mappers centralizados** para comunicación entre capas
- ✅ **Programación reactiva** no bloqueante
- ✅ **Separación total de concerns**

**Resultado:** Código **mantenible, escalable, testeable y profesional** listo para producción. 🚀
