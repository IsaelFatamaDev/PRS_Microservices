# 📐 ESTÁNDAR DE NOMBRES, ESTRUCTURA Y ATRIBUTOS — SISTEMA JASS

**Objetivo:** Unificar nombres, carpetas, atributos de tablas y convenciones en **todos** los microservicios para que el sistema sea fácil de mantener y refactorizar.

**Fecha:** 30 Enero 2026  
**Aplicable a:** Todos los microservicios Java (vg-ms-*)

---

## 📑 ÍNDICE

1. [Paquetes Java](#1-paquetes-java)
2. [Estructura de carpetas (todos los MS iguales)](#2-estructura-de-carpetas-todos-los-ms-iguales)
3. [Nombres de tablas y colecciones](#3-nombres-de-tablas-y-colecciones)
4. [Nombres de columnas y campos en BD](#4-nombres-de-columnas-y-campos-en-bd)
5. [Atributos estándar en todas las tablas/colecciones](#5-atributos-estándar-en-todas-las-tablascolecciones)
6. [Nombres de clases Java](#6-nombres-de-clases-java)
7. [Nombres de DTOs (request/response)](#7-nombres-de-dtos-requestresponse)
8. [Rutas REST (API)](#8-rutas-rest-api)
9. [Nombres de repositorios, servicios y mappers](#9-nombres-de-repositorios-servicios-y-mappers)
10. [Enums y constantes](#10-enums-y-constantes)
11. [Checklist de refactorización](#11-checklist-de-refactorización)

---

## 1. PAQUETES JAVA

### Regla única

- **Formato:** `pe.edu.vallegrande.vgms{contexto}`  
- **Solo minúsculas**, **sin guiones ni guiones bajos**.  
- El “contexto” es una sola palabra o varias pegadas en camelCase para el paquete (en minúsculas: solo la primera letra del contexto en mayúscula en el nombre del artefacto, no en el paquete).

### Nombres de paquete por microservicio

| Microservicio              | Paquete base (INCORRECTO hoy)     | Paquete base CORRECTO        |
|---------------------------|-----------------------------------|------------------------------|
| vg-ms-users               | vgmsusers                         | `pe.edu.vallegrande.vgmsusers` |
| vg-ms-organizations       | vgmsorganizations                 | `pe.edu.vallegrande.vgmsorganizations` |
| vg-ms-payments-billing    | vg_ms_payment                     | `pe.edu.vallegrande.vgmspayments` |
| vg-ms-infrastructure      | ms_infraestructura                | `pe.edu.vallegrande.vgmsinfrastructure` |
| vg-ms-claims-incidents   | vg_ms_claims_incidents            | `pe.edu.vallegrande.vgmsclaimsincidents` |
| vg-ms-water-quality       | ms_water_quality                  | `pe.edu.vallegrande.vgmswaterquality` |
| vg-ms-distribution        | (revisar)                         | `pe.edu.vallegrande.vgmsdistribution` |
| vg-ms-inventory-purchases | vgmsinventorypurchases            | `pe.edu.vallegrande.vgmsinventory` |
| vg-ms-authentication      | (revisar)                         | `pe.edu.vallegrande.vgmsauth` |
| vg-ms-gateway             | (revisar)                         | `pe.edu.vallegrande.vgmsgateway` |

**Regla:** El nombre del artefacto Maven puede ser `vg-ms-users`, pero el paquete Java es **siempre** `pe.edu.vallegrande.vgms*` en minúsculas, sin `_`.

---

## 2. ESTRUCTURA DE CARPETAS (TODOS LOS MS IGUALES)

Todos los microservicios deben tener **exactamente** la misma estructura bajo `src/main/java/pe/edu/vallegrande/vgms{contexto}/`:

```
vgms{contexto}/
├── {Context}Application.java
│
├── domain/
│   ├── model/                    # Entidades de dominio (POJO, sin anotaciones BD)
│   │   ├── User.java
│   │   └── vo/
│   │       └── OrganizationId.java
│   ├── enums/
│   │   └── UserStatus.java
│   ├── event/                    # Eventos de dominio (para RabbitMQ)
│   │   └── UserCreatedEvent.java
│   └── port/
│       ├── in/
│       │   └── CreateUserUseCase.java
│       └── out/
│           └── UserRepositoryPort.java
│
├── application/
│   ├── usecase/
│   │   └── impl/
│   │       └── CreateUserUseCaseImpl.java
│   ├── dto/
│   │   ├── request/
│   │   └── response/
│   └── mapper/
│       └── UserDtoMapper.java
│
└── infrastructure/
    ├── persistence/
    │   ├── entity/              # PostgreSQL: XxxEntity (o document/ para MongoDB)
    │   │   ├── UserEntity.java
    │   │   └── BaseEntity.java
    │   # O para MongoDB:
    │   ├── document/
    │   │   ├── UserDocument.java
    │   │   └── BaseDocument.java
    │   ├── repository/
    │   │   └── UserRepositoryImpl.java
    │   └── mapper/
    │       └── UserPersistenceMapper.java
    ├── rest/
    │   ├── admin/
    │   │   └── AdminUserRest.java
    │   ├── client/
    │   │   └── ClientUserRest.java
    │   ├── internal/
    │   │   └── InternalUserRest.java
    │   └── common/
    │       └── CommonUserRest.java
    ├── client/                   # Clientes HTTP a otros MS
    │   ├── internal/
    │   └── external/
    ├── messaging/                # RabbitMQ (cuando aplique)
    │   ├── config/
    │   ├── publisher/
    │   └── consumer/
    ├── config/
    ├── security/
    └── exception/
        ├── GlobalExceptionHandler.java
        └── custom/
```

### Reglas de carpetas

- **Una sola** carpeta de servicios/casos de uso: `application/usecase` (no `service` y `services` mezclados).
- **Una sola** carpeta de persistencia: `infrastructure/persistence/entity` **o** `infrastructure/persistence/document`, no ambas en el mismo MS.
- **Exception** en un solo lugar: `infrastructure/exception` (no `handlers` aparte).
- **DTOs** solo bajo `application/dto/request` y `application/dto/response`; si se dejan en infrastructure, que sea `infrastructure/dto/request` y `response` y sin mezclar con `mapper` dentro de dto.

---

## 3. NOMBRES DE TABLAS Y COLECCIONES

### Reglas

- **Minúsculas**, **snake_case**.
- **Plural** en inglés: la tabla guarda “varios” registros.
- **Sin prefijo** del microservicio en el nombre (ej. no `payments_receipts`, sí `receipts` si el schema ya es `payments`).

### Ejemplos correctos

| Entidad      | Tabla PostgreSQL   | Colección MongoDB |
|-------------|---------------------|-------------------|
| User        | users               | users             |
| Organization| organizations       | organizations     |
| Zone        | zones               | zones             |
| Receipt     | receipts            | —                 |
| Payment     | payments            | —                 |
| PaymentDetail | payment_details   | —                 |
| WaterBox    | water_boxes         | —                 |
| Complaint   | —                   | complaints        |
| Incident    | —                   | incidents         |

### Nombres a corregir en el sistema actual

| Actual (incorrecto o inconsistente) | Correcto   |
|-------------------------------------|------------|
| users_demo                           | users      |
| auth_credentials                     | user_credentials (o mantener si es solo credenciales) |

---

## 4. NOMBRES DE COLUMNAS Y CAMPOS EN BD

### Reglas

- **Siempre snake_case** en base de datos (PostgreSQL y MongoDB).
- En MongoDB, usar `@Field("snake_case")` si el campo en Java es camelCase.

### Convención de nombres de columnas

| Concepto        | Nombre en BD     | Ejemplo                    |
|-----------------|------------------|----------------------------|
| PK              | id               | id (no user_id en tabla users) |
| FK              | {entidad}_id     | organization_id, user_id, zone_id |
| Código legible  | {entidad}_code   | receipt_code, user_code, box_code |
| Fechas          | {contexto}_at / _date | created_at, updated_at, issue_date |
| Estado          | status           | status                     |
| Activo/borrado  | active           | active (boolean)           |
| Usuario auditor | created_by, updated_by | created_by, updated_by |

### Ejemplos correctos

```text
id, organization_id, user_code, email, first_name, last_name,
status, active, created_at, updated_at, created_by, updated_by
```

**Evitar:** `payment_id` como nombre de columna PK en la tabla `payments`; la PK es `id`. Reservar `*_id` para FKs.

---

## 5. ATRIBUTOS ESTÁNDAR EN TODAS LAS TABLAS/COLECCIONES

Todas las tablas/colecciones deben tener **como mínimo** estos campos cuando apliquen:

### 5.1 Auditoría (obligatoria en todas)

| Campo BD      | Tipo (PostgreSQL)   | Tipo (MongoDB) | Uso                    |
|---------------|----------------------|----------------|------------------------|
| created_at    | TIMESTAMP            | Date           | Alta del registro      |
| updated_at    | TIMESTAMP            | Date           | Última modificación    |
| created_by    | VARCHAR(255) / UUID  | String         | Usuario/Keycloak ID    |
| updated_by    | VARCHAR(255) / UUID  | String         | Usuario que modificó   |

- **Estándar de tipo de fecha en Java:** `LocalDateTime` para ambos (evitar mezclar `Instant` en un MS y `LocalDateTime` en otro).

### 5.2 Multi-tenancy (obligatorio en datos por JASS)

| Campo BD          | Tipo        | Uso                          |
|-------------------|------------|-----------------------------|
| organization_id   | VARCHAR/UUID | Identificador de la JASS (tenant) |

- Todas las tablas/colecciones que pertenezcan a una JASS deben tener `organization_id`.
- Índice compuesto: `(organization_id, ...)` en consultas frecuentes.

### 5.3 Soft delete / vigencia (recomendado)

| Campo BD | Tipo    | Uso                          |
|----------|--------|------------------------------|
| active   | BOOLEAN | true = vigente, false = dado de baja |

- Por defecto `true`. No borrar físicamente; filtrar por `active = true` en lecturas.

### 5.4 Clase base en Java (estándar)

**PostgreSQL (BaseEntity):**

```java
// infrastructure/persistence/entity/BaseEntity.java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 255)
    private String createdBy;

    @Column(name = "updated_by", length = 255)
    private String updatedBy;

    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
```

**MongoDB (BaseDocument):**

```java
// infrastructure/persistence/document/BaseDocument.java
public abstract class BaseDocument {
    @Field("created_at")
    @CreatedDate
    private LocalDateTime createdAt;

    @Field("updated_at")
    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_by")
    private String updatedBy;

    @Field("active")
    private Boolean active = true;
}
```

- Todas las entidades/documentos que representen recursos por JASS deben extender la base y tener **organization_id** en la clase concreta.

---

## 6. NOMBRES DE CLASES JAVA

### 6.1 Dominio (domain/model)

- **Singular**, **PascalCase**: `User`, `Organization`, `Receipt`, `Payment`, `WaterBox`, `Complaint`, `Incident`.
- Sin sufijos: no `UserModel`, no `UserEntity` en dominio.

### 6.2 Persistencia (infrastructure)

- **PostgreSQL:** `{Entidad}Entity` → `UserEntity`, `ReceiptEntity`, `PaymentDetailEntity`.
- **MongoDB:** `{Entidad}Document` → `UserDocument`, `OrganizationDocument`.
- **Base:** `BaseEntity` o `BaseDocument` (una sola por proyecto).

### 6.3 DTOs

- Request: ver sección 7.
- Response: ver sección 7.
- Comunes: `ApiResponse`, `ErrorMessage`, `PagedResponse` (sufijo claro).

### 6.4 REST (controladores)

- Formato: `{Rol}{Recurso}Rest` en PascalCase.
- Ejemplos: `AdminUserRest`, `ClientUserRest`, `InternalUserRest`, `CommonUserRest`.
- Para un solo recurso por controlador: `AdminPaymentRest`, `AdminReceiptRest` (no solo `AdminRest` si hay varios recursos).

### 6.5 Excepciones

- Sufijo `Exception`: `ResourceNotFoundException`, `ValidationException`, `DuplicateOrganizationException`.
- Ubicación: `infrastructure/exception/custom/`.

---

## 7. NOMBRES DE DTOs (REQUEST/RESPONSE)

### Regla única

- **Request:** `{Acción o contexto}{Recurso}Request` (sin mezclar “Dto” y “Request” en el mismo nombre).
- **Response:** `{Recurso}Response` o `{Recurso}{Detalle}Response` para variantes.

### Request (entrada)

| Uso              | Nombre                  | Ejemplo                |
|------------------|-------------------------|------------------------|
| Crear            | Create{Recurso}Request  | CreateUserRequest, CreateReceiptRequest |
| Actualizar       | Update{Recurso}Request  | UpdateUserRequest      |
| Parcial (PATCH)  | Patch{Recurso}Request   | PatchUserRequest       |
| Filtros/listado  | Filter{Recurso}Request  | FilterUserRequest      |
| Acción específica| {Acción}{Recurso}Request| PaymentCreateRequest → CreatePaymentRequest |

**Eliminar inconsistencia:** no `CreateAccountRequestDto` y `CreateUserRequest`; elegir uno: solo `Request` (recomendado) o solo `XxxDto` para DTOs, pero no mezclar.

### Response (salida)

| Uso           | Nombre                    | Ejemplo                    |
|----------------|----------------------------|----------------------------|
| Estándar       | {Recurso}Response          | UserResponse, ReceiptResponse |
| Con detalle    | {Recurso}DetailResponse    | UserDetailResponse         |
| Lista/resumen  | {Recurso}SummaryResponse   | UserSummaryResponse        |
| Lista paginada | PagedResponse&lt;{Recurso}Response&gt; | PagedResponse&lt;UserResponse&gt; |

### Comunes (todos los MS iguales)

- `ApiResponse<T>` o `ResponseDto<T>` (uno solo por proyecto).
- `ErrorMessage` para errores.
- `ValidationError` para errores de validación.
- `PagedResponse<T>` con `content`, `page`, `size`, `totalElements`, `totalPages`.

---

## 8. RUTAS REST (API)

### Reglas

- Prefijo de versión: **/api/v1/** (siempre).
- Recurso en **minúsculas**, **plural**: `/users`, `/organizations`, `/receipts`, `/payments`.
- Sin verbos en la URL: usar método HTTP (GET, POST, PUT, PATCH, DELETE).

### Estructura estándar de rutas

| Rol (en código) | Prefijo (ejemplo)   | Ejemplo de ruta completa           |
|------------------|---------------------|-------------------------------------|
| Admin            | /api/v1/admin       | /api/v1/admin/users, /api/v1/admin/receipts |
| Client           | /api/v1/client      | /api/v1/client/profile, /api/v1/client/receipts |
| Common           | /api/v1/common      | /api/v1/common/me, /api/v1/common/roles |
| Internal (MS-MS) | /internal/v1        | /internal/v1/users/by-organization/{id} |

### Ejemplos correctos

```text
GET    /api/v1/admin/users
GET    /api/v1/admin/users/{id}
POST   /api/v1/admin/users
PUT    /api/v1/admin/users/{id}
PATCH  /api/v1/admin/users/{id}
DELETE /api/v1/admin/users/{id}

GET    /api/v1/client/receipts
GET    /api/v1/common/profile/me
GET    /internal/v1/organizations/{organizationId}/users
```

### A evitar

- Rutas sin versión: `/api/users` → `/api/v1/users`.
- Mezcla de singular/plural: siempre plural para colecciones.
- Verbos en path: `/getUser` → `GET /users/{id}`.

---

## 9. NOMBRES DE REPOSITORIOS, SERVICIOS Y MAPPERS

### Repositorios

- **Interfaz (puerto):** `{Recurso}RepositoryPort` en domain/port/out, o `{Recurso}Repository` si no se usa puerto.
- **Implementación:** `{Recurso}RepositoryImpl` en infrastructure/persistence/repository.
- **Spring Data:** interfaz `{Recurso}Repository` extiende `ReactiveCrudRepository<Entity, Id>` o `JpaRepository<Entity, Id>`.

### Casos de uso / servicios de aplicación

- **Interfaz (puerto):** `CreateUserUseCase`, `GetUserByIdUseCase` (acción + recurso + UseCase).
- **Implementación:** `CreateUserUseCaseImpl`, `GetUserByIdUseCaseImpl` en application/usecase/impl.

### Mappers

- **Dominio ↔ persistencia:** `{Recurso}PersistenceMapper` o `{Recurso}Mapper` (Domain ↔ Entity/Document).
- **Dominio ↔ DTO:** `{Recurso}DtoMapper` en application/mapper (Domain ↔ Request/Response).
- Una sola convención: o todo “Mapper” o todo “PersistenceMapper” + “DtoMapper”.

---

## 10. ENUMS Y CONSTANTES

### Enums

- **PascalCase**, **singular** para el tipo: `UserStatus`, `PaymentStatus`, `IncidentSeverity`.
- Valores del enum: **UPPER_SNAKE_CASE** en Java (se persisten como string en BD): `ACTIVE`, `PENDING`, `PAID`, `OVERDUE`.

```java
public enum UserStatus {
    PENDING, ACTIVE, SUSPENDED, INACTIVE
}

public enum PaymentStatus {
    PENDING, PAID, OVERDUE, CANCELLED
}
```

### Constantes

- Clase: `Constants` o `{Context}Constants` (ej. `PaymentConstants`).
- Campos: `UPPER_SNAKE_CASE`: `MAX_PAGE_SIZE`, `DEFAULT_PAGE_SIZE`.

---

## 11. CHECKLIST DE REFACTORIZACIÓN

Usar este checklist **por cada microservicio** al refactorizar.

### Paquetes y estructura

- [ ] Paquete base `pe.edu.vallegrande.vgms*` en minúsculas, sin `_`.
- [ ] Carpetas: `domain/model`, `domain/enums`, `application/usecase`, `application/dto`, `infrastructure/persistence/entity` o `document`, `infrastructure/rest`, `infrastructure/exception`.
- [ ] No mezclar `service`/`services` ni `entity`/`entities`; elegir uno y mantenerlo.

### Base de datos

- [ ] Tablas/colecciones en **snake_case**, **plural** (users, receipts, payment_details).
- [ ] Columnas/campos en BD en **snake_case** (organization_id, created_at, updated_at).
- [ ] Todas las tablas con **created_at**, **updated_at**, y si aplica **created_by**, **updated_by**, **active**.
- [ ] Tablas por JASS con **organization_id** e índices adecuados.
- [ ] PK se llama **id**; FKs **{entidad}_id**.

### Dominio

- [ ] Clases de dominio **sin** anotaciones de BD (@Table, @Document, @Column, @Id).
- [ ] Nombres en **singular** (User, Receipt, Payment).

### Persistencia (infrastructure)

- [ ] Entidades: `{Recurso}Entity` o `{Recurso}Document`; base `BaseEntity`/`BaseDocument`.
- [ ] Base con **createdAt**, **updatedAt**, **createdBy**, **updatedBy**, **active** (y en BD snake_case).

### DTOs y REST

- [ ] Request: `CreateXxxRequest`, `UpdateXxxRequest`, `FilterXxxRequest`.
- [ ] Response: `XxxResponse`, `XxxDetailResponse`, `PagedResponse<T>`.
- [ ] Rutas con prefijo **/api/v1/** (o /internal/v1/) y recurso en **plural**.

### Nombres de clases

- [ ] Controladores: `AdminUserRest`, `ClientReceiptRest`, etc.
- [ ] Excepciones: `XxxException` en `infrastructure/exception/custom/`.
- [ ] Un solo `GlobalExceptionHandler` en `infrastructure/exception`.

---

## 📎 RESUMEN DE CAMBIOS TÍPICOS POR MS

| Microservicio | Cambios principales |
|---------------|---------------------|
| vg-ms-users | Paquete ya ok; users_demo → users; AuthCredential a dominio puro + AuthCredentialDocument; DTOs Request/Response unificados; rutas /api/v1/*. |
| vg-ms-organizations | application/services → usecase; dto/mapper dentro de dto → application/mapper; BaseDocument con LocalDateTime y created_by, updated_by. |
| vg-ms-payments-billing | vg_ms_payment → vgmspayments; Receipts → Receipt (singular); domain sin @Table; BaseEntity con created_by, updated_by, active; rutas /api/v1/admin/receipts, /api/v1/admin/payments. |
| vg-ms-infrastructure | ms_infraestructura → vgmsinfrastructure; persistence/entity con BaseEntity estándar; WaterBoxEntity con updatedAt. |
| vg-ms-claims-incidents | vg_ms_claims_incidents → vgmsclaimsincidents; DTO → Request/Response; handlers → exception; document/embedded si aplica. |
| vg-ms-water-quality | ms_water_quality → vgmswaterquality; User en dominio puro + UserDocument en infrastructure. |
| vg-ms-distribution | Revisar paquete; domain sin @Document; BaseDocument estándar. |
| vg-ms-inventory-purchases | vgmsinventorypurchases → vgmsinventory; entities → entity; BaseEntity con auditoría completa. |

---

**Documento:** Estándar de nombres, estructura y atributos v1.0  
**Fecha:** 30 Enero 2026  
**Uso:** Referencia obligatoria al refactorizar o crear nuevos microservicios.
