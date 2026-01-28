# vg-ms-users

Microservicio de Gestión de Usuarios para el sistema JASS Digital.

## 📋 Descripción

Microservicio construido con **Spring Boot 3.2**, **WebFlux**, **R2DBC**, **PostgreSQL** y **RabbitMQ** siguiendo **Arquitectura Hexagonal** y **Domain-Driven Design (DDD)**.

## 🏗️ Arquitectura

```
Hexagonal Architecture + DDD
├── domain/              → Lógica de negocio pura
│   ├── models/          → Entidades del dominio
│   ├── ports/           → Interfaces (in/out)
│   └── exceptions/      → Excepciones personalizadas
├── application/         → Casos de uso
│   ├── usecases/        → Implementación de lógica
│   ├── dto/             → Request/Response
│   ├── mappers/         → Conversión de objetos
│   └── events/          → Eventos de dominio
└── infrastructure/      → Detalles técnicos
    ├── adapters/        → Implementaciones
    ├── persistence/     → R2DBC + PostgreSQL
    └── config/          → Configuraciones
```

## 🚀 Tecnologías

- **Java 21**
- **Spring Boot 3.2.0**
- **Spring WebFlux** (Reactive)
- **Spring Data R2DBC** (PostgreSQL)
- **Flyway** (Migraciones)
- **RabbitMQ** (Mensajería asíncrona)
- **Resilience4j** (Circuit Breaker)
- **Lombok** (Reducir boilerplate)
- **Docker** & **Docker Compose**

## 📦 Estructura del Proyecto

```
vg-ms-users/
├── src/main/java/pe/edu/vallegrande/vgmsusers/
│   ├── domain/
│   │   ├── models/
│   │   │   ├── User.java
│   │   │   └── valueobjects/
│   │   │       ├── Role.java (SUPER_ADMIN, ADMIN, CLIENT, OPERATOR)
│   │   │       ├── DocumentType.java (DNI, PASSPORT, RUC)
│   │   │       └── RecordStatus.java (ACTIVE, INACTIVE)
│   │   ├── ports/in/
│   │   │   ├── ICreateUserUseCase.java
│   │   │   ├── IGetUserUseCase.java
│   │   │   ├── IUpdateUserUseCase.java
│   │   │   └── IDeleteUserUseCase.java
│   │   ├── ports/out/
│   │   │   ├── IUserRepository.java
│   │   │   ├── IAuthenticationClient.java
│   │   │   ├── IOrganizationClient.java
│   │   │   ├── INotificationClient.java
│   │   │   └── IUserEventPublisher.java
│   │   └── exceptions/
│   │       ├── UserNotFoundException.java
│   │       ├── DuplicateUserException.java
│   │       └── OrganizationNotFoundException.java
│   ├── application/
│   │   ├── usecases/
│   │   │   ├── CreateUserUseCaseImpl.java
│   │   │   ├── GetUserUseCaseImpl.java
│   │   │   ├── UpdateUserUseCaseImpl.java
│   │   │   └── DeleteUserUseCaseImpl.java
│   │   ├── dto/
│   │   │   ├── request/
│   │   │   │   ├── CreateUserRequest.java
│   │   │   │   └── UpdateUserRequest.java
│   │   │   ├── response/
│   │   │   │   └── UserResponse.java
│   │   │   └── common/
│   │   │       ├── ApiResponse.java
│   │   │       └── ErrorMessage.java
│   │   ├── mappers/
│   │   │   └── UserMapper.java
│   │   └── events/
│   │       ├── UserCreatedEvent.java
│   │       └── publishers/
│   │           └── UserEventPublisherImpl.java
│   └── infrastructure/
│       ├── adapters/in/rest/
│       │   └── UserRest.java
│       ├── adapters/out/
│       │   ├── persistence/
│       │   │   └── UserRepositoryImpl.java
│       │   └── external/
│       │       ├── AuthenticationClientImpl.java (Circuit Breaker)
│       │       ├── OrganizationClientImpl.java (Circuit Breaker)
│       │       └── NotificationClientImpl.java (Circuit Breaker)
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
│           └── GlobalExceptionHandler.java
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   ├── application-prod.yml
│   └── db/migration/
│       └── V1__create_users_table.sql
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

## 🛠️ Instalación y Ejecución

### Requisitos Previos

- Java 21
- Maven 3.9+
- Docker & Docker Compose
- PostgreSQL 16 (si se ejecuta localmente)

### 1. Clonar el repositorio

```bash
cd vg-ms-users
```

### 2. Compilar el proyecto

```bash
mvn clean package -DskipTests
```

### 3. Ejecutar con Docker Compose

```bash
docker-compose up --build
```

### 4. Ejecutar localmente (desarrollo)

```bash
# Asegúrate de tener PostgreSQL corriendo en localhost:5432
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🌐 Endpoints API

