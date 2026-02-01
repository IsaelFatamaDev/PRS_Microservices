# 📘 DOCUMENTACIÓN TÉCNICA DEFINITIVA: SISTEMA JASS v2.0

**Versión:** 1.0 (Planificación Final)  
**Fecha:** 31 Enero 2026  
**Arquitectura:** Microservicios Granulares + Hexagonal + Reactiva  

---

## 1. 🎯 Visión General

El Sistema JASS es una plataforma SaaS (Software as a Service) Multi-Tenant diseñada para gestionar múltiples Juntas Administradoras de Servicios de Saneamiento (JASS) en una sola infraestructura.

### Principios Fundamentales
1.  **Reactividad Total (Reactive First)**: Uso estricto de Spring WebFlux y R2DBC para maximizar el throughput con pocos recursos.
2.  **Aislamiento Multi-JASS**: Seguridad estricta donde una JASS nunca ve datos de otra (Partitioning lógico por `organization_id`).
3.  **Arquitectura Limpia**: Separación total entre Dominio (Reglas de Negocio) e Infraestructura (Frameworks/DB).
4.  **Resiliencia**: El sistema debe tolerar fallos en servicios no críticos (ej. Notificaciones) sin detener la operación principal (ej. Cobros).

---

## 2. 🏗️ Arquitectura de Microservicios (Los 11 Pilares)

El sistema se divide en 11 dominios acotados (Bounded Contexts).

### 🛡️ Nivel 1: Core & Seguridad
| Servicio | Tech | Base de Datos | Responsabilidad |
| :--- | :--- | :--- | :--- |
| **`vg-ms-authentication`** | Java | *Stateless* | Proxy Auth (Keycloak). Mapeo de Roles. |
| **`vg-ms-users`** | Java | **PostgreSQL** | Gestión de Identidades. Roles Duales (Admin/Cliente). |
| **`vg-ms-gateway`** | Java | *Stateless* | Router (Spring Cloud Gateway), Rate Limiting. |

### 🏢 Nivel 2: Organización & Infraestructura
| Servicio | Tech | Base de Datos | Responsabilidad |
| :--- | :--- | :--- | :--- |
| **`vg-ms-organizations`** | Java | **PostgreSQL** | Configuración JASS (Tarifas, Zonas, Parámetros). |
| **`vg-ms-infrastructure`** | Java | **PostgreSQL** | Cajas de Agua, Asignaciones, **Transferencias**. |

### 💰 Nivel 3: Operaciones Comerciales (El "Cuaderno")
| Servicio | Tech | Base de Datos | Responsabilidad |
| :--- | :--- | :--- | :--- |
| **`vg-ms-commercial-operations`** | Java | **PostgreSQL** | Facturación (Recibos), Pagos, Deudas, Cortes, Caja Chica. |
| **`vg-ms-claims-incidents`** | Java | **MongoDB** | Reclamos de Clientes y Reporte de Averías. |

### 💧 Nivel 4: Operaciones de Campo
| Servicio | Tech | Base de Datos | Responsabilidad |
| :--- | :--- | :--- | :--- |
| **`vg-ms-distribution`** | Java | **MongoDB** | Horarios, Turnos de Agua, Racionamiento. |
| **`vg-ms-water-quality`** | Java | **MongoDB** | Muestreo de Cloro/pH, Cumplimiento Normativo. |
| **`vg-ms-inventory`** | Java | **PostgreSQL** | Almacén, Kardex, Compras de Insumos. |

### 📢 Nivel 5: Soporte
| Servicio | Tech | Base de Datos | Responsabilidad |
| :--- | :--- | :--- | :--- |
| **`vg-ms-notifications`** | Node.js | **MongoDB** | Dispatcher (WhatsApp, SMS, Email). Plantillas. |

---

## 3. 🔐 Estrategia Multi-Tenancy (Multi-JASS)

El sistema soporta cientos de JASS simultáneas con una sola base de código.

