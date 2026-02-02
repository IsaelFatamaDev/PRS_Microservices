# 🧑‍💼 vg-ms-users - Microservicio de Usuarios

> **Arquitectura Hexagonal + DDD + Clean Code + SOLID**

## 📋 Descripción

Microservicio responsable de la gestión completa de usuarios del sistema JASS Digital. Maneja el ciclo de vida completo: creación, actualización, eliminación lógica (soft delete), restauración y eliminación física (purge).

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────────────┐
│                        🌐 REST API (Port 8081)                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    📥 INFRASTRUCTURE                          │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────┐  │   │
│  │  │   REST      │  │  R2DBC      │  │  WebClient + RabbitMQ│  │   │
│  │  │  Adapters   │  │  Repository │  │  External Clients    │  │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────────┬───────────┘  │   │
│  └─────────┼────────────────┼───────────────────┼───────────────┘   │
│            │                │                   │                    │
│  ┌─────────▼────────────────▼───────────────────▼───────────────┐   │
│  │                    📦 APPLICATION                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────┐  │   │
│  │  │   Use Cases │  │   Mappers   │  │   DTOs & Events      │  │   │
│  │  │   (CRUD)    │  │             │  │                      │  │   │
│  │  └──────┬──────┘  └─────────────┘  └──────────────────────┘  │   │
│  └─────────┼────────────────────────────────────────────────────┘   │
│            │                                                         │
│  ┌─────────▼────────────────────────────────────────────────────┐   │
│  │                    💎 DOMAIN                                  │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────┐  │   │
│  │  │   Models    │  │    Ports    │  │    Exceptions        │  │   │
│  │  │   (User)    │  │  (in/out)   │  │                      │  │   │
│  │  └─────────────┘  └─────────────┘  └──────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 📂 Estructura de Carpetas

```
vg-ms-users/
├── src/main/java/pe/edu/vallegrande/vgmsusers/
│   ├── domain/                          → 💎 Núcleo de negocio (sin dependencias)
│   │   ├── models/                      → Entidades de dominio
│   │   │   ├── User.java
│   │   │   └── valueobjects/
│   │   │       ├── Role.java
│   │   │       ├── DocumentType.java
│   │   │       └── RecordStatus.java
│   │   ├── ports/                       → Interfaces (contratos)
│   │   │   ├── in/                      → Casos de uso (entrada)
│   │   │   │   ├── ICreateUserUseCase.java
│   │   │   │   ├── IGetUserUseCase.java
│   │   │   │   ├── IUpdateUserUseCase.java
│   │   │   │   ├── IDeleteUserUseCase.java
│   │   │   │   ├── IRestoreUserUseCase.java
│   │   │   │   └── IPurgeUserUseCase.java
│   │   │   └── out/                     → Repositorios y clientes (salida)
│   │   │       ├── IUserRepository.java
│   │   │       ├── IAuthenticationClient.java
│   │   │       ├── IOrganizationClient.java
│   │   │       ├── INotificationClient.java
│   │   │       └── IUserEventPublisher.java
│   │   └── exceptions/                  → Excepciones de dominio
│   │       ├── DomainException.java
│   │       ├── NotFoundException.java
│   │       ├── BusinessRuleException.java
│   │       ├── ValidationException.java
│   │       ├── ConflictException.java
│   │       ├── ExternalServiceException.java
│   │       ├── UserNotFoundException.java
│   │       ├── DuplicateDocumentException.java
│   │       └── InvalidContactException.java
│   │
│   ├── application/                     → 📦 Orquestación de casos de uso
│   │   ├── usecases/                    → Implementaciones de casos de uso
│   │   │   ├── CreateUserUseCaseImpl.java
│   │   │   ├── GetUserUseCaseImpl.java
│   │   │   ├── UpdateUserUseCaseImpl.java
│   │   │   ├── DeleteUserUseCaseImpl.java
│   │   │   ├── RestoreUserUseCaseImpl.java
│   │   │   └── PurgeUserUseCaseImpl.java
│   │   ├── dto/                         → Objetos de transferencia
│   │   │   ├── common/
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── PageResponse.java
│   │   │   │   └── ErrorMessage.java
│   │   │   ├── request/
│   │   │   │   ├── CreateUserRequest.java
│   │   │   │   └── UpdateUserRequest.java
│   │   │   └── response/
│   │   │       └── UserResponse.java
│   │   ├── mappers/                     → Conversiones Entity <-> Domain <-> DTO
│   │   │   └── UserMapper.java
│   │   └── events/                      → DTOs de eventos (RabbitMQ)
│   │       ├── UserCreatedEvent.java
│   │       ├── UserUpdatedEvent.java
│   │       ├── UserDeletedEvent.java
│   │       ├── UserRestoredEvent.java
│   │       └── UserPurgedEvent.java
│   │
│   └── infrastructure/                  → 📥 Adaptadores externos
│       ├── adapters/
│       │   ├── in/rest/                 → Controladores REST
│       │   │   ├── UserRest.java
│       │   │   └── GlobalExceptionHandler.java
│       │   └── out/
│       │       ├── persistence/         → Implementación repositorio
│       │       │   └── UserRepositoryImpl.java
│       │       ├── external/            → Clientes WebClient
│       │       │   ├── AuthenticationClientImpl.java
│       │       │   ├── OrganizationClientImpl.java
│       │       │   └── NotificationClientImpl.java
│       │       └── messaging/           → Publisher RabbitMQ
│       │           └── UserEventPublisherImpl.java
│       ├── persistence/
│       │   ├── entities/
│       │   │   └── UserEntity.java
│       │   └── repositories/
│       │       └── UserR2dbcRepository.java
│       └── config/
│           ├── R2dbcConfig.java
│           ├── WebClientConfig.java
│           ├── RabbitMQConfig.java
│           ├── Resilience4jConfig.java
│           ├── SecurityConfig.java
│           └── RequestContextFilter.java
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       └── V1__create_users_table.sql
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## ⚠️ Notas Importantes

### CORS
>
> **CORS se configura ÚNICAMENTE en `vg-ms-gateway`**, NO en este ni ningún otro microservicio individual.
> Los microservicios están detrás del Gateway, por lo que las peticiones del browser llegan primero al Gateway.

### RabbitMQ
>
> **Exchanges, Queues y Bindings** se configuran en la clase `RabbitMQConfig.java`, **NO en YAML**.
> En `application.yml` solo va: host, port, username, password, publisher-confirm-type.

---

## 🔧 Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje |
| Spring Boot | 3.5.x | Framework |
| Spring WebFlux | 3.2.x | API Reactiva |
| R2DBC PostgreSQL | - | Base de datos reactiva |
| Flyway | 9.x | Migraciones |
| RabbitMQ | 3.12 | Mensajería async |
| Resilience4j | 2.x | Circuit Breaker |
| Lombok | 1.18.x | Boilerplate |
| MapStruct | 1.5.x | Mapeo de objetos |

---

## 🔌 Dependencias Externas

| Servicio | Puerto | Propósito |
|----------|--------|-----------|
| vg-ms-authentication | 8082 | Crear usuario en Keycloak |
| vg-ms-organizations | 8083 | Validar organización, zona, calle |
| vg-ms-notification | 8090 | Enviar WhatsApp de bienvenida |

---

## 📡 Eventos RabbitMQ

### Exchange: `jass.events` (compartido por todos los microservicios)

| Routing Key | Evento | Descripción |
|-------------|--------|-------------|
| `user.created` | UserCreatedEvent | Usuario creado |
| `user.updated` | UserUpdatedEvent | Usuario actualizado |
| `user.deleted` | UserDeletedEvent | Usuario eliminado (soft) |
| `user.restored` | UserRestoredEvent | Usuario restaurado |
| `user.purged` | UserPurgedEvent | Usuario eliminado (hard) |

> **📌 Nota:** El exchange y routing keys se configuran en `RabbitMQConfig.java`, NO en YAML.

---

## 🚀 Endpoints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/users` | Listar usuarios activos |
| `GET` | `/api/v1/users/all` | Listar todos (incluye inactivos) |
| `GET` | `/api/v1/users/{id}` | Obtener por ID |
| `GET` | `/api/v1/users/document/{documentNumber}` | Buscar por documento |
| `GET` | `/api/v1/users/organization/{organizationId}` | Usuarios por organización |
| `POST` | `/api/v1/users` | Crear usuario |
| `PUT` | `/api/v1/users/{id}` | Actualizar usuario |
| `DELETE` | `/api/v1/users/{id}` | Eliminar lógico (soft delete) |
| `PATCH` | `/api/v1/users/{id}/restore` | Restaurar usuario |
| `DELETE` | `/api/v1/users/{id}/purge` | Eliminar físico (hard delete) |

