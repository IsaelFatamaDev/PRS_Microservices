# MEJORAS BACKEND — REVISIÓN COMPLETA DEL CÓDIGO

**Origen:** Revisión real de todos los archivos de backend (Java y Node.js) y configuración en el repositorio.  
**Fecha:** 30 Enero 2026  
**Alcance:** 11 microservicios — vg-ms-users, vg-ms-organizations, vg-ms-payments-billing, vg-ms-infrastructure, vg-ms-claims-incidents, vg-ms-water-quality, vg-ms-distribution, vg-ms-inventory-purchases, vg-ms-authentication, vg-ms-gateway, **vg-ms-notifications** (Node.js).

---

## ÍNDICE

1. [Resumen por microservicio](#1-resumen-por-microservicio)
2. [Paquetes Java](#2-paquetes-java)
3. [Estructura de carpetas](#3-estructura-de-carpetas)
4. [Dominio vs persistencia (hexagonal)](#4-dominio-vs-persistencia-hexagonal)
5. [Tablas y colecciones](#5-tablas-y-colecciones)
6. [Columnas y atributos estándar](#6-columnas-y-atributos-estándar)
7. [Rutas REST](#7-rutas-rest)
8. [DTOs (request/response)](#8-dtos-requestresponse)
9. [Seguridad y credenciales](#9-seguridad-y-credenciales)
10. [Excepciones y handlers](#10-excepciones-y-handlers)
11. [Nombres de clases e interfaces](#11-nombres-de-clases-e-interfaces)
12. [Plan de cambios por MS (Java)](#12-plan-de-cambios-por-ms-java)
13. [vg-ms-notifications (Node.js) — análisis y mejoras](#13-vg-ms-notifications-nodejs--análisis-y-mejoras)

---

## 1. RESUMEN POR MICROSERVICIO

| MS | Paquete actual | Seguridad | Dominio puro | Rutas /api/v1 | Base audit | Observaciones |
|----|----------------|-----------|--------------|----------------|------------|---------------|
| **vg-ms-users** | vgmsusers | Sí OAuth2 | No (AuthCredential @Document) | No | N/A (document) | users_demo, DTO mezcla Request/Dto |
| **vg-ms-organizations** | vgmsorganizations | Sí OAuth2 | Sí | No | BaseDocument Instant, sin created_by | application/services, dto/mapper dentro de dto |
| **vg-ms-payments-billing** | **vg_ms_payment** | **No SecurityConfig** | No (Receipts @Table en domain) | No | BaseEntity sin created_by/updated_by | Receipts en domain, PaymentDRequest typo |
| **vg-ms-infrastructure** | **ms_infraestructura** | **permitAll, OAuth2 comentado** | Sí | No | Sin BaseEntity, solo createdAt | config/ en raíz, exceptions/ (plural), IWaterBoxService |
| **vg-ms-claims-incidents** | **vg_ms_claims_incidents** | **No SecurityConfig** | Sí | No | record_status, sin active | GlobalExceptionHandler en **handlers/** |
| **vg-ms-water-quality** | **ms_water_quality** | Sí OAuth2 | No (User @Document en domain) | No | N/A | application/config + application/services |
| **vg-ms-distribution** | **msdistribution** | Sí OAuth2 | No (Route, Schedule @Document en domain) | No | BaseDocument con createdBy | AdminRest @RequestMapping("/internal"), colecciones route/schedule/program |
| **vg-ms-inventory-purchases** | vgmsinventorypurchases | Sí OAuth2 | Sí | No | N/A (entities) | **entities/** (plural), MaterialEntity @Table("products") |
| **vg-ms-authentication** | vgmsauthentication | Sí OAuth2 | N/A | No | N/A | admin-password: admin en YAML |
| **vg-ms-gateway** | vgmsgateway | Sí OAuth2 | N/A | N/A | N/A | OK |
| **vg-ms-notifications** | Node.js (sin paquete Java) | **Sin seguridad** | N/A | No | N/A | Solo WhatsApp, **sin SMS**, **sin Keycloak**, credenciales en texto plano por WhatsApp |

---

## 2. PAQUETES JAVA

**Regla deseada:** `pe.edu.vallegrande.vgms{contexto}` — todo en minúsculas, **sin guiones bajos**.

| Microservicio | Paquete actual | Acción |
|---------------|----------------|--------|
| vg-ms-payments-billing | `pe.edu.vallegrande.vg_ms_payment` | Cambiar a `pe.edu.vallegrande.vgmspayments` |
| vg-ms-infrastructure | `pe.edu.vallegrande.ms_infraestructura` | Cambiar a `pe.edu.vallegrande.vgmsinfrastructure` |
| vg-ms-claims-incidents | `pe.edu.vallegrande.vg_ms_claims_incidents` | Cambiar a `pe.edu.vallegrande.vgmsclaimsincidents` |
| vg-ms-water-quality | `pe.edu.vallegrande.ms_water_quality` | Cambiar a `pe.edu.vallegrande.vgmswaterquality` |
| vg-ms-distribution | `pe.edu.vallegrande.msdistribution` | Cambiar a `pe.edu.vallegrande.vgmsdistribution` |
| vg-ms-users | vgmsusers | OK |
| vg-ms-organizations | vgmsorganizations | OK |
| vg-ms-inventory-purchases | vgmsinventorypurchases | OK (opcional acortar a vgmsinventory) |
| vg-ms-authentication | vgmsauthentication | OK |
| vg-ms-gateway | vgmsgateway | OK |

**Impacto:** Cambiar `package` en todos los `.java` y mover carpetas bajo el nuevo paquete. Ajustar `pom.xml` si hay referencias al artefacto/groupId.

---

## 3. ESTRUCTURA DE CARPETAS

**Inconsistencias detectadas:**

| Tema | Qué hay ahora | Estandarizar a |
|------|----------------|-----------------|
| Servicios aplicación | users: `application/service/`; orgs, payments, distribution, claims, inventory, water-quality: `application/services/` | Una sola: `application/services/` (o `application/usecase/`) en todos |
| Persistencia | users, orgs, claims, distribution, water-quality: `infrastructure/document/`; payments: `infrastructure/entity/`; inventory: `infrastructure/entities/`; infrastructure: `infrastructure/persistence/entity/` | PostgreSQL: `infrastructure/persistence/entity/` (singular). MongoDB: `infrastructure/persistence/document/` (singular). Eliminar `entities/` |
| Config seguridad | infrastructure: `config/SecurityConfig` en raíz del módulo; resto: `infrastructure/security/` o `application/config/` | Un solo criterio: `infrastructure/config/SecurityConfig` o `infrastructure/security/SecurityConfig` en todos |
| GlobalExceptionHandler | claims: `infrastructure/handlers/GlobalExceptionHandler`; infrastructure: `infrastructure/exceptions/GlobalExceptionHandler` | Siempre `infrastructure/exception/GlobalExceptionHandler` (singular “exception”) |
| Excepciones | infrastructure: carpeta `exceptions/` (plural) | `exception/` (singular) como en el resto |

**Resumen por MS:**

- **vg-ms-infrastructure:** `config/SecurityConfig` está en raíz (fuera de `infrastructure/`). `infrastructure/exceptions/` → renombrar a `exception/`. No hay `application/` con servicios; están en `application/services/` con interfaces `IWaterBoxService` (prefijo I).
- **vg-ms-claims-incidents:** Mover `handlers/GlobalExceptionHandler` a `exception/GlobalExceptionHandler`.
- **vg-ms-inventory-purchases:** Renombrar carpeta `entities/` a `entity/` (o mover bajo `persistence/entity/` si se unifica con otros MS).
- **vg-ms-water-quality:** Tiene tanto `application/config/SecurityConfig` como `infrastructure/security/JwtService`; unificar criterio (p. ej. seguridad solo en `infrastructure/security/`).

---

## 4. DOMINIO VS PERSISTENCIA (HEXAGONAL)

**Regla:** En `domain/` no debe haber anotaciones de BD (`@Document`, `@Table`, `@Entity`, `@Column`, `@Id`, etc.). La persistencia vive en `infrastructure/persistence/entity/` o `document/`.

**Violaciones encontradas (archivo y anotación):**

| Microservicio | Archivo en domain | Problema |
|---------------|-------------------|----------|
| vg-ms-users | `domain/models/AuthCredential.java` | `@Document(collection = "auth_credentials")` |
| vg-ms-water-quality | `domain/models/User.java` | `@Document(collection = "users")` |
| vg-ms-payments-billing | `domain/models/Receipts.java` | `@Table("receipts")` + `@Column` + `@Id` |
| vg-ms-distribution | `domain/models/DistributionRoute.java` | `@Document(collection = "route")` |
| vg-ms-distribution | `domain/models/DistributionSchedule.java` | `@Document(collection = "schedule")` |

**Acciones:**

1. **vg-ms-users:** Crear `AuthCredential` sin anotaciones en `domain/models/`. Crear `AuthCredentialDocument` en `infrastructure/document/` con `@Document(collection = "auth_credentials")`. Mapper Domain ↔ Document. Repositorio y servicios usan Document y mapper.
2. **vg-ms-water-quality:** Igual: `User` puro en domain, `UserDocument` en infrastructure con `@Document(collection = "users")`, mapper y repositorio sobre Document.
3. **vg-ms-payments-billing:** `Receipts` en domain debe ser POJO sin anotaciones. Crear `ReceiptEntity` en `infrastructure/entity/` con `@Table("receipts")` y mapeo de columnas. Repositorio y servicios usan Entity y mapper. Renombrar clase de dominio a `Receipt` (singular).
4. **vg-ms-distribution:** `DistributionRoute` y `DistributionSchedule` en domain sin `@Document`. Crear `DistributionRouteDocument` y `DistributionScheduleDocument` en `infrastructure/document/` con colecciones `distribution_routes` y `distribution_schedules` (ver sección 5). Mappers y repositorios sobre los Document.

---

## 5. TABLAS Y COLECCIONES

**Reglas:** Minúsculas, snake_case, **plural**. Evitar nombres demasiado genéricos cuando hay varios contextos (p. ej. prefijo por bounded context).

**Cambios detectados:**

| MS | Actual | Debe ser |
|----|--------|----------|
| vg-ms-users | `users_demo` (UserDocument) | `users` |
| vg-ms-distribution | `route` (DistributionRoute) | `distribution_routes` |
| vg-ms-distribution | `schedule` (DistributionSchedule) | `distribution_schedules` |
| vg-ms-distribution | `program` (DistributionProgramDocument) | `distribution_programs` |
| vg-ms-inventory-purchases | MaterialEntity → `products` | Mantener `products` si el negocio lo llama “producto”; si el agregado es “Material”, valorar tabla `materials` y migración. Documentar decisión. |

**Resto:** organizations (organizations, zones, streets, parameters, fares), payments (payments, payment_details, receipts), infrastructure (water_boxes, water_box_assignments, water_box_transfers), claims (complaints, incident_types, etc.), water-quality (testing_points, quality_tests, daily_records, users) están en snake_case; revisar solo plural y prefijos si se quiere homogeneizar (p. ej. `quality_tests` ya está bien).

---

## 6. COLUMNAS Y ATRIBUTOS ESTÁNDAR

**6.1 PK y nombres de columna**

- **Receipts (vg-ms-payments-billing):** En dominio/entidad se usa `receipts_id` como PK. En tabla `receipts` la PK debe llamarse `id`. Cambiar a `id` en entidad y en BD si aplica.
- **Tipos:** `created_at` / `updated_at` deben ser tipo fecha/hora (LocalDateTime o Instant), no String. En `Receipts` actual: `createdAt` como String → cambiar a LocalDateTime (o Instant) y columna `created_at`.

**6.2 Auditoría en bases**

En todos los MS con persistencia, unificar:

- `created_at`, `updated_at` (timestamp)
- `created_by`, `updated_by` (opcional pero recomendado)
- `active` (boolean, soft delete) donde aplique

**Estado por MS:**

- **vg-ms-organizations (BaseDocument):** Solo `createdAt`, `updatedAt` (Instant). Añadir `created_by`, `updated_by` y, si aplica, `active`. Unificar tipo de fecha con el resto (p. ej. LocalDateTime).
- **vg-ms-payments-billing (BaseEntity):** Solo `created_at`, `updated_at`. Añadir `created_by`, `updated_by`, `active`.
- **vg-ms-distribution (BaseDocument):** Tiene `createdBy`; revisar que todos los documentos que extienden BaseDocument tengan también `updated_by` y `active` si aplica.
- **vg-ms-claims-incidents (BaseDocument):** Usa `record_status` en lugar de `active`; valorar unificar a `active` (boolean) o dejar `record_status` y documentarlo como estándar de este MS.
- **vg-ms-infrastructure:** No hay BaseEntity. WaterBoxEntity, WaterBoxAssignmentEntity, WaterBoxTransferEntity tienen `created_at` pero no `updated_at` ni `created_by`/`updated_by`. Añadir BaseEntity con auditoría completa y que las entidades la extiendan; añadir `updated_at` (y opcionalmente created_by/updated_by) en tablas.

---

## 7. RUTAS REST

**Regla:** Prefijo de versión `/api/v1/` en todos los endpoints públicos (y `/internal/v1/` para MS-to-MS si se quiere versionado interno).

**Estado actual:** Ningún MS usa `/api/v1/`. Todos usan `/api/admin`, `/api/client`, `/api/common`, `/api/management`, `/api/internal`, etc., sin versión.

**Cambios:**

- Añadir prefijo `v1` en todas las rutas de los controladores (o en el Gateway si se enruta por prefijo). Ejemplos:
  - `/api/admin` → `/api/v1/admin`
  - `/api/client` → `/api/v1/client`
  - `/api/common` → `/api/v1/common`
  - `/api/management` → `/api/v1/management`
  - `/api/internal` → `/internal/v1` (o mantener `/internal` si el Gateway no versiona interno)
- **vg-ms-distribution:** `AdminRest` tiene `@RequestMapping("/internal")`. Si es un controlador de administración, debe ser algo como `/api/v1/admin` (o el path que corresponda al recurso de distribución). Corregir a `/api/v1/admin` (o el path acordado) y dejar `/internal` solo para endpoints realmente internos MS-to-MS.

**Resumen de rutas actuales por MS (para refactor):**

- users: /api/management, /internal, /api/users, /api/common/profile, /api/common, /api/client, /api/admin
- organizations: /api/management, /api/internal, /api/admin
- payments: /api/client, /api/admin
- inventory: /api/admin/suppliers, /api/admin/purchases, /api/admin/product-categories, /api/admin/materials, /api/admin/inventory-movements
- infrastructure: /api/management, /internal, /api/client, /api/admin
- distribution: /internal (AdminRest)
- claims: /api/internal, /api/client, /api/admin
- water-quality: /api/admin/quality
- authentication: /api/auth

---

## 8. DTOs (REQUEST/RESPONSE)

**Reglas:** Misma convención en todos: o bien `XxxRequest`/`XxxResponse`, o bien `XxxDto`; no mezclar sufijos `Request`/`Dto` en el mismo tipo. Preferible: Request/Response para API REST.

**Inconsistencias:**

- **vg-ms-users:** `CreateAccountRequestDto` (Dto) vs `CreateUserRequest`, `UpdateUserRequest` (Request). Unificar a `CreateAccountRequest` (o mantener nombre pero quitar “Dto”). Resto de responses ya usan “Response”.
- **vg-ms-payments-billing:** `PaymentDRequest`, `PaymentDResponse` — parece typo de “Detail”. Renombrar a `PaymentDetailRequest` y `PaymentDetailResponse` (o equivalente según uso).
- **vg-ms-claims-incidents:** Uso de sufijo `DTO`: `ComplaintCategoryDTO`, `IncidentCreateDTO`, etc. Unificar a Request/Response donde sean entrada/salida de API (p. ej. `IncidentCreateRequest`, `ComplaintCategoryResponse`). Los DTO internos pueden seguir como XxxDTO si se desea, pero documentar.

**Ubicación:** En la mayoría están en `infrastructure/dto/request` y `response`. En organizations hay `dto/mapper` dentro de dto; mover mappers a `infrastructure/mapper` (o `application/mapper`) para no mezclar DTOs con mappers.

---

## 9. SEGURIDAD Y CREDENCIALES

**9.1 SecurityConfig**

- **vg-ms-payments-billing:** No existe `SecurityConfig`. Añadir SecurityConfig con OAuth2 Resource Server (JWT) y autorización por rutas (admin/client/internal) igual que en otros MS.
- **vg-ms-claims-incidents:** No existe `SecurityConfig`. Igual: añadir SecurityConfig con OAuth2 y rutas protegidas.
- **vg-ms-infrastructure:** SecurityConfig existe pero tiene `anyRequest().permitAll()` y OAuth2 comentado. Activar OAuth2 Resource Server y restringir rutas (p. ej. actuator público, resto autenticado/por rol).

**9.2 Credenciales en YAML**

No debe haber contraseñas, tokens ni URIs con usuario/contraseña por defecto en el repositorio. Usar variables de entorno (y en producción un vault).

**Archivos con valores sensibles detectados:**

- **vg-ms-users (application.yml / application-prod.yml):**  
  - `SPRING_DATA_MONGODB_URL` por defecto con URI que incluye usuario y contraseña.  
  - `EXTERNAL_DIACOLECTA_RENIEC_TOKEN` con valor por defecto.  
  → Quitar valores por defecto; exigir variables de entorno en prod.
- **vg-ms-payments-billing (application.yml):**  
  - `SPRING_R2DBC_PASSWORD:npg_FvwbUB26GcHE`.  
  → Quitar default; usar solo `${SPRING_R2DBC_PASSWORD}`.
- **vg-ms-infrastructure (application.yml, application-dev.yml):**  
  - `DB_PASSWORD:npg_U7Lo1WpkAvmi`, `KEYCLOAK_ADMIN_PASSWORD:admin`.  
  → Quitar defaults sensibles.
- **vg-ms-inventory-purchases (application-dev.yml):**  
  - `password: SistemaJass12`, URLs de Keycloak fijas.  
  → Valores solo por variable de entorno o profile dev sin commitear secretos.
- **vg-ms-claims-incidents (application-dev.yml):**  
  - MongoDB URI con usuario y contraseña en la URL.  
  → No dejar credenciales en el repo; usar variables.
- **vg-ms-authentication (application.yml):**  
  - `keycloak.admin-password: admin`.  
  → Quitar valor por defecto; usar variable de entorno.

En todos: en `application-dev.yml` usar solo valores seguros (ej. “password” para BD local) o documentar que no se usan en producción; en `application.yml` y `application-prod.yml` no poner secretos.

---

## 10. EXCEPCIONES Y HANDLERS

- **vg-ms-claims-incidents:** `GlobalExceptionHandler` está en `infrastructure/handlers/`. Mover a `infrastructure/exception/GlobalExceptionHandler` para alinear con el resto de MS.
- **vg-ms-infrastructure:** Carpeta `infrastructure/exceptions/` (plural) y ahí está `GlobalExceptionHandler`. Renombrar carpeta a `exception/` (singular).
- **Nombres de excepciones:** En claims hay `DatosInvalidosException`, `ErrorServidorException`, `RecursoNoEncontradoException`. Para homogeneizar con otros MS se puede usar `ValidationException`, `ResourceNotFoundException`; si se mantienen nombres en español, documentar en el estándar.

---

## 11. NOMBRES DE CLASES E INTERFACES

- **Receipts → Receipt:** En vg-ms-payments-billing el modelo de dominio se llama `Receipts` (plural). En dominio y API usar singular: `Receipt` (y `ReceiptEntity` en persistencia).
- **PaymentDRequest / PaymentDResponse:** Renombrar a algo como `PaymentDetailRequest` y `PaymentDetailResponse` (o el nombre que refleje su uso).
- **Material vs products:** En inventory, la entidad es `MaterialEntity` pero la tabla es `products`. Decidir: si el dominio es “Material”, considerar tabla `materials`; si es “Product”, renombrar entidad/clase a `Product`/`ProductEntity`. Dejar documentado.
- **Interfaces con prefijo I:** En vg-ms-infrastructure están `IWaterBoxService`, `IWaterBoxAssignmentService`, `IWaterBoxTransferService`. En Java no es convención usar “I”; unificar a `WaterBoxService` (interfaz) e `impl.WaterBoxServiceImpl` (implementación), como en el resto de MS.

---

## 12. Plan de cambios por MS (Java)

**vg-ms-users**  
- Quitar `@Document` de `AuthCredential`; crear `AuthCredentialDocument` en infrastructure y mapper.  
- Colección `users_demo` → `users`.  
- Unificar DTOs: `CreateAccountRequestDto` → `CreateAccountRequest` (o nombre unificado).  
- Añadir prefijo `/api/v1/` a rutas.  
- Credenciales y MongoDB/RENIEC solo por variables de entorno.

**vg-ms-organizations**  
- BaseDocument: añadir `created_by`, `updated_by`, `active`; unificar tipo de fecha (LocalDateTime).  
- Mover `dto/mapper` fuera de dto (p. ej. a `infrastructure/mapper`).  
- Rutas con `/api/v1/`.

**vg-ms-payments-billing**  
- Paquete: `vg_ms_payment` → `vgmspayments`.  
- Añadir SecurityConfig con OAuth2.  
- Dominio: `Receipts` sin anotaciones → modelo `Receipt`; crear `ReceiptEntity` en infrastructure con `@Table("receipts")` y mapper.  
- BaseEntity: añadir `created_by`, `updated_by`, `active`.  
- PK de receipts: usar `id`; `created_at` como LocalDateTime.  
- Renombrar `PaymentDRequest`/`PaymentDResponse` a `PaymentDetailRequest`/`PaymentDetailResponse`.  
- Quitar password por defecto en YAML.  
- Rutas con `/api/v1/`.

**vg-ms-infrastructure**  
- Paquete: `ms_infraestructura` → `vgmsinfrastructure`.  
- Activar OAuth2 en SecurityConfig; quitar `permitAll`.  
- Mover `config/SecurityConfig` a `infrastructure/config/` o `infrastructure/security/`.  
- Crear BaseEntity con `created_at`, `updated_at`, `created_by`, `updated_by`, `active`; hacer que las entidades de water_boxes la extiendan y añadir columna `updated_at` (y las demás) en BD.  
- Renombrar `exceptions/` a `exception/`.  
- Interfaces: `IWaterBoxService` → `WaterBoxService`.  
- Credenciales sin valores por defecto en repo.  
- Rutas con `/api/v1/`.

**vg-ms-claims-incidents**  
- Paquete: `vg_ms_claims_incidents` → `vgmsclaimsincidents`.  
- Añadir SecurityConfig con OAuth2.  
- Mover `handlers/GlobalExceptionHandler` a `exception/GlobalExceptionHandler`.  
- Unificar DTOs a Request/Response donde sea entrada/salida de API.  
- Rutas con `/api/v1/`.  
- Credenciales MongoDB sin valor en repo.

**vg-ms-water-quality**  
- Paquete: `ms_water_quality` → `vgmswaterquality`.  
- Quitar `@Document` de `domain/models/User.java`; crear `UserDocument` en infrastructure y mapper.  
- Unificar ubicación de SecurityConfig (p. ej. todo en `infrastructure/security/`).  
- Rutas con `/api/v1/`.

**vg-ms-distribution**  
- Paquete: `msdistribution` → `vgmsdistribution`.  
- Dominio: quitar `@Document` de `DistributionRoute` y `DistributionSchedule`; crear `DistributionRouteDocument` y `DistributionScheduleDocument` con colecciones `distribution_routes` y `distribution_schedules`.  
- Colección `program` → `distribution_programs` en `DistributionProgramDocument`.  
- AdminRest: cambiar `@RequestMapping("/internal")` a `/api/v1/admin` (o path acordado para admin de distribución).  
- Rutas con `/api/v1/`.

**vg-ms-inventory-purchases**  
- Renombrar carpeta `entities/` a `entity/` (o usar `persistence/entity/`).  
- Decidir y documentar: Material vs Product (tabla `products` vs `materials` y nombre de entidad).  
- Rutas con `/api/v1/`.  
- application-dev: no commitear contraseña real; usar variable.

**vg-ms-authentication**  
- Quitar `admin-password: admin` de YAML; usar variable de entorno.  
- Rutas con `/api/v1/` si expone endpoints propios.

**vg-ms-gateway**  
- Sin cambios estructurales mayores; si se añade versionado, enrutar `/api/v1/*` a los MS correspondientes.

**vg-ms-notifications**  
- Ver sección 13 (análisis completo del MS Node.js).

---

## 13. vg-ms-notifications (Node.js) — análisis y mejoras

**Revisión realizada sobre:** `src/` (index.js, config/, controllers/, routes/, services/, utils/), package.json, .env.example, Dockerfile, docker-compose.yml. No se ha usado el README como base.

### 13.1 Estado actual (código real)

| Aspecto | Estado |
|--------|--------|
| **Stack** | Node.js 18, Express 5, pnpm |
| **Canal de envío** | Solo **WhatsApp** (whatsapp-web.js con LocalAuth + Puppeteer/Chromium). **No hay proveedor SMS.** |
| **Seguridad** | **Ninguna.** No hay middleware de validación JWT/Keycloak. Todos los endpoints son públicos. |
| **Rutas** | `/api/qr`, `/api/status`, `/api/send`, `/api/send-media`, `/api/welcome`, `/api/logout`; `/api/jass/payment-receipt`, `/api/jass/credentials`, `/api/jass/payment-reminder`. Sin prefijo `/api/v1/`. |
| **Credenciales de usuario** | Se envían **en texto plano** por WhatsApp (welcome: username + password; jass/credentials: usuario + contraseña). Riesgo de seguridad y no recomendable para usuarios rurales. |
| **Estructura** | config (solo port + puppeteer + whatsapp webVersionPath), controllers (notificationController), routes (notificationRoutes, jassRoutes), services (solo whatsappClient), utils (logger). Sin capa de dominio ni validación centralizada de errores. |
| **Validación** | express-validator solo en algunos POST (send, welcome); jassRoutes sin validator, validación manual con if. |
| **Logging** | Winston en controller y whatsappClient; jassRoutes usa `console.error`. |
| **Health** | Dockerfile/docker-compose usan `/api/status` como healthcheck; no hay endpoint dedicado `/health`. |
| **Integración** | vg-ms-users llama a `notificationUrl + "/welcome"` (envía número, username, password, clientName). Gateway enruta `/notifications/**` al MS. |

### 13.2 Contexto: usuarios rurales y seguridad con Keycloak

- **Usuarios rurales:** En muchas zonas hay mayor penetración de **SMS** que de datos; WhatsApp depende de internet. El sistema debe contemplar **SMS** como canal principal o fallback para recordatorios de pago, avisos de corte, credenciales de acceso, etc.
- **Seguridad:** El estándar del sistema es **Keycloak**. Cualquier microservicio que exponga APIs debe validar el JWT (o token interno) que llega desde el Gateway. vg-ms-notifications hoy no valida nada: cualquier cliente que conozca la URL puede enviar mensajes o leer estado/QR.
- **Credenciales:** No se deben enviar contraseñas en claro por WhatsApp ni por SMS. Alternativas: enlace de activación/cambio de contraseña, o mensaje tipo “Accede a [url] para configurar tu acceso” sin incluir la contraseña.

### 13.3 Mejoras obligatorias

**Seguridad (Keycloak)**  
- Añadir middleware de validación JWT (Keycloak): verificar `Authorization: Bearer <token>`, validar firma/issuer con la configuración del realm (JWKS o issuer).  
- Endpoints que solo deban ser usados por otros MS (ej. llamado desde users al crear usuario): validar token de servicio o header interno acordado (p. ej. `X-Internal-Service`, JWE).  
- Dejar públicos solo: `/health`, y si aplica `/api/v1/notifications/status` o `/api/v1/notifications/qr` para admin autenticado (no anónimo).  
- Rutas `/api/jass/*` y `/api/send`, `/api/welcome`, etc. deben requerir autenticación (rol ADMIN o servicio interno).

**SMS para usuarios rurales**  
- Integrar un **proveedor de SMS** (Twilio, AWS SNS, otro local en Perú) para: recordatorios de pago, avisos de corte, códigos de verificación, mensajes cuando WhatsApp no esté disponible.  
- Definir estrategia de canal: preferencia por SMS en zonas rurales o fallback a SMS si WhatsApp falla; mensajes cortos y claros (evitar jerga técnica).  
- Configuración por entorno: API key/secret del proveedor SMS en variables de entorno, no en código.

**Credenciales de acceso**  
- Dejar de enviar contraseña en texto plano en `/api/welcome` y `/api/jass/credentials`.  
- En su lugar: enviar enlace de activación (token de un solo uso generado por auth/users) o mensaje “Ingresa a [URL] para activar tu cuenta y definir tu contraseña”.  
- Coordinar con vg-ms-users y vg-ms-authentication: flujo de “usuario creado” → notificación con enlace, no con password.

**Estructura y estándares**  
- Rutas: prefijo `/api/v1/` (ej. `/api/v1/notifications/send`, `/api/v1/notifications/whatsapp/status`, `/api/v1/notifications/sms/send`).  
- Health: endpoint GET `/health` o `/api/v1/health` que devuelva estado del servicio y, si aplica, estado de WhatsApp (y SMS) para el healthcheck del Gateway/Docker.  
- Estructura de carpetas sugerida (Node.js): `src/config/`, `src/middleware/` (auth, errorHandler, validate), `src/controllers/`, `src/routes/`, `src/services/` (whatsappService, smsService, notificationOrchestrator), `src/utils/`, `src/errors/` para errores tipados.  
- Validación: express-validator (o similar) en todas las rutas que reciban body; respuestas de error unificadas (código, mensaje, detalles).  
- Logging: usar siempre el logger (Winston); eliminar `console.error`/`console.log` en producción.  
- Variables de entorno: puerto, Keycloak (issuer, jwksUri o audience), URLs de Gateway/MS, credenciales de WhatsApp (si aplica), credenciales del proveedor SMS; documentar en `.env.example` sin valores reales.

**Integración con el resto del sistema**  
- Gateway ya enruta `/notifications/**` al MS; asegurar que las rutas del MS coincidan con lo que el Gateway reescribe (ej. `/notifications` → base del MS).  
- vg-ms-users: actualizar NotificationClient para llamar a la nueva ruta versionada y, cuando se implemente, enviar solo datos necesarios para “enlace de activación” en lugar de password.  
- Consumo desde otros MS: solo con JWT propagado o token interno; no exponer endpoints sensibles sin autenticación.

### 13.4 Plan de cambios concretos (vg-ms-notifications)

1. **Seguridad**  
   - Implementar middleware de validación JWT (Keycloak) y aplicarlo a todas las rutas excepto `/health` (y las que se decida dejar públicas con rol).  
   - Proteger endpoints internos (llamados desde users, payments, etc.) con token de servicio o header validado.

2. **SMS**  
   - Elegir proveedor (Twilio u otro).  
   - Añadir `src/services/smsService.js` (o equivalente) que envíe SMS.  
   - Exponer endpoint `/api/v1/notifications/sms/send` (o similar) con validación y uso del servicio.  
   - Definir en documentación o config cuándo usar SMS vs WhatsApp (rural, fallback, etc.).

3. **Credenciales**  
   - Dejar de enviar password en `/api/welcome` y `/api/jass/credentials`.  
   - Coordinar con auth/users para envío de enlace de activación o mensaje sin contraseña.

4. **Rutas y API**  
   - Añadir prefijo `/api/v1/` a todas las rutas.  
   - Añadir GET `/health` (o `/api/v1/health`) y usarlo en Docker/Gateway.  
   - Unificar nombres de rutas (ej. `/api/v1/notifications/whatsapp/send`, `/api/v1/notifications/jass/payment-receipt`) y documentar.

5. **Estructura**  
   - Introducir `middleware/auth.js` (JWT), `middleware/errorHandler.js`, `middleware/validate.js` (validator).  
   - Separar servicios: whatsappService, smsService; orquestador si hace falta elegir canal.  
   - Centralizar errores y respuestas en formato estándar.

6. **Config y env**  
   - Añadir config de Keycloak (issuer, jwksUri, audience).  
   - Añadir config del proveedor SMS.  
   - Actualizar `.env.example` con todas las variables necesarias (sin valores sensibles).

7. **Logging y observabilidad**  
   - Reemplazar todos los `console.*` por el logger.  
   - Incluir en logs: tipo de notificación (whatsapp/sms), destino (sin registrar número completo si hay política de privacidad), resultado (éxito/fallo), sin registrar contenido sensible.

8. **Tests**  
   - Añadir pruebas (Jest o similar) para servicios de envío (mock del proveedor) y para middleware de auth (token válido/inválido).

Con esto, el documento cubre los **11 microservicios** e incluye el análisis completo de vg-ms-notifications a partir del código, con foco en seguridad Keycloak, SMS para zonas rurales y mejora global del servicio de notificaciones.

---

**Documento generado a partir de la revisión del código en el repositorio.**  
**Versión:** 1.1 — 30 Enero 2026 (incluye vg-ms-notifications como 11.º microservicio).