### Aislamiento de Seguridad
1.  **Token JWT**: Contiene el claim `organization_id`.
2.  **Contexto Reactivo**: Un `WebFilter` extrae el ID y lo inyecta en el contexto de ejecución.
3.  **Repositorios Seguros**: Todas las consultas a BD filtran automáticamente por el ID del contexto.
    *   *Intento*: Un usuario intenta pedir datos de la JASS vecina.
    *   *Resultado*: `403 Forbidden` o `404 Not Found` (Filtrado a nivel de Row/Document).

### Configuración Dinámica (`vg-ms-organizations`)
Cada JASS configura sus reglas sin cambiar código:
*   `COBRO_REPOSICION`: S/ 10.00 (JASS A) vs S/ 50.00 (JASS B).
*   `DIA_CORTE`: Día 15 (JASS A) vs Día 30 (JASS B).

---

## 4. 🛠️ Stack Tecnológico & Estándares

### Backend Java (Core)
*   **Framework**: Spring Boot 3.3+
*   **Paradigma**: **Reactive Programming (WebFlux + Netty)**.
*   **Persistencia SQL**: **R2DBC** (PostgreSQL Async).
*   **Persistencia NoSQL**: Spring Data MongoDB Reactive.
*   **Cliente HTTP**: `WebClient` con **Resilience4j** (Circuit Breaker, Retry, Timeout).

### Backend Node.js (Notificaciones)
*   **Framework**: NestJS (Recomendado) o Express Modular.
*   **Arquitectura**: Modular Monolith.

### Frontend
*   **Framework**: Angular 18+ (Standalone Components).
*   **UX**: Soporte para cambio de perfil (Admin <-> Usuario) en la misma sesión.

---

## 5. 📡 Estrategia de Comunicación

Regla de Oro para desacoplamiento:

### A. Comunicación Síncrona (HTTP/Feign)
**Uso**: Cuando la respuesta es crítica para continuar la operación.
*   *Ejemplo*: `infraestructura` valida si un usuario tiene deuda en `comercial` antes de aprobar una transferencia.

### B. Comunicación Asíncrona (Eventos/RabbitMQ)
**Uso**: Para efectos secundarios, notificaciones y consistencia eventual.
*   *Exchange*: `jass.direct` (Bus de Eventos del Sistema).
*   *Ejemplo*:
    1.  `vg-ms-users` crea usuario -> Emite `USER_REGISTERED`.
    2.  `vg-ms-infrastructure` escucha -> Crea/Asigna Caja de Agua.
    3.  El usuario ve su caja asignada segundos después.

---

## 6. 🏗️ Estándar de Carpetas (Clean Architecture Revisado)

Esta es la **Estructura Definitiva** (basada en tu propuesta) para todos los microservicios Java.

### ☕ Estándar Java (Spring WebFlux + R2DBC/Mongo)