### Usuarios

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/api/users` | Crear usuario |
| GET | `/api/users/{userId}` | Obtener usuario por ID |
| GET | `/api/users/username/{username}` | Obtener usuario por username |
| GET | `/api/users` | Listar todos los usuarios |
| GET | `/api/users/role/{role}` | Listar usuarios por rol |
| GET | `/api/users/status/{status}` | Listar usuarios por estado |
| GET | `/api/users/organization/{orgId}` | Listar usuarios por organización |
| PUT | `/api/users/{userId}` | Actualizar usuario |
| DELETE | `/api/users/{userId}` | Soft delete usuario |
| PATCH | `/api/users/{userId}/restore` | Restaurar usuario eliminado |

### Ejemplo Request - Crear Usuario

```json
POST /api/users
{
  "username": "juan.perez",
  "password": "SecurePass123",
  "firstName": "Juan",
  "lastName": "Pérez",
  "documentType": "DNI",
  "documentNumber": "12345678",
  "email": "juan.perez@example.com",
  "phone": "987654321",
  "address": "Jr. Los Andes 123",
  "role": "CLIENT",
  "organizationId": "org-001",
  "zoneId": "zone-001",
  "streetId": "street-001"
}
```

### Response

```json
{
  "status": 200,
  "message": "User created successfully",
  "data": {
    "userId": "7f3e4d2a-1234-5678-9abc-def012345678",
    "username": "juan.perez",
    "firstName": "Juan",
    "lastName": "Pérez",
    "fullName": "Juan Pérez",
    "documentType": "DNI",
    "documentNumber": "12345678",
    "email": "juan.perez@example.com",
    "phone": "987654321",
    "address": "Jr. Los Andes 123",
    "role": "CLIENT",
    "status": "ACTIVE",
    "organizationId": "org-001",
    "zoneId": "zone-001",
    "streetId": "street-001",
    "createdAt": "2024-01-20T10:30:00",
    "updatedAt": null
  },
  "timestamp": "2024-01-20T10:30:00.123"
}
```

## 🔧 Configuración

### Variables de Entorno (Producción)

```env
SPRING_PROFILE=prod
PORT=8081
DB_HOST=postgres-users
DB_PORT=5432
DB_NAME=vg_users
DB_USER=postgres
DB_PASSWORD=postgres
RABBITMQ_HOST=genetic-yolane-vallegrandesistema-a92b57a3.koyeb.app
RABBITMQ_PORT=443
RABBITMQ_USER=admin
RABBITMQ_PASSWORD=admin
```

### RabbitMQ

**Local:** `http://localhost:15672/` (admin/admin)
**Producción:** `https://genetic-yolane-vallegrandesistema-a92b57a3.koyeb.app/`

## 🔄 Eventos RabbitMQ

El microservicio publica eventos a través de RabbitMQ:

- `users.created` → Notificar creación de usuario
- `users.updated` → Notificar actualización de usuario
- `users.deleted` → Notificar eliminación de usuario

## 🛡️ Circuit Breaker

Implementado con **Resilience4j** para los siguientes servicios:

- `authenticationService` → vg-ms-authentication
- `organizationService` → vg-ms-organizations
- `notificationService` → vg-ms-notification

## 📊 Base de Datos

**PostgreSQL 16** - Base de datos: `vg_users`

### Tabla: users

| Campo | Tipo | Descripción |
|-------|------|-------------|
| user_id | UUID | PK |
| username | VARCHAR(50) | UNIQUE |
| first_name | VARCHAR(100) | |
| last_name | VARCHAR(100) | |
| document_type | VARCHAR(20) | DNI, PASSPORT, RUC |
| document_number | VARCHAR(20) | UNIQUE |
| email | VARCHAR(255) | |
| phone | VARCHAR(20) | |
| address | TEXT | |
| role | VARCHAR(20) | SUPER_ADMIN, ADMIN, CLIENT, OPERATOR |
| status | VARCHAR(20) | ACTIVE, INACTIVE |
| organization_id | VARCHAR(50) | |
| zone_id | VARCHAR(50) | |
| street_id | VARCHAR(50) | |
| created_at | TIMESTAMP | |
| updated_at | TIMESTAMP | |
| created_by | UUID | |
| updated_by | UUID | |

## 📝 Migraciones Flyway

Las migraciones se aplican automáticamente al iniciar:

- `V1__create_users_table.sql` → Crea tabla users con índices

## 🧪 Testing

```bash
mvn test
```

## 📄 Licencia

Este proyecto es parte del sistema JASS Digital desarrollado para la gestión de organizaciones de agua potable.

## 👥 Autor

**Valle Grande University - Software Development Team**
