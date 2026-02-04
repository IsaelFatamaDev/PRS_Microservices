# 🔐 vg-ms-authentication - Microservicio de Autenticación

> **Arquitectura Hexagonal + DDD + Clean Code + SOLID**

## 📋 Descripción

Microservicio responsable de la autenticación y gestión de identidades. **Es un PROXY a Keycloak**, NO almacena credenciales localmente. Escucha eventos de usuarios para sincronizar con Keycloak.

---

## 🏗️ Arquitectura

```
┌─────────────────────────────────────────────────────────────────────┐
│                        🌐 REST API (Port 8082)                       │
├─────────────────────────────────────────────────────────────────────┤
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐   │
│  │                    📥 INFRASTRUCTURE                          │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────┐  │   │
│  │  │   REST      │  │  Keycloak   │  │  RabbitMQ Listeners  │  │   │
│  │  │  Adapters   │  │  Client     │  │  (User Events)       │  │   │
│  │  └──────┬──────┘  └──────┬──────┘  └──────────┬───────────┘  │   │
│  └─────────┼────────────────┼───────────────────┼───────────────┘   │
│            │                │                   │                    │
│  ┌─────────▼────────────────▼───────────────────▼───────────────┐   │
│  │                    📦 APPLICATION                             │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────┐  │   │
│  │  │   Use Cases │  │   Mappers   │  │   DTOs & Events      │  │   │
│  │  │   (Auth)    │  │             │  │   (External)         │  │   │
│  │  └──────┬──────┘  └─────────────┘  └──────────────────────┘  │   │
│  └─────────┼────────────────────────────────────────────────────┘   │
│            │                                                         │
│  ┌─────────▼────────────────────────────────────────────────────┐   │
│  │                    💎 DOMAIN                                  │   │
│  │  ┌─────────────┐  ┌─────────────┐  ┌──────────────────────┐  │   │
│  │  │   Models    │  │    Ports    │  │    Exceptions        │  │   │
│  │  │ (Credentials│  │  (in/out)   │  │                      │  │   │
│  │  └─────────────┘  └─────────────┘  └──────────────────────┘  │   │
│  └──────────────────────────────────────────────────────────────┘   │
│                                                                      │
└─────────────────────────────────────────────────────────────────────┘
                                    │
                                    ▼
                    ┌───────────────────────────┐
                    │       🔑 KEYCLOAK         │
                    │    (Identity Provider)    │
                    │   ┌─────────────────┐     │
                    │   │  Realm:         │     │
                    │   │  sistema-jass   │     │
                    │   └─────────────────┘     │
                    └───────────────────────────┘
```

---

## 📂 Estructura de Carpetas