```text
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
│   │   │                   │   ├── models/                      → Entidades de negocio Puras
│   │   │                   │   │   ├── User.java
│   │   │                   │   │   └── Payment.java
│   │   │                   │   │
│   │   │                   │   ├── ports/                       → Puertos (Interfaces)
│   │   │                   │   │   ├── in/                      → Casos de uso (Input - Qué ofrece)
│   │   │                   │   │   │   ├── ICreateUserUseCase.java
│   │   │                   │   │   │   ├── IGetUserUseCase.java
│   │   │                   │   │   │   └── IUpdateUserUseCase.java
│   │   │                   │   │   │
│   │   │                   │   │   └── out/                     → Repositorios (Output - Qué necesita)
│   │   │                   │   │       ├── IUserRepository.java
│   │   │                   │   │       ├── IPaymentRepository.java
│   │   │                   │   │       └── IEventPublisher.java
│   │   │                   │   │
│   │   │                   │   └── exceptions/                  → Excepciones de dominio
│   │   │                   │       ├── UserNotFoundException.java
│   │   │                   │       └── BusinessRuleException.java
│   │   │                   │
│   │   │                   ├── application/                     [CAPA DE APLICACIÓN]
│   │   │                   │   ├── usecases/                    → Implementación casos de uso
│   │   │                   │   │   ├── CreateUserUseCaseImpl.java
│   │   │                   │   │   └── UpdateUserUseCaseImpl.java
│   │   │                   │   │
│   │   │                   │   ├── services/                    → Servicios de aplicación (Orquestación opcional)
│   │   │                   │   │   └── UserApplicationService.java
│   │   │                   │   │
│   │   │                   │   ├── dto/                         → Data Transfer Objects (Contratos)
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
│   │   │                   │   └── events/                      → Eventos de dominio (Definición)
│   │   │                   │       ├── UserCreatedEvent.java
│   │   │                   │       └── PaymentProcessedEvent.java
│   │   │                   │
│   │   │                   └── infrastructure/                  [CAPA DE INFRAESTRUCTURA]
│   │   │                       │
│   │   │                       ├── adapters/                    → Adaptadores (Implementaciones)
│   │   │                       │   │
│   │   │                       │   ├── in/                      → Adaptadores de entrada (Drivers)
│   │   │                       │   │   ├── rest/                → Controllers REST Reactivos
│   │   │                       │   │   │   ├── UserController.java
│   │   │                       │   │   │   └── PaymentController.java
│   │   │                       │   │   │
│   │   │                       │   │   └── messaging/           → Listeners de RabbitMQ
│   │   │                       │   │       ├── UserEventListener.java
│   │   │                       │   │       └── PaymentEventListener.java
│   │   │                       │   │
│   │   │                       │   └── out/                     → Adaptadores de salida (Driven)
│   │   │                       │       ├── persistence/         → Implementación de Repositorios (Domain Ports)
│   │   │                       │       │   ├── UserRepositoryImpl.java
│   │   │                       │       │   └── PaymentRepositoryImpl.java
│   │   │                       │       │
│   │   │                       │       ├── messaging/           → Producers RabbitMQ
│   │   │                       │       │   ├── RabbitMQEventPublisher.java
│   │   │                       │       │   └── UserEventProducer.java
│   │   │                       │       │
│   │   │                       │       └── external/            → Clientes WebClient HTTP
│   │   │                       │           ├── OrganizationServiceClient.java
│   │   │                       │           └── NotificationServiceClient.java
│   │   │                       │
│   │   │                       ├── persistence/                 → Detalles de BD (R2DBC/Mongo)
│   │   │                       │   ├── entities/                → R2DBC Entities (@Table)
│   │   │                       │   │   ├── UserEntity.java
│   │   │                       │   │   └── PaymentEntity.java
│   │   │                       │   │
│   │   │                       │   ├── documents/               → MongoDB Documents (@Document)
│   │   │                       │   │   ├── OrganizationDocument.java
│   │   │                       │   │   └── NotificationDocument.java
│   │   │                       │   │
│   │   │                       │   └── repositories/            → Spring Data Reactive Repos
│   │   │                       │       ├── UserR2dbcRepository.java       → ReactiveCrudRepository
│   │   │                       │       └── OrganizationReactiveRepository.java  → ReactiveMongoRepository
│   │   │                       │
│   │   │                       ├── config/                      → Configuraciones Spring
│   │   │                       │   ├── WebFluxConfig.java
│   │   │                       │   ├── R2dbcConfig.java
│   │   │                       │   ├── MongoReactiveConfig.java
│   │   │                       │   ├── WebClientConfig.java
│   │   │                       │   ├── RabbitMQConfig.java
│   │   │                       │   ├── Resilience4jConfig.java
│   │   │                       │   └── SecurityConfig.java
│   │   │                       │
│   │   │                       └── shared/                      → Utilidades transversales
│   │   │                           ├── constants/
│   │   │                           │   └── ErrorMessages.java
│   │   │                           ├── utils/
│   │   │                           │   └── DateUtils.java
│   │   │                           └── exceptions/
│   │   │                               └── GlobalExceptionHandler.java
│   │   │
│   │   └── resources/
│   │       ├── application.yml                              → Configuración base
│   │       ├── application-dev.yml                          → Perfil local
│   │       ├── application-prod.yml                         → Perfil nube
│   │       ├── db/
│   │       │   └── migration/                               → Flyway (Schema SQL)
│   │       │       ├── V1__create_users_table.sql
│   │       │       └── V2__create_payments_table.sql
│   │       └── mongodb/                                     → Scripts Mongo
│   │
│   └── test/                                                → Pruebas (Unitarias e Integración)
├── docker-compose.yml
├── Dockerfile
├── pom.xml
└── README.md
```

