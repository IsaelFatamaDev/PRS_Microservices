# 🏢 VG-MS-ORGANIZATIONS

> **Microservicio de Organizaciones, Zonas, Calles, Tarifas y Parámetros del sistema JASS Digital.**

## 📐 Arquitectura Hexagonal

```
┌─────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE                           │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐ │
│  │  REST APIs   │  │   MongoDB    │  │      RabbitMQ         │ │
│  │ (Adapters IN)│  │(Adapters OUT)│  │   (Adapters OUT)      │ │
│  └──────┬───────┘  └──────┬───────┘  └───────────┬───────────┘ │
│         │                 │                      │              │
│  ┌──────▼─────────────────▼──────────────────────▼───────────┐ │
│  │                    PORTS (Interfaces)                      │ │
│  │  ┌─────────────────┐            ┌──────────────────────┐  │ │
│  │  │    Ports IN      │            │     Ports OUT        │  │ │
│  │  │  (Use Cases)     │            │  (Repository, Event) │  │ │
│  │  └────────┬─────────┘            └──────────┬───────────┘  │ │
│  └───────────┼─────────────────────────────────┼──────────────┘ │
│              │                                 │                │
│  ┌───────────▼─────────────────────────────────▼──────────────┐ │
│  │                      APPLICATION                           │ │
│  │  ┌─────────────┐  ┌────────┐  ┌─────────┐  ┌──────────┐  │ │
│  │  │  Use Cases   │  │  DTOs  │  │ Mappers │  │  Events  │  │ │
│  │  └──────┬───────┘  └────────┘  └─────────┘  └──────────┘  │ │
│  └─────────┼──────────────────────────────────────────────────┘ │
│            │                                                    │
│  ┌─────────▼──────────────────────────────────────────────────┐ │
│  │                        DOMAIN                              │ │
│  │  ┌─────────────┐  ┌──────────────┐  ┌──────────────────┐  │ │
│  │  │   Models     │  │ Value Objects│  │   Exceptions     │  │ │
│  │  └─────────────┘  └──────────────┘  └──────────────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📂 Estructura del Proyecto

```
vg-ms-organizations/
├── src/main/java/pe/edu/vallegrande/vgmsorganizations/
│   ├── VgMsOrganizationsApplication.java              # 🚀 Main Class
│   │
│   ├── domain/                                         # 🧩 CAPA DE DOMINIO
│   │   ├── models/                                     # 📦 Modelos de negocio
│   │   │   ├── Organization.java                       #    └─ Model
│   │   │   ├── Zone.java                               #    └─ Model
│   │   │   ├── Street.java                             #    └─ Model
│   │   │   ├── Fare.java                               #    └─ Model
│   │   │   ├── Parameter.java                          #    └─ Model
│   │   │   └── valueobjects/                           # 🏷️ Value Objects (Enums)
│   │   │       ├── RecordStatus.java                   #    └─ Enum
│   │   │       ├── StreetType.java                     #    └─ Enum
│   │   │       ├── FareType.java                       #    └─ Enum
│   │   │       └── ParameterType.java                  #    └─ Enum
│   │   ├── ports/                                      # 🔌 Puertos (Interfaces)
│   │   │   ├── in/                                     # ⬅️ Puertos de ENTRADA
│   │   │   │   ├── organization/
│   │   │   │   │   ├── ICreateOrganizationUseCase.java #    └─ Interface
│   │   │   │   │   ├── IGetOrganizationUseCase.java    #    └─ Interface
│   │   │   │   │   ├── IUpdateOrganizationUseCase.java #    └─ Interface
│   │   │   │   │   ├── IDeleteOrganizationUseCase.java #    └─ Interface
│   │   │   │   │   └── IRestoreOrganizationUseCase.java#    └─ Interface
│   │   │   │   ├── zone/
│   │   │   │   │   ├── ICreateZoneUseCase.java         #    └─ Interface
│   │   │   │   │   ├── IGetZoneUseCase.java            #    └─ Interface
│   │   │   │   │   ├── IUpdateZoneUseCase.java         #    └─ Interface
│   │   │   │   │   ├── IDeleteZoneUseCase.java         #    └─ Interface
│   │   │   │   │   └── IRestoreZoneUseCase.java        #    └─ Interface
│   │   │   │   ├── street/
│   │   │   │   │   ├── ICreateStreetUseCase.java       #    └─ Interface
│   │   │   │   │   ├── IGetStreetUseCase.java          #    └─ Interface
│   │   │   │   │   ├── IUpdateStreetUseCase.java       #    └─ Interface
│   │   │   │   │   ├── IDeleteStreetUseCase.java       #    └─ Interface
│   │   │   │   │   └── IRestoreStreetUseCase.java      #    └─ Interface
│   │   │   │   ├── fare/
│   │   │   │   │   ├── ICreateFareUseCase.java         #    └─ Interface
│   │   │   │   │   ├── IGetFareUseCase.java            #    └─ Interface
│   │   │   │   │   ├── IUpdateFareUseCase.java         #    └─ Interface
│   │   │   │   │   ├── IDeleteFareUseCase.java         #    └─ Interface
│   │   │   │   │   └── IRestoreFareUseCase.java        #    └─ Interface
│   │   │   │   └── parameter/
│   │   │   │       ├── ICreateParameterUseCase.java    #    └─ Interface
│   │   │   │       ├── IGetParameterUseCase.java       #    └─ Interface
│   │   │   │       ├── IUpdateParameterUseCase.java    #    └─ Interface
│   │   │   │       ├── IDeleteParameterUseCase.java    #    └─ Interface
│   │   │   │       └── IRestoreParameterUseCase.java   #    └─ Interface
│   │   │   └── out/                                    # ➡️ Puertos de SALIDA
│   │   │       ├── organization/
│   │   │       │   ├── IOrganizationRepository.java    #    └─ Interface (Repository)
│   │   │       │   └── IOrganizationEventPublisher.java#    └─ Interface (Event)
│   │   │       ├── zone/
│   │   │       │   ├── IZoneRepository.java            #    └─ Interface (Repository)
│   │   │       │   └── IZoneEventPublisher.java        #    └─ Interface (Event)
│   │   │       ├── street/
│   │   │       │   ├── IStreetRepository.java          #    └─ Interface (Repository)
│   │   │       │   └── IStreetEventPublisher.java      #    └─ Interface (Event)
│   │   │       ├── fare/
│   │   │       │   ├── IFareRepository.java            #    └─ Interface (Repository)
│   │   │       │   └── IFareEventPublisher.java        #    └─ Interface (Event)
│   │   │       └── parameter/
│   │   │           ├── IParameterRepository.java       #    └─ Interface (Repository)
│   │   │           └── IParameterEventPublisher.java   #    └─ Interface (Event)
│   │   └── exceptions/                                 # ❌ Excepciones de dominio
│   │       ├── base/                                   #    └─ Excepciones genéricas
│   │       │   ├── DomainException.java                #       └─ Exception (Base)
│   │       │   ├── NotFoundException.java              #       └─ Exception
│   │       │   ├── BusinessRuleException.java          #       └─ Exception
│   │       │   ├── ValidationException.java            #       └─ Exception
│   │       │   └── ConflictException.java              #       └─ Exception
│   │       └── specific/                               #    └─ Excepciones específicas
│   │           ├── OrganizationNotFoundException.java  #       └─ Exception
│   │           ├── ZoneNotFoundException.java          #       └─ Exception
│   │           ├── StreetNotFoundException.java        #       └─ Exception
│   │           ├── FareNotFoundException.java          #       └─ Exception
│   │           ├── ParameterNotFoundException.java     #       └─ Exception
│   │           └── DuplicateOrganizationException.java #       └─ Exception
│   │
│   ├── application/                                    # ⚙️ CAPA DE APLICACIÓN
│   │   ├── usecases/                                   # 🎯 Casos de uso (Servicios)
│   │   │   ├── organization/
│   │   │   │   ├── CreateOrganizationUseCaseImpl.java  #    └─ @Service
│   │   │   │   ├── GetOrganizationUseCaseImpl.java     #    └─ @Service
│   │   │   │   ├── UpdateOrganizationUseCaseImpl.java  #    └─ @Service
│   │   │   │   ├── DeleteOrganizationUseCaseImpl.java  #    └─ @Service
│   │   │   │   └── RestoreOrganizationUseCaseImpl.java #    └─ @Service
│   │   │   ├── zone/
│   │   │   │   ├── CreateZoneUseCaseImpl.java          #    └─ @Service
│   │   │   │   ├── GetZoneUseCaseImpl.java             #    └─ @Service
│   │   │   │   ├── UpdateZoneUseCaseImpl.java          #    └─ @Service
│   │   │   │   ├── DeleteZoneUseCaseImpl.java          #    └─ @Service
│   │   │   │   └── RestoreZoneUseCaseImpl.java         #    └─ @Service
│   │   │   ├── street/
│   │   │   │   ├── CreateStreetUseCaseImpl.java        #    └─ @Service
│   │   │   │   ├── GetStreetUseCaseImpl.java           #    └─ @Service
│   │   │   │   ├── UpdateStreetUseCaseImpl.java        #    └─ @Service
│   │   │   │   ├── DeleteStreetUseCaseImpl.java        #    └─ @Service
│   │   │   │   └── RestoreStreetUseCaseImpl.java       #    └─ @Service
│   │   │   ├── fare/
│   │   │   │   ├── CreateFareUseCaseImpl.java          #    └─ @Service
│   │   │   │   ├── GetFareUseCaseImpl.java             #    └─ @Service
│   │   │   │   ├── UpdateFareUseCaseImpl.java          #    └─ @Service
│   │   │   │   ├── DeleteFareUseCaseImpl.java          #    └─ @Service
│   │   │   │   └── RestoreFareUseCaseImpl.java         #    └─ @Service
│   │   │   └── parameter/
│   │   │       ├── CreateParameterUseCaseImpl.java     #    └─ @Service
│   │   │       ├── GetParameterUseCaseImpl.java        #    └─ @Service
│   │   │       ├── UpdateParameterUseCaseImpl.java     #    └─ @Service
│   │   │       ├── DeleteParameterUseCaseImpl.java     #    └─ @Service
│   │   │       └── RestoreParameterUseCaseImpl.java    #    └─ @Service
│   │   ├── dto/                                        # 📝 DTOs (Data Transfer Objects)
│   │   │   ├── common/                                 #    └─ DTOs comunes
│   │   │   │   ├── ApiResponse.java                    #       └─ Record/Class
│   │   │   │   ├── PageResponse.java                   #       └─ Record/Class
│   │   │   │   └── ErrorMessage.java                   #       └─ Record/Class
│   │   │   ├── organization/
│   │   │   │   ├── CreateOrganizationRequest.java      #    └─ DTO Request
│   │   │   │   ├── UpdateOrganizationRequest.java      #    └─ DTO Request
│   │   │   │   └── OrganizationResponse.java           #    └─ DTO Response
│   │   │   ├── zone/
│   │   │   │   ├── CreateZoneRequest.java              #    └─ DTO Request
│   │   │   │   ├── UpdateZoneRequest.java              #    └─ DTO Request
│   │   │   │   └── ZoneResponse.java                   #    └─ DTO Response
│   │   │   ├── street/
│   │   │   │   ├── CreateStreetRequest.java            #    └─ DTO Request
│   │   │   │   ├── UpdateStreetRequest.java            #    └─ DTO Request
│   │   │   │   └── StreetResponse.java                 #    └─ DTO Response
│   │   │   ├── fare/
│   │   │   │   ├── CreateFareRequest.java              #    └─ DTO Request
│   │   │   │   ├── UpdateFareRequest.java              #    └─ DTO Request
│   │   │   │   └── FareResponse.java                   #    └─ DTO Response
│   │   │   └── parameter/
│   │   │       ├── CreateParameterRequest.java         #    └─ DTO Request
│   │   │       ├── UpdateParameterRequest.java         #    └─ DTO Request
│   │   │       └── ParameterResponse.java              #    └─ DTO Response
│   │   ├── mappers/                                    # 🗺️ Mappers (Conversión)
│   │   │   ├── OrganizationMapper.java                 #    └─ @Component
│   │   │   ├── ZoneMapper.java                         #    └─ @Component
│   │   │   ├── StreetMapper.java                       #    └─ @Component
│   │   │   ├── FareMapper.java                         #    └─ @Component
│   │   │   └── ParameterMapper.java                    #    └─ @Component
│   │   └── events/                                     # 📨 Eventos de dominio
│   │       ├── organization/
│   │       │   ├── OrganizationCreatedEvent.java       #    └─ Record/Class
│   │       │   ├── OrganizationUpdatedEvent.java       #    └─ Record/Class
│   │       │   ├── OrganizationDeletedEvent.java       #    └─ Record/Class
│   │       │   └── OrganizationRestoredEvent.java      #    └─ Record/Class
│   │       ├── zone/
│   │       │   ├── ZoneCreatedEvent.java               #    └─ Record/Class
│   │       │   ├── ZoneUpdatedEvent.java               #    └─ Record/Class
│   │       │   ├── ZoneDeletedEvent.java               #    └─ Record/Class
│   │       │   └── ZoneRestoredEvent.java              #    └─ Record/Class
│   │       ├── street/
│   │       │   ├── StreetCreatedEvent.java             #    └─ Record/Class
│   │       │   ├── StreetUpdatedEvent.java             #    └─ Record/Class
│   │       │   ├── StreetDeletedEvent.java             #    └─ Record/Class
│   │       │   └── StreetRestoredEvent.java            #    └─ Record/Class
│   │       ├── fare/
│   │       │   ├── FareCreatedEvent.java               #    └─ Record/Class
│   │       │   ├── FareUpdatedEvent.java               #    └─ Record/Class
│   │       │   ├── FareDeletedEvent.java               #    └─ Record/Class
│   │       │   └── FareRestoredEvent.java              #    └─ Record/Class
│   │       └── parameter/
│   │           ├── ParameterCreatedEvent.java          #    └─ Record/Class
│   │           ├── ParameterUpdatedEvent.java          #    └─ Record/Class
│   │           ├── ParameterDeletedEvent.java          #    └─ Record/Class
│   │           └── ParameterRestoredEvent.java         #    └─ Record/Class
│   │
│   └── infrastructure/                                 # 🔌 CAPA DE INFRAESTRUCTURA
│       ├── adapters/                                   # 🔄 ADAPTADORES (Arquitectura Hexagonal)
│       │   ├── in/                                     #    └─ Adaptadores de ENTRADA
│       │   │   └── rest/                               #       └─ Controladores REST
│       │   │       ├── OrganizationRest.java           #          └─ @RestController
│       │   │       ├── ZoneRest.java                   #          └─ @RestController
│       │   │       ├── StreetRest.java                 #          └─ @RestController
│       │   │       ├── FareRest.java                   #          └─ @RestController
│       │   │       ├── ParameterRest.java              #          └─ @RestController
│       │   │       └── GlobalExceptionHandler.java     #          └─ @RestControllerAdvice
│       │   └── out/                                    #    └─ Adaptadores de SALIDA
│       │       ├── persistence/                        #       └─ Implementaciones Repository
│       │       │   ├── OrganizationRepositoryImpl.java #          └─ @Repository (impl IOrganizationRepository)
│       │       │   ├── ZoneRepositoryImpl.java         #          └─ @Repository (impl IZoneRepository)
│       │       │   ├── StreetRepositoryImpl.java       #          └─ @Repository (impl IStreetRepository)
│       │       │   ├── FareRepositoryImpl.java         #          └─ @Repository (impl IFareRepository)
│       │       │   └── ParameterRepositoryImpl.java    #          └─ @Repository (impl IParameterRepository)
│       │       └── messaging/                          #       └─ Implementaciones EventPublisher
│       │           ├── OrganizationEventPublisherImpl.java #      └─ @Component (impl IOrganizationEventPublisher)
│       │           ├── ZoneEventPublisherImpl.java     #          └─ @Component (impl IZoneEventPublisher)
│       │           ├── StreetEventPublisherImpl.java   #          └─ @Component (impl IStreetEventPublisher)
│       │           ├── FareEventPublisherImpl.java     #          └─ @Component (impl IFareEventPublisher)
│       │           └── ParameterEventPublisherImpl.java#          └─ @Component (impl IParameterEventPublisher)
│       │
│       ├── messaging/                                  # 📬 MESSAGING (Eventos externos)
│       │   └── listeners/                              #    └─ Listeners de eventos EXTERNOS
│       │       └── (vacío - organizations no escucha eventos externos)
│       │
│       ├── persistence/                                # 💾 PERSISTENCIA MongoDB
│       │   ├── documents/                              #    └─ Documentos MongoDB
│       │   │   ├── OrganizationDocument.java           #       └─ @Document(collection="organizations")
│       │   │   ├── ZoneDocument.java                   #       └─ @Document(collection="zones")
│       │   │   ├── StreetDocument.java                 #       └─ @Document(collection="streets")
│       │   │   ├── FareDocument.java                   #       └─ @Document(collection="fares")
│       │   │   └── ParameterDocument.java              #       └─ @Document(collection="parameters")
│       │   └── repositories/                           #    └─ Repositorios Reactivos Spring Data
│       │       ├── OrganizationMongoRepository.java    #       └─ Interface extends ReactiveMongoRepository
│       │       ├── ZoneMongoRepository.java            #       └─ Interface extends ReactiveMongoRepository
│       │       ├── StreetMongoRepository.java          #       └─ Interface extends ReactiveMongoRepository
│       │       ├── FareMongoRepository.java            #       └─ Interface extends ReactiveMongoRepository
│       │       └── ParameterMongoRepository.java       #       └─ Interface extends ReactiveMongoRepository
│       │
│       ├── security/                                   # 🔐 SEGURIDAD (Headers del Gateway)
│       │   ├── AuthenticatedUser.java                  #    └─ DTO del usuario autenticado
│       │   ├── GatewayHeadersExtractor.java            #    └─ @Component (extrae X-User-Id, X-Organization-Id, X-Roles)
│       │   ├── GatewayHeadersFilter.java               #    └─ @Component WebFilter (almacena en Reactor Context)
│       │   └── SecurityContextAdapter.java             #    └─ @Component (impl ISecurityContext)
│       │
│       └── config/                                     # ⚙️ CONFIGURACIONES
│           ├── MongoConfig.java                        #    └─ @Configuration (MongoDB Reactive)
│           ├── RabbitMQConfig.java                     #    └─ @Configuration (Exchange, Queues, Bindings)
│           ├── SecurityConfig.java                     #    └─ @Configuration (WebFlux Security - sin OAuth2)
│           └── RequestContextFilter.java               #    └─ @Component WebFilter (MDC para logging)
│
├── src/main/resources/                                 # 📁 Recursos
│   ├── application.yml                                 #    └─ Config base
│   ├── application-dev.yml                             #    └─ Config desarrollo
│   └── application-prod.yml                            #    └─ Config producción
├── src/test/java/                                      # 🧪 Tests
├── pom.xml                                             # 📦 Maven config
├── Dockerfile                                          # 🐳 Docker config
└── docker-compose.yml                                  # 🐳 Docker Compose
```

---

## 🛠️ Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 LTS | Lenguaje base |
| Spring Boot | 3.5.10 | Framework principal |
| Spring WebFlux | 3.5.10 | API reactiva |
| Spring Data MongoDB Reactive | 3.5.10 | Persistencia reactiva |
| MongoDB | 7.x | Base de datos documental |
| RabbitMQ | 3.13.x | Mensajería asíncrona |
| SpringDoc OpenAPI | 2.3.0 | Documentación API |
| Lombok | 1.18.x | Reducción de boilerplate |
| Spring Security | 3.5.10 | Seguridad WebFlux |
| Spring Actuator | 3.5.10 | Health checks y métricas |

---

## 🔌 Dependencias Externas

| Dependencia | Tipo | Descripción |
|-------------|------|-------------|
| MongoDB | Base de datos | Almacenamiento de documentos |
| RabbitMQ | Mensajería | Publicación de eventos |

> **📌 NOTA:** Este microservicio **NO** llama a otros microservicios vía WebClient. Solo publica eventos a RabbitMQ. Por tanto, **NO** necesita Resilience4j ni WebClient.

---

## 📡 Eventos RabbitMQ

| Routing Key | Evento | Descripción |
|-------------|--------|-------------|
| `organization.created` | OrganizationCreatedEvent | Organización creada |
| `organization.updated` | OrganizationUpdatedEvent | Organización actualizada |
| `organization.deleted` | OrganizationDeletedEvent | Organización eliminada (soft) |
| `organization.restored` | OrganizationRestoredEvent | Organización restaurada |
| `zone.created` | ZoneCreatedEvent | Zona creada |
| `zone.updated` | ZoneUpdatedEvent | Zona actualizada |
| `zone.deleted` | ZoneDeletedEvent | Zona eliminada (soft) |
| `zone.restored` | ZoneRestoredEvent | Zona restaurada |
| `street.created` | StreetCreatedEvent | Calle creada |
| `street.updated` | StreetUpdatedEvent | Calle actualizada |
| `street.deleted` | StreetDeletedEvent | Calle eliminada (soft) |
| `street.restored` | StreetRestoredEvent | Calle restaurada |
| `fare.created` | FareCreatedEvent | Tarifa creada |
| `fare.updated` | FareUpdatedEvent | Tarifa actualizada |
| `fare.deleted` | FareDeletedEvent | Tarifa eliminada (soft) |
| `fare.restored` | FareRestoredEvent | Tarifa restaurada |
| `parameter.created` | ParameterCreatedEvent | Parámetro creado |
| `parameter.updated` | ParameterUpdatedEvent | Parámetro actualizado |
| `parameter.deleted` | ParameterDeletedEvent | Parámetro eliminado (soft) |
| `parameter.restored` | ParameterRestoredEvent | Parámetro restaurado |

> Exchange compartido: `jass.events` (Topic Exchange)

---

## 🌐 Endpoints

### Organizations `/api/v1/organizations`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/organizations` | Crear organización |
| GET | `/organizations/{id}` | Obtener por ID |
| GET | `/organizations` | Listar activas |
| GET | `/organizations/all` | Listar todas (incluye inactivas) |
| PUT | `/organizations/{id}` | Actualizar |
| DELETE | `/organizations/{id}` | Eliminar (soft delete) |
| PATCH | `/organizations/{id}/restore` | Restaurar |

