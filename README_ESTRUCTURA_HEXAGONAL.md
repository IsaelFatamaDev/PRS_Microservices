# 🏗️ ESTRUCTURA BASE - ARQUITECTURA HEXAGONAL + REACTIVE + EVENTS

## Fecha: 20 Enero 2026

---

## 🚀 STACK TECNOLÓGICO

```
┌─────────────────────────────────────────────────────────────────┐
│                    STACK REACTIVO COMPLETO                       │
├─────────────────────────────────────────────────────────────────┤
│ 🌐 Web Framework       → Spring WebFlux (Mono/Flux)            │
│ 🗄️  PostgreSQL          → R2DBC (Reactive Relational)          │
│ 🍃 MongoDB             → Spring Data MongoDB Reactive           │
│ 🐰 Message Broker      → RabbitMQ + Reactor RabbitMQ           │
│ 🔗 REST Client         → WebClient (Reactive HTTP)              │
│ 🛡️  Resilience         → Resilience4j (Circuit Breaker)        │
│ 🐳 Deployment          → Docker Compose + VPC                   │
│ 📦 Paquete Base        → pe.edu.vallegrande.{microservicio}    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📚 TABLA DE CONTENIDOS

1. [Stack Tecnológico](#stack)
2. [Convenciones de Nomenclatura](#convenciones)
3. [Arquitectura Hexagonal](#arquitectura)
4. [Comunicación entre Microservicios](#comunicacion)
5. [ApiResponse y ErrorMessage](#apiresponse)
6. [Estructura de Carpetas Base](#estructura-base)
7. [Estructura por Microservicio](#microservicios)
8. [Eventos con RabbitMQ](#rabbitmq)
9. [Docker Compose](#docker)
10. [Ejemplos de Código Reactivo](#ejemplos)

---

## 🎨 CONVENCIONES DE NOMENCLATURA {#convenciones}

### ⭐ ESTÁNDAR DEFINIDO

```
┌─────────────────┬───────────────────┬────────────────────────────────────┐
│ CAPA/ELEMENTO   │ CONVENCIÓN        │ EJEMPLO                            │
├─────────────────┼───────────────────┼────────────────────────────────────┤
│ Base de Datos   │ snake_case        │ user_id, created_at                │
│ Paquetes Java   │ lowercase         │ pe.edu.vallegrande.vgmsusers       │
│ Clases          │ PascalCase        │ UserEntity, PaymentService         │
│ Interfaces      │ PascalCase + I    │ IUserRepository                    │
│ Campos/Métodos  │ camelCase         │ userId, getUserById()              │
│ Constantes      │ UPPER_SNAKE_CASE  │ MAX_RETRY_ATTEMPTS                 │
│ API Endpoints   │ kebab-case        │ /api/water-boxes                   │
│ JSON Response   │ camelCase         │ {"userId": "..."}                  │
│ Reactive Types  │ Mono/Flux         │ Mono<User>, Flux<Payment>          │
└─────────────────┴───────────────────┴────────────────────────────────────┘
```

---

## 🏛️ ARQUITECTURA HEXAGONAL {#arquitectura}

### Principios Básicos

```
┌──────────────────────────────────────────────────────────────┐
│                    HEXAGONAL ARCHITECTURE                     │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│   DOMAIN (Núcleo - Lógica de Negocio Pura)                  │
│   ├── models/          → Entidades, Value Objects           │
│   ├── ports/           → Interfaces (Contratos)             │
│   │   ├── in/          → Use Cases (entrada)                │
│   │   └── out/         → Repositories, Services (salida)    │
│   └── exceptions/      → Excepciones de dominio             │
│                                                               │
│   APPLICATION (Casos de Uso - Orquestación)                  │
│   ├── usecases/        → Implementación de casos de uso     │
│   ├── services/        → Servicios de aplicación            │
│   ├── mappers/         → DTOs ↔ Domain Models               │
│   └── events/          → Publicadores de eventos            │
│                                                               │
│   INFRASTRUCTURE (Adaptadores - Frameworks)                   │
│   ├── adapters/                                              │
│   │   ├── in/          → REST Controllers, Event Listeners  │
│   │   └── out/         → R2DBC, MongoDB Reactive, RabbitMQ │
│   ├── config/          → Configuraciones Spring             │
│   ├── persistence/     → Entities, Reactive Repositories    │
│   ├── messaging/       → RabbitMQ Producers/Consumers       │
│   └── external/        → WebClient REST Clients             │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

### Flujo de Datos Reactivo

```
HTTP Request → Controller → UseCase → Domain Logic → Repository → Database
  Mono<T>       Mono<T>     Mono<T>      Mono<T>        Mono<T>      Reactive
     ↓             ↓           ↓            ↓             ↓
  [IN ADAPTER]  [APP]      [DOMAIN]      [APP]    [OUT ADAPTER]

Events Flow (Fire & Forget):
UseCase → EventPublisher → RabbitMQ → EventListener → UseCase
           (async)          (queue)     (consumer)      (process)
```

---

## 🔗 COMUNICACIÓN ENTRE MICROSERVICIOS {#comunicacion}

### 🎯 Arquitectura Híbrida: REST + Events

```
┌─────────────────────────────────────────────────────────────────┐
│           COMUNICACIÓN HÍBRIDA: REST + EVENTS                    │
├─────────────────────────────────────────────────────────────────┤
│                                                                  │
│  1️⃣ VALIDACIONES SÍNCRONAS (REST - WebClient Reactivo)         │
│  ════════════════════════════════════════════════════════════   │
│                                                                  │
│  User Service (crear admin)                                     │
│       ↓ HTTP GET (WebClient)                                    │
│  Organization Service: ¿Existe org X?                           │
│       ↓ Mono<Boolean>                                           │
│  Si existe → Crear admin                                        │
│  Si no → Error 400 "Organization not found"                     │
│                                                                  │
│  ✅ Usa WebClient (reactivo, no bloqueante)                     │
│  ✅ Circuit Breaker (Resilience4j)                              │
│  ✅ Timeout: 2 segundos                                         │
│  ✅ Fallback: Error controlado                                  │
│  ✅ Comunicación interna Docker (rápida)                        │
│                                                                  │
│                                                                  │
│  2️⃣ NOTIFICACIONES ASÍNCRONAS (RabbitMQ Events)                │
│  ════════════════════════════════════════════════════════════   │
│                                                                  │
│  User Service (admin creado)                                    │
│       ↓ Publish: AdminCreatedEvent                              │
│  RabbitMQ Exchange                                              │
│       ↓ Route to queues                                         │
│  Notification Service → Envía email de bienvenida               │
│  Organization Service → Actualiza estadísticas                  │
│  Audit Service → Registra evento                                │
│                                                                  │
│  ✅ Desacoplamiento total                                       │
│  ✅ No bloquea respuesta HTTP                                   │
│  ✅ Eventual consistency OK                                     │
│  ✅ Fire & Forget pattern                                       │
│                                                                  │
└─────────────────────────────────────────────────────────────────┘
```

### 📋 Reglas de Decisión: ¿REST o Events?

```
┌──────────────────────────┬─────────────┬────────────────────────┐
│ CASO DE USO              │ MÉTODO      │ RAZÓN                  │
├──────────────────────────┼─────────────┼────────────────────────┤
│ Validar si org existe    │ REST        │ Crítico, síncrono      │
│ Obtener datos de org     │ REST        │ Necesario inmediato    │
│ Verificar permisos       │ REST        │ Seguridad crítica      │
│ Validar unicidad email   │ REST        │ Validación inmediata   │
│                          │             │                        │
│ Notificar admin creado   │ Event       │ No crítico, async      │
│ Actualizar estadísticas  │ Event       │ Eventual consistency   │
│ Enviar emails            │ Event       │ Background job         │
│ Registrar auditoría      │ Event       │ No bloquea respuesta   │
│ Sincronizar caches       │ Event       │ Propagación de cambios │
└──────────────────────────┴─────────────┴────────────────────────┘
```

---

## 📦 APIRESPONSE Y ERRORMESSAGE {#apiresponse}

### ApiResponse (Wrapper Estándar)

```java
package pe.edu.vallegrande.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private ErrorMessage error;

    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    // Success responses
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("Operación exitosa")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    // Error responses
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, ErrorMessage error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
```

### ErrorMessage

```java
package pe.edu.vallegrande.shared.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {
    private String message;
    private String code;
    private String errorCode;
    private int httpStatus;
    private String details;
    private LocalDateTime timestamp;
}
```

### Ejemplo de Uso en Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        return createUserUseCase.execute(request)
                .map(user -> ResponseEntity
                        .status(HttpStatus.CREATED)
                        .body(ApiResponse.success("Usuario creado exitosamente", user)))
                .onErrorResume(OrganizationNotFoundException.class, ex ->
                        Mono.just(ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage()))));
    }
}
```

---

## 📁 ESTRUCTURA DE CARPETAS BASE {#estructura-base}

### Plantilla General (Aplica para TODOS los microservicios)

```
vg-ms-{microservicio}/
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── pe/
│   │   │       └── edu/
│   │   │           └── vallegrande/
│   │   │               └── {microservicio}/
│   │   │                   │
│   │   │                   ├── domain/                          [CAPA DE DOMINIO]
│   │   │                   │   ├── models/                      → Entidades de negocio
│   │   │                   │   │   ├── User.java
│   │   │                   │   │   └── Payment.java
│   │   │                   │   │
│   │   │                   │   ├── ports/                       → Puertos (Interfaces)
│   │   │                   │   │   ├── in/                      → Casos de uso (entrada)
│   │   │                   │   │   │   ├── ICreateUserUseCase.java
│   │   │                   │   │   │   ├── IGetUserUseCase.java
│   │   │                   │   │   │   └── IUpdateUserUseCase.java
│   │   │                   │   │   │
│   │   │                   │   │   └── out/                     → Repositorios (salida)
│   │   │                   │   │       ├── IUserRepository.java
│   │   │                   │   │       ├── IPaymentRepository.java
│   │   │                   │   │       └── IEventPublisher.java
│   │   │                   │   │
│   │   │                   │   └── exceptions/                  → Excepciones de dominio
│   │   │                   │       ├── UserNotFoundException.java
│   │   │                   │       ├── InvalidDataException.java
│   │   │                   │       └── BusinessRuleException.java
│   │   │                   │
│   │   │                   ├── application/                     [CAPA DE APLICACIÓN]
│   │   │                   │   ├── usecases/                    → Implementación casos de uso
│   │   │                   │   │   ├── CreateUserUseCaseImpl.java
│   │   │                   │   │   ├── GetUserUseCaseImpl.java
│   │   │                   │   │   └── UpdateUserUseCaseImpl.java
│   │   │                   │   │
│   │   │                   │   ├── services/                    → Servicios de aplicación
│   │   │                   │   │   ├── UserApplicationService.java
│   │   │                   │   │   └── PaymentApplicationService.java
│   │   │                   │   │
│   │   │                   │   ├── dto/                         → Data Transfer Objects
│   │   │                   │   │   ├── common/                  → ApiResponse, ErrorMessage
│   │   │                   │   │   │   ├── ApiResponse.java
│   │   │                   │   │   │   └── ErrorMessage.java
│   │   │                   │   │   ├── request/
│   │   │                   │   │   │   ├── CreateUserRequest.java
│   │   │                   │   │   │   └── UpdateUserRequest.java
│   │   │                   │   │   └── response/
│   │   │                   │   │       ├── UserResponse.java
│   │   │                   │   │       └── PaymentResponse.java
│   │   │                   │   │
│   │   │                   │   ├── mappers/                     → Mapeadores DTO ↔ Domain
│   │   │                   │   │   ├── UserMapper.java
│   │   │                   │   │   └── PaymentMapper.java
│   │   │                   │   │
│   │   │                   │   └── events/                      → Eventos de dominio
│   │   │                   │       ├── UserCreatedEvent.java
│   │   │                   │       ├── PaymentProcessedEvent.java
│   │   │                   │       └── publishers/
│   │   │                   │           └── EventPublisherImpl.java
│   │   │                   │
│   │   │                   └── infrastructure/                  [CAPA DE INFRAESTRUCTURA]
│   │   │                       │
│   │   │                       ├── adapters/                    → Adaptadores
│   │   │                       │   │
│   │   │                       │   ├── in/                      → Adaptadores de entrada
│   │   │                       │   │   ├── rest/                → Controllers REST
│   │   │                       │   │   │   ├── UserController.java
│   │   │                       │   │   │   └── PaymentController.java
│   │   │                       │   │   │
│   │   │                       │   │   └── messaging/           → Listeners de eventos
│   │   │                       │   │       ├── UserEventListener.java
│   │   │                       │   │       └── PaymentEventListener.java
│   │   │                       │   │
│   │   │                       │   └── out/                     → Adaptadores de salida
│   │   │                       │       ├── persistence/         → Implementaciones BD
│   │   │                       │       │   ├── UserRepositoryImpl.java
│   │   │                       │       │   └── PaymentRepositoryImpl.java
│   │   │                       │       │
│   │   │                       │       ├── messaging/           → Producers RabbitMQ
│   │   │                       │       │   ├── RabbitMQEventPublisher.java
│   │   │                       │       │   └── UserEventProducer.java
│   │   │                       │       │
│   │   │                       │       └── external/            → WebClient REST
│   │   │                       │           ├── OrganizationServiceClient.java
│   │   │                       │           └── NotificationServiceClient.java
│   │   │                       │
│   │   │                       ├── persistence/                 → Entidades de BD
│   │   │                       │   ├── entities/                → R2DBC Entities (PostgreSQL)
│   │   │                       │   │   ├── UserEntity.java
│   │   │                       │   │   └── PaymentEntity.java
│   │   │                       │   │
│   │   │                       │   ├── documents/               → MongoDB Documents
│   │   │                       │   │   ├── OrganizationDocument.java
│   │   │                       │   │   └── NotificationDocument.java
│   │   │                       │   │
│   │   │                       │   └── repositories/            → Reactive Repos
│   │   │                       │       ├── UserR2dbcRepository.java       → R2DBC
│   │   │                       │       └── OrganizationReactiveRepository.java  → Mongo Reactive
│   │   │                       │
│   │   │                       ├── config/                      → Configuraciones
│   │   │                       │   ├── WebFluxConfig.java
│   │   │                       │   ├── R2dbcConfig.java
│   │   │                       │   ├── MongoReactiveConfig.java
│   │   │                       │   ├── WebClientConfig.java
│   │   │                       │   ├── RabbitMQConfig.java
│   │   │                       │   ├── Resilience4jConfig.java
│   │   │                       │   └── SecurityConfig.java
│   │   │                       │
│   │   │                       └── shared/                      → Utilidades compartidas
│   │   │                           ├── constants/
│   │   │                           │   └── ErrorMessages.java
│   │   │                           ├── utils/
│   │   │                           │   ├── DateUtils.java
│   │   │                           │   └── ValidationUtils.java
│   │   │                           └── exceptions/
│   │   │                               └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                              → Configuración principal
│   │       ├── application-dev.yml                          → Perfil desarrollo
│   │       ├── application-docker.yml                       → Perfil Docker
│   │       ├── application-prod.yml                         → Perfil producción
│   │       ├── db/
│   │       │   └── migration/                               → Flyway migrations (PostgreSQL)
│   │       │       ├── V1__create_users_table.sql
│   │       │       └── V2__create_payments_table.sql
│   │       └── mongodb/
│   │           └── indexes/                                 → Scripts de índices MongoDB
│   │               └── organization_indexes.js
│   │
│   └── test/
│       └── java/
│           └── pe/
│               └── edu/
│                   └── vallegrande/
│                       └── {microservicio}/
│                           ├── domain/                          → Tests de dominio (unit)
│                           ├── application/                     → Tests de aplicación (unit)
│                           └── infrastructure/                  → Tests de infraestructura (integration)
│
├── target/                                                  → Compilados (ignorar en git)
├── .gitignore
├── docker-compose.yml                                       → Compose para desarrollo local
├── Dockerfile                                               → Imagen Docker del microservicio
├── pom.xml                                                  → Dependencias Maven
└── README.md                                                → Documentación del microservicio
```

---

## 🎯 ESTRUCTURA POR MICROSERVICIO {#microservicios}

### 1. vg-ms-users (PostgreSQL - REACTIVO)

```
vg-ms-users/
├── src/main/
│   ├── java/pe/edu/vallegrande/vgmsusers/
│   │   ├── domain/
│   │   │   ├── models/
│   │   │   │   ├── User.java                           → [CLASS] Modelo de dominio
│   │   │   │   └── Role.java                           → [ENUM] SUPER_ADMIN, ADMIN, CLIENT
│   │   │   ├── ports/
│   │   │   │   ├── in/
│   │   │   │   │   ├── ICreateUserUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IGetUserUseCase.java            → [INTERFACE]
│   │   │   │   │   ├── IUpdateUserUseCase.java         → [INTERFACE]
│   │   │   │   │   ├── IDeleteUserUseCase.java         → [INTERFACE]
│   │   │   │   │   └── IAuthenticateUserUseCase.java   → [INTERFACE]
│   │   │   │   └── out/
│   │   │   │       ├── IUserRepository.java            → [INTERFACE] Reactivo (Mono/Flux)
│   │   │   │       ├── IRoleRepository.java            → [INTERFACE] Reactivo (Mono/Flux)
│   │   │   │       ├── IOrganizationClient.java        → [INTERFACE] WebClient para validar org
│   │   │   │       └── IUserEventPublisher.java        → [INTERFACE] RabbitMQ
│   │   │   └── exceptions/
│   │   │       ├── UserNotFoundException.java          → [CLASS] extends RuntimeException
│   │   │       ├── OrganizationNotFoundException.java  → [CLASS] extends RuntimeException
│   │   │       └── InvalidCredentialsException.java    → [CLASS] extends RuntimeException
│   │   │
│   │   ├── application/
│   │   │   ├── usecases/
│   │   │   │   ├── CreateUserUseCaseImpl.java          → [CLASS] @Service implements ICreateUserUseCase
│   │   │   │   ├── GetUserUseCaseImpl.java             → [CLASS] @Service implements IGetUserUseCase
│   │   │   │   ├── UpdateUserUseCaseImpl.java          → [CLASS] @Service implements IUpdateUserUseCase
│   │   │   │   └── AuthenticateUserUseCaseImpl.java    → [CLASS] @Service implements IAuthenticateUserUseCase
│   │   │   ├── dto/
│   │   │   │   ├── common/
│   │   │   │   │   ├── ApiResponse.java                → [CLASS] ✅ ESTÁNDAR (T data, String message, int status)
│   │   │   │   │   └── ErrorMessage.java               → [CLASS] ✅ ESTÁNDAR (String message, int status, LocalDateTime)
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateUserRequest.java          → [CLASS] @Valid
│   │   │   │   │   ├── UpdateUserRequest.java          → [CLASS] @Valid
│   │   │   │   │   └── LoginRequest.java               → [CLASS] @Valid
│   │   │   │   └── response/
│   │   │   │       ├── UserResponse.java               → [CLASS] DTO
│   │   │   │       └── AuthResponse.java               → [CLASS] DTO (token, user)
│   │   │   ├── mappers/
│   │   │   │   └── UserMapper.java                     → [CLASS] @Component (Entity ↔ Domain ↔ DTO)
│   │   │   └── events/
│   │   │       ├── UserCreatedEvent.java               → [CLASS] Evento de dominio
│   │   │       ├── UserUpdatedEvent.java               → [CLASS] Evento de dominio
│   │   │       ├── UserDeletedEvent.java               → [CLASS] Evento de dominio
│   │   │       └── publishers/
│   │   │           └── UserEventPublisherImpl.java     → [CLASS] @Component implements IUserEventPublisher
│   │   │
│   │   └── infrastructure/
│   │       ├── adapters/
│   │       │   ├── in/
│   │       │   │   ├── rest/
│   │       │   │   │   └── UserController.java         → [CLASS] @RestController retorna Mono<ApiResponse<T>>
│   │       │   │   └── messaging/
│   │       │   │       └── OrganizationEventListener.java → [CLASS] @Component @RabbitListener
│   │       │   └── out/
│   │       │       ├── persistence/
│   │       │       │   ├── UserRepositoryImpl.java     → [CLASS] @Repository implements IUserRepository
│   │       │       │   └── RoleRepositoryImpl.java     → [CLASS] @Repository implements IRoleRepository
│   │       │       ├── external/                       → ✅ REST Clients (WebClient)
│   │       │       │   └── OrganizationClientImpl.java → [CLASS] @Component implements IOrganizationClient
│   │       │       └── messaging/
│   │       │           └── RabbitMQUserEventPublisher.java → [CLASS] @Component implements IUserEventPublisher
│   │       ├── persistence/
│   │       │   ├── entities/
│   │       │   │   ├── UserEntity.java                 → [CLASS] @Table(name="users") R2DBC
│   │       │   │   └── RoleEntity.java                 → [CLASS] @Table(name="roles") R2DBC
│   │       │   └── repositories/
│   │       │       ├── UserR2dbcRepository.java        → [INTERFACE] extends R2dbcRepository<UserEntity, UUID>
│   │       │       └── RoleR2dbcRepository.java        → [INTERFACE] extends R2dbcRepository<RoleEntity, UUID>
│   │       └── config/
│   │           ├── R2dbcConfig.java                    → [CLASS] @Configuration PostgreSQL Reactive
│   │           ├── WebClientConfig.java                → [CLASS] @Configuration WebClient Bean
│   │           ├── RabbitMQConfig.java                 → [CLASS] @Configuration RabbitMQ
│   │           ├── Resilience4jConfig.java             → [CLASS] @Configuration Circuit Breaker
│   │           ├── SecurityConfig.java                 → [CLASS] @Configuration Role-based Authorization (sin JWT)
│   │           └── RequestContextFilter.java           → [CLASS] WebFilter Lee headers del Gateway (X-User-Id, X-Role)
│   │
│   └── resources/
│       ├── application.yml                             → Base común
│       ├── application-dev.yml                         → Docker local (localhost:5432)
│       ├── application-prod.yml                        → Docker Compose VPC
│       └── db/migration/
│           ├── V1__create_users_table.sql              → SQL Script
│           └── V2__create_roles_table.sql              → SQL Script
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