---

## 📚 Documentación por Capas

Para mantener la documentación organizada, cada capa tiene su propio README:

| Capa | Archivo | Descripción |
|------|---------|-------------|
| 💎 Domain | [README_DOMAIN.md](README_DOMAIN.md) | Modelos, Ports, Excepciones |
| 📦 Application | [README_APPLICATION.md](README_APPLICATION.md) | UseCases, DTOs, Mappers, Events |
| 📥 Infrastructure | [README_INFRASTRUCTURE.md](README_INFRASTRUCTURE.md) | REST, Repository, Clients, Config |

---

## ⚡ Principios Aplicados

### SOLID

| Principio | Aplicación |
|-----------|------------|
| **S** - Single Responsibility | Cada UseCase tiene una sola responsabilidad |
| **O** - Open/Closed | Extensión vía interfaces (ports) |
| **L** - Liskov Substitution | Implementaciones intercambiables |
| **I** - Interface Segregation | Interfaces pequeñas y específicas |
| **D** - Dependency Inversion | Domain no depende de Infrastructure |

### Clean Code

- ✅ Nombres descriptivos y significativos
- ✅ Funciones pequeñas (máximo 20 líneas)
- ✅ Sin comentarios innecesarios (código auto-documentado)
- ✅ Manejo de errores con excepciones específicas
- ✅ Inmutabilidad cuando es posible

### Arquitectura Hexagonal

- ✅ Domain aislado (sin dependencias externas)
- ✅ Ports definen contratos
- ✅ Adapters implementan los contratos
- ✅ Inversión de dependencias

---

## 🏃 Ejecución Local

```bash
# Clonar el repositorio
git clone https://github.com/IsaelFatamaDev/PRS_Microservices.git

# Ir al microservicio
cd vg-ms-users

# Levantar dependencias
docker-compose up -d postgres rabbitmq

# Ejecutar la aplicación
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## 🐳 Docker

```bash
# Build
docker build -t vg-ms-users:latest .

# Run
docker run -p 8081:8081 --env-file .env vg-ms-users:latest
```

---

## 📊 Health Check

```bash
curl http://localhost:8081/actuator/health
```

---

## 👤 Autor

**Valle Grande - Proyecto JASS Digital**

---

> **Siguiente paso**: Lee [README_DOMAIN.md](README_DOMAIN.md) para ver la implementación de la capa de dominio.