### Zones `/api/v1/zones`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/zones` | Crear zona |
| GET | `/zones/{id}` | Obtener por ID |
| GET | `/zones` | Listar activas |
| GET | `/zones/all` | Listar todas |
| GET | `/zones/organization/{organizationId}` | Listar por organización |
| PUT | `/zones/{id}` | Actualizar |
| DELETE | `/zones/{id}` | Eliminar (soft delete) |
| PATCH | `/zones/{id}/restore` | Restaurar |

### Streets `/api/v1/streets`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/streets` | Crear calle |
| GET | `/streets/{id}` | Obtener por ID |
| GET | `/streets` | Listar activas |
| GET | `/streets/all` | Listar todas |
| GET | `/streets/zone/{zoneId}` | Listar por zona |
| PUT | `/streets/{id}` | Actualizar |
| DELETE | `/streets/{id}` | Eliminar (soft delete) |
| PATCH | `/streets/{id}/restore` | Restaurar |

### Fares `/api/v1/fares`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/fares` | Crear tarifa |
| GET | `/fares/{id}` | Obtener por ID |
| GET | `/fares` | Listar activas |
| GET | `/fares/all` | Listar todas |
| GET | `/fares/organization/{organizationId}` | Listar por organización |
| PUT | `/fares/{id}` | Actualizar |
| DELETE | `/fares/{id}` | Eliminar (soft delete) |
| PATCH | `/fares/{id}/restore` | Restaurar |