### 2. vg-ms-organizations (MongoDB)

```

vg-ms-organizations/
└── src/main/java/com/vanguardia/organizations/
    ├── domain/
    │   ├── models/
    │   │   ├── Organization.java
    │   │   ├── Zone.java
    │   │   ├── Street.java
    │   │   └── valueobjects/
    │   │       ├── OrganizationId.java
    │   │       ├── Address.java
    │   │       └── Coordinates.java
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ICreateOrganizationUseCase.java
    │   │   │   ├── IGetOrganizationUseCase.java
    │   │   │   ├── ICreateZoneUseCase.java
    │   │   │   └── ICreateStreetUseCase.java
    │   │   └── out/
    │   │       ├── IOrganizationRepository.java
    │   │       ├── IZoneRepository.java
    │   │       └── IOrganizationEventPublisher.java
    │   └── exceptions/
    │       ├── OrganizationNotFoundException.java
    │       └── DuplicateOrganizationException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── CreateOrganizationUseCaseImpl.java
    │   │   ├── GetOrganizationUseCaseImpl.java
    │   │   └── CreateZoneUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── CreateOrganizationRequest.java
    │   │   │   └── CreateZoneRequest.java
    │   │   └── response/
    │   │       ├── OrganizationResponse.java
    │   │       └── ZoneResponse.java
    │   ├── mappers/
    │   │   ├── OrganizationMapper.java
    │   │   └── ZoneMapper.java
    │   └── events/
    │       ├── OrganizationCreatedEvent.java
    │       ├── ZoneCreatedEvent.java
    │       └── publishers/
    │           └── OrganizationEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   └── rest/
        │   │       ├── OrganizationController.java
        │   │       ├── ZoneController.java
        │   │       └── StreetController.java
        │   └── out/
        │       ├── persistence/
        │       │   ├── OrganizationRepositoryImpl.java
        │       │   └── ZoneRepositoryImpl.java
        │       └── messaging/
        │           └── RabbitMQOrganizationEventPublisher.java
        ├── persistence/
        │   ├── documents/
        │   │   ├── OrganizationDocument.java            → @Document(collection="organizations")
        │   │   ├── ZoneDocument.java                    → @Document(collection="zones")
        │   │   └── StreetDocument.java                  → @Document(collection="streets")
        │   └── repositories/
        │       ├── OrganizationMongoRepository.java     → extends MongoRepository
        │       ├── ZoneMongoRepository.java
        │       └── StreetMongoRepository.java
        └── config/
            ├── MongoConfig.java
            └── RabbitMQConfig.java

```

### 3. vg-ms-payments-billing (PostgreSQL)

```

vg-ms-payments-billing/
└── src/main/java/com/vanguardia/payments/
    ├── domain/
    │   ├── models/
    │   │   ├── Payment.java
    │   │   ├── Bill.java
    │   │   ├── Debt.java
    │   │   └── valueobjects/
    │   │       ├── PaymentId.java
    │   │       ├── Money.java                       → Amount + Currency
    │   │       └── BillPeriod.java                  → Year + Month
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ICreatePaymentUseCase.java
    │   │   │   ├── IGenerateBillUseCase.java
    │   │   │   ├── ICalculateDebtUseCase.java
    │   │   │   └── IProcessPaymentUseCase.java
    │   │   └── out/
    │   │       ├── IPaymentRepository.java
    │   │       ├── IBillRepository.java
    │   │       ├── IDebtRepository.java
    │   │       └── IPaymentEventPublisher.java
    │   └── exceptions/
    │       ├── PaymentNotFoundException.java
    │       ├── InsufficientAmountException.java
    │       └── BillAlreadyPaidException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── CreatePaymentUseCaseImpl.java
    │   │   ├── GenerateBillUseCaseImpl.java
    │   │   ├── CalculateDebtUseCaseImpl.java
    │   │   └── ProcessPaymentUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── CreatePaymentRequest.java
    │   │   │   └── GenerateBillRequest.java
    │   │   └── response/
    │   │       ├── PaymentResponse.java
    │   │       ├── BillResponse.java
    │   │       └── DebtSummaryResponse.java
    │   ├── mappers/
    │   │   ├── PaymentMapper.java
    │   │   └── BillMapper.java
    │   └── events/
    │       ├── PaymentCreatedEvent.java
    │       ├── PaymentProcessedEvent.java
    │       ├── BillGeneratedEvent.java
    │       └── publishers/
    │           └── PaymentEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   ├── rest/
        │   │   │   ├── PaymentController.java
        │   │   │   ├── BillController.java
        │   │   │   └── DebtController.java
        │   │   └── messaging/
        │   │       └── ConsumptionEventListener.java   → Escucha consumos para generar facturas
        │   └── out/
        │       ├── persistence/
        │       │   ├── PaymentRepositoryImpl.java
        │       │   ├── BillRepositoryImpl.java
        │       │   └── DebtRepositoryImpl.java
        │       └── messaging/
        │           └── RabbitMQPaymentEventPublisher.java
        ├── persistence/
        │   ├── entities/
        │   │   ├── PaymentEntity.java                  → @Entity @Table(name="payments")
        │   │   ├── BillEntity.java                     → @Entity @Table(name="bills")
        │   │   └── DebtEntity.java                     → @Entity @Table(name="debts")
        │   └── repositories/
        │       ├── PaymentJpaRepository.java
        │       ├── BillJpaRepository.java
        │       └── DebtJpaRepository.java
        └── config/
            ├── DatabaseConfig.java
            └── RabbitMQConfig.java

```

### 4. vg-ms-water-quality (MongoDB)

```

vg-ms-water-quality/
└── src/main/java/com/vanguardia/waterquality/
    ├── domain/
    │   ├── models/
    │   │   ├── QualityTest.java
    │   │   ├── TestParameter.java
    │   │   ├── Chlorine.java
    │   │   └── valueobjects/
    │   │       ├── TestId.java
    │   │       ├── PhValue.java                        → Value Object con rango válido
    │   │       └── TestResult.java                     → APPROVED/REJECTED/PENDING
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ICreateQualityTestUseCase.java
    │   │   │   ├── IGetQualityTestUseCase.java
    │   │   │   └── IAnalyzeWaterQualityUseCase.java
    │   │   └── out/
    │   │       ├── IQualityTestRepository.java
    │   │       ├── IChlorineRepository.java
    │   │       └── IQualityEventPublisher.java
    │   └── exceptions/
    │       ├── TestNotFoundException.java
    │       └── InvalidParameterException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── CreateQualityTestUseCaseImpl.java
    │   │   ├── GetQualityTestUseCaseImpl.java
    │   │   └── AnalyzeWaterQualityUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   └── CreateQualityTestRequest.java
    │   │   └── response/
    │   │       ├── QualityTestResponse.java
    │   │       └── WaterQualityReportResponse.java
    │   ├── mappers/
    │   │   └── QualityTestMapper.java
    │   └── events/
    │       ├── QualityTestCreatedEvent.java
    │       ├── QualityTestApprovedEvent.java
    │       ├── QualityTestRejectedEvent.java
    │       └── publishers/
    │           └── QualityEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   └── rest/
        │   │       └── QualityTestController.java
        │   └── out/
        │       ├── persistence/
        │       │   ├── QualityTestRepositoryImpl.java
        │       │   └── ChlorineRepositoryImpl.java
        │       └── messaging/
        │           └── RabbitMQQualityEventPublisher.java
        ├── persistence/
        │   ├── documents/
        │   │   ├── QualityTestDocument.java            → @Document(collection="quality_tests")
        │   │   ├── TestParameterDocument.java          → @Document(collection="test_parameters")
        │   │   └── ChlorineDocument.java               → @Document(collection="chlorine_records")
        │   └── repositories/
        │       ├── QualityTestMongoRepository.java
        │       ├── TestParameterMongoRepository.java
        │       └── ChlorineMongoRepository.java
        └── config/
            ├── MongoConfig.java
            └── RabbitMQConfig.java

```

### 5. vg-ms-inventory-purchases (PostgreSQL)

```

vg-ms-inventory-purchases/
└── src/main/java/com/vanguardia/inventory/
    ├── domain/
    │   ├── models/
    │   │   ├── Product.java
    │   │   ├── Kardex.java
    │   │   ├── Purchase.java
    │   │   ├── Movement.java
    │   │   └── valueobjects/
    │   │       ├── ProductId.java
    │   │       ├── Stock.java                          → Cantidad + Unidad medida
    │   │       └── MovementType.java                   → ENTRY/EXIT/ADJUSTMENT
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ICreateProductUseCase.java
    │   │   │   ├── IRegisterPurchaseUseCase.java
    │   │   │   ├── IRegisterConsumptionUseCase.java
    │   │   │   └── IGetKardexUseCase.java
    │   │   └── out/
    │   │       ├── IProductRepository.java
    │   │       ├── IKardexRepository.java
    │   │       ├── IPurchaseRepository.java
    │   │       └── IInventoryEventPublisher.java
    │   └── exceptions/
    │       ├── ProductNotFoundException.java
    │       ├── InsufficientStockException.java
    │       └── InvalidMovementException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── CreateProductUseCaseImpl.java
    │   │   ├── RegisterPurchaseUseCaseImpl.java
    │   │   ├── RegisterConsumptionUseCaseImpl.java
    │   │   └── GetKardexUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── CreateProductRequest.java
    │   │   │   ├── RegisterPurchaseRequest.java
    │   │   │   └── RegisterConsumptionRequest.java
    │   │   └── response/
    │   │       ├── ProductResponse.java
    │   │       ├── KardexResponse.java
    │   │       └── StockReportResponse.java
    │   ├── mappers/
    │   │   ├── ProductMapper.java
    │   │   └── KardexMapper.java
    │   └── events/
    │       ├── ProductCreatedEvent.java
    │       ├── PurchaseRegisteredEvent.java
    │       ├── ConsumptionRegisteredEvent.java
    │       ├── LowStockAlertEvent.java
    │       └── publishers/
    │           └── InventoryEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   ├── rest/
        │   │   │   ├── ProductController.java
        │   │   │   ├── PurchaseController.java
        │   │   │   └── KardexController.java
        │   │   └── messaging/
        │   │       └── MaintenanceEventListener.java   → Escucha mantenimientos para consumos
        │   └── out/
        │       ├── persistence/
        │       │   ├── ProductRepositoryImpl.java
        │       │   ├── KardexRepositoryImpl.java
        │       │   └── PurchaseRepositoryImpl.java
        │       └── messaging/
        │           └── RabbitMQInventoryEventPublisher.java
        ├── persistence/
        │   ├── entities/
        │   │   ├── ProductEntity.java                  → @Entity @Table(name="products")
        │   │   ├── KardexEntity.java                   → @Entity @Table(name="kardex")
        │   │   └── PurchaseEntity.java                 → @Entity @Table(name="purchases")
        │   └── repositories/
        │       ├── ProductJpaRepository.java
        │       ├── KardexJpaRepository.java
        │       └── PurchaseJpaRepository.java
        └── config/
            ├── DatabaseConfig.java
            └── RabbitMQConfig.java

```

