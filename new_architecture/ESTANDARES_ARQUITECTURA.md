# 🏗️ ESTÁNDARES DE ARQUITECTURA - JASS DIGITAL

Este documento define los **ESTÁNDARES OBLIGATORIOS** que todos los microservicios deben implementar.

---

## 1. 📋 SHARED PACKAGE - CLASES COMUNES

### 1.1 BaseEntity (Para TODOS los modelos de dominio)

```java
// Ubicación: src/main/java/pe/edu/vallegrande/shared/domain/BaseEntity.java
package pe.edu.vallegrande.shared.domain;

import java.time.LocalDateTime;

public abstract class BaseEntity {
    protected String id;                    // UUID generado
    protected String organizationId;        // Multi-tenancy (OBLIGATORIO)
    protected RecordStatus recordStatus;    // ACTIVE, INACTIVE (OBLIGATORIO)
    protected LocalDateTime createdAt;      // Auditoría
    protected String createdBy;             // userId que creó
    protected LocalDateTime updatedAt;      // Auditoría
    protected String updatedBy;             // userId que actualizó

    // Getters, Setters, equals, hashCode
}
```

### 1.2 RecordStatus (Estándar UNIVERSAL)

```java
// Ubicación: src/main/java/pe/edu/vallegrande/shared/valueobjects/RecordStatus.java
package pe.edu.vallegrande.shared.valueobjects;

public enum RecordStatus {
    ACTIVE,      // Registro activo y visible
    INACTIVE     // Registro inactivo (soft delete)
}
```

### 1.3 ApiResponse (DTO Estándar de Respuesta)

```java
// Ubicación: src/main/java/pe/edu/vallegrande/shared/dto/ApiResponse.java
package pe.edu.vallegrande.shared.dto;

import java.time.LocalDateTime;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;
    private String path;

    // Constructors, Getters, Setters
}
```

### 1.4 ErrorMessage (DTO Estándar de Error)

```java
// Ubicación: src/main/java/pe/edu/vallegrande/shared/dto/ErrorMessage.java
package pe.edu.vallegrande.shared.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ErrorMessage {
    private int status;
    private String error;
    private String message;
    private List<String> details;
    private LocalDateTime timestamp;
    private String path;

    // Constructors, Getters, Setters
}
```

---

## 2. 🔐 AUTENTICACIÓN Y SEGURIDAD

### 2.1 ❌ NO SE GUARDAN PASSWORDS EN BASE DE DATOS

**REGLA ABSOLUTA**:

- **vg-ms-authentication** NO guarda passwords
- La autenticación se delega 100% a **Keycloak**
- vg-ms-authentication es un **PROXY** que:
  1. Recibe credenciales (username/password)
  2. Las envía a Keycloak via Admin API
  3. Retorna el JWT generado por Keycloak
  4. NO almacena NADA en PostgreSQL (tabla credentials NO existe)

### 2.2 Flujo de Autenticación

```
┌──────────┐      POST /login       ┌─────────────────┐
│  CLIENT  │ ──────────────────────> │ vg-ms-auth      │
└──────────┘   {username, password}  │                 │
                                     │  1. Valida con  │
                                     │     Keycloak    │
                                     │                 │
                                     │  2. Obtiene JWT │
                                     │                 │
┌──────────┐      JWT Token          │  3. Consulta    │
│  CLIENT  │ <────────────────────── │     vg-ms-users │
└──────────┘   {token, userInfo}     │     (role, org) │
                                     └─────────────────┘
```

### 2.3 Configuración Keycloak

```yaml
# application.yml - vg-ms-authentication
keycloak:
  auth-server-url: http://keycloak:8080
  realm: jass-digital
  resource: jass-users-service
  credentials:
    secret: ${KEYCLOAK_CLIENT_SECRET}
  admin:
    username: admin
    password: ${KEYCLOAK_ADMIN_PASSWORD}
```

---

## 3. 🔗 ESTRATEGIA DE COMUNICACIÓN ENTRE SERVICIOS

### 3.1 Comunicación Síncrona (REST + Circuit Breaker)

**REGLA**: Solo usar REST cuando es **ESTRICTAMENTE NECESARIO** validar datos en tiempo real.

#### Servicios que SÍ usan WebClient + Circuit Breaker

| Servicio Origen     | Servicio Destino      | Propósito                                    | Circuit Breaker |
|---------------------|-----------------------|----------------------------------------------|-----------------|
| vg-ms-users         | vg-ms-organizations   | Validar organization/zone/street existe      | ✅ SÍ           |
| vg-ms-users         | vg-ms-authentication  | Crear credenciales en Keycloak               | ✅ SÍ           |
| vg-ms-users         | vg-ms-notification    | Enviar WhatsApp bienvenida                   | ✅ SÍ           |
| vg-ms-infrastructure| vg-ms-payments        | Validar que usuario NO tiene deuda           | ✅ SÍ           |