### Parameters `/api/v1/parameters`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/parameters` | Crear parámetro |
| GET | `/parameters/{id}` | Obtener por ID |
| GET | `/parameters` | Listar activos |
| GET | `/parameters/all` | Listar todos |
| GET | `/parameters/organization/{organizationId}` | Listar por organización |
| PUT | `/parameters/{id}` | Actualizar |
| DELETE | `/parameters/{id}` | Eliminar (soft delete) |
| PATCH | `/parameters/{id}/restore` | Restaurar |

---

## 📚 Documentación por Capas

| Capa | Archivo | Contenido |
|------|---------|-----------|
| Domain | [README_DOMAIN.md](README_DOMAIN.md) | Modelos, Value Objects, Ports, Exceptions |
| Application | [README_APPLICATION.md](README_APPLICATION.md) | Use Cases, DTOs, Mappers, Events |
| Infrastructure | [README_INFRASTRUCTURE.md](README_INFRASTRUCTURE.md) | REST, MongoDB, RabbitMQ, Config |

---

## 🎯 Principios SOLID Aplicados

| Principio | Aplicación |
|-----------|-----------|
| **S** - Single Responsibility | Cada use case tiene una única responsabilidad |
| **O** - Open/Closed | Nuevas entidades se agregan sin modificar existentes |
| **L** - Liskov Substitution | Interfaces de puertos permiten cambiar implementaciones |
| **I** - Interface Segregation | Puertos separados por entidad y dirección (in/out) |
| **D** - Dependency Inversion | Dominio depende de abstracciones, no de MongoDB ni RabbitMQ |

---

## � Entorno Local (Docker)

Para ejecutar las dependencias necesarias de este microservicio localmente:

### MongoDB

```bash
docker run -d \
  --name mongo_jass \
  -p 27017:27017 \
  -v jass_mongo_data:/data/db \
  mongo:latest
```

### RabbitMQ (con Management UI)

```bash
docker run -d \
  --name rabbit_jass \
  -p 5672:5672 \
  -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=guest \
  -e RABBITMQ_DEFAULT_PASS=guest \
  rabbitmq:3-management
```

---

## �🚀 Ejecución

```bash
# Desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Producción
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Docker
docker build -t vg-ms-organizations .
docker run -p 8083:8083 vg-ms-organizations
```

---

## 📊 Resumen

| Capa | Clases | Descripción |
|------|--------|-------------|
| **Domain** | 26 | 5 modelos, 4 value objects, 10 ports out, 25 ports in, 11 exceptions |
| **Application** | 50 | 25 use cases, 10 DTOs request, 5 DTOs response, 3 DTOs common, 5 mappers, 20 events |
| **Infrastructure** | 26 | 6 REST controllers, 5 repository impls, 5 event publishers, 5 documents, 5 mongo repos, 4 configs |
| **TOTAL** | **~100** | Microservicio completo |