### 📍 Guía de Ubicación Rápida

| Componente | Carpeta Exacta |
| :--- | :--- |
| **Repositorios Spring Data** | `infrastructure/persistence/repositories` |
| **Implementación Domain Repo** | `infrastructure/adapters/out/persistence` |
| **Controllers (Endpoints)** | `infrastructure/adapters/in/rest` |
| **DTOs (Request/Response)** | `application/dto` |
| **Mappers (@MapStruct)** | `application/mappers` |
| **Configuración DB** | `infrastructure/config/R2dbcConfig.java` |
| **Entidades (Tablas)** | `infrastructure/persistence/entities` |
| **Entidades (Dominio)** | `domain/models` |
| **Casos de Uso (Impl)** | `application/usecases` |

### Estándar Node.js (Modular)
```text
src/
├── modules/                      # Módulos de Negocio
│   ├── notifications/
│   │   ├── application/          # Services, DTOs
│   │   ├── domain/               # Models, Interfaces
│   │   └── infrastructure/       # Controllers, MongoSchemas, Providers
│   └── templates/
└── shared/                       # Guards, Interceptors
```

---

## 7. 🧪 Plan de Bases de Datos

| Servicio | DB | Razón |
| :--- | :--- | :--- |
| **Auth** | - | Stateless (Keycloak) |
| **Users** | **PostgreSQL** | Integridad Relacional (Roles, Users, JASS) |
| **Organizations** | **PostgreSQL** | Jerarquía Estricta (JASS -> Zona -> Calle) |
| **Infrastructure** | **PostgreSQL** | Historial de Transferencias (ACID crítico) |
| **Commercial** | **PostgreSQL** | **CRÍTICO**. Dinero, Deuda, Transacciones ACID. |
| **Inventory** | **PostgreSQL** | Integridad de Inventario (Kardex). |
| **Distribution** | **MongoDB** | Esquemas flexibles de turnos y horarios. |
| **Quality** | **MongoDB** | Parámetros de laboratorio variables. |
| **Claims** | **MongoDB** | Evidencias multimedia y logs de chat. |
| **Notifications** | **MongoDB** | Logs JSON de respuestas de proveedores. |

---

## 8. 📋 Principales Flujos de Negocio

### Transferencia de Caja (Mudanza)
1.  **Admin** solicita transferencia de Caja X de Usuario A -> Usuario B.
2.  `vg-ms-infra` llama (HTTP) a `vg-ms-commercial`: "¿Usuario A tiene deuda?".
3.  Si Deuda > 0: **ERROR**. "Pague antes de transferir".
4.  Si Deuda = 0: `vg-ms-infra` cierra asignación A, crea asignación B.
5.  Emite evento `TRANSFER_COMPLETED`.

### Ciclo de Facturación (Recibo Complejo)
1.  `vg-ms-commercial` genera recibo mensual.
2.  Consulta `Parametros` en `vg-ms-organizations` (Costo Cuota, Costo Multas).
3.  Suma: Cuota + Multas (Asamblea/Faena) + Deuda Anterior.
4.  Guarda Recibo (Estado: PENDING).
5.  Emite evento `RECEIPT_GENERATED`.
6.  `vg-ms-notifications` envía alerta WhatsApp con Link de Pago.