### 6. vg-ms-claims-incidents (MongoDB)

```

vg-ms-claims-incidents/
└── src/main/java/com/vanguardia/claims/
    ├── domain/
    │   ├── models/
    │   │   ├── Claim.java
    │   │   ├── Incident.java
    │   │   ├── Comment.java
    │   │   └── valueobjects/
    │   │       ├── ClaimId.java
    │   │       ├── Priority.java                       → LOW/MEDIUM/HIGH/CRITICAL
    │   │       └── Status.java                         → OPEN/IN_PROGRESS/RESOLVED/CLOSED
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ICreateClaimUseCase.java
    │   │   │   ├── IUpdateClaimStatusUseCase.java
    │   │   │   └── IResolveClaimUseCase.java
    │   │   └── out/
    │   │       ├── IClaimRepository.java
    │   │       ├── IIncidentRepository.java
    │   │       └── IClaimEventPublisher.java
    │   └── exceptions/
    │       ├── ClaimNotFoundException.java
    │       └── InvalidStatusTransitionException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── CreateClaimUseCaseImpl.java
    │   │   ├── UpdateClaimStatusUseCaseImpl.java
    │   │   └── ResolveClaimUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── CreateClaimRequest.java
    │   │   │   └── UpdateStatusRequest.java
    │   │   └── response/
    │   │       ├── ClaimResponse.java
    │   │       └── IncidentReportResponse.java
    │   ├── mappers/
    │   │   └── ClaimMapper.java
    │   └── events/
    │       ├── ClaimCreatedEvent.java
    │       ├── ClaimResolvedEvent.java
    │       └── publishers/
    │           └── ClaimEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   └── rest/
        │   │       └── ClaimController.java
        │   └── out/
        │       ├── persistence/
        │       │   ├── ClaimRepositoryImpl.java
        │       │   └── IncidentRepositoryImpl.java
        │       └── messaging/
        │           └── RabbitMQClaimEventPublisher.java
        ├── persistence/
        │   ├── documents/
        │   │   ├── ClaimDocument.java                  → @Document(collection="claims")
        │   │   └── IncidentDocument.java               → @Document(collection="incidents")
        │   └── repositories/
        │       ├── ClaimMongoRepository.java
        │       └── IncidentMongoRepository.java
        └── config/
            ├── MongoConfig.java
            └── RabbitMQConfig.java

```

### 7. vg-ms-distribution (PostgreSQL)

```

vg-ms-distribution/
└── src/main/java/com/vanguardia/distribution/
    ├── domain/
    │   ├── models/
    │   │   ├── WaterBox.java
    │   │   ├── Consumption.java
    │   │   ├── Reading.java
    │   │   └── valueobjects/
    │   │       ├── WaterBoxId.java
    │   │       ├── MeterReading.java                   → Reading + Date + Inspector
    │   │       └── WaterBoxStatus.java                 → ACTIVE/INACTIVE/DAMAGED
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ICreateWaterBoxUseCase.java
    │   │   │   ├── IRegisterReadingUseCase.java
    │   │   │   ├── ICalculateConsumptionUseCase.java
    │   │   │   └── IGetConsumptionHistoryUseCase.java
    │   │   └── out/
    │   │       ├── IWaterBoxRepository.java
    │   │       ├── IConsumptionRepository.java
    │   │       ├── IReadingRepository.java
    │   │       └── IDistributionEventPublisher.java
    │   └── exceptions/
    │       ├── WaterBoxNotFoundException.java
    │       ├── InvalidReadingException.java
    │       └── ConsumptionCalculationException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── CreateWaterBoxUseCaseImpl.java
    │   │   ├── RegisterReadingUseCaseImpl.java
    │   │   ├── CalculateConsumptionUseCaseImpl.java
    │   │   └── GetConsumptionHistoryUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── CreateWaterBoxRequest.java
    │   │   │   └── RegisterReadingRequest.java
    │   │   └── response/
    │   │       ├── WaterBoxResponse.java
    │   │       ├── ConsumptionResponse.java
    │   │       └── ConsumptionHistoryResponse.java
    │   ├── mappers/
    │   │   ├── WaterBoxMapper.java
    │   │   └── ConsumptionMapper.java
    │   └── events/
    │       ├── WaterBoxCreatedEvent.java
    │       ├── ReadingRegisteredEvent.java
    │       ├── ConsumptionCalculatedEvent.java
    │       ├── HighConsumptionAlertEvent.java
    │       └── publishers/
    │           └── DistributionEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   ├── rest/
        │   │   │   ├── WaterBoxController.java
        │   │   │   ├── ConsumptionController.java
        │   │   │   └── ReadingController.java
        │   │   └── messaging/
        │   │       └── WaterBoxEventListener.java
        │   └── out/
        │       ├── persistence/
        │       │   ├── WaterBoxRepositoryImpl.java
        │       │   ├── ConsumptionRepositoryImpl.java
        │       │   └── ReadingRepositoryImpl.java
        │       └── messaging/
        │           └── RabbitMQDistributionEventPublisher.java
        ├── persistence/
        │   ├── entities/
        │   │   ├── WaterBoxEntity.java                 → @Entity @Table(name="water_boxes")
        │   │   ├── ConsumptionEntity.java              → @Entity @Table(name="consumptions")
        │   │   └── ReadingEntity.java                  → @Entity @Table(name="readings")
        │   └── repositories/
        │       ├── WaterBoxJpaRepository.java
        │       ├── ConsumptionJpaRepository.java
        │       └── ReadingJpaRepository.java
        └── config/
            ├── DatabaseConfig.java
            └── RabbitMQConfig.java

```

### 8. vg-ms-infrastructure (PostgreSQL)

```

vg-ms-infrastructure/
└── src/main/java/com/vanguardia/infrastructure/
    ├── domain/
    │   ├── models/
    │   │   ├── Asset.java                              → Activo (tuberías, bombas, etc.)
    │   │   ├── Maintenance.java
    │   │   ├── WorkOrder.java
    │   │   └── valueobjects/
    │   │       ├── AssetId.java
    │   │       ├── AssetType.java                      → PIPE/PUMP/VALVE/TANK
    │   │       └── MaintenanceType.java                → PREVENTIVE/CORRECTIVE/EMERGENCY
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ICreateAssetUseCase.java
    │   │   │   ├── IScheduleMaintenanceUseCase.java
    │   │   │   ├── ICompleteMaintenanceUseCase.java
    │   │   │   └── IGetMaintenanceHistoryUseCase.java
    │   │   └── out/
    │   │       ├── IAssetRepository.java
    │   │       ├── IMaintenanceRepository.java
    │   │       ├── IWorkOrderRepository.java
    │   │       └── IInfrastructureEventPublisher.java
    │   └── exceptions/
    │       ├── AssetNotFoundException.java
    │       ├── MaintenanceNotFoundException.java
    │       └── InvalidMaintenanceStateException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── CreateAssetUseCaseImpl.java
    │   │   ├── ScheduleMaintenanceUseCaseImpl.java
    │   │   ├── CompleteMaintenanceUseCaseImpl.java
    │   │   └── GetMaintenanceHistoryUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── CreateAssetRequest.java
    │   │   │   ├── ScheduleMaintenanceRequest.java
    │   │   │   └── CompleteMaintenanceRequest.java
    │   │   └── response/
    │   │       ├── AssetResponse.java
    │   │       ├── MaintenanceResponse.java
    │   │       └── MaintenanceHistoryResponse.java
    │   ├── mappers/
    │   │   ├── AssetMapper.java
    │   │   └── MaintenanceMapper.java
    │   └── events/
    │       ├── AssetCreatedEvent.java
    │       ├── MaintenanceScheduledEvent.java
    │       ├── MaintenanceCompletedEvent.java
    │       └── publishers/
    │           └── InfrastructureEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   └── rest/
        │   │       ├── AssetController.java
        │   │       ├── MaintenanceController.java
        │   │       └── WorkOrderController.java
        │   └── out/
        │       ├── persistence/
        │       │   ├── AssetRepositoryImpl.java
        │       │   ├── MaintenanceRepositoryImpl.java
        │       │   └── WorkOrderRepositoryImpl.java
        │       └── messaging/
        │           └── RabbitMQInfrastructureEventPublisher.java
        ├── persistence/
        │   ├── entities/
        │   │   ├── AssetEntity.java                    → @Entity @Table(name="assets")
        │   │   ├── MaintenanceEntity.java              → @Entity @Table(name="maintenances")
        │   │   └── WorkOrderEntity.java                → @Entity @Table(name="work_orders")
        │   └── repositories/
        │       ├── AssetJpaRepository.java
        │       ├── MaintenanceJpaRepository.java
        │       └── WorkOrderJpaRepository.java
        └── config/
            ├── DatabaseConfig.java
            └── RabbitMQConfig.java

```

### 9. vg-ms-notification (MongoDB)

```

vg-ms-notification/
└── src/main/java/com/vanguardia/notification/
    ├── domain/
    │   ├── models/
    │   │   ├── Notification.java
    │   │   ├── Template.java
    │   │   └── valueobjects/
    │   │       ├── NotificationId.java
    │   │       ├── Channel.java                        → EMAIL/SMS/PUSH/IN_APP
    │   │       └── NotificationStatus.java             → PENDING/SENT/FAILED/READ
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ISendNotificationUseCase.java
    │   │   │   ├── IMarkAsReadUseCase.java
    │   │   │   └── IGetNotificationsUseCase.java
    │   │   └── out/
    │   │       ├── INotificationRepository.java
    │   │       ├── ITemplateRepository.java
    │   │       ├── IEmailService.java
    │   │       ├── ISmsService.java
    │   │       └── INotificationEventPublisher.java
    │   └── exceptions/
    │       ├── NotificationNotFoundException.java
    │       └── SendNotificationException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── SendNotificationUseCaseImpl.java
    │   │   ├── MarkAsReadUseCaseImpl.java
    │   │   └── GetNotificationsUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   └── SendNotificationRequest.java
    │   │   └── response/
    │   │       └── NotificationResponse.java
    │   ├── mappers/
    │   │   └── NotificationMapper.java
    │   └── events/
    │       ├── NotificationSentEvent.java
    │       └── publishers/
    │           └── NotificationEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   ├── rest/
        │   │   │   └── NotificationController.java
        │   │   └── messaging/
        │   │       ├── UserEventListener.java          → Escucha eventos de usuarios
        │   │       ├── PaymentEventListener.java       → Escucha eventos de pagos
        │   │       ├── ClaimEventListener.java         → Escucha eventos de reclamos
        │   │       └── QualityEventListener.java       → Escucha eventos de calidad
        │   └── out/
        │       ├── persistence/
        │       │   ├── NotificationRepositoryImpl.java
        │       │   └── TemplateRepositoryImpl.java
        │       ├── messaging/
        │       │   └── RabbitMQNotificationEventPublisher.java
        │       └── external/
        │           ├── EmailServiceImpl.java           → AWS SES, SendGrid, etc.
        │           └── SmsServiceImpl.java             → Twilio, AWS SNS, etc.
        ├── persistence/
        │   ├── documents/
        │   │   ├── NotificationDocument.java           → @Document(collection="notifications")
        │   │   └── TemplateDocument.java               → @Document(collection="templates")
        │   └── repositories/
        │       ├── NotificationMongoRepository.java
        │       └── TemplateMongoRepository.java
        └── config/
            ├── MongoConfig.java
            ├── RabbitMQConfig.java
            └── ExternalServicesConfig.java

```

### 10. vg-ms-authentication (PostgreSQL)

```

vg-ms-authentication/
└── src/main/java/com/vanguardia/authentication/
    ├── domain/
    │   ├── models/
    │   │   ├── Session.java
    │   │   ├── Token.java
    │   │   ├── RefreshToken.java
    │   │   └── valueobjects/
    │   │       ├── SessionId.java
    │   │       ├── Jwt.java                            → JWT wrapper con validación
    │   │       └── TokenType.java                      → ACCESS/REFRESH/RESET_PASSWORD
    │   ├── ports/
    │   │   ├── in/
    │   │   │   ├── ILoginUseCase.java
    │   │   │   ├── ILogoutUseCase.java
    │   │   │   ├── IRefreshTokenUseCase.java
    │   │   │   └── IValidateTokenUseCase.java
    │   │   └── out/
    │   │       ├── ISessionRepository.java
    │   │       ├── ITokenRepository.java
    │   │       ├── IUserValidationService.java         → Llama a vg-ms-users
    │   │       └── IAuthEventPublisher.java
    │   └── exceptions/
    │       ├── InvalidTokenException.java
    │       ├── ExpiredTokenException.java
    │       └── SessionNotFoundException.java
    │
    ├── application/
    │   ├── usecases/
    │   │   ├── LoginUseCaseImpl.java
    │   │   ├── LogoutUseCaseImpl.java
    │   │   ├── RefreshTokenUseCaseImpl.java
    │   │   └── ValidateTokenUseCaseImpl.java
    │   ├── dto/
    │   │   ├── request/
    │   │   │   ├── LoginRequest.java
    │   │   │   └── RefreshTokenRequest.java
    │   │   └── response/
    │   │       ├── LoginResponse.java
    │   │       └── TokenResponse.java
    │   ├── mappers/
    │   │   └── SessionMapper.java
    │   └── events/
    │       ├── UserLoggedInEvent.java
    │       ├── UserLoggedOutEvent.java
    │       └── publishers/
    │           └── AuthEventPublisherImpl.java
    │
    └── infrastructure/
        ├── adapters/
        │   ├── in/
        │   │   └── rest/
        │   │       └── AuthenticationController.java
        │   └── out/
        │       ├── persistence/
        │       │   ├── SessionRepositoryImpl.java
        │       │   └── TokenRepositoryImpl.java
        │       ├── messaging/
        │       │   └── RabbitMQAuthEventPublisher.java
        │       └── external/
        │           └── UserServiceClient.java          → Feign client a vg-ms-users
        ├── persistence/
        │   ├── entities/
        │   │   ├── SessionEntity.java                  → @Entity @Table(name="sessions")
        │   │   └── TokenEntity.java                    → @Entity @Table(name="tokens")
        │   └── repositories/
        │       ├── SessionJpaRepository.java
        │       └── TokenJpaRepository.java
        └── config/
            ├── DatabaseConfig.java
            ├── RabbitMQConfig.java
            ├── JwtConfig.java
            └── FeignConfig.java

```

### 11. vg-ms-gateway (API Gateway) - 🔐 **AUTENTICACIÓN CENTRALIZADA**

```
vg-ms-gateway/
├── src/main/
│   ├── java/pe/edu/vallegrande/gateway/
│   │   ├── config/
│   │   │   ├── GatewayConfig.java                      → [CLASS] @Configuration Spring Cloud Gateway routes
│   │   │   ├── CorsConfig.java                         → [CLASS] @Configuration CORS
│   │   │   ├── SecurityConfig.java                     → [CLASS] @Configuration JWT Validation
│   │   │   └── LoadBalancerConfig.java                 → [CLASS] @Configuration Load balancing
│   │   │
│   │   ├── filters/
│   │   │   ├── JwtAuthenticationFilter.java            → [CLASS] ✅ Pre-filter: VALIDA JWT + PROPAGA HEADERS
│   │   │   ├── LoggingFilter.java                      → [CLASS] Post-filter: logging
│   │   │   ├── RateLimitFilter.java                    → [CLASS] Rate limiting
│   │   │   └── CircuitBreakerFilter.java               → [CLASS] Circuit breaker pattern
│   │   │
│   │   ├── security/
│   │   │   ├── JwtUtil.java                            → [CLASS] JWT Validation & Claims extraction
│   │   │   └── RoleBasedAccessControl.java             → [CLASS] Role-based route protection
    │
    ├── routes/
    │   ├── UserRoutes.java                             → /api/users/**→ vg-ms-users
    │   ├── OrganizationRoutes.java                     → /api/organizations/** → vg-ms-organizations
    │   ├── PaymentRoutes.java                          → /api/payments/**→ vg-ms-payments-billing
    │   ├── WaterQualityRoutes.java                     → /api/water-quality/** → vg-ms-water-quality
    │   ├── InventoryRoutes.java                        → /api/inventory/**→ vg-ms-inventory-purchases
    │   ├── ClaimRoutes.java                            → /api/claims/** → vg-ms-claims-incidents
    │   ├── DistributionRoutes.java                     → /api/distribution/**→ vg-ms-distribution
    │   ├── InfrastructureRoutes.java                   → /api/infrastructure/** → vg-ms-infrastructure
    │   ├── NotificationRoutes.java                     → /api/notifications/**→ vg-ms-notification
    │   └── AuthRoutes.java                             → /api/auth/** → vg-ms-authentication
    │
    └── exceptions/
        ├── GatewayExceptionHandler.java                → Global exception handling
        └── ServiceUnavailableException.java

```