#### Servicios que NO usan WebClient (solo headers)

- ✅ vg-ms-payments
- ✅ vg-ms-water-quality
- ✅ vg-ms-distribution
- ✅ vg-ms-inventory
- ✅ vg-ms-claims-incidents

**Razón**: Estos servicios reciben `X-User-Id`, `X-Organization-Id`, `X-Role` en headers (inyectados por Gateway) y NO necesitan validar contra otros servicios.

### 3.2 Configuración Circuit Breaker (Resilience4j)

**TODOS** los servicios que usan WebClient deben tener:

```yaml
# application.yml
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
      authenticationService:
        # ... misma config
      notificationService:
        # ... misma config

  retry:
    instances:
      organizationService:
        max-attempts: 3
        wait-duration: 500ms
      authenticationService:
        max-attempts: 3
        wait-duration: 500ms
```

**Archivos necesarios**:

```
infrastructure/
└── config/
    ├── WebClientConfig.java          → Bean WebClient
    └── Resilience4jConfig.java       → Circuit Breaker config
```

### 3.3 Comunicación Asíncrona (RabbitMQ)

**REGLA**: Usar eventos para notificaciones y propagación de cambios NO críticos.

#### Eventos Publicados

| Evento                     | Publisher              | Subscribers                          |
|----------------------------|------------------------|--------------------------------------|
| UserCreatedEvent           | vg-ms-users            | vg-ms-notification                   |
| PaymentCompletedEvent      | vg-ms-payments         | vg-ms-infrastructure (reconectar)    |
| WaterBoxAssignedEvent      | vg-ms-infrastructure   | vg-ms-notification                   |
| OrganizationCreatedEvent   | vg-ms-organizations    | vg-ms-notification                   |
| QualityTestFailedEvent     | vg-ms-water-quality    | vg-ms-notification, vg-ms-claims     |

**Archivos necesarios**:

```
infrastructure/
└── config/
    └── RabbitMQConfig.java           → Exchanges, Queues, Bindings

application/
└── events/
    ├── UserCreatedEvent.java
    └── publishers/
        └── UserEventPublisherImpl.java
```

---

## 4. 🛡️ SEGURIDAD Y MULTI-TENANCY

### 4.1 RequestContextFilter (OBLIGATORIO en TODOS los servicios)

```java
// infrastructure/config/RequestContextFilter.java
@Component
public class RequestContextFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        String userId = request.getHeaders().getFirst("X-User-Id");
        String username = request.getHeaders().getFirst("X-Username");
        String role = request.getHeaders().getFirst("X-Role");
        String organizationId = request.getHeaders().getFirst("X-Organization-Id");

        // Validar que organizationId existe (Multi-tenancy)
        if (organizationId == null || organizationId.isBlank()) {
            return Mono.error(new UnauthorizedException("Missing organization context"));
        }

        // Guardar en context para uso en todo el request
        return chain.filter(exchange)
            .contextWrite(ctx -> ctx
                .put("userId", userId)
                .put("username", username)
                .put("role", role)
                .put("organizationId", organizationId)
            );
    }
}
```

### 4.2 Headers Estándar

| Header               | Fuente       | Descripción                              |
|----------------------|--------------|------------------------------------------|
| X-User-Id            | Gateway/JWT  | UUID del usuario autenticado             |
| X-Username           | Gateway/JWT  | Username (email o código)                |
| X-Role               | Gateway/JWT  | SUPER_ADMIN, ADMIN, CLIENT               |
| X-Organization-Id    | Gateway/JWT  | UUID de la organización (tenant)         |
| Authorization        | Client       | Bearer {JWT_TOKEN}                       |

---

## 5. 📊 AUDITORÍA Y TRAZABILIDAD

### 5.1 Campos de Auditoría (OBLIGATORIOS)

**TODOS** los modelos de dominio (`User`, `Payment`, `WaterBox`, etc.) deben heredar de `BaseEntity` o incluir:

```java
public class Payment extends BaseEntity {
    // Campos específicos del dominio
    private String paymentCode;
    private BigDecimal amount;

    // BaseEntity ya provee:
    // - id
    // - organizationId (Multi-tenancy)
    // - recordStatus (ACTIVE/INACTIVE)
    // - createdAt, createdBy, updatedAt, updatedBy
}
```

