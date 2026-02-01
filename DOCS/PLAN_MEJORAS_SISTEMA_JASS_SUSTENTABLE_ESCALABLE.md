# 🏗️ PLAN DE MEJORAS - SISTEMA JASS SUSTENTABLE Y ESCALABLE

**Documento:** Análisis integral y propuesta de mejoras para múltiples JASS  
**Fecha:** 30 Enero 2026  
**Alcance:** Backend, Frontend, Base de Datos, Comunicación, Arquitectura Hexagonal, DDD, Eventos (RabbitMQ)

---

## 📑 ÍNDICE

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Estado Actual y Gaps](#2-estado-actual-y-gaps)
3. [Nueva Estructura Backend (Hexagonal + DDD)](#3-nueva-estructura-backend-hexagonal--ddd)
4. [Arquitectura de Eventos con RabbitMQ](#4-arquitectura-de-eventos-con-rabbitmq)
5. [Mejoras en Base de Datos y Tablas](#5-mejoras-en-base-de-datos-y-tablas)
6. [Mejoras en Comunicación entre Microservicios](#6-mejoras-en-comunicación-entre-microservicios)
7. [Mejoras en Frontend (Angular)](#7-mejoras-en-frontend-angular)
8. [Multi-JASS: Escalabilidad y Sustentabilidad](#8-multi-jass-escalabilidad-y-sustentabilidad)
9. [Roadmap de Implementación](#9-roadmap-de-implementación)

---

## 1. RESUMEN EJECUTIVO

### Objetivo

Transformar el Sistema JASS Digital en una plataforma **sustentable y escalable** para **múltiples JASS**, con:

- **Arquitectura hexagonal** y **DDD** consistentes en todos los microservicios.
- **Comunicación asíncrona** con **RabbitMQ** para desacoplamiento y resiliencia.
- **Base de datos** bien modelada, con **multi-tenancy** por organización (JASS).
- **Frontend** modular, mantenible y preparado para varios portales (Admin, Cliente, Super Admin).
- **Seguridad** unificada (Keycloak + JWT + JWE para MS-to-MS).
- **Observabilidad** (trazabilidad, métricas, logs centralizados).

### Hallazgos Principales del Análisis

| Área | Estado Actual | Objetivo |
|------|---------------|----------|
| Arquitectura hexagonal | 5 de 11 MS con violaciones (dominio con anotaciones BD) | 100% dominio puro |
| Seguridad | 3 MS sin SecurityConfig; credenciales en código | 100% OAuth2 + secrets en env/vault |
| Comunicación asíncrona | 0% (solo HTTP síncrono) | RabbitMQ para eventos de dominio |
| Circuit Breaker MS-to-MS | Solo en Gateway | Resilience4j en cada cliente interno |
| Multi-JASS | Parcial (organizationId en algunos modelos) | Tenant por request + políticas por JASS |
| Frontend | Monolito Angular con acoplamiento | Módulos por contexto + state claro |

---

## 2. ESTADO ACTUAL Y GAPS

### 2.1 Backend

- **Violaciones hexagonal:** vg-ms-users (AuthCredential), vg-ms-distribution (Route, Schedule), vg-ms-payments-billing (Receipts), vg-ms-water-quality (User) — modelos de dominio con `@Document`/`@Table`/`@Id`.
- **Seguridad:** vg-ms-infrastructure (desactivada), vg-ms-payments-billing y vg-ms-claims-incidents sin SecurityConfig.
- **Credenciales:** URIs MongoDB/PostgreSQL, Keycloak y tokens en `application*.yml`.
- **Comunicación:** 100% síncrona (WebClient); sin colas ni eventos.
- **Resiliencia:** Circuit Breaker solo en Gateway; llamadas directas MS-to-MS sin retry/timeout estándar.

### 2.2 Base de Datos

- Mezcla MongoDB/PostgreSQL bien justificada en DOCS, pero:
  - Falta **esquema único de multi-tenancy** (organizationId/jassCode) en todas las tablas/colecciones.
  - Algunos MS usan `userId` como Long vs String (Keycloak ID) — inconsistencia.
  - Sin **Flyway/Liquibase** estandarizado en todos los MS con PostgreSQL.
  - Índices compuestos por (organizationId, ...) no documentados de forma uniforme.

### 2.3 Frontend (vg-sistemajass-web)

- Estructura por `core/`, `layouts/`, `modules/`, `shared/`, `views/` es razonable.
- Gaps: estado global disperso, posible acoplamiento a APIs por MS, falta de diseño “multi-tenant” explícito (cambiar de JASS sin re-login).
- No hay evidencia de SSR/PWA ni estrategia de offline para zonas rurales.

### 2.4 Comunicación y Eventos

- **RabbitMQ:** no está integrado en el proyecto (no hay dependencias ni configuración en los MS).
- No hay eventos de dominio publicados (ej. `user.created`, `payment.received`, `incident.registered`).

---

## 3. NUEVA ESTRUCTURA BACKEND (HEXAGONAL + DDD)

### 3.1 Principios a Aplicar en Todos los Microservicios

1. **Dominio puro:** `domain/` sin anotaciones de Spring, JPA, MongoDB ni R2DBC. Solo POJOs, value objects, entidades de dominio y lógica de negocio.
2. **Puertos (interfaces) en dominio o aplicación:** casos de uso (application) y puertos de persistencia/externos (infrastructure implementa).
3. **Adaptadores:** REST, Repositories, Clients, Listeners de mensajes en `infrastructure/`.
4. **Un microservicio = un bounded context** (DDD): Users, Organizations, Infrastructure, Payments, etc., con vocabulario y agregados bien delimitados.

### 3.2 Estructura de Carpetas Unificada (Java)

```
vg-ms-{nombre}/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── README.md
└── src/main/
    ├── java/pe/edu/vallegrande/{package}/
    │   ├── {Service}Application.java
    │   │
    │   ├── domain/                          # NÚCLEO (sin dependencias externas)
    │   │   ├── model/                        # Entidades, agregados, value objects
    │   │   │   ├── {AggregateRoot}.java
    │   │   │   ├── {Entity}.java
    │   │   │   └── vo/
    │   │   │       └── {ValueObject}.java
    │   │   ├── enums/
    │   │   ├── events/                      # Eventos de dominio (para RabbitMQ)
    │   │   │   └── {Something}CreatedEvent.java
    │   │   ├── exception/                    # Excepciones de dominio
    │   │   └── port/                         # Puertos (interfaces)
    │   │       ├── out/
    │   │       │   ├── persistence/
    │   │       │   │   └── {Aggregate}Repository.java
    │   │       │   └── messaging/
    │   │       │       └── EventPublisher.java
    │   │       └── in/
    │   │           └── usecase/
    │   │               └── {Action}{Aggregate}UseCase.java
    │   │
    │   ├── application/                     # Casos de uso (orquestación)
    │   │   ├── usecase/impl/
    │   │   │   └── {Action}{Aggregate}UseCaseImpl.java
    │   │   ├── dto/                         # DTOs de aplicación (request/response)
    │   │   │   ├── request/
    │   │   │   └── response/
    │   │   └── mapper/                      # Domain <-> DTO
    │   │
    │   └── infrastructure/                  # Adaptadores
    │       ├── persistence/
    │       │   ├── entity/                   # JPA/R2DBC o @Document
    │       │   │   └── {Aggregate}Entity.java
    │       │   ├── repository/
    │       │   │   └── {Aggregate}RepositoryImpl.java
    │       │   └── mapper/                  # Domain <-> Entity
    │       ├── rest/                        # Controladores REST
    │       │   ├── admin/
    │       │   ├── client/
    │       │   ├── internal/
    │       │   └── common/
    │       ├── messaging/                   # RabbitMQ
    │       │   ├── config/
    │       │   ├── publisher/
    │       │   └── consumer/
    │       ├── client/                      # Clientes HTTP (otros MS)
    │       │   ├── internal/
    │       │   └── external/
    │       ├── config/
    │       ├── security/
    │       └── exception/
    │           └── GlobalExceptionHandler.java
    │
    └── resources/
        ├── application.yml
        ├── application-dev.yml
        ├── application-prod.yml
        └── db/migration/                    # Flyway (PostgreSQL)
            └── V1__initial_schema.sql
```

### 3.3 Reglas de Mapeo

- **Nunca** exponer entidades de persistencia (`*Entity`, `*Document`) fuera de `infrastructure`.
- **Siempre** mapear: Request DTO → Domain → Entity (persistencia) y Entity → Domain → Response DTO.
- **Dominio:** métodos de negocio en las entidades/aggregates (ej. `receipt.calculateTotal()`, `user.canAccessOrganization(jassCode)`).

### 3.4 Multi-JASS en el Dominio

- Incluir en agregados y consultas un **tenant identifier**: `organizationId` o `jassCode` (según estándar del proyecto).
- En REST: extraer tenant de JWT (claim `organization` o `jass_code`) o de header `X-Organization-Id` para rutas internas.
- En BD: índice compuesto `(organization_id, ...)` en todas las tablas/colecciones que sean por JASS.

---

## 4. ARQUITECTURA DE EVENTOS CON RABBITMQ

### 4.1 Rol de RabbitMQ

- **Desacoplar** microservicios: no llamar a “notification” por HTTP al crear usuario; publicar `user.created` y que Notification consuma.
- **Resiliencia:** si Notification está caído, los mensajes se encolan.
- **Escalabilidad:** varios consumidores por cola.
- **Auditoría:** eventos de dominio reutilizables para logs y reporting.

### 4.2 Exchanges y Enrutado (Propuesta)

| Exchange | Tipo | Uso |
|---------|------|-----|
| `jass.user.events` | topic | user.created, user.updated, user.deactivated |
| `jass.payment.events` | topic | payment.received, receipt.generated, receipt.overdue |
| `jass.organization.events` | topic | organization.created, zone.updated, fare.updated |
| `jass.infrastructure.events` | topic | waterbox.assigned, waterbox.transferred, cut.applied |
| `jass.incident.events` | topic | incident.created, incident.resolved, complaint.created |
| `jass.notification.commands` | topic | send.email, send.sms, send.whatsapp |
| `jass.audit.events` | topic | audit.entity.created, audit.entity.updated |
| `jass.dlq` | direct | Dead letter queue para reintentos fallidos |

### 4.3 Colas Sugeridas

- `notification.email.queue` → binding desde `jass.notification.commands` (routing key `send.email.#`).
- `notification.sms.queue` → `send.sms.#`.
- `notification.whatsapp.queue` → `send.whatsapp.#`.
- `audit.log.queue` → consumo de `jass.audit.events`.
- Por cada MS que deba reaccionar: por ejemplo `payments.user.events.queue` (payments escucha `user.*` si necesita datos de usuario).

### 4.4 Contrato de Eventos (Ejemplo)

- **Nombre:** verbo en pasado + entidad (ej. `UserCreated`, `PaymentReceived`).
- **Payload:** JSON con id, ids relacionados, organizationId/jassCode, timestamp, versión.
- **Headers:** `event-type`, `trace-id`, `timestamp`, `source-service`.

Ejemplo:

```json
{
  "eventType": "UserCreated",
  "aggregateId": "usr-123",
  "organizationId": "org-jass-rinc",
  "payload": {
    "email": "user@example.com",
    "firstName": "Juan",
    "lastName": "Perez",
    "roles": ["CLIENT"]
  },
  "occurredAt": "2026-01-30T10:00:00Z",
  "version": 1
}
```

### 4.5 Implementación en Spring Boot

- Dependencias: `spring-boot-starter-amqp`, opcionalmente `reactor-rabbitmq` para uso reactivo.
- Configuración en `application.yml`: `spring.rabbitmq.*` (host, port, user, password, virtual-host).
- **Publicador:** en el caso de uso, después de persistir, inyectar `EventPublisher` (puerto) y publicar el evento; implementación en `infrastructure/messaging/publisher` que envíe al exchange correcto.
- **Consumidor:** `@RabbitListener` en `infrastructure/messaging/consumer`, deserializar payload, llamar a un caso de uso de aplicación (ej. “Enviar email de bienvenida”) y hacer ACK/NACK según resultado.
- **DLQ:** configurar dead-letter exchange y reintentos limitados (ej. 3) antes de enviar a DLQ.

---

## 5. MEJORAS EN BASE DE DATOS Y TABLAS

### 5.1 Multi-Tenancy Consistente

- **Campo estándar:** `organization_id` (UUID o código JASS) en todas las tablas/colecciones que pertenezcan a una JASS.
- **Política:** todas las consultas filtrar por `organization_id` (extraído del contexto de seguridad o header).
- **PostgreSQL:** schemas por JASS opcional (más aislado pero más complejo); como mínimo, índice `(organization_id, ...)` en todas las tablas.

### 5.2 Normalización y Convenciones

- **IDs:** preferir UUID para entidades que se replican o referencian entre MS; IDs numéricos solo donde sea requisito (ej. códigos legibles tipo REC-2026-000001).
- **Auditoría:** `created_at`, `updated_at`, `created_by`, `updated_by` en todas las tablas; en MongoDB, mismo criterio en documentos.
- **Soft delete:** campo `active` o `deleted_at` donde aplique (usuarios, organizaciones).

### 5.3 Índices Recomendados (Resumen)

- **PostgreSQL (payments):** `(organization_id, user_id)`, `(organization_id, payment_status, due_date)`, `(receipt_code)` unique.
- **PostgreSQL (users):** `(organization_id, email)` unique, `(organization_id, status)`.
- **MongoDB (organizations):** `{ organizationId: 1, code: 1 }` unique, índices TTL donde corresponda (notifications, daily_records).

### 5.4 Migraciones

- **PostgreSQL:** Flyway en cada MS que use PostgreSQL; scripts en `src/main/resources/db/migration/`.
- **MongoDB:** scripts de índices versionados (ej. en `/db/migration/mongo/`) y aplicación al desplegar o mediante job inicial.

---

## 6. MEJORAS EN COMUNICACIÓN ENTRE MICROSERVICIOS

### 6.1 Síncrona (HTTP)

- **Solo** para flujos que requieran respuesta inmediata (ej. validar organización, obtener usuario por ID para mostrar en pantalla).
- **Gateway** como único punto de entrada desde el frontend; los MS no exponer URLs internas al exterior.
- **WebClient** con:
  - Timeout estándar (ej. connect 3s, read 10s).
  - Resilience4j: Circuit Breaker + Retry + TimeLimiter por cliente (users, organizations, etc.).
  - Propagación de JWT (Authorization: Bearer) para llamadas internas.
- **JWE** para datos sensibles en cuerpo o headers entre MS (según estándar PRS231).

### 6.2 Asíncrona (RabbitMQ)

- **Preferir** eventos para: notificaciones, auditoría, actualización de vistas desnormalizadas, integraciones que no requieran respuesta en la misma request.
- Evitar “llamada HTTP + publicación de evento” redundante; un solo canal (evento) cuando sea suficiente.

### 6.3 Estándar de Timeouts y Reintentos

- Connect timeout: 3s.
- Read timeout: 10s (ajustable por operación pesada).
- Retry: 2–3 intentos con backoff exponencial (500ms, 1s, 2s).
- Circuit Breaker: umbral de fallos 50%, ventana 10 llamadas, duración en abierto 60s.

---

## 7. MEJORAS EN FRONTEND (ANGULAR)

### 7.1 Estructura y Módulos

- **Módulos por contexto:** Users, Organizations, Payments, Infrastructure, Claims, etc., con lazy loading.
- **Core:** auth, interceptors, guards, servicios singleton (API base, tenant).
- **Shared:** componentes reutilizables (tablas, formularios, pipes) sin lógica de negocio.
- **State:** definir si se usa NgRx, Signals o servicios con señales; evitar estado duplicado y no sincronizado con el backend.

### 7.2 Multi-JASS en el Frontend

- **Selector de JASS:** si un usuario puede pertenecer a varias JASS, incluir en el header o menú un selector de organización; guardar en estado/session y enviar `X-Organization-Id` o equivalente en todas las peticiones.
- **Rutas:** mantener contexto de organización en rutas cuando sea necesario (ej. `/jass/:jassCode/dashboard`).

### 7.3 API y Errores

- **Un único punto de entrada:** todas las llamadas al backend vía Gateway (una base URL).
- **Manejo de errores:** interceptor global que traduzca códigos HTTP y cuerpos de error a mensajes y acciones (redirect login, mensaje toast, etc.).
- **Tipado:** interfaces TypeScript alineadas con DTOs del backend (generar con OpenAPI si es posible).

### 7.4 Rendimiento y UX

- Lazy loading de rutas y módulos.
- Paginación y filtros en listados grandes.
- Estrategia de caché para datos maestros (zonas, tarifas) por organización.
- Considerar PWA y modo offline para zonas con conectividad limitada (fase posterior).

---

## 8. MULTI-JASS: ESCALABILIDAD Y SUSTENTABILIDAD

### 8.1 Modelo de Datos

- Cada JASS = una **organización** con código único (ej. JASS-RINC, JASS-BELLA).
- Todos los recursos (usuarios, cajas, recibos, zonas, etc.) asociados a `organization_id`.
- Configuraciones por JASS: tarifas, parámetros, proveedores de notificación, etc., en Organizations o en tablas de parámetros por organización.

### 8.2 Seguridad y Aislamiento

- Keycloak: grupos o roles por JASS (ej. `admin_jass_rinc`, `operator_jass_bella`) o atributos en token (lista de `jass_codes`).
- En cada MS: validar que el `organizationId` del request pertenezca al usuario (o a su rol de super-admin).
- No exponer datos de una JASS a otra sin autorización explícita (super-admin o reportes agregados).

### 8.3 Escalabilidad Operativa

- **Despliegue:** mismo código de cada MS para todas las JASS; diferenciación por configuración (BD, tenant en runtime).
- **Base de datos:** una instancia de PostgreSQL y una de MongoDB por entorno (dev/prod) con múltiples tenants; si crece mucho, evaluar schema por JASS o cluster por región.
- **Costes:** monitoreo de uso por organización (opcional) para facturación o límites.

### 8.4 Sustentabilidad del Código

- Código común (DTOs, contratos de eventos, constantes) en librerías compartidas (Maven modules o paquetes publicados) para evitar duplicación entre MS.
- Documentación de APIs (OpenAPI) generada y publicada.
- Tests automatizados (unit + integración) y pipeline CI que ejecute tests y despliegue en entornos pre-producción.

---

## 9. ROADMAP DE IMPLEMENTACIÓN

### Fase 1 – Crítico (4–6 semanas)

1. **Seguridad:** Activar OAuth2 Resource Server en vg-ms-infrastructure, vg-ms-payments-billing, vg-ms-claims-incidents. Eliminar credenciales de los YAML; usar variables de entorno o vault.
2. **Arquitectura hexagonal:** Refactorizar los 5 MS con violaciones: separar dominio puro y entidades de persistencia con mappers.
3. **Resiliencia:** Añadir Resilience4j (Circuit Breaker, Retry, TimeLimiter) en los WebClient de los MS que llaman a otros MS. Estandarizar timeouts.

### Fase 2 – Eventos y Comunicación (4–6 semanas)

4. **RabbitMQ:** Instalar y configurar RabbitMQ (docker-compose y/o entorno compartido). Definir exchanges, colas y bindings.
5. **Publicación de eventos:** En Users (user.created), Payments (payment.received, receipt.generated), Organizations (zone.updated), etc., publicar eventos desde los casos de uso.
6. **Consumidores:** MS Notification consumiendo eventos y enviando email/SMS/WhatsApp; opcionalmente Audit MS o servicio que persista en cola de auditoría.

### Fase 3 – Base de Datos y Multi-JASS (2–4 semanas)

7. **Multi-tenancy:** Revisar todas las tablas/colecciones y asegurar `organization_id` e índices. Ajustar consultas y validaciones.
8. **Migraciones:** Estandarizar Flyway en MS con PostgreSQL; versionar scripts de índices en MongoDB.

### Fase 4 – Frontend y Observabilidad (3–4 semanas)

9. **Frontend:** Refactorizar estado y selector de JASS; lazy loading y tipado de APIs; manejo de errores unificado.
10. **Observabilidad:** Distributed tracing (OpenTelemetry/Zipkin), métricas (Prometheus) y logs estructurados; dashboards básicos (Grafana) y alertas.

### Fase 5 – Sustentabilidad (continuo)

11. **Librerías compartidas:** Extraer contratos de eventos, DTOs comunes y constantes a módulos reutilizables.
12. **Testing y CI:** Aumentar cobertura de tests; pipeline de CI (build, test, análisis estático, despliegue a dev/staging).
13. **Documentación:** OpenAPI actualizado, README por MS, y este plan como referencia de arquitectura.

---

## 10. TABLA RESUMEN DE MEJORAS POR ÁREA

| Área | Mejora | Prioridad | Esfuerzo |
|------|--------|-----------|----------|
| **Backend** | Separar dominio puro (sin @Document/@Entity en domain/) en 5 MS | Crítica | 1–2 semanas |
| **Backend** | Activar SecurityConfig + OAuth2 en 3 MS (infra, payments, claims) | Crítica | 3–5 días |
| **Backend** | Eliminar credenciales de YAML; usar env/vault | Crítica | 2–3 días |
| **Backend** | Estructura hexagonal con puertos (domain/port) y adaptadores (infrastructure) | Alta | 2–3 semanas |
| **Comunicación** | Integrar RabbitMQ: exchanges, colas, DLQ | Alta | 1 semana |
| **Comunicación** | Publicar eventos (user.created, payment.received, etc.) | Alta | 1–2 semanas |
| **Comunicación** | Consumidores en Notification (y opcional Audit) | Alta | 1 semana |
| **Comunicación** | Resilience4j en WebClient (Circuit Breaker, Retry, TimeLimiter) | Alta | 3–5 días |
| **Comunicación** | Propagación JWT y JWE en llamadas MS-to-MS | Media | 2–3 días |
| **BD** | organization_id + índices en todas las tablas/colecciones | Alta | 1 semana |
| **BD** | Flyway en MS PostgreSQL; migraciones versionadas | Media | 3–5 días |
| **BD** | Convención de auditoría (created_at, updated_at, created_by) | Media | 2–3 días |
| **Frontend** | Selector de JASS y header X-Organization-Id en todas las peticiones | Alta | 3–5 días |
| **Frontend** | Lazy loading de módulos por contexto | Media | 2–3 días |
| **Frontend** | Interceptor de errores y tipado de APIs (OpenAPI) | Media | 3–5 días |
| **Multi-JASS** | Validación tenant en cada request (JWT + organizationId) | Alta | 2–3 días |
| **Observabilidad** | Trazabilidad (trace-id), métricas (Prometheus), logs estructurados | Media | 1–2 semanas |

---

## 📎 REFERENCIAS

- **`DOCS/ESTANDAR_NAMING_ESTRUCTURA_Y_ATRIBUTOS.md`** – Nombres, estructura de carpetas, atributos de tablas y convenciones unificadas en todos los microservicios (uso obligatorio al refactorizar).
- `PRS231_STANDAR_BACKEND.md` – Estándares de backend y seguridad.
- `DOCS/ESTANDAR_ARQUITECTURA_HEXAGONAL_MEJORADO.md` – Estructura hexagonal detallada.
- `DOCS/ANALISIS_COMPLETO_MICROSERVICIOS.md` – Hallazgos y recomendaciones por MS.
- `DOCS/BASE_DATOS_POR_MICROSERVICIO.md` – Asignación BD y entidades.
- `DOCS/PROPUESTA_REFACTORIZACION_COMPLETA_DESDE_CERO.md` – Opción de rehacer desde cero y arquitectura objetivo.
- `DOCS/FLUJO_PAGOS_RECIBOS_COMPLETO.md` – Flujo de pagos y recibos.

---

**Documento generado:** 30 Enero 2026  
**Versión:** 1.0  
**Estado:** Propuesta para revisión e implementación por fases.