```
vg-ms-authentication/
├── src/main/java/pe/edu/vallegrande/vgmsauthentication/
│   │
│   ├── domain/                          → 💎 Núcleo de negocio (sin dependencias Spring)
│   │   ├── models/
│   │   │   └── UserCredentials.java                → [CLASS] DTO temporal (NO persiste)
│   │   ├── ports/
│   │   │   ├── in/                                 → Casos de uso (entrada)
│   │   │   │   ├── ILoginUseCase.java              → [INTERFACE]
│   │   │   │   ├── ILogoutUseCase.java             → [INTERFACE]
│   │   │   │   ├── IRefreshTokenUseCase.java       → [INTERFACE]
│   │   │   │   └── IValidateTokenUseCase.java      → [INTERFACE]
│   │   │   └── out/                                → Clientes externos (salida)
│   │   │       ├── IKeycloakClient.java            → [INTERFACE]
│   │   │       ├── IUserServiceClient.java         → [INTERFACE]
│   │   │       └── ISecurityContext.java           → [INTERFACE]
│   │   └── exceptions/
│   │       ├── DomainException.java                → [ABSTRACT CLASS]
│   │       ├── NotFoundException.java              → [CLASS] extends DomainException
│   │       ├── BusinessRuleException.java          → [CLASS] extends DomainException
│   │       ├── ExternalServiceException.java       → [CLASS] extends DomainException
│   │       ├── InvalidCredentialsException.java    → [CLASS] extends BusinessRuleException
│   │       ├── KeycloakException.java              → [CLASS] extends ExternalServiceException
│   │       ├── TokenExpiredException.java          → [CLASS] extends BusinessRuleException
│   │       └── TokenInvalidException.java          → [CLASS] extends BusinessRuleException
│   │
│   ├── application/                     → 📦 Orquestación de casos de uso
│   │   ├── usecases/
│   │   │   ├── LoginUseCaseImpl.java               → [CLASS] @Service @RequiredArgsConstructor
│   │   │   ├── LogoutUseCaseImpl.java              → [CLASS] @Service @RequiredArgsConstructor
│   │   │   ├── RefreshTokenUseCaseImpl.java        → [CLASS] @Service @RequiredArgsConstructor
│   │   │   └── ValidateTokenUseCaseImpl.java       → [CLASS] @Service @RequiredArgsConstructor
│   │   ├── dto/
│   │   │   ├── common/
│   │   │   │   ├── ApiResponse.java                → [RECORD]
│   │   │   │   └── ErrorMessage.java               → [RECORD]
│   │   │   ├── request/
│   │   │   │   ├── LoginRequest.java               → [RECORD] @Valid
│   │   │   │   ├── RefreshTokenRequest.java        → [RECORD] @Valid
│   │   │   │   └── LogoutRequest.java              → [RECORD] @Valid
│   │   │   └── response/
│   │   │       ├── LoginResponse.java              → [RECORD]
│   │   │       ├── TokenResponse.java              → [RECORD]
│   │   │       └── UserInfoResponse.java           → [RECORD]
│   │   ├── mappers/
│   │   │   └── AuthMapper.java                     → [INTERFACE] @Mapper (MapStruct)
│   │   └── events/
│   │       └── external/                           → DTOs de eventos que ESCUCHA
│   │           ├── UserCreatedEvent.java           → [CLASS] @Data @Builder
│   │           ├── UserUpdatedEvent.java           → [CLASS] @Data @Builder
│   │           ├── UserDeletedEvent.java           → [CLASS] @Data @Builder
│   │           ├── UserRestoredEvent.java          → [CLASS] @Data @Builder
│   │           └── UserPurgedEvent.java            → [CLASS] @Data @Builder
│   │
│   └── infrastructure/                  → 📥 Adaptadores externos
│       ├── adapters/
│       │   ├── in/rest/
│       │   │   ├── AuthRest.java                   → [CLASS] @RestController @RequestMapping
│       │   │   └── GlobalExceptionHandler.java     → [CLASS] @RestControllerAdvice @Slf4j
│       │   └── out/
│       │       └── external/
│       │           ├── KeycloakClientImpl.java     → [CLASS] @Component @RequiredArgsConstructor
│       │           └── UserServiceClientImpl.java  → [CLASS] @Component @RequiredArgsConstructor
│       ├── messaging/
│       │   └── listeners/
│       │       └── UserEventListener.java          → [CLASS] @Component @RabbitListener @Slf4j
│       ├── security/
│       │   ├── AuthenticatedUser.java              → [CLASS] @Data @Builder
│       │   ├── GatewayHeadersExtractor.java        → [CLASS] @Component
│       │   ├── GatewayHeadersFilter.java           → [CLASS] @Component implements WebFilter
│       │   └── SecurityContextAdapter.java         → [CLASS] @Component implements ISecurityContext
│       └── config/
│           ├── KeycloakConfig.java                 → [CLASS] @Configuration @Bean
│           ├── WebClientConfig.java                → [CLASS] @Configuration @Bean
│           ├── RabbitMQConfig.java                 → [CLASS] @Configuration @Bean
│           ├── Resilience4jConfig.java             → [CLASS] @Configuration
│           ├── SecurityConfig.java                 → [CLASS] @Configuration @EnableWebFluxSecurity (NO CORS)
│           └── RequestContextFilter.java           → [CLASS] @Component implements WebFilter
│               📝 Swagger detecta endpoints automáticamente (springdoc-openapi)
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
│
├── Dockerfile
├── docker-compose.yml
├── pom.xml
└── README.md
```

---

## ⚠️ Notas Importantes

### Este Microservicio NO Tiene Base de Datos

> **vg-ms-authentication es un PROXY a Keycloak.**
>
> - NO guarda passwords
> - NO tiene tablas propias
> - Toda la gestión de credenciales está en Keycloak

### Solo ESCUCHA eventos (no publica)

> Este microservicio **SOLO ESCUCHA** eventos de `jass.events`:
>
> - `user.created` → Crear usuario en Keycloak
> - `user.deleted` → Deshabilitar usuario en Keycloak
> - `user.restored` → Rehabilitar usuario en Keycloak
> - `user.purged` → Eliminar usuario de Keycloak permanentemente

### CORS

> **CORS se configura ÚNICAMENTE en `vg-ms-gateway`**, NO en este microservicio.