---

## 🐰 EVENTOS CON RABBITMQ {#rabbitmq}

### Arquitectura de Eventos

```

┌──────────────────────────────────────────────────────────────────┐
│                    RABBITMQ EVENT ARCHITECTURE                    │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  PRODUCER                    RABBITMQ                  CONSUMER  │
│  (Publisher)                                          (Listener) │
│                                                                   │
│  ┌──────────┐              ┌─────────┐              ┌──────────┐│
│  │UseCase   │─────────────>│Exchange │─────────────>│Listener  ││
│  │          │  publish()   │         │  route()     │          ││
│  │publishes │              │  Topic  │              │subscribes││
│  │event     │              │Exchange │              │to queue  ││
│  └──────────┘              └─────────┘              └──────────┘│
│                                 │                                 │
│                                 │                                 │
│                            ┌────┴────┐                           │
│                            │  Queue  │                           │
│                            │         │                           │
│                            │ Durable │                           │
│                            └─────────┘                           │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘

```

### Exchanges y Queues

```

EXCHANGES (Topic):
├── vanguardia.users.exchange
├── vanguardia.organizations.exchange
├── vanguardia.payments.exchange
├── vanguardia.waterquality.exchange
├── vanguardia.inventory.exchange
├── vanguardia.claims.exchange
├── vanguardia.distribution.exchange
├── vanguardia.infrastructure.exchange
├── vanguardia.notifications.exchange
└── vanguardia.auth.exchange

QUEUES:
├── vanguardia.users.created.queue
├── vanguardia.users.updated.queue
├── vanguardia.payments.processed.queue
├── vanguardia.claims.created.queue
├── vanguardia.quality.tested.queue
├── vanguardia.consumption.calculated.queue
├── vanguardia.maintenance.completed.queue
└── vanguardia.notifications.sent.queue

ROUTING KEYS (Pattern):
├── users.created              → Usuario creado
├── users.updated              → Usuario actualizado
├── users.deleted              → Usuario eliminado
├── organizations.created      → Organización creada
├── payments.created           → Pago registrado
├── payments.processed         → Pago procesado
├── bills.generated            → Factura generada
├── quality.test.approved      → Prueba aprobada
├── quality.test.rejected      → Prueba rechazada
├── inventory.low_stock        → Stock bajo (alerta)
├── claims.created             → Reclamo creado
├── claims.resolved            → Reclamo resuelto
├── consumption.calculated     → Consumo calculado
├── consumption.high_alert     → Consumo alto (alerta)
├── maintenance.scheduled      → Mantenimiento programado
├── maintenance.completed      → Mantenimiento completado
├── notifications.sent         → Notificación enviada
└── auth.login                 → Usuario autenticado

```

### Configuración RabbitMQ (Estándar para todos los microservicios)

**Archivo:** `infrastructure/config/RabbitMQConfig.java`

```java
package com.vanguardia.{microservicio}.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    // Exchange name (Topic Exchange)
    public static final String EXCHANGE_NAME = "vanguardia.{microservicio}.exchange";

    // Queue names
    public static final String CREATED_QUEUE = "vanguardia.{entity}.created.queue";
    public static final String UPDATED_QUEUE = "vanguardia.{entity}.updated.queue";
    public static final String DELETED_QUEUE = "vanguardia.{entity}.deleted.queue";

    // Routing keys
    public static final String CREATED_ROUTING_KEY = "{entity}.created";
    public static final String UPDATED_ROUTING_KEY = "{entity}.updated";
    public static final String DELETED_ROUTING_KEY = "{entity}.deleted";

    /**
     * Topic Exchange - Permite routing patterns flexibles
     */
    @Bean
    public TopicExchange exchange() {
        return ExchangeBuilder
                .topicExchange(EXCHANGE_NAME)
                .durable(true)
                .build();
    }

    /**
     * Queue para eventos de creación
     */
    @Bean
    public Queue createdQueue() {
        return QueueBuilder
                .durable(CREATED_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 horas TTL
                .build();
    }

    /**
     * Queue para eventos de actualización
     */
    @Bean
    public Queue updatedQueue() {
        return QueueBuilder
                .durable(UPDATED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    /**
     * Queue para eventos de eliminación
     */
    @Bean
    public Queue deletedQueue() {
        return QueueBuilder
                .durable(DELETED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .build();
    }

    /**
     * Binding: Exchange → Queue con routing key
     */
    @Bean
    public Binding createdBinding(Queue createdQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(createdQueue)
                .to(exchange)
                .with(CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding updatedBinding(Queue updatedQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(updatedQueue)
                .to(exchange)
                .with(UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding deletedBinding(Queue deletedQueue, TopicExchange exchange) {
        return BindingBuilder
                .bind(deletedQueue)
                .to(exchange)
                .with(DELETED_ROUTING_KEY);
    }

    /**
     * Converter JSON para mensajes
     */
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate configurado con converter JSON
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                         Jackson2JsonMessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
```

### Eventos de Dominio (Base Event)

**Archivo:** `application/events/BaseEvent.java`

```java
package com.vanguardia.{microservicio}.application.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent implements Serializable {

    private String eventId;           // UUID del evento
    private String eventType;         // Tipo de evento (UserCreatedEvent, etc.)
    private LocalDateTime timestamp;  // Fecha y hora del evento
    private String aggregateId;       // ID de la entidad (userId, paymentId, etc.)
    private String source;            // Nombre del microservicio origen

    public BaseEvent(String eventType, String aggregateId, String source) {
        this.eventId = UUID.randomUUID().toString();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.source = source;
    }
}
```

### Ejemplo de Evento Específico

**Archivo:** `application/events/UserCreatedEvent.java`

```java
package com.vanguardia.users.application.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserCreatedEvent extends BaseEvent {

    private String userId;
    private String organizationId;
    private String email;
    private String fullName;
    private String role;

    public UserCreatedEvent(String userId, String organizationId, String email,
                           String fullName, String role) {
        super("UserCreatedEvent", userId, "vg-ms-users");
        this.userId = userId;
        this.organizationId = organizationId;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
    }
}
```

### Event Publisher (Puerto)

**Archivo:** `domain/ports/out/IEventPublisher.java`

```java
package com.vanguardia.{microservicio}.domain.ports.out;

import com.vanguardia.{microservicio}.application.events.BaseEvent;

public interface IEventPublisher {

    /**
     * Publica un evento en RabbitMQ
     * @param event Evento a publicar
     */
    void publish(BaseEvent event);

    /**
     * Publica un evento con routing key específica
     * @param event Evento a publicar
     * @param routingKey Routing key personalizada
     */
    void publish(BaseEvent event, String routingKey);
}
```

### Event Publisher Implementation

**Archivo:** `infrastructure/adapters/out/messaging/RabbitMQEventPublisher.java`

```java
package com.vanguardia.{microservicio}.infrastructure.adapters.out.messaging;

import com.vanguardia.{microservicio}.application.events.BaseEvent;
import com.vanguardia.{microservicio}.domain.ports.out.IEventPublisher;
import com.vanguardia.{microservicio}.infrastructure.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQEventPublisher implements IEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publish(BaseEvent event) {
        String routingKey = getRoutingKeyFromEvent(event);
        publish(event, routingKey);
    }

    @Override
    public void publish(BaseEvent event, String routingKey) {
        try {
            log.info("Publishing event: {} with routing key: {}",
                    event.getEventType(), routingKey);

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    routingKey,
                    event
            );

            log.info("Event published successfully: {}", event.getEventId());
        } catch (Exception e) {
            log.error("Error publishing event: {}", event.getEventType(), e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    private String getRoutingKeyFromEvent(BaseEvent event) {
        String eventType = event.getEventType();

        // UserCreatedEvent → users.created
        if (eventType.endsWith("CreatedEvent")) {
            return extractEntityName(eventType) + ".created";
        } else if (eventType.endsWith("UpdatedEvent")) {
            return extractEntityName(eventType) + ".updated";
        } else if (eventType.endsWith("DeletedEvent")) {
            return extractEntityName(eventType) + ".deleted";
        }

        return "default.routing.key";
    }

    private String extractEntityName(String eventType) {
        // UserCreatedEvent → user
        return eventType
                .replace("CreatedEvent", "")
                .replace("UpdatedEvent", "")
                .replace("DeletedEvent", "")
                .toLowerCase();
    }
}
```

### Event Listener (Consumer)

**Archivo:** `infrastructure/adapters/in/messaging/UserEventListener.java`

```java
package com.vanguardia.notifications.infrastructure.adapters.in.messaging;

import com.vanguardia.notifications.domain.ports.in.ISendNotificationUseCase;
import com.vanguardia.users.application.events.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final ISendNotificationUseCase sendNotificationUseCase;

    /**
     * Escucha eventos de usuarios creados
     * Cuando se crea un usuario, envía notificación de bienvenida
     */
    @RabbitListener(queues = "vanguardia.users.created.queue")
    public void handleUserCreatedEvent(UserCreatedEvent event) {
        try {
            log.info("Received UserCreatedEvent: userId={}", event.getUserId());

            // Enviar notificación de bienvenida
            sendNotificationUseCase.execute(
                    event.getUserId(),
                    "WELCOME_EMAIL",
                    Map.of(
                            "fullName", event.getFullName(),
                            "email", event.getEmail()
                    )
            );

            log.info("Welcome notification sent for user: {}", event.getUserId());
        } catch (Exception e) {
            log.error("Error handling UserCreatedEvent", e);
            // Aquí podrías implementar retry o DLQ (Dead Letter Queue)
        }
    }
}
```

### Flujo de Eventos Ejemplo

```
┌──────────────────────────────────────────────────────────────────┐
│                   EJEMPLO: CREAR USUARIO                          │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│  1. POST /api/users                                              │
│     ↓                                                             │
│  2. UserController.createUser()                                  │
│     ↓                                                             │
│  3. CreateUserUseCaseImpl.execute()                              │
│     ├─> Valida datos                                             │
│     ├─> Crea usuario en BD (userRepository.save())               │
│     └─> Publica evento (eventPublisher.publish())                │
│         ↓                                                         │
│  4. RabbitMQEventPublisher.publish()                             │
│     └─> Envía UserCreatedEvent al exchange                       │
│         ↓                                                         │
│  5. RabbitMQ Exchange                                            │
│     └─> Routing key: "users.created"                             │
│         └─> Enruta a queue: "vanguardia.users.created.queue"     │
│             ↓                                                     │
│  6. UserEventListener (en vg-ms-notification)                    │
│     └─> Escucha queue y recibe UserCreatedEvent                  │
│         ↓                                                         │
│  7. SendNotificationUseCaseImpl.execute()                        │
│     └─> Envía email de bienvenida al usuario                     │
│         ↓                                                         │
│  8. EmailServiceImpl.sendEmail()                                 │
│     └─> AWS SES / SendGrid                                       │
│                                                                   │
└──────────────────────────────────────────────────────────────────┘
```

### Patrones de Eventos Comunes

```
1. CHOREOGRAPHY (Sin orquestador central)
   ──────────────────────────────────────
   Usuario creado → Notificación enviada
   Pago procesado → Factura generada → Notificación enviada
   Consumo calculado → Factura generada → Pago procesado

2. SAGA PATTERN (Transacciones distribuidas)
   ─────────────────────────────────────────
   CreateOrder → ReserveInventory → ProcessPayment → SendNotification
   (Si falla alguno, ejecuta compensación)

3. EVENT SOURCING (Almacenar todos los eventos)
   ───────────────────────────────────────────
   Cada cambio de estado genera un evento persistente
   Permite reconstruir el estado actual desde los eventos

4. CQRS (Command Query Responsibility Segregation)
   ───────────────────────────────────────────────
   Comandos (write) publican eventos
   Queries (read) escuchan eventos y actualizan vistas
```

---

## 💻 EJEMPLOS DE CÓDIGO {#ejemplos}

### Ejemplo Completo: User Entity (PostgreSQL)

**Archivo:** `infrastructure/persistence/entities/UserEntity.java`

```java
package com.vanguardia.users.infrastructure.persistence.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_organization_id", columnList = "organization_id"),
    @Index(name = "idx_users_document", columnList = "organization_id, document_number")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "id", updatable = false, nullable = false, columnDefinition = "UUID")
    private UUID id;

    @Column(name = "organization_id", nullable = false, columnDefinition = "UUID")
    private UUID organizationId;

    // Email es OPCIONAL - Muchas zonas rurales no tienen email
    @Column(name = "email", nullable = true, length = 255)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "document_number", length = 50)
    private String documentNumber;

    // Teléfono es OPCIONAL - Muchas zonas rurales no tienen acceso
    @Column(name = "phone", nullable = true, length = 20)
    private String phone;

    @Column(name = "role", nullable = false, length = 50)
    private String role;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", columnDefinition = "UUID")
    private UUID createdBy;

    @Column(name = "updated_by", columnDefinition = "UUID")
    private UUID updatedBy;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### Ejemplo Completo: Organization Document (MongoDB)

**Archivo:** `infrastructure/persistence/documents/OrganizationDocument.java`

```java
package com.vanguardia.organizations.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "organizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrganizationDocument {

    @Id
    private String id;  // MongoDB genera automáticamente si es null

    @Field("name")
    @Indexed(unique = true)
    private String name;

    @Field("acronym")
    private String acronym;

    @Field("district")
    private String district;

    @Field("province")
    private String province;

    @Field("region")
    private String region;

    @Field("address")
    private String address;

    // Teléfono OPCIONAL - Zonas rurales sin acceso
    @Field("phone")
    private String phone;

    // Email OPCIONAL - Zonas rurales sin acceso
    @Field("email")
    private String email;

    @Field("president_name")
    private String presidentName;

    @Field("is_active")
    private Boolean isActive;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;

    // Para MongoDB, usamos String como ID (UUID convertido a String)
    // Si queremos generar UUID manualmente:
    public void generateId() {
        if (this.id == null) {
            this.id = java.util.UUID.randomUUID().toString();
        }
    }

    public void prePersist() {
        if (this.id == null) {
            generateId();
        }
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
```

### Ejemplo: Use Case Implementation

**Archivo:** `application/usecases/CreateUserUseCaseImpl.java`

```java
package com.vanguardia.users.application.usecases;

import com.vanguardia.users.application.dto.request.CreateUserRequest;
import com.vanguardia.users.application.dto.response.UserResponse;
import com.vanguardia.users.application.events.UserCreatedEvent;
import com.vanguardia.users.application.mappers.UserMapper;
import com.vanguardia.users.domain.exceptions.DuplicateEmailException;
import com.vanguardia.users.domain.models.User;
import com.vanguardia.users.domain.ports.in.ICreateUserUseCase;
import com.vanguardia.users.domain.ports.out.IUserRepository;
import com.vanguardia.users.domain.ports.out.IEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateUserUseCaseImpl implements ICreateUserUseCase {

    private final IUserRepository userRepository;
    private final IEventPublisher eventPublisher;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserResponse execute(CreateUserRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        // 1. Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email already exists: " + request.getEmail());
        }

        // 2. Mapear DTO → Domain Model
        User user = userMapper.toDomain(request);

        // 3. Guardar en BD (a través del puerto)
        User savedUser = userRepository.save(user);

        // 4. Publicar evento de dominio
        UserCreatedEvent event = new UserCreatedEvent(
                savedUser.getId().toString(),
                savedUser.getOrganizationId().toString(),
                savedUser.getEmail(),
                savedUser.getFullName(),
                savedUser.getRole()
        );
        eventPublisher.publish(event);

        log.info("User created successfully with ID: {}", savedUser.getId());

        // 5. Mapear Domain Model → DTO Response
        return userMapper.toResponse(savedUser);
    }
}
```

### Ejemplo: Repository Implementation

**Archivo:** `infrastructure/adapters/out/persistence/UserRepositoryImpl.java`

```java
package com.vanguardia.users.infrastructure.adapters.out.persistence;