### 5.2 Implementación en UseCases

```java
@Service
public class CreatePaymentUseCaseImpl implements ICreatePaymentUseCase {

    @Override
    public Mono<Payment> execute(CreatePaymentRequest request) {
        return Mono.deferContextual(ctx -> {
            String userId = ctx.get("userId");
            String organizationId = ctx.get("organizationId");

            Payment payment = new Payment();
            payment.setId(UUID.randomUUID().toString());
            payment.setOrganizationId(organizationId);
            payment.setRecordStatus(RecordStatus.ACTIVE);
            payment.setCreatedAt(LocalDateTime.now());
            payment.setCreatedBy(userId);
            // ... resto de lógica

            return paymentRepository.save(payment);
        });
    }
}
```

---

## 6. 🗄️ BASE DE DATOS - ESTÁNDARES

### 6.1 Tablas PostgreSQL (Estructura Estándar)

```sql
-- Ejemplo: tabla users
CREATE TABLE users (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    organization_id     UUID NOT NULL,
    record_status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    updated_at          TIMESTAMP,
    updated_by          UUID,

    -- Campos específicos
    user_code           VARCHAR(50) UNIQUE NOT NULL,
    first_name          VARCHAR(100) NOT NULL,
    -- ...

    CONSTRAINT check_record_status CHECK (record_status IN ('ACTIVE', 'INACTIVE'))
);

-- Índices estándar
CREATE INDEX idx_users_organization ON users(organization_id);
CREATE INDEX idx_users_status ON users(record_status);
CREATE INDEX idx_users_created_at ON users(created_at);
```

### 6.2 Documents MongoDB (Estructura Estándar)

```javascript
// Ejemplo: collection quality_tests
{
    "_id": ObjectId("..."),
    "testCode": "QT-2024-001",
    "organizationId": "uuid-here",
    "recordStatus": "ACTIVE",
    "createdAt": ISODate("2024-01-31T..."),
    "createdBy": "uuid-user",
    "updatedAt": ISODate("2024-01-31T..."),
    "updatedBy": "uuid-user",

    // Campos específicos
    "testType": "CHLORINE",
    "result": "APPROVED",
    // ...
}

// Índices estándar
db.quality_tests.createIndex({ "organizationId": 1, "recordStatus": 1 });
db.quality_tests.createIndex({ "testCode": 1 }, { unique: true });
db.quality_tests.createIndex({ "createdAt": -1 });
```

---

## 7. 🎯 RESUMEN DE CONFIGURACIONES POR SERVICIO

| Servicio              | WebClient | Circuit Breaker | RabbitMQ | RequestContextFilter |
|-----------------------|-----------|-----------------|----------|----------------------|
| vg-ms-users           | ✅        | ✅              | ✅       | ✅                   |
| vg-ms-authentication  | ✅        | ✅              | ❌       | ✅                   |
| vg-ms-organizations   | ❌        | ❌              | ✅       | ✅                   |
| vg-ms-payments        | ❌        | ❌              | ✅       | ✅                   |
| vg-ms-infrastructure  | ✅        | ✅              | ✅       | ✅                   |
| vg-ms-water-quality   | ❌        | ❌              | ✅       | ✅                   |
| vg-ms-distribution    | ❌        | ❌              | ✅       | ✅                   |
| vg-ms-inventory       | ❌        | ❌              | ✅       | ✅                   |
| vg-ms-claims          | ❌        | ❌              | ✅       | ✅                   |
| vg-ms-notification    | N/A       | N/A             | ✅       | ✅ (Express)         |
| vg-ms-gateway         | N/A       | N/A             | ❌       | ❌                   |

---

## 8. ✅ CHECKLIST DE CUMPLIMIENTO

Para cada microservicio, verificar:

- [ ] Modelos heredan de `BaseEntity` o implementan campos de auditoría
- [ ] Todos los modelos tienen `RecordStatus` (ACTIVE/INACTIVE)
- [ ] DTOs usan `ApiResponse<T>` y `ErrorMessage`
- [ ] Implementa `RequestContextFilter` para leer headers
- [ ] NO guarda passwords (solo Keycloak maneja autenticación)
- [ ] Usa Circuit Breaker si hace llamadas REST a otros servicios
- [ ] Publica eventos a RabbitMQ si cambios requieren propagación
- [ ] Tablas/Collections tienen índices en `organization_id`, `record_status`, `created_at`
- [ ] UseCases extraen `userId` y `organizationId` del contexto reactivo
- [ ] Controllers retornan `Mono<ApiResponse<T>>` o `Flux<T>`