---

## 🔧 Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje |
| Spring Boot | 3.5.x | Framework |
| Spring WebFlux | 3.2.x | API Reactiva |
| Keycloak Admin Client | 23.x | Comunicación con Keycloak |
| RabbitMQ | 3.12 | Escuchar eventos de usuarios |
| Resilience4j | 2.x | Circuit Breaker |
| Lombok | 1.18.x | Boilerplate |

> ⚠️ **NOTA IMPORTANTE**: Este microservicio **NO necesita**:
>
> - ❌ `spring-boot-starter-oauth2-resource-server` - No valida tokens, solo los solicita
> - ❌ `spring-cloud-starter-netflix-eureka-client` - Opcional, solo si usas Eureka en otros MS

---

## 🔄 Diagrama de Flujo Completo

### 1. Login Flow

```
┌──────────┐      ┌──────────────┐      ┌─────────────────────┐      ┌───────────┐
│  Client  │      │  vg-gateway  │      │ vg-ms-authentication│      │  Keycloak │
│  (Web)   │      │  (port 8080) │      │    (port 8082)      │      │(port 9090)│
└────┬─────┘      └──────┬───────┘      └──────────┬──────────┘      └─────┬─────┘
     │                   │                         │                       │
     │ POST /api/auth/login                        │                       │
     │ {username, password}                        │                       │
     │──────────────────>│                         │                       │
     │                   │                         │                       │
     │                   │ Forward request         │                       │
     │                   │────────────────────────>│                       │
     │                   │                         │                       │
     │                   │                         │ POST /token           │
     │                   │                         │ grant_type=password   │
     │                   │                         │──────────────────────>│
     │                   │                         │                       │
     │                   │                         │ {access_token,        │
     │                   │                         │  refresh_token,       │
     │                   │                         │  expires_in}          │
     │                   │                         │<──────────────────────│
     │                   │                         │                       │
     │                   │ LoginResponse           │                       │
     │                   │<────────────────────────│                       │
     │                   │                         │                       │
     │ {access_token,    │                         │                       │
     │  refresh_token,   │                         │                       │
     │  user_info}       │                         │                       │
     │<──────────────────│                         │                       │
```

### 2. Token Refresh Flow

```
┌──────────┐      ┌─────────────────────┐      ┌───────────┐
│  Client  │      │ vg-ms-authentication│      │  Keycloak │
└────┬─────┘      └──────────┬──────────┘      └─────┬─────┘
     │                       │                       │
     │ POST /auth/refresh    │                       │
     │ {refresh_token}       │                       │
     │──────────────────────>│                       │
     │                       │                       │
     │                       │ POST /token           │
     │                       │ grant_type=refresh    │
     │                       │──────────────────────>│
     │                       │                       │
     │                       │ {new_access_token,    │
     │                       │  new_refresh_token}   │
     │                       │<──────────────────────│
     │                       │                       │
     │ TokenResponse         │                       │
     │<──────────────────────│                       │
```

### 3. User Sync Flow (RabbitMQ - Eventos)

```
┌─────────────┐      ┌──────────────┐      ┌─────────────────────┐      ┌───────────┐
│ vg-ms-users │      │   RabbitMQ   │      │ vg-ms-authentication│      │  Keycloak │
│ (port 8081) │      │ (port 5672)  │      │    (port 8082)      │      │(port 9090)│
└──────┬──────┘      └──────┬───────┘      └──────────┬──────────┘      └─────┬─────┘
       │                    │                         │                       │
       │ 1. User created    │                         │                       │
       │    in database     │                         │                       │
       │                    │                         │                       │
       │ 2. Publish event   │                         │                       │
       │    user.created    │                         │                       │
       │───────────────────>│                         │                       │
       │                    │                         │                       │
       │                    │ 3. Consume event        │                       │
       │                    │────────────────────────>│                       │
       │                    │                         │                       │
       │                    │                         │ 4. POST /admin/users  │
       │                    │                         │    Create user        │
       │                    │                         │──────────────────────>│
       │                    │                         │                       │
       │                    │                         │ 5. 201 Created        │
       │                    │                         │<──────────────────────│
       │                    │                         │                       │
       │                    │                         │ 6. POST /admin/roles  │
       │                    │                         │    Assign role        │
       │                    │                         │──────────────────────>│
       │                    │                         │                       │
       │                    │                         │ 7. 204 No Content     │
       │                    │                         │<──────────────────────│
```