import com.vanguardia.users.domain.exceptions.UserNotFoundException;
import com.vanguardia.users.domain.models.User;
import com.vanguardia.users.domain.ports.out.IUserRepository;
import com.vanguardia.users.infrastructure.persistence.entities.UserEntity;
import com.vanguardia.users.infrastructure.persistence.repositories.UserJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements IUserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity savedEntity = jpaRepository.save(entity);
        return toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(this::toDomain);
    }

    @Override
    public User getById(UUID id) {
        return findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll()
                .stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    // Mapper: Entity → Domain
    private User toDomain(UserEntity entity) {
        return User.builder()
                .id(entity.getId())
                .organizationId(entity.getOrganizationId())
                .email(entity.getEmail())
                .passwordHash(entity.getPasswordHash())
                .fullName(entity.getFullName())
                .documentType(entity.getDocumentType())
                .documentNumber(entity.getDocumentNumber())
                .phone(entity.getPhone())
                .role(entity.getRole())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    // Mapper: Domain → Entity
    private UserEntity toEntity(User domain) {
        return UserEntity.builder()
                .id(domain.getId())
                .organizationId(domain.getOrganizationId())
                .email(domain.getEmail())
                .passwordHash(domain.getPasswordHash())
                .fullName(domain.getFullName())
                .documentType(domain.getDocumentType())
                .documentNumber(domain.getDocumentNumber())
                .phone(domain.getPhone())
                .role(domain.getRole())
                .isActive(domain.getIsActive())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
}
```

### Ejemplo: REST Controller

**Archivo:** `infrastructure/adapters/in/rest/UserController.java`

```java
package com.vanguardia.users.infrastructure.adapters.in.rest;

import com.vanguardia.users.application.dto.request.CreateUserRequest;
import com.vanguardia.users.application.dto.request.UpdateUserRequest;
import com.vanguardia.users.application.dto.response.UserResponse;
import com.vanguardia.users.domain.ports.in.ICreateUserUseCase;
import com.vanguardia.users.domain.ports.in.IGetUserUseCase;
import com.vanguardia.users.domain.ports.in.IUpdateUserUseCase;
import com.vanguardia.users.domain.ports.in.IDeleteUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ICreateUserUseCase createUserUseCase;
    private final IGetUserUseCase getUserUseCase;
    private final IUpdateUserUseCase updateUserUseCase;
    private final IDeleteUserUseCase deleteUserUseCase;

    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        log.info("POST /api/users - Creating user with email: {}", request.getEmail());
        UserResponse response = createUserUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        log.info("GET /api/users/{} - Getting user by ID", id);
        UserResponse response = getUserUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("GET /api/users - Getting all users");
        List<UserResponse> response = getUserUseCase.executeAll();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserRequest request) {
        log.info("PUT /api/users/{} - Updating user", id);
        UserResponse response = updateUserUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        log.info("DELETE /api/users/{} - Deleting user", id);
        deleteUserUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/organization/{organizationId}")
    public ResponseEntity<List<UserResponse>> getUsersByOrganization(
            @PathVariable UUID organizationId) {
        log.info("GET /api/users/organization/{} - Getting users by organization", organizationId);
        List<UserResponse> response = getUserUseCase.executeByOrganization(organizationId);
        return ResponseEntity.ok(response);
    }
}
```

---

## 📦 DEPENDENCIAS REACTIVAS (pom.xml) {#docker}

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.2.1</version>
    </parent>

    <groupId>pe.edu.vallegrande</groupId>
    <artifactId>vg-ms-{microservicio}</artifactId>
    <version>1.0.0</version>

    <dependencies>
        <!-- ═══════════════════ REACTIVE STACK ═══════════════════ -->

        <!-- Spring WebFlux (Reactive Web) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <!-- R2DBC PostgreSQL (Reactive Relational DB) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-r2dbc</artifactId>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>r2dbc-postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>

        <!-- MongoDB Reactive -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
        </dependency>

        <!-- RabbitMQ Reactive (Reactor RabbitMQ) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>
        <dependency>
            <groupId>io.projectreactor.rabbitmq</groupId>
            <artifactId>reactor-rabbitmq</artifactId>
        </dependency>

        <!-- Resilience4j (Circuit Breaker) -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
            <version>2.2.0</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-reactor</artifactId>
            <version>2.2.0</version>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Flyway (Migraciones PostgreSQL) -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
        </dependency>

        <!-- Testing Reactive -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
</project>
```

---

## 🐳 DOCKER COMPOSE COMPLETO (VPC)

```yaml
# docker-compose.yml
# Sistema JASS - Todos los microservicios en una red privada
version: '3.8'

networks:
  vanguardia-network:
    driver: bridge

volumes:
  postgres_data:
  mongodb_data:
  rabbitmq_data:

services:
  # ═══════════════════════════════════════════════════════════════
  # DATABASES
  # ═══════════════════════════════════════════════════════════════

  postgres:
    image: postgres:16-alpine
    container_name: vg-postgres
    environment:
      POSTGRES_USER: vanguardia
      POSTGRES_PASSWORD: vanguardia2026
      POSTGRES_DB: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./scripts/init-postgres.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - vanguardia-network
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U vanguardia"]
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  mongodb:
    image: mongo:7-jammy
    container_name: vg-mongodb
    environment:
      MONGO_INITDB_ROOT_USERNAME: vanguardia
      MONGO_INITDB_ROOT_PASSWORD: vanguardia2026
    ports:
      - "27017:27017"
    volumes:
      - mongodb_data:/data/db
    networks:
      - vanguardia-network
    healthcheck:
      test: echo 'db.runCommand("ping").ok' | mongosh localhost:27017/test --quiet
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  # ═══════════════════════════════════════════════════════════════
  # MESSAGE BROKER
  # ═══════════════════════════════════════════════════════════════

  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: vg-rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: vanguardia
      RABBITMQ_DEFAULT_PASS: vanguardia2026
      RABBITMQ_DEFAULT_VHOST: vanguardia
    ports:
      - "5672:5672"    # AMQP port
      - "15672:15672"  # Management UI
    volumes:
      - rabbitmq_data:/var/lib/rabbitmq
    networks:
      - vanguardia-network
    healthcheck:
      test: rabbitmq-diagnostics -q ping
      interval: 10s
      timeout: 5s
      retries: 5
    restart: unless-stopped

  # ═══════════════════════════════════════════════════════════════
  # MICROSERVICES
  # ═══════════════════════════════════════════════════════════════

  # Gateway (Puerto 8080)
  vg-ms-gateway:
    build:
      context: ./vg-ms-gateway
      dockerfile: Dockerfile
    container_name: vg-ms-gateway
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      # Service URLs (comunicación interna Docker)
      SERVICES_USERS_URL: http://vg-ms-users:8081
      SERVICES_ORGANIZATIONS_URL: http://vg-ms-organizations:8082
      SERVICES_PAYMENTS_URL: http://vg-ms-payments:8083
      SERVICES_WATERQUALITY_URL: http://vg-ms-waterquality:8084
      SERVICES_INVENTORY_URL: http://vg-ms-inventory:8085
      SERVICES_CLAIMS_URL: http://vg-ms-claims:8086
      SERVICES_DISTRIBUTION_URL: http://vg-ms-distribution:8087
      SERVICES_INFRASTRUCTURE_URL: http://vg-ms-infrastructure:8088
      SERVICES_NOTIFICATION_URL: http://vg-ms-notification:8089
      SERVICES_AUTH_URL: http://vg-ms-authentication:8090
    networks:
      - vanguardia-network
    depends_on:
      - vg-ms-authentication
    restart: unless-stopped

  # Authentication (Puerto 8090)
  vg-ms-authentication:
    build:
      context: ./vg-ms-authentication
      dockerfile: Dockerfile
    container_name: vg-ms-authentication
    ports:
      - "8090:8090"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_authentication
      SPRING_R2DBC_USERNAME: vanguardia
      SPRING_R2DBC_PASSWORD: vanguardia2026
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
      JWT_SECRET: vanguardia-secret-key-2026-super-secure
      JWT_EXPIRATION: 86400000
    networks:
      - vanguardia-network
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Users (Puerto 8081)
  vg-ms-users:
    build:
      context: ./vg-ms-users
      dockerfile: Dockerfile
    container_name: vg-ms-users
    ports:
      - "8081:8081"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_users
      SPRING_R2DBC_USERNAME: vanguardia
      SPRING_R2DBC_PASSWORD: vanguardia2026
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
      # REST Clients (WebClient)
      SERVICES_ORGANIZATIONS_URL: http://vg-ms-organizations:8082
    networks:
      - vanguardia-network
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Organizations (Puerto 8082)
  vg-ms-organizations:
    build:
      context: ./vg-ms-organizations
      dockerfile: Dockerfile
    container_name: vg-ms-organizations
    ports:
      - "8082:8082"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATA_MONGODB_URI: mongodb://vanguardia:vanguardia2026@mongodb:27017/vg_organizations?authSource=admin
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
    networks:
      - vanguardia-network
    depends_on:
      mongodb:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Payments (Puerto 8083)
  vg-ms-payments:
    build:
      context: ./vg-ms-payments-billing
      dockerfile: Dockerfile
    container_name: vg-ms-payments
    ports:
      - "8083:8083"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_payments
      SPRING_R2DBC_USERNAME: vanguardia
      SPRING_R2DBC_PASSWORD: vanguardia2026
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
      SERVICES_DISTRIBUTION_URL: http://vg-ms-distribution:8087
    networks:
      - vanguardia-network
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Water Quality (Puerto 8084)
  vg-ms-waterquality:
    build:
      context: ./vg-ms-water-quality
      dockerfile: Dockerfile
    container_name: vg-ms-waterquality
    ports:
      - "8084:8084"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATA_MONGODB_URI: mongodb://vanguardia:vanguardia2026@mongodb:27017/vg_waterquality?authSource=admin
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
    networks:
      - vanguardia-network
    depends_on:
      mongodb:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Inventory (Puerto 8085)
  vg-ms-inventory:
    build:
      context: ./vg-ms-inventory-purchases
      dockerfile: Dockerfile
    container_name: vg-ms-inventory
    ports:
      - "8085:8085"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_inventory
      SPRING_R2DBC_USERNAME: vanguardia
      SPRING_R2DBC_PASSWORD: vanguardia2026
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
    networks:
      - vanguardia-network
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Claims (Puerto 8086)
  vg-ms-claims:
    build:
      context: ./vg-ms-claims-incidents
      dockerfile: Dockerfile
    container_name: vg-ms-claims
    ports:
      - "8086:8086"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATA_MONGODB_URI: mongodb://vanguardia:vanguardia2026@mongodb:27017/vg_claims?authSource=admin
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
    networks:
      - vanguardia-network
    depends_on:
      mongodb:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Distribution (Puerto 8087)
  vg-ms-distribution:
    build:
      context: ./vg-ms-distribution
      dockerfile: Dockerfile
    container_name: vg-ms-distribution
    ports:
      - "8087:8087"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_distribution
      SPRING_R2DBC_USERNAME: vanguardia
      SPRING_R2DBC_PASSWORD: vanguardia2026
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
    networks:
      - vanguardia-network
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Infrastructure (Puerto 8088)
  vg-ms-infrastructure:
    build:
      context: ./vg-ms-infrastructure
      dockerfile: Dockerfile
    container_name: vg-ms-infrastructure
    ports:
      - "8088:8088"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_infrastructure
      SPRING_R2DBC_USERNAME: vanguardia
      SPRING_R2DBC_PASSWORD: vanguardia2026
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
    networks:
      - vanguardia-network
    depends_on:
      postgres:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped

  # Notification (Puerto 8089)
  vg-ms-notification:
    build:
      context: ./vg-ms-notification
      dockerfile: Dockerfile
    container_name: vg-ms-notification
    ports:
      - "8089:8089"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATA_MONGODB_URI: mongodb://vanguardia:vanguardia2026@mongodb:27017/vg_notifications?authSource=admin
      RABBITMQ_HOST: rabbitmq
      RABBITMQ_PORT: 5672
      RABBITMQ_USERNAME: vanguardia
      RABBITMQ_PASSWORD: vanguardia2026
      RABBITMQ_VIRTUAL_HOST: vanguardia
    networks:
      - vanguardia-network
    depends_on:
      mongodb:
        condition: service_healthy
      rabbitmq:
        condition: service_healthy
    restart: unless-stopped
```

### Script de Inicialización PostgreSQL

**Archivo:** `scripts/init-postgres.sql`

```sql
-- Crear bases de datos para cada microservicio
CREATE DATABASE vg_authentication;
CREATE DATABASE vg_users;
CREATE DATABASE vg_payments;
CREATE DATABASE vg_distribution;
CREATE DATABASE vg_infrastructure;
CREATE DATABASE vg_inventory;

-- Conectar a cada BD y habilitar extensión UUID
\c vg_authentication;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_users;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_payments;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_distribution;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_infrastructure;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

\c vg_inventory;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
```

---

## ⚙️ CONFIGURACIÓN APPLICATION.YML

### 📋 **Perfiles de Configuración**

Cada microservicio debe tener 3 archivos de configuración:

```
src/main/resources/
├── application.yml              → Configuración base (común a todos los perfiles)
├── application-dev.yml          → Desarrollo (Docker local en subsistema)
└── application-prod.yml         → Producción (Docker Compose VPC)
```

---

### 1️⃣ **application.yml** (BASE - Común a todos los perfiles)

**Archivo:** `src/main/resources/application.yml`

```yaml
# ═══════════════════════════════════════════════════════════════
# CONFIGURACIÓN BASE - vg-ms-users
# Valores comunes para TODOS los perfiles (dev, prod)
# ═══════════════════════════════════════════════════════════════

spring:
  application:
    name: vg-ms-users

  # ═══════════════════ FLYWAY (Migraciones) ═══════════════════
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public

  # ═══════════════════ JACKSON (JSON Serialization) ═══════════════════
  jackson:
    default-property-inclusion: non_null
    serialization:
      write-dates-as-timestamps: false
    time-zone: America/Lima

# ═══════════════════ SERVER CONFIGURATION ═══════════════════
server:
  port: 8081
  error:
    include-message: always
    include-binding-errors: always

# ═══════════════════ LOGGING ═══════════════════
logging:
  level:
    root: INFO
    pe.edu.vallegrande.users: DEBUG
    org.springframework.r2dbc: DEBUG
    io.r2dbc.postgresql.QUERY: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"

# ═══════════════════ MANAGEMENT (Actuator) ═══════════════════
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
  endpoint:
    health:
      show-details: always

# ═══════════════════ RESILIENCE4J (Circuit Breaker) ═══════════════════
resilience4j:
  circuitbreaker:
    instances:
      organizationService:
        register-health-indicator: true
        sliding-window-size: 10
        minimum-number-of-calls: 5
        permitted-number-of-calls-in-half-open-state: 3
        wait-duration-in-open-state: 10s
        failure-rate-threshold: 50
        slow-call-duration-threshold: 2s
        slow-call-rate-threshold: 50

  retry:
    instances:
      organizationService:
        max-attempts: 3
        wait-duration: 500ms
        retry-exceptions:
          - java.io.IOException
          - org.springframework.web.reactive.function.client.WebClientRequestException
```

---

### 2️⃣ **application-dev.yml** (DESARROLLO - Docker Local)

**Archivo:** `src/main/resources/application-dev.yml`

```yaml
# ═══════════════════════════════════════════════════════════════
# PERFIL DE DESARROLLO (dev)
# Docker local en subsistema WSL/Linux
# Activar con: --spring.profiles.active=dev
# ═══════════════════════════════════════════════════════════════

spring:
  # ═══════════════════ R2DBC (PostgreSQL Reactive) ═══════════════════
  r2dbc:
    url: r2dbc:postgresql://localhost:5432/sistemajass
    username: sistemajass_user
    password: 123456
    pool:
      enabled: true
      initial-size: 10
      max-size: 20
      max-idle-time: 30m
      validation-query: SELECT 1

  # ═══════════════════ FLYWAY (Usa JDBC para migraciones) ═══════════════════
  flyway:
    url: jdbc:postgresql://localhost:5432/sistemajass
    user: sistemajass_user
    password: 123456
    enabled: true

  # ═══════════════════ RABBITMQ ═══════════════════
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

# ═══════════════════ WEBCLIENT (REST Clients) ═══════════════════
services:
  organizations:
    url: http://localhost:8082
    timeout: 2000  # 2 segundos

# ═══════════════════ LOGGING (Más detallado en dev) ═══════════════════
logging:
  level:
    root: INFO
    pe.edu.vallegrande.vgmsusers: DEBUG
    org.springframework.r2dbc: DEBUG
    io.r2dbc.postgresql.QUERY: DEBUG
    org.springframework.amqp: DEBUG
    org.flywaydb: DEBUG
```

---

### 3️⃣ **application-prod.yml** (PRODUCCIÓN - Docker Compose)

**Archivo:** `src/main/resources/application-prod.yml`

```yaml
# ═══════════════════════════════════════════════════════════════
# PERFIL DE PRODUCCIÓN (prod/docker)
# Docker Compose con VPC interna
# Activar con: --spring.profiles.active=prod
# Variables de entorno desde docker-compose.yml
# ═══════════════════════════════════════════════════════════════

spring:
  # ═══════════════════ R2DBC (PostgreSQL Reactive) ═══════════════════
  r2dbc:
    url: ${SPRING_R2DBC_URL:r2dbc:postgresql://postgres:5432/vg_users}
    username: ${SPRING_R2DBC_USERNAME:vanguardia}
    password: ${SPRING_R2DBC_PASSWORD:vanguardia2026}
    pool:
      enabled: true
      initial-size: 20
      max-size: 50
      max-idle-time: 30m
      validation-query: SELECT 1

  # ═══════════════════ FLYWAY (Usa JDBC para migraciones) ═══════════════════
  flyway:
    url: jdbc:postgresql://postgres:5432/vg_users
    user: ${SPRING_R2DBC_USERNAME:vanguardia}
    password: ${SPRING_R2DBC_PASSWORD:vanguardia2026}
    enabled: true

  # ═══════════════════ RABBITMQ ═══════════════════
  rabbitmq:
    host: ${RABBITMQ_HOST:rabbitmq}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:vanguardia}
    password: ${RABBITMQ_PASSWORD:vanguardia2026}
    virtual-host: ${RABBITMQ_VIRTUAL_HOST:vanguardia}

# ═══════════════════ WEBCLIENT (REST Clients) ═══════════════════
services:
  organizations:
    url: ${SERVICES_ORGANIZATIONS_URL:http://vg-ms-organizations:8082}
    timeout: 2000  # 2 segundos

# ═══════════════════ LOGGING (Menos detallado en prod) ═══════════════════
logging:
  level:
    root: WARN
    pe.edu.vallegrande.vgmsusers: INFO
    org.springframework.r2dbc: WARN
    io.r2dbc.postgresql.QUERY: WARN
    org.springframework.amqp: WARN
```

---

## 📋 **CONFIGURACIONES PARA MICROSERVICIOS CON MONGODB**

### **application-dev.yml** (Para microservicios MongoDB)

```yaml
# Para: vg-ms-organizations, vg-ms-water-quality, vg-ms-claims-incidents, vg-ms-notification

spring:
  # ═══════════════════ MONGODB REACTIVE ═══════════════════
  data:
    mongodb:
      uri: mongodb://admin:admin123@localhost:27017/JASS_DIGITAL?authSource=admin
      auto-index-creation: true

  # ═══════════════════ RABBITMQ ═══════════════════
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
    virtual-host: /

# ═══════════════════ WEBCLIENT (si necesita llamar a otros servicios) ═══════════════════
services:
  users:
    url: http://localhost:8081
    timeout: 2000
```

### **application-prod.yml** (Para microservicios MongoDB)

```yaml
spring:
  # ═══════════════════ MONGODB REACTIVE ═══════════════════
  data:
    mongodb:
      uri: ${SPRING_DATA_MONGODB_URI:mongodb://vanguardia:vanguardia2026@mongodb:27017/vg_organizations?authSource=admin}
      auto-index-creation: true

  # ═══════════════════ RABBITMQ ═══════════════════
  rabbitmq:
    host: ${RABBITMQ_HOST:rabbitmq}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:vanguardia}
    password: ${RABBITMQ_PASSWORD:vanguardia2026}
    virtual-host: ${RABBITMQ_VIRTUAL_HOST:vanguardia}

# ═══════════════════ WEBCLIENT ═══════════════════
services:
  users:
    url: ${SERVICES_USERS_URL:http://vg-ms-users:8081}
    timeout: 2000
```

---

## 🚀 **COMANDOS PARA EJECUTAR**

### **Desarrollo (local):**

```bash
# Levantar PostgreSQL en Docker (subsistema)
docker run -d --name sistemajass-postgres \
  -e POSTGRES_DB=sistemajass \
  -e POSTGRES_USER=sistemajass_user \
  -e POSTGRES_PASSWORD=123456 \
  -p 5432:5432 \
  -v sistemajass_pgdata:/var/lib/postgresql/data \
  postgres:16

# Levantar RabbitMQ (opcional para dev)
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3.13-management-alpine

# Ejecutar microservicio con perfil dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# O con Java
java -jar target/vg-ms-users-0.0.1-SNAPSHOT.jar --spring.profiles.active=dev
```

### **Producción (Docker Compose):**

```bash
# Levantar todo el sistema
docker-compose up -d

# Ver logs de un servicio
docker-compose logs -f vg-ms-users

# Reiniciar un servicio
docker-compose restart vg-ms-users
```

---

## 📝 **RESUMEN DE PUERTOS**

```
┌────────────────────────────┬────────┬─────────────────────────────┐
│ SERVICIO                   │ PUERTO │ PERFIL                      │
├────────────────────────────┼────────┼─────────────────────────────┤
│ vg-ms-gateway              │ 8080   │ dev, prod                   │
│ vg-ms-users                │ 8081   │ dev, prod                   │
│ vg-ms-organizations        │ 8082   │ dev, prod                   │
│ vg-ms-payments             │ 8083   │ dev, prod                   │
│ vg-ms-waterquality         │ 8084   │ dev, prod                   │
│ vg-ms-inventory            │ 8085   │ dev, prod                   │
│ vg-ms-claims               │ 8086   │ dev, prod                   │
│ vg-ms-distribution         │ 8087   │ dev, prod                   │
│ vg-ms-infrastructure       │ 8088   │ dev, prod                   │
│ vg-ms-notification         │ 8089   │ dev, prod                   │
│ vg-ms-authentication       │ 8090   │ dev, prod                   │
│                            │        │                             │
│ PostgreSQL                 │ 5432   │ dev: localhost, prod: VPC   │
│ MongoDB                    │ 27017  │ dev: localhost, prod: VPC   │
│ RabbitMQ (AMQP)           │ 5672   │ dev: localhost, prod: VPC   │
│ RabbitMQ (Management UI)  │ 15672  │ dev: localhost, prod: VPC   │
└────────────────────────────┴────────┴─────────────────────────────┘
```

---

## 🔐 SEGURIDAD Y GESTIÓN DE ROLES

---

## 🤔 **CONCEPTOS CLAVE: Autenticación vs Autorización**

```
┌─────────────────────────────────────────────────────────────────────┐
│ AUTENTICACIÓN (Authentication)                                      │
│ ❓ "¿Quién eres?"                                                    │
│ ✅ Login con username/password                                       │
│ ✅ Generar JWT token                                                 │
│ ✅ Validar JWT token (firma, expiración)                            │
│ 📍 RESPONSABLE: vg-ms-authentication + Gateway                      │
├─────────────────────────────────────────────────────────────────────┤
│ AUTORIZACIÓN (Authorization)                                        │
│ ❓ "¿Qué puedes hacer?"                                              │
│ ✅ Verificar rol (SUPER_ADMIN, ADMIN, CLIENT)                       │
│ ✅ Verificar permisos (puede acceder a este recurso?)               │
│ ✅ Verificar reglas de negocio (solo sus datos?)                    │
│ 📍 RESPONSABLE: Gateway + Microservicios                            │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🏗️ **ARQUITECTURA DE SEGURIDAD COMPLETA**

### 📐 **Patrón: Gateway-First Authentication + Distributed Authorization**

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                         FLUJO COMPLETO DE SEGURIDAD                          │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  1️⃣ REGISTRO DE USUARIO                                                     │
│     Cliente → Gateway → vg-ms-users → vg-ms-authentication                  │
│                                                                              │
│     POST /api/users                                                          │
│     {                                                                        │
│       "username": "juan.perez",                                              │
│       "firstName": "Juan",                                                   │
│       "password": "123456",                                                  │
│       "organizationId": "uuid-123",                                          │
│       "roles": "CLIENT"                                                      │
│     }                                                                        │
│                                                                              │
│     ┌─────────────┐      ┌─────────────┐      ┌─────────────────────┐      │
│     │   Gateway   │─────▶│ vg-ms-users │─────▶│ vg-ms-authentication│      │
│     │  (sin JWT)  │      │             │      │                     │      │
│     └─────────────┘      └──────┬──────┘      └──────────┬──────────┘      │
│                                 │                         │                 │
│                                 │ 1. Crear usuario       │                 │
│                                 │ 2. Llamar Auth →       │                 │
│                                 │                         │                 │
│                                 │                 3. Hash password          │
│                                 │                 4. Guardar credentials    │
│                                 │                         │                 │
│                                 │ ◀────── OK ─────────────┘                 │
│                                 │                                           │
│     ✅ Usuario creado                                                        │
│                                                                              │
│  ──────────────────────────────────────────────────────────────────────────  │
│                                                                              │
│  2️⃣ LOGIN (AUTENTICACIÓN)                                                   │
│     Cliente → Gateway → vg-ms-authentication                                │
│                                                                              │
│     POST /api/auth/login  (RUTA PÚBLICA - sin JWT)                          │
│     {                                                                        │
│       "username": "juan.perez",                                              │
│       "password": "123456"                                                   │
│     }                                                                        │
│                                                                              │
│     ┌─────────────┐      ┌─────────────────────┐      ┌─────────────┐      │
│     │   Gateway   │─────▶│ vg-ms-authentication│─────▶│ vg-ms-users │      │
│     │  (sin JWT)  │      │                     │      │             │      │
│     └─────────────┘      └──────────┬──────────┘      └─────────────┘      │
│           │                         │                                       │
│           │                  1. Validar password                            │
│           │                  2. Consultar roles/org                         │
│           │                  3. Generar JWT:                                │
│           │                     {                                           │
│           │                       "userId": "uuid",                         │
│           │                       "role": "CLIENT",                         │
│           │                       "organizationId": "uuid",                 │
│           │                       "exp": "24h"                              │
│           │                     }                                           │
│           │                         │                                       │
│           │ ◀────── JWT Token ──────┘                                       │
│           │                                                                 │
│           ▼                                                                 │
│     Cliente recibe:                                                         │
│     {                                                                       │
│       "token": "eyJhbGciOiJIUzI1NiIs...",                                   │
│       "user": { "id": "uuid", "username": "juan.perez", "role": "CLIENT" } │
│     }                                                                       │
│                                                                              │
│  ──────────────────────────────────────────────────────────────────────────  │
│                                                                              │
│  3️⃣ ACCESO A RECURSO PROTEGIDO (AUTORIZACIÓN)                               │
│     Cliente → Gateway → vg-ms-payments                                      │
│                                                                              │
│     GET /api/payments  (CON JWT)                                             │
│     Header: Authorization: Bearer eyJhbGciOiJIUzI1NiIs...                    │
│                                                                              │
│     ┌─────────────┐                  ┌─────────────────┐                    │
│     │   Gateway   │                  │  vg-ms-payments │                    │
│     │             │                  │                 │                    │
│     │ 1. Valida   │                  │ 4. Lee headers  │                    │
│     │    JWT ✅    │                  │    X-User-Id    │                    │
│     │             │                  │    X-Role       │                    │
│     │ 2. Extrae   │                  │    X-Org-Id     │                    │
│     │    claims   │                  │                 │                    │
│     │    (role,   │                  │ 5. Verifica rol │                    │
│     │    userId,  │   3. Propaga     │    @PreAuthorize│                    │
│     │    orgId)   │      headers     │                 │                    │
│     │             │─────────────────▶│ 6. Valida       │                    │
│     │             │   X-User-Id      │    reglas de    │                    │
│     │             │   X-Role         │    negocio      │                    │
│     │             │   X-Org-Id       │    (CLIENT solo │                    │
│     │             │                  │    sus datos)   │                    │
│     └─────────────┘                  └─────────────────┘                    │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎭 **RESPONSABILIDADES POR COMPONENTE**

### 1️⃣ **vg-ms-authentication** (Puerto 8090)

**ROL:** Autenticación - "¿Quién eres?"

```java
// Responsabilidades:
✅ POST /api/auth/login          → Validar username/password, generar JWT
✅ POST /api/auth/register       → Hash password, crear credenciales
✅ POST /api/auth/refresh-token  → Refrescar JWT
✅ POST /api/auth/logout         → Invalidar token (blacklist)
✅ POST /api/auth/forgot-password → Reset de contraseña

// NO hace:
❌ Gestionar usuarios (vg-ms-users lo hace)
❌ Validar JWT en cada request (Gateway lo hace)
❌ Autorización (microservicios lo hacen)
```

**Arquitectura:**

```
vg-ms-authentication/
├── domain/
│   ├── models/
│   │   └── Credentials.java              → username, passwordHash
│   └── ports/
│       ├── in/
│       │   ├── ILoginUseCase.java
│       │   └── IRegisterCredentialsUseCase.java
│       └── out/
│           ├── ICredentialsRepository.java
│           └── IUserServiceClient.java   → WebClient a vg-ms-users
│
├── application/
│   ├── usecases/
│   │   └── LoginUseCaseImpl.java         → Genera JWT
│   └── dto/
│       └── LoginResponse.java            → { token, user }
│
└── infrastructure/
    ├── persistence/
    │   └── entities/
    │       └── CredentialsEntity.java    → @Table("credentials")
    └── security/
        └── JwtService.java               → generateToken(), validateToken()
```

---

### 2️⃣ **vg-ms-gateway** (Puerto 8080)

**ROL:** Punto de entrada - Validación JWT

```java
// Responsabilidades:
✅ Validar JWT en CADA request (excepto /api/auth/*)
✅ Extraer claims (userId, role, organizationId)
✅ Verificar rol básico para la ruta
✅ Propagar headers a microservicios:
   - X-User-Id
   - X-Role
   - X-Organization-Id
✅ Routing a microservicios

// NO hace:
❌ Generar JWT (vg-ms-authentication lo hace)
❌ Autorización detallada (microservicios lo hacen)
❌ Lógica de negocio
```

**Configuración:**

```yaml
# application.yml
spring:
  cloud:
    gateway:
      routes:
        # RUTA PÚBLICA (sin JWT)
        - id: auth-service
          uri: http://vg-ms-authentication:8090
          predicates:
            - Path=/api/auth/**
          filters:
            - RewritePath=/api/auth/(?<segment>.*), /${segment}

        # RUTAS PROTEGIDAS (con JWT)
        - id: users-service
          uri: http://vg-ms-users:8081
          predicates:
            - Path=/api/users/**
          filters:
            - JwtAuthenticationFilter  # ✅ Valida JWT aquí
            - RewritePath=/api/users/(?<segment>.*), /${segment}
```

---

### 3️⃣ **vg-ms-users** (Puerto 8081)

**ROL:** Gestión de usuarios - Datos de perfil

```java
// Responsabilidades:
✅ CRUD de usuarios (datos personales)
✅ Relacionar usuario con organización/zona/calle
✅ Consultar datos de usuario (para vg-ms-authentication)
✅ Autorización: @PreAuthorize("hasRole('ADMIN')")
✅ Validación de reglas de negocio

// Interacción con vg-ms-authentication:
POST /api/users (crear usuario)
  1. Guarda datos en tabla "users"
  2. Llama a vg-ms-authentication:
     POST /internal/credentials
     {
       "userId": "uuid",
       "username": "juan.perez",
       "password": "123456"  // sin hash
     }
  3. vg-ms-authentication hace hash y guarda
```

---

### 4️⃣ **Otros Microservicios** (vg-ms-payments, etc.)

**ROL:** Autorización + Lógica de negocio

```java
// Responsabilidades:
✅ Leer headers del Gateway (X-User-Id, X-Role, X-Org-Id)
✅ Aplicar autorización: @PreAuthorize
✅ Validar reglas de negocio:
   - CLIENT solo ve sus propios pagos
   - ADMIN solo ve pagos de su organización
   - SUPER_ADMIN ve todo

// NO hace:
❌ Validar JWT (Gateway lo hace)
❌ Generar JWT (vg-ms-authentication lo hace)
```

---

## 🐳 **CONFIGURACIÓN DOCKER COMPOSE**

```yaml
version: '3.8'

networks:
  jass-network:
    driver: bridge

services:
  # ═══════════════════ GATEWAY (Punto de entrada) ═══════════════════
  vg-ms-gateway:
    build: ./vg-ms-gateway
    container_name: vg-gateway
    ports:
      - "8080:8080"  # ✅ ÚNICO PUERTO EXPUESTO PÚBLICAMENTE
    environment:
      SPRING_PROFILES_ACTIVE: prod
      JWT_SECRET: ${JWT_SECRET}  # Mismo secret que authentication
    networks:
      - jass-network
    depends_on:
      - vg-ms-authentication
      - vg-ms-users

  # ═══════════════════ AUTHENTICATION (Genera JWT) ═══════════════════
  vg-ms-authentication:
    build: ./vg-ms-authentication
    container_name: vg-authentication
    # ❌ NO exponer puerto público (solo red interna)
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_authentication
      SPRING_R2DBC_USERNAME: jass_user
      SPRING_R2DBC_PASSWORD: jass2026
      JWT_SECRET: ${JWT_SECRET}  # Mismo secret que gateway
      SERVICES_USERS_URL: http://vg-ms-users:8081  # ✅ Llama a users
    networks:
      - jass-network
    depends_on:
      - postgres

  # ═══════════════════ USERS (Datos de usuarios) ═══════════════════
  vg-ms-users:
    build: ./vg-ms-users
    container_name: vg-users
    # ❌ NO exponer puerto público
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/vg_users
      SERVICES_AUTHENTICATION_URL: http://vg-ms-authentication:8090  # ✅ Llama a auth
      SERVICES_ORGANIZATIONS_URL: http://vg-ms-organizations:8082
    networks:
      - jass-network

  # ═══════════════════ OTROS MICROSERVICIOS ═══════════════════
  vg-ms-payments:
    build: ./vg-ms-payments-billing
    container_name: vg-payments
    # ❌ NO exponer puerto público
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SERVICES_USERS_URL: http://vg-ms-users:8081
    networks:
      - jass-network
```

---

## 🔑 **VARIABLES DE ENTORNO (.env)**

```bash
# JWT Secret (DEBE SER EL MISMO en Gateway y Authentication)
JWT_SECRET=VanguardiaJASS2026SecretKeyMinimo32CaracteresParaHMACSHA256Seguridad

# PostgreSQL
POSTGRES_USER=jass_user
POSTGRES_PASSWORD=jass2026
```

---

## ✅ **RESUMEN FINAL**

```
┌──────────────────────────────────────────────────────────────────┐
│ PATRÓN DE SEGURIDAD: Gateway-First + Distributed Authorization  │
├──────────────────────────────────────────────────────────────────┤
│                                                                  │
│ 1. CLIENTE → Gateway (puerto 8080 público)                      │
│                                                                  │
│ 2. LOGIN: Gateway → vg-ms-authentication                        │
│    ✅ Genera JWT con claims (userId, role, orgId)               │
│                                                                  │
│ 3. REQUESTS: Gateway valida JWT                                 │
│    ✅ Extrae claims                                              │
│    ✅ Propaga headers (X-User-Id, X-Role, X-Org-Id)             │
│    ✅ Enruta a microservicio correcto                            │
│                                                                  │
│ 4. MICROSERVICIOS: Lee headers (NO valida JWT)                  │
│    ✅ Aplica autorización (@PreAuthorize)                        │
│    ✅ Valida reglas de negocio                                   │
│                                                                  │
│ 5. RED INTERNA: Microservicios NO expuestos                     │
│    ✅ Solo Gateway público (puerto 8080)                         │
│    ✅ Microservicios se llaman por nombres Docker               │
│                                                                  │
└──────────────────────────────────────────────────────────────────┘
```

**VENTAJAS:**

- ✅ JWT validado UNA SOLA VEZ (performance)
- ✅ Microservicios NO necesitan JWT secret (seguridad)
- ✅ Separación clara: Authentication vs Authorization
- ✅ Red VPC privada (microservicios protegidos)
- ✅ Cumple DDD/Hexagonal (bounded contexts claros)

---

### 🏗️ **Arquitectura de Seguridad: Gateway Centralizado**

```
┌─────────────────────────────────────────────────────────────────────┐
│                    FLUJO DE AUTENTICACIÓN/AUTORIZACIÓN              │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│  1. Cliente → Gateway                                               │
│     Header: Authorization: Bearer {JWT}                             │
│                                                                     │
│  2. Gateway (JwtAuthenticationFilter)                               │
│     ├─> Valida JWT (firma, expiración)                             │
│     ├─> Extrae claims (userId, role, organizationId)               │
│     ├─> Verifica rol permitido para la ruta                        │
│     └─> Propaga headers a microservicios:                          │
│         • X-User-Id: {userId}                                       │
│         • X-Role: {role}                                            │
│         • X-Organization-Id: {organizationId}                       │
│                                                                     │
│  3. Microservicio (RequestContextFilter)                            │
│     ├─> Lee headers propagados (NO valida JWT)                     │
│     ├─> Aplica autorización específica (@PreAuthorize)             │
│     └─> Valida reglas de negocio (CLIENT solo sus datos)           │
│                                                                     │
└─────────────────────────────────────────────────────────────────────┘
```

**✅ VENTAJAS:**

- JWT validado **UNA SOLA VEZ** en el Gateway (performance)
- Microservicios **NO necesitan secret JWT** (seguridad)
- Microservicios **confían en headers** del Gateway (simplificación)
- Red interna (VPC) protegida (solo Gateway expuesto)

---

### 📋 **Roles del Sistema**

```java
package pe.edu.vallegrande.vgmsusers.domain.models;

public enum Role {
    SUPER_ADMIN,  // Acceso total al sistema (crear organizaciones, gestionar todo)
    ADMIN,        // Administrador de una organización (CRUD en su org)
    CLIENT        // Usuario final (consultas, pagos, reportes)
}
```

### 🎯 **Matriz de Permisos por Endpoint**

```
┌─────────────────────────────────────┬──────────────┬─────────┬─────────┐
│ ENDPOINT                            │ SUPER_ADMIN  │ ADMIN   │ CLIENT  │
├─────────────────────────────────────┼──────────────┼─────────┼─────────┤
│ POST   /api/organizations           │      ✅      │    ❌   │    ❌   │
│ GET    /api/organizations/{id}      │      ✅      │    ✅   │    ✅   │
│ PUT    /api/organizations/{id}      │      ✅      │    ✅   │    ❌   │
│ DELETE /api/organizations/{id}      │      ✅      │    ❌   │    ❌   │
│                                     │              │         │         │
│ POST   /api/users                   │      ✅      │    ✅   │    ❌   │
│ GET    /api/users                   │      ✅      │    ✅   │    ❌   │
│ GET    /api/users/{id}              │      ✅      │    ✅   │    ✅   │
│ PUT    /api/users/{id}              │      ✅      │    ✅   │    ✅*  │
│ DELETE /api/users/{id}              │      ✅      │    ✅   │    ❌   │
│                                     │              │         │         │
│ POST   /api/payments                │      ✅      │    ✅   │    ✅   │
│ GET    /api/payments                │      ✅      │    ✅   │    ✅*  │
│ GET    /api/payments/{id}           │      ✅      │    ✅   │    ✅*  │
│                                     │              │         │         │
│ GET    /api/reports/consumption     │      ✅      │    ✅   │    ✅*  │
│ GET    /api/reports/debts           │      ✅      │    ✅   │    ✅*  │
│ GET    /api/reports/payments        │      ✅      │    ✅   │    ✅*  │
└─────────────────────────────────────┴──────────────┴─────────┴─────────┘

* = Solo puede acceder a sus propios datos (validación por userId u organizationId)
```

---

### 🛡️ **Configuración de Seguridad**

---

## 🚪 **1. GATEWAY - Autenticación JWT (Punto de entrada)**

### **JwtAuthenticationFilter.java** (Gateway)

```java
package pe.edu.vallegrande.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.gateway.security.JwtUtil;
import reactor.core.publisher.Mono;

import java.util.Map;

@Slf4j
@Component
public class JwtAuthenticationFilter extends AbstractGatewayFilterFactory<JwtAuthenticationFilter.Config> {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getPath().value();

            // Rutas públicas (sin JWT)
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            // Extraer JWT
            String token = extractToken(request);
            if (token == null) {
                log.warn("No JWT token found for path: {}", path);
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }

            try {
                // ✅ VALIDAR JWT
                Map<String, String> claims = jwtUtil.validateAndExtractClaims(token);

                String userId = claims.get("userId");
                String role = claims.get("role");
                String organizationId = claims.get("organizationId");

                // ✅ VERIFICAR ROL PERMITIDO PARA LA RUTA
                if (!isRoleAllowedForPath(path, role)) {
                    log.warn("Role {} not allowed for path: {}", role, path);
                    exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                    return exchange.getResponse().setComplete();
                }

                // ✅ PROPAGAR HEADERS A MICROSERVICIOS
                ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .header("X-Role", role)
                    .header("X-Organization-Id", organizationId)
                    .build();

                log.info("JWT validated for user: {} with role: {}", userId, role);
                return chain.filter(exchange.mutate().request(modifiedRequest).build());

            } catch (Exception e) {
                log.error("Invalid JWT: {}", e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        };
    }

    private String extractToken(ServerHttpRequest request) {
        String bearerToken = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/actuator/") ||
               path.equals("/health");
    }

    private boolean isRoleAllowedForPath(String path, String role) {
        // SUPER_ADMIN: acceso total
        if ("SUPER_ADMIN".equals(role)) return true;

        // Rutas solo para SUPER_ADMIN
        if (path.matches(".*/organizations$") && path.contains("POST")) return false;
        if (path.matches(".*/organizations/.*/DELETE")) return false;

        // Resto de rutas: ADMIN y CLIENT también pueden acceder
        // (validación específica en microservicios)
        return true;
    }

    public static class Config {
        // Configuración adicional si es necesaria
    }
}
```

---

## 🏢 **2. MICROSERVICIOS - Autorización (Sin validar JWT)**

### **RequestContextFilter.java** (Microservicios)

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class RequestContextFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // ✅ LEER HEADERS PROPAGADOS DESDE EL GATEWAY
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String role = exchange.getRequest().getHeaders().getFirst("X-Role");
        String organizationId = exchange.getRequest().getHeaders().getFirst("X-Organization-Id");

        // Si no hay headers, permitir (rutas públicas o error del gateway)
        if (userId == null || role == null) {
            return chain.filter(exchange);
        }

        // ✅ CREAR AUTHENTICATION SIN VALIDAR JWT (Gateway ya lo hizo)
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // Agregar información adicional
        UserContext userContext = new UserContext(userId, organizationId, role);
        authentication.setDetails(userContext);

        log.debug("Request context set for user: {} with role: {}", userId, role);

        // ✅ ESTABLECER CONTEXTO DE SEGURIDAD REACTIVO
        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    // Clase para almacenar contexto del usuario
    public static class UserContext {
        private final String userId;
        private final String organizationId;
        private final String role;

        public UserContext(String userId, String organizationId, String role) {
            this.userId = userId;
            this.organizationId = organizationId;
            this.role = role;
        }

        public String getUserId() { return userId; }
        public String getOrganizationId() { return organizationId; }
        public String getRole() { return role; }
    }
}
```