### 4. Validated API Request Flow

```
┌──────────┐      ┌──────────────┐      ┌─────────────────────┐      ┌───────────┐
│  Client  │      │  vg-gateway  │      │   Any Microservice  │      │  Keycloak │
│  (Web)   │      │  (port 8080) │      │  (vg-ms-users, etc) │      │(port 9090)│
└────┬─────┘      └──────┬───────┘      └──────────┬──────────┘      └─────┬─────┘
     │                   │                         │                       │
     │ GET /api/users    │                         │                       │
     │ Authorization:    │                         │                       │
     │ Bearer {token}    │                         │                       │
     │──────────────────>│                         │                       │
     │                   │                         │                       │
     │                   │ 1. Validate JWT         │                       │
     │                   │ (introspect or JWKS)    │                       │
     │                   │────────────────────────────────────────────────>│
     │                   │                         │                       │
     │                   │ 2. Token valid          │                       │
     │                   │<────────────────────────────────────────────────│
     │                   │                         │                       │
     │                   │ 3. Forward + Headers    │                       │
     │                   │ X-User-Id: uuid         │                       │
     │                   │ X-User-Roles: ADMIN     │                       │
     │                   │────────────────────────>│                       │
     │                   │                         │                       │
     │                   │ 4. Response             │                       │
     │                   │<────────────────────────│                       │
     │                   │                         │                       │
     │ Response          │                         │                       │
     │<──────────────────│                         │                       │
```

---

## 🔑 Keycloak Configuration

### Entornos

| Entorno | URL | Realm |
|---------|-----|-------|
| **Local (Docker)** | `http://localhost:9090` | `sistema-jass` |
| **Producción** | `https://lab.vallegrande.edu.pe/jass/keycloak` | `sistema-jass` |

### Endpoints OAuth2/OIDC (Producción)

| Endpoint | URL |
|----------|-----|
| Token | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/token` |
| Userinfo | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/userinfo` |
| Logout | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/logout` |
| Introspect | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/token/introspect` |
| JWKS | `https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass/protocol/openid-connect/certs` |

### Grant Types Soportados

- `authorization_code`
- `password` (Resource Owner Password Credentials)
- `refresh_token`
- `client_credentials`

---

## 📡 Eventos RabbitMQ

### Exchange: `jass.events` (compartido por todos los microservicios)

| Routing Key | Acción | Descripción |
|-------------|--------|-------------|
| `user.created` | Crear en Keycloak | Crea usuario con email y rol |
| `user.updated` | Actualizar en Keycloak | Actualiza firstName, lastName, email |
| `user.deleted` | Deshabilitar en Keycloak | `enabled = false` |
| `user.restored` | Rehabilitar en Keycloak | `enabled = true` |
| `user.purged` | Eliminar de Keycloak | Elimina permanentemente |

---

## 🔌 Dependencias Externas

| Servicio | Puerto | Propósito |
|----------|--------|-----------|
| Keycloak | 9090 (local) / 443 (prod) | Identity Provider |
| vg-ms-users | 8081 | Obtener datos de usuario |
| RabbitMQ | 5672 | Escuchar eventos |

---

## 📚 Documentación Detallada

- [README_DOMAIN.md](README_DOMAIN.md) - Capa de Dominio
- [README_APPLICATION.md](README_APPLICATION.md) - Capa de Aplicación
- [README_INFRASTRUCTURE.md](README_INFRASTRUCTURE.md) - Capa de Infraestructura

---

## 🐳 Docker Compose (Local)

```yaml
version: "3.9"

services:
  postgres:
    image: postgres:15-alpine
    container_name: keycloak-db
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: keycloak123!
    ports:
      - "5600:5432"
    volumes:
      - keycloak_pgdata:/var/lib/postgresql/data
    restart: unless-stopped
    networks:
      - keycloak-net

  keycloak:
    image: quay.io/keycloak/keycloak:26.0.8
    container_name: keycloak
    depends_on:
      - postgres
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
    restart: unless-stopped
    networks:
      - keycloak-net

volumes:
  keycloak_pgdata:

networks:
  keycloak-net:
    driver: bridge
```

---

## 🚀 Endpoints API

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/login` | Login con username/password |
| POST | `/auth/refresh` | Refrescar access token |
| POST | `/auth/logout` | Cerrar sesión |
| GET | `/auth/userinfo` | Obtener información del usuario autenticado |
| POST | `/auth/validate` | Validar token (interno) |
| POST | `/auth/introspect` | Introspección de token |