---

### **SecurityConfig.java** (Microservicios - Simplificado)

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity  // ✅ Habilita @PreAuthorize en métodos
public class SecurityConfig {

    private final RequestContextFilter requestContextFilter;

    public SecurityConfig(RequestContextFilter requestContextFilter) {
        this.requestContextFilter = requestContextFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

            // ═══════════════ AUTORIZACIÓN (Gateway ya validó JWT) ═══════════════
            .authorizeExchange(exchanges -> exchanges
                // Actuator público
                .pathMatchers("/actuator/**", "/health").permitAll()

                // Resto: requiere autenticación (headers del Gateway)
                // Autorización específica se maneja con @PreAuthorize en controllers
                .anyExchange().authenticated()
            )

            // ═══════════════ FILTRO PARA LEER HEADERS ═══════════════
            .addFilterAt(requestContextFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

**🔑 DIFERENCIAS CLAVE:**

- ❌ **NO valida JWT** (Gateway ya lo hizo)
- ✅ **Lee headers** X-User-Id, X-Role, X-Organization-Id
- ✅ **Autorización simplificada** con @PreAuthorize
- ✅ **Confía en el Gateway** (red interna VPC)

```java
package pe.edu.vallegrande.users.infrastructure.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtAuthenticationFilter implements WebFilter {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // Rutas públicas: omitir validación JWT
        if (path.startsWith("/api/auth/") || path.startsWith("/actuator/") || path.equals("/health")) {
            return chain.filter(exchange);
        }

        String token = extractToken(exchange);

        if (token == null) {
            log.warn("No JWT token found in request to {}", path);
            return chain.filter(exchange);
        }

        try {
            Claims claims = validateToken(token);

            String userId = claims.getSubject();
            String role = claims.get("role", String.class);
            String organizationId = claims.get("organizationId", String.class);

            // Crear Authentication con rol como authority
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userId, null, authorities);

            // Agregar claims adicionales como detalles
            authentication.setDetails(new JwtUserDetails(userId, organizationId, role));

            // Establecer contexto de seguridad reactivo
            return chain.filter(exchange)
                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));

        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return chain.filter(exchange);
        }
    }

    private String extractToken(ServerWebExchange exchange) {
        String bearerToken = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private Claims validateToken(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parserBuilder()
            .setSigningKey(key)
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    // Clase para almacenar información adicional del JWT
    public static class JwtUserDetails {
        private final String userId;
        private final String organizationId;
        private final String role;

        public JwtUserDetails(String userId, String organizationId, String role) {
            this.userId = userId;
            this.organizationId = organizationId;
            this.role = role;
        }

        public String getUserId() { return userId; }
        public String getOrganizationId() { return organizationId; }
        public String getRole() { return role; }
    }
}
```

---

### 🎯 **Uso de @PreAuthorize en Controllers**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.adapters.in.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsusers.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsusers.application.dto.request.CreateUserRequest;
import pe.edu.vallegrande.vgmsusers.application.dto.response.UserResponse;
import pe.edu.vallegrande.vgmsusers.domain.ports.in.ICreateUserUseCase;
import pe.edu.vallegrande.vgmsusers.infrastructure.config.RequestContextFilter.UserContext;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ICreateUserUseCase createUserUseCase;

    // ═══════════════ SUPER_ADMIN + ADMIN ═══════════════
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public Mono<ApiResponse<UserResponse>> createUser(@RequestBody CreateUserRequest request) {
        return createUserUseCase.execute(request)
            .map(user -> ApiResponse.success(user, "Usuario creado exitosamente"));
    }

    // ═══════════════ SUPER_ADMIN + ADMIN ═══════════════
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'ADMIN')")
    public Mono<ApiResponse<List<UserResponse>>> listUsers() {
        // SUPER_ADMIN: ve todos los usuarios
        // ADMIN: solo usuarios de su organización (validar en UseCase)
        return getUsersUseCase.execute()
            .collectList()
            .map(users -> ApiResponse.success(users, "Usuarios obtenidos"));
    }

    // ═══════════════ CUALQUIER USUARIO AUTENTICADO ═══════════════
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ApiResponse<UserResponse>> getUserById(
            @PathVariable String id,
            Authentication authentication) {

        // Validación en capa de negocio:
        // - CLIENT: solo puede ver su propio perfil
        // - ADMIN: usuarios de su organización
        // - SUPER_ADMIN: cualquier usuario

        JwtUserDetails details = (JwtUserDetails) authentication.getDetails();
        String requesterId = authentication.getName();
        String requesterRole = details.getRole();
        String requesterOrgId = details.getOrganizationId();

        return getUserUseCase.execute(id, requesterId, requesterRole, requesterOrgId)
            .map(user -> ApiResponse.success(user, "Usuario obtenido"));
    }

    // ═══════════════ VALIDACIÓN EN CAPA DE NEGOCIO ═══════════════
    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Mono<ApiResponse<UserResponse>> updateUser(
            @PathVariable String id,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {

        JwtUserDetails details = (JwtUserDetails) authentication.getDetails();

        // El UseCase validará:
        // - CLIENT: solo puede actualizar su propio perfil
        // - ADMIN: usuarios de su organización
        // - SUPER_ADMIN: cualquier usuario

        return updateUserUseCase.execute(id, request, details)
            .map(user -> ApiResponse.success(user, "Usuario actualizado"));
    }

    // ═══════════════ SOLO SUPER_ADMIN ═══════════════
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPER_ADMIN')")
    public Mono<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        return deleteUserUseCase.execute(id)
            .then(Mono.just(ApiResponse.success(null, "Usuario eliminado")));
    }
}
```

---

### 🔑 **Generación de JWT en AuthenticationService**

```java
package pe.edu.vallegrande.vgmsusers.application.usecases;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.vgmsusers.domain.models.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Service
public class AuthenticateUserUseCaseImpl {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-hours:24}")
    private int jwtExpirationHours;

    public String generateToken(User user) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        LocalDateTime expiration = LocalDateTime.now().plusHours(jwtExpirationHours);

        return Jwts.builder()
            .setSubject(user.getId().toString())               // userId
            .claim("role", user.getRole().name())              // SUPER_ADMIN, ADMIN, CLIENT
            .claim("organizationId", user.getOrganizationId().toString())
            .claim("fullName", user.getFullName())
            .setIssuedAt(new Date())
            .setExpiration(Date.from(expiration.atZone(ZoneId.systemDefault()).toInstant()))
            .signWith(key)
            .compact();
    }
}
```

---

### ⚙️ **Configuración JWT en application.yml**

```yaml
# ═══════════════════ JWT CONFIGURATION ═══════════════════
jwt:
  secret: ${JWT_SECRET:VanguardiaJASS2026SecretKeyMinimo32CaracteresParaHMACSHA256Seguridad}
  expiration-hours: 24  # Token válido por 24 horas
```

---

### 📦 **Dependencias Maven para Spring Security WebFlux + JWT**

```xml
<!-- Spring Security WebFlux -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- JWT (jjwt) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.5</version>
    <scope>runtime</scope>
</dependency>
```

---

### ✅ **Resumen de Estrategia de Seguridad**

```
┌─────────────────────────────────────────────────────────────────────┐
│ ARQUITECTURA DE SEGURIDAD EN 3 CAPAS                                │
├─────────────────────────────────────────────────────────────────────┤
│                                                                     │
│ 1. GATEWAY (Autenticación Centralizada)                            │
│    ├─> JwtAuthenticationFilter                                     │
│    │   ├─> Valida JWT (firma, expiración)                          │
│    │   ├─> Extrae claims (userId, role, organizationId)            │
│    │   ├─> Verifica rol básico para la ruta                        │
│    │   └─> Propaga headers (X-User-Id, X-Role, X-Organization-Id)  │
│    └─> Rutas públicas: /api/auth/*, /actuator/*, /health           │
│                                                                     │
│ 2. MICROSERVICIOS (Autorización)                                   │
│    ├─> RequestContextFilter                                        │
│    │   ├─> Lee headers propagados del Gateway                      │
│    │   └─> Establece contexto de seguridad (sin validar JWT)       │
│    ├─> @PreAuthorize en Controllers                                │
│    │   └─> Validación de roles permitidos                          │
│    └─> UseCase (Lógica de negocio)                                 │
│        ├─> Validación de permisos específicos                      │
│        │   (CLIENT solo ve sus datos)                              │
│        │   (ADMIN solo ve datos de su organización)                │
│        └─> Validación de reglas de negocio                         │
│                                                                     │
│ 3. RED VPC PRIVADA                                                  │
│    └─> Solo Gateway expuesto públicamente                          │
│        └─> Microservicios inaccesibles desde internet              │
└─────────────────────────────────────────────────────────────────────┘
```

**🔐 SEGURIDAD EN CAPAS:**

- **Gateway**: JWT + Role-based routing (SUPER_ADMIN rutas admin)
- **Microservicios**: Authorization + Business rules (CLIENT solo sus datos)
- **VPC**: Red privada (microservicios no expuestos)

---

## 🎯 CONCLUSIÓN

Esta arquitectura reactiva + hexagonal + eventos proporciona:

✅ **Stack 100% Reactivo** (WebFlux, R2DBC, MongoDB Reactive)
✅ **Comunicación Híbrida** (REST síncrono + Events asíncrono)
✅ **Separación de responsabilidades** (Domain, Application, Infrastructure)
✅ **ApiResponse/ErrorMessage** estándar en todas las respuestas
✅ **WebClient** para comunicación REST reactiva
✅ **Circuit Breaker** (Resilience4j) para tolerancia a fallos
✅ **Docker Compose** con VPC privada
✅ **Convenciones consistentes** (snake_case BD + camelCase Java)
✅ **Paquete base** pe.edu.vallegrande.{microservicio}
✅ **Multi-organización** validado en tiempo real
✅ **3 perfiles de configuración** (base, dev, prod)
✅ **Seguridad JWT + Roles** (SUPER_ADMIN, ADMIN, CLIENT)

---

## 📦 CÓDIGO COMPLETO - COPIAR Y PEGAR

### 🔧 **1. RabbitMQConfig.java**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMQConfig {

    // ═══════════════════ EXCHANGES ═══════════════════
    public static final String USER_EXCHANGE = "jass.users.exchange";
    public static final String ORGANIZATION_EXCHANGE = "jass.organizations.exchange";

    // ═══════════════════ QUEUES ═══════════════════
    public static final String USER_CREATED_QUEUE = "jass.users.created.queue";
    public static final String USER_UPDATED_QUEUE = "jass.users.updated.queue";
    public static final String USER_DELETED_QUEUE = "jass.users.deleted.queue";
    // ═══════════════════ ROUTING KEYS ═══════════════════
    public static final String USER_CREATED_ROUTING_KEY = "user.created";
    public static final String USER_UPDATED_ROUTING_KEY = "user.updated";
    public static final String USER_DELETED_ROUTING_KEY = "user.deleted";

    // ═══════════════════ MESSAGE CONVERTER ═══════════════════
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    // ═══════════════════ RABBIT TEMPLATE ═══════════════════
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }

    // ═══════════════════ LISTENER CONTAINER FACTORY ═══════════════════
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setPrefetchCount(10);
        return factory;
    }

    // ═══════════════════ USER EXCHANGE ═══════════════════
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE, true, false);
    }

    // ═══════════════════ USER QUEUES ═══════════════════
    @Bean
    public Queue userCreatedQueue() {
        return QueueBuilder.durable(USER_CREATED_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 horas
                .withArgument("x-max-length", 10000)     // Máximo 10k mensajes
                .build();
    }

    @Bean
    public Queue userUpdatedQueue() {
        return QueueBuilder.durable(USER_UPDATED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(USER_DELETED_QUEUE)
                .withArgument("x-message-ttl", 86400000)
                .withArgument("x-max-length", 10000)
                .build();
    }

    // ═══════════════════ BINDINGS ═══════════════════
    @Bean
    public Binding userCreatedBinding(Queue userCreatedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userCreatedQueue)
                .to(userExchange)
                .with(USER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding userUpdatedBinding(Queue userUpdatedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userUpdatedQueue)
                .to(userExchange)
                .with(USER_UPDATED_ROUTING_KEY);
    }

    @Bean
    public Binding userDeletedBinding(Queue userDeletedQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userDeletedQueue)
                .to(userExchange)
                .with(USER_DELETED_ROUTING_KEY);
    }
}
```

---

### 🔧 **2. RequestContextFilter.java**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class RequestContextFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        // ✅ LEER HEADERS PROPAGADOS DESDE EL GATEWAY
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        String role = exchange.getRequest().getHeaders().getFirst("X-Role");
        String organizationId = exchange.getRequest().getHeaders().getFirst("X-Organization-Id");

        // Si no hay headers, permitir (rutas públicas o error del gateway)
        if (userId == null || role == null) {
            log.debug("No user context headers found, proceeding without authentication");
            return chain.filter(exchange);
        }

        // ✅ CREAR AUTHENTICATION SIN VALIDAR JWT (Gateway ya lo hizo)
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
        UsernamePasswordAuthenticationToken authentication =
            new UsernamePasswordAuthenticationToken(userId, null, authorities);

        // Agregar información adicional
        UserContext userContext = new UserContext(userId, organizationId, role);
        authentication.setDetails(userContext);

        log.debug("Request context set for user: {} with role: {} in organization: {}",
                  userId, role, organizationId);

        // ✅ ESTABLECER CONTEXTO DE SEGURIDAD REACTIVO
        return chain.filter(exchange)
            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
    }

    // Clase para almacenar contexto del usuario
    public static class UserContext {
        private final String userId;
        private final String organizationId;
        private final String role;

        public UserContext(String userId, String organizationId, String role) {
            this.userId = userId;
            this.organizationId = organizationId;
            this.role = role;
        }

        public String getUserId() { return userId; }
        public String getOrganizationId() { return organizationId; }
        public String getRole() { return role; }
    }
}
```

---

### 🔧 **3. Resilience4jConfig.java**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Slf4j
@Configuration
public class Resilience4jConfig {

    // ═══════════════════ CIRCUIT BREAKER CONFIGURATION ═══════════════════
    @Bean
    public CircuitBreakerConfig circuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                // Estado ABIERTO después del 50% de fallos
                .failureRateThreshold(50)

                // Mínimo de llamadas antes de calcular tasa de fallos
                .minimumNumberOfCalls(5)

                // Ventana deslizante de 10 llamadas
                .slidingWindowSize(10)
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)

                // Tiempo en estado ABIERTO antes de pasar a HALF_OPEN
                .waitDurationInOpenState(Duration.ofSeconds(10))

                // Llamadas permitidas en estado HALF_OPEN
                .permittedNumberOfCallsInHalfOpenState(3)

                // Tiempo máximo para considerar una llamada lenta
                .slowCallDurationThreshold(Duration.ofSeconds(2))

                // Porcentaje de llamadas lentas para abrir el circuito
                .slowCallRateThreshold(50)

                // Registro de eventos
                .recordExceptions(
                        org.springframework.web.reactive.function.client.WebClientRequestException.class,
                        java.io.IOException.class,
                        java.util.concurrent.TimeoutException.class
                )

                .build();
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(CircuitBreakerConfig circuitBreakerConfig) {
        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    // ═══════════════════ CIRCUIT BREAKERS POR SERVICIO ═══════════════════
    @Bean(name = "organizationServiceCircuitBreaker")
    public CircuitBreaker organizationServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("organizationService");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    log.warn("OrganizationService Circuit Breaker state transition: {} -> {}",
                             event.getStateTransition().getFromState(),
                             event.getStateTransition().getToState()))
                .onError(event ->
                    log.error("OrganizationService Circuit Breaker error: {}",
                              event.getThrowable().getMessage()))
                .onSuccess(event ->
                    log.debug("OrganizationService Circuit Breaker success"));

        return circuitBreaker;
    }

    @Bean(name = "authenticationServiceCircuitBreaker")
    public CircuitBreaker authenticationServiceCircuitBreaker(CircuitBreakerRegistry registry) {
        CircuitBreaker circuitBreaker = registry.circuitBreaker("authenticationService");

        circuitBreaker.getEventPublisher()
                .onStateTransition(event ->
                    log.warn("AuthenticationService Circuit Breaker state transition: {} -> {}",
                             event.getStateTransition().getFromState(),
                             event.getStateTransition().getToState()));

        return circuitBreaker;
    }

    // ═══════════════════ RETRY CONFIGURATION ═══════════════════
    @Bean
    public RetryConfig retryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3)                                    // 3 intentos
                .waitDuration(Duration.ofMillis(500))              // 500ms entre intentos
                .retryExceptions(
                        org.springframework.web.reactive.function.client.WebClientRequestException.class,
                        java.io.IOException.class
                )
                .ignoreExceptions(
                        IllegalArgumentException.class,
                        org.springframework.web.reactive.function.client.WebClientResponseException.BadRequest.class
                )
                .build();
    }

    @Bean
    public RetryRegistry retryRegistry(RetryConfig retryConfig) {
        return RetryRegistry.of(retryConfig);
    }

    @Bean(name = "organizationServiceRetry")
    public Retry organizationServiceRetry(RetryRegistry registry) {
        Retry retry = registry.retry("organizationService");

        retry.getEventPublisher()
                .onRetry(event ->
                    log.warn("OrganizationService retry attempt #{}: {}",
                             event.getNumberOfRetryAttempts(),
                             event.getLastThrowable().getMessage()));

        return retry;
    }

    // ═══════════════════ TIME LIMITER CONFIGURATION ═══════════════════
    @Bean
    public TimeLimiterConfig timeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(3))  // Timeout de 3 segundos
                .build();
    }
}
```

---

### 🔧 **4. UserEntity.java**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @Column("id")
    private UUID id;

    @Column("user_code")
    private String userCode;  // Código único del usuario (ej: USR-001)

    @Column("organization_id")
    private UUID organizationId;

    @Column("username")
    private String username;  // Usuario para login

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("document_type")
    private String documentType;  // DNI, CE, PASAPORTE

    @Column("document_number")
    private String documentNumber;

    // Email OPCIONAL - Zonas rurales sin acceso
    @Column("email")
    private String email;

    // Teléfono OPCIONAL - Zonas rurales sin acceso
    @Column("phone")
    private String phone;

    @Column("address")
    private String address;  // Dirección completa

    @Column("street_id")
    private UUID streetId;  // Relación con Street (vg-ms-organizations)

    @Column("zone_id")
    private UUID zoneId;  // Relación con Zone (vg-ms-organizations)

    @Column("status")
    private String status;  // ACTIVE, INACTIVE, SUSPENDED

    @Column("roles")
    private String roles;  // SUPER_ADMIN, ADMIN, CLIENT (o JSON array si múltiples)

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;

    @Column("created_by")
    private UUID createdBy;

    @Column("updated_by")
    private UUID updatedBy;
}
```

---

### 🔧 **5. Role.java (ENUM)**

```java
package pe.edu.vallegrande.vgmsusers.domain.models;

public enum Role {
    SUPER_ADMIN,  // Acceso total al sistema
    ADMIN,        // Administrador de organización
    CLIENT        // Usuario final
}
```

---

### 🔧 **6. SecurityConfig.java (Microservicio)**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity  // ✅ Habilita @PreAuthorize
public class SecurityConfig {

    private final RequestContextFilter requestContextFilter;

    public SecurityConfig(RequestContextFilter requestContextFilter) {
        this.requestContextFilter = requestContextFilter;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
            .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
            .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())

            .authorizeExchange(exchanges -> exchanges
                // Actuator público
                .pathMatchers("/actuator/**", "/health").permitAll()

                // Resto: requiere autenticación (headers del Gateway)
                .anyExchange().authenticated()
            )

            // Filtro para leer headers del Gateway
            .addFilterAt(requestContextFilter, SecurityWebFiltersOrder.AUTHENTICATION)

            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

---

### 🔧 **7. WebClientConfig.java**

```java
package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Configuration
public class WebClientConfig {

    @Value("${services.organizations.url:http://vg-ms-organizations:8082}")
    private String organizationsServiceUrl;

    @Value("${services.organizations.timeout:2000}")
    private int timeout;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder()
                .filter(logRequest())
                .filter(logResponse());
    }

    @Bean(name = "organizationWebClient")
    public WebClient organizationWebClient(WebClient.Builder builder) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout)
                .responseTimeout(Duration.ofMillis(timeout))
                .doOnConnected(conn ->
                    conn.addHandlerLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(timeout, TimeUnit.MILLISECONDS)));

        return builder
                .baseUrl(organizationsServiceUrl)
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    // Filtro de logging para request
    private ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.debug("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) ->
                values.forEach(value -> log.trace("Header: {}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    // Filtro de logging para response
    private ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(clientResponse -> {
            log.debug("Response status: {}", clientResponse.statusCode());
            return Mono.just(clientResponse);
        });
    }
}
```

---

### 🔧 **8. R2dbcConfig.java**

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

@Configuration
@EnableR2dbcRepositories(basePackages = "pe.edu.vallegrande.vgmsusers.infrastructure.persistence.repositories")
@EnableR2dbcAuditing  // Habilita @CreatedDate y @LastModifiedDate
@EnableTransactionManagement
public class R2dbcConfig {

    @Bean
    public ReactiveTransactionManager transactionManager(ConnectionFactory connectionFactory) {
        return new R2dbcTransactionManager(connectionFactory);
    }
}
```

---

**Siguiente paso:** Generar estructura de carpetas física y crear archivos base para cada microservicio 🚀
