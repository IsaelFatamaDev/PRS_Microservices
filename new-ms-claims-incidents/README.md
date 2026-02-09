# 📋 VG-MS-CLAIMS-INCIDENTS

> **Microservicio de Reclamos de Clientes y Reporte de Averías/Incidentes del sistema JASS Digital.**

## 📐 Arquitectura Hexagonal

```
┌─────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE                           │
│  ┌──────────────┐  ┌──────────────┐  ┌───────────────────────┐ │
│  │  REST APIs   │  │   MongoDB    │  │      RabbitMQ         │ │
│  │ (Adapters IN)│  │(Adapters OUT)│  │   (Adapters OUT)      │ │
│  └──────┬───────┘  └──────┬───────┘  └───────────┬───────────┘ │
│         │                 │                      │              │
│  ┌──────┼─────────────────┼──────────────────────┼───────────┐ │
│  │      │          EXTERNAL CLIENTS              │           │ │
│  │  ┌───▼──────────┐  ┌──────────────────────┐   │           │ │
│  │  │ vg-ms-users  │  │ vg-ms-infrastructure │   │           │ │
│  │  │ (WebClient)  │  │    (WebClient)       │   │           │ │
│  │  └──────────────┘  └──────────────────────┘   │           │ │
│  └───────┼─────────────────┼─────────────────────┼───────────┘ │
│          │                 │                     │              │
│  ┌───────▼─────────────────▼─────────────────────▼───────────┐ │
│  │                    PORTS (Interfaces)                      │ │
│  │  ┌─────────────────┐            ┌──────────────────────┐  │ │
│  │  │    Ports IN      │            │     Ports OUT        │  │ │
│  │  │  (Use Cases)     │            │  (Repo, Event, Client│  │ │
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
│  │  │   Models     │  │ Value Objects│  │  Exceptions      │  │ │
│  │  │ + Services   │  │              │  │                  │  │ │
│  │  └─────────────┘  └──────────────┘  └──────────────────┘  │ │
│  └────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

---

## 📂 Estructura del Proyecto

```
vg-ms-claims-incidents/
├── src/main/java/pe/edu/vallegrande/vgmsclaims/
│   ├── VgMsClaimsIncidentsApplication.java            # 🚀 Main Class
│   │
│   ├── domain/                                         # 🧩 CAPA DE DOMINIO
│   │   ├── models/                                     # 📦 Modelos de negocio
│   │   │   ├── Complaint.java                          #    └─ Model
│   │   │   ├── ComplaintCategory.java                  #    └─ Model
│   │   │   ├── ComplaintResponse.java                  #    └─ Model
│   │   │   ├── Incident.java                           #    └─ Model
│   │   │   ├── IncidentType.java                       #    └─ Model
│   │   │   ├── IncidentResolution.java                 #    └─ Model
│   │   │   └── valueobjects/                           # 🏷️ Value Objects (Enums)
│   │   │       ├── RecordStatus.java                   #    └─ Enum
│   │   │       ├── ComplaintPriority.java              #    └─ Enum
│   │   │       ├── ComplaintStatus.java                #    └─ Enum
│   │   │       ├── ResponseType.java                   #    └─ Enum
│   │   │       ├── IncidentSeverity.java               #    └─ Enum
│   │   │       ├── IncidentStatus.java                 #    └─ Enum
│   │   │       ├── ResolutionType.java                 #    └─ Enum
│   │   │       └── MaterialUsed.java                   #    └─ Value Object (Embedded)
│   │   ├── ports/                                      # 🔌 Puertos (Interfaces)
│   │   │   ├── in/                                     # ⬅️ Puertos de ENTRADA
│   │   │   │   ├── complaint/
│   │   │   │   │   ├── ICreateComplaintUseCase.java    #    └─ Interface
│   │   │   │   │   ├── IGetComplaintUseCase.java       #    └─ Interface
│   │   │   │   │   ├── IUpdateComplaintUseCase.java    #    └─ Interface
│   │   │   │   │   ├── IDeleteComplaintUseCase.java    #    └─ Interface
│   │   │   │   │   ├── IRestoreComplaintUseCase.java   #    └─ Interface
│   │   │   │   │   ├── IAddResponseUseCase.java        #    └─ Interface
│   │   │   │   │   └── ICloseComplaintUseCase.java     #    └─ Interface
│   │   │   │   ├── complaint-category/
│   │   │   │   │   ├── ICreateComplaintCategoryUseCase.java
│   │   │   │   │   ├── IGetComplaintCategoryUseCase.java
│   │   │   │   │   ├── IUpdateComplaintCategoryUseCase.java
│   │   │   │   │   ├── IDeleteComplaintCategoryUseCase.java
│   │   │   │   │   └── IRestoreComplaintCategoryUseCase.java
│   │   │   │   ├── incident/
│   │   │   │   │   ├── ICreateIncidentUseCase.java     #    └─ Interface
│   │   │   │   │   ├── IGetIncidentUseCase.java        #    └─ Interface
│   │   │   │   │   ├── IUpdateIncidentUseCase.java     #    └─ Interface
│   │   │   │   │   ├── IDeleteIncidentUseCase.java     #    └─ Interface
│   │   │   │   │   ├── IRestoreIncidentUseCase.java    #    └─ Interface
│   │   │   │   │   ├── IAssignIncidentUseCase.java     #    └─ Interface
│   │   │   │   │   ├── IResolveIncidentUseCase.java    #    └─ Interface
│   │   │   │   │   └── ICloseIncidentUseCase.java      #    └─ Interface
│   │   │   │   └── incident-type/
│   │   │   │       ├── ICreateIncidentTypeUseCase.java
│   │   │   │       ├── IGetIncidentTypeUseCase.java
│   │   │   │       ├── IUpdateIncidentTypeUseCase.java
│   │   │   │       ├── IDeleteIncidentTypeUseCase.java
│   │   │   │       └── IRestoreIncidentTypeUseCase.java
│   │   │   └── out/                                    # ➡️ Puertos de SALIDA
│   │   │       ├── IComplaintRepository.java           #    └─ Interface (Repository)
│   │   │       ├── IComplaintCategoryRepository.java   #    └─ Interface (Repository)
│   │   │       ├── IComplaintResponseRepository.java   #    └─ Interface (Repository)
│   │   │       ├── IIncidentRepository.java            #    └─ Interface (Repository)
│   │   │       ├── IIncidentTypeRepository.java        #    └─ Interface (Repository)
│   │   │       ├── IIncidentResolutionRepository.java  #    └─ Interface (Repository)
│   │   │       ├── IClaimsEventPublisher.java          #    └─ Interface (Event)
│   │   │       ├── IUserServiceClient.java             #    └─ Interface (WebClient)
│   │   │       ├── IInfrastructureClient.java          #    └─ Interface (WebClient)
│   │   │       └── ISecurityContext.java               #    └─ Interface (Security)
│   │   ├── services/                                   # 🔧 Domain Services
│   │   │   └── ClaimsAuthorizationService.java         #    └─ Reglas de autorización
│   │   └── exceptions/                                 # ❌ Excepciones de dominio
│   │       ├── base/
│   │       │   ├── DomainException.java                #       └─ Exception (Base)
│   │       │   ├── NotFoundException.java              #       └─ Exception
│   │       │   ├── BusinessRuleException.java          #       └─ Exception
│   │       │   ├── ValidationException.java            #       └─ Exception
│   │       │   └── ConflictException.java              #       └─ Exception
│   │       └── specific/
│   │           ├── ComplaintNotFoundException.java      #       └─ Exception
│   │           ├── IncidentNotFoundException.java       #       └─ Exception
│   │           ├── ComplaintAlreadyClosedException.java #       └─ Exception
│   │           ├── IncidentAlreadyResolvedException.java#       └─ Exception
│   │           ├── InvalidTransitionException.java     #       └─ Exception
│   │           └── UnauthorizedAssignmentException.java#       └─ Exception
│   │
│   ├── application/                                    # ⚙️ CAPA DE APLICACIÓN
│   │   ├── usecases/
│   │   │   ├── complaint/
│   │   │   │   ├── CreateComplaintUseCaseImpl.java     #    └─ @Service
│   │   │   │   ├── GetComplaintUseCaseImpl.java        #    └─ @Service
│   │   │   │   ├── UpdateComplaintUseCaseImpl.java     #    └─ @Service
│   │   │   │   ├── DeleteComplaintUseCaseImpl.java     #    └─ @Service
│   │   │   │   ├── RestoreComplaintUseCaseImpl.java    #    └─ @Service
│   │   │   │   ├── AddResponseUseCaseImpl.java         #    └─ @Service
│   │   │   │   └── CloseComplaintUseCaseImpl.java      #    └─ @Service
│   │   │   ├── complaint-category/
│   │   │   │   ├── CreateComplaintCategoryUseCaseImpl.java
│   │   │   │   ├── GetComplaintCategoryUseCaseImpl.java
│   │   │   │   ├── UpdateComplaintCategoryUseCaseImpl.java
│   │   │   │   ├── DeleteComplaintCategoryUseCaseImpl.java
│   │   │   │   └── RestoreComplaintCategoryUseCaseImpl.java
│   │   │   ├── incident/
│   │   │   │   ├── CreateIncidentUseCaseImpl.java      #    └─ @Service
│   │   │   │   ├── GetIncidentUseCaseImpl.java         #    └─ @Service
│   │   │   │   ├── UpdateIncidentUseCaseImpl.java      #    └─ @Service
│   │   │   │   ├── DeleteIncidentUseCaseImpl.java      #    └─ @Service
│   │   │   │   ├── RestoreIncidentUseCaseImpl.java     #    └─ @Service
│   │   │   │   ├── AssignIncidentUseCaseImpl.java      #    └─ @Service
│   │   │   │   ├── ResolveIncidentUseCaseImpl.java     #    └─ @Service
│   │   │   │   └── CloseIncidentUseCaseImpl.java       #    └─ @Service
│   │   │   └── incident-type/
│   │   │       ├── CreateIncidentTypeUseCaseImpl.java
│   │   │       ├── GetIncidentTypeUseCaseImpl.java
│   │   │       ├── UpdateIncidentTypeUseCaseImpl.java
│   │   │       ├── DeleteIncidentTypeUseCaseImpl.java
│   │   │       └── RestoreIncidentTypeUseCaseImpl.java
│   │   ├── dto/
│   │   │   ├── common/
│   │   │   │   ├── ApiResponse.java                    #       └─ Record
│   │   │   │   ├── PageResponse.java                   #       └─ Record
│   │   │   │   └── ErrorMessage.java                   #       └─ Record
│   │   │   ├── complaint/
│   │   │   │   ├── CreateComplaintRequest.java         #    └─ Record
│   │   │   │   ├── UpdateComplaintRequest.java         #    └─ Record
│   │   │   │   ├── AddResponseRequest.java             #    └─ Record
│   │   │   │   ├── ComplaintResponse.java              #    └─ Record
│   │   │   │   └── ComplaintDetailResponse.java        #    └─ Record
│   │   │   ├── complaint-category/
│   │   │   │   ├── CreateComplaintCategoryRequest.java #    └─ Record
│   │   │   │   ├── UpdateComplaintCategoryRequest.java #    └─ Record
│   │   │   │   └── ComplaintCategoryResponse.java      #    └─ Record
│   │   │   ├── incident/
│   │   │   │   ├── CreateIncidentRequest.java          #    └─ Record
│   │   │   │   ├── UpdateIncidentRequest.java          #    └─ Record
│   │   │   │   ├── AssignIncidentRequest.java          #    └─ Record
│   │   │   │   ├── ResolveIncidentRequest.java         #    └─ Record
│   │   │   │   ├── IncidentResponse.java               #    └─ Record
│   │   │   │   └── IncidentDetailResponse.java         #    └─ Record
│   │   │   └── incident-type/
│   │   │       ├── CreateIncidentTypeRequest.java      #    └─ Record
│   │   │       ├── UpdateIncidentTypeRequest.java      #    └─ Record
│   │   │       └── IncidentTypeResponse.java           #    └─ Record
│   │   ├── mappers/
│   │   │   ├── ComplaintMapper.java                    #    └─ @Component
│   │   │   └── IncidentMapper.java                     #    └─ @Component
│   │   └── events/
│   │       ├── complaint/
│   │       │   ├── ComplaintCreatedEvent.java           #    └─ Record
│   │       │   ├── ComplaintUpdatedEvent.java           #    └─ Record
│   │       │   ├── ComplaintClosedEvent.java            #    └─ Record
│   │       │   └── ComplaintResponseAddedEvent.java     #    └─ Record
│   │       └── incident/
│   │           ├── IncidentCreatedEvent.java            #    └─ Record
│   │           ├── IncidentAssignedEvent.java           #    └─ Record
│   │           ├── IncidentUpdatedEvent.java            #    └─ Record
│   │           ├── IncidentResolvedEvent.java           #    └─ Record
│   │           ├── IncidentClosedEvent.java             #    └─ Record
│   │           └── UrgentIncidentAlertEvent.java        #    └─ Record
│   │
│   └── infrastructure/                                 # 🔌 CAPA DE INFRAESTRUCTURA
│       ├── adapters/
│       │   ├── in/rest/
│       │   │   ├── ComplaintRest.java                  #    └─ @RestController
│       │   │   ├── IncidentRest.java                   #    └─ @RestController
│       │   │   └── GlobalExceptionHandler.java         #    └─ @RestControllerAdvice
│       │   └── out/
│       │       ├── persistence/
│       │       │   ├── ComplaintRepositoryImpl.java     #    └─ @Repository
│       │       │   └── IncidentRepositoryImpl.java     #    └─ @Repository
│       │       ├── messaging/
│       │       │   └── ClaimsEventPublisherImpl.java   #    └─ @Component
│       │       └── external/
│       │           ├── UserServiceClientImpl.java      #    └─ @Component (WebClient)
│       │           └── InfrastructureClientImpl.java   #    └─ @Component (WebClient)
│       ├── persistence/
│       │   ├── documents/
│       │   │   ├── ComplaintDocument.java              #    └─ @Document(collection="complaints")
│       │   │   ├── ComplaintCategoryDocument.java      #    └─ @Document(collection="complaint_categories")
│       │   │   ├── ComplaintResponseDocument.java      #    └─ @Document(collection="complaint_responses")
│       │   │   ├── IncidentDocument.java               #    └─ @Document(collection="incidents")
│       │   │   ├── IncidentTypeDocument.java           #    └─ @Document(collection="incident_types")
│       │   │   ├── IncidentResolutionDocument.java     #    └─ @Document(collection="incident_resolutions")
│       │   │   └── MaterialUsedEmbedded.java           #    └─ Embedded Document
│       │   └── repositories/
│       │       ├── ComplaintMongoRepository.java        #    └─ ReactiveMongoRepository
│       │       ├── ComplaintCategoryMongoRepository.java#    └─ ReactiveMongoRepository
│       │       ├── ComplaintResponseMongoRepository.java#    └─ ReactiveMongoRepository
│       │       ├── IncidentMongoRepository.java         #    └─ ReactiveMongoRepository
│       │       ├── IncidentTypeMongoRepository.java     #    └─ ReactiveMongoRepository
│       │       └── IncidentResolutionMongoRepository.java # └─ ReactiveMongoRepository
│       ├── security/
│       │   ├── AuthenticatedUser.java
│       │   ├── GatewayHeadersExtractor.java
│       │   ├── GatewayHeadersFilter.java
│       │   └── SecurityContextAdapter.java
│       └── config/
│           ├── MongoConfig.java
│           ├── RabbitMQConfig.java
│           ├── SecurityConfig.java
│           ├── WebClientConfig.java
│           ├── Resilience4jConfig.java
│           └── RequestContextFilter.java
│
├── src/main/resources/
│   ├── application.yml
│   ├── application-dev.yml
│   └── application-prod.yml
├── pom.xml
├── Dockerfile
└── docker-compose.yml
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
| RabbitMQ | 3.13.x | Mensajería asíncrona (publicación) |
| SpringDoc OpenAPI | 2.3.0 | Documentación API |
| Lombok | 1.18.x | Reducción de boilerplate |
| Spring Security | 3.5.10 | Seguridad WebFlux |
| Spring Actuator | 3.5.10 | Health checks y métricas |
| Resilience4j | 2.x | Circuit Breaker para WebClient |
| WebClient | 3.5.10 | Llamadas a otros microservicios |

---

## 🔌 Dependencias Externas

| Dependencia | Tipo | Descripción |
|-------------|------|-------------|
| MongoDB | Base de datos | Almacenamiento de documentos |
| RabbitMQ | Mensajería | Publicación de eventos |
| vg-ms-users (8081) | WebClient | Validar existencia de usuarios |
| vg-ms-infrastructure (8089) | WebClient | Consultar zonas/materiales |

> **📌 NOTA:** Este microservicio **SÍ** llama a otros microservicios vía WebClient. Por tanto, **necesita** Resilience4j y WebClientConfig.

---

## 📡 Eventos RabbitMQ

> Exchange compartido: `jass.events` (Topic Exchange)

### Eventos Publicados

| Routing Key | Evento | Descripción |
|-------------|--------|-------------|
| `complaint.created` | ComplaintCreatedEvent | Reclamo registrado |
| `complaint.updated` | ComplaintUpdatedEvent | Reclamo actualizado |
| `complaint.closed` | ComplaintClosedEvent | Reclamo cerrado |
| `complaint.response.added` | ComplaintResponseAddedEvent | Respuesta agregada al reclamo |
| `incident.created` | IncidentCreatedEvent | Incidente reportado |
| `incident.assigned` | IncidentAssignedEvent | Incidente asignado a técnico |
| `incident.updated` | IncidentUpdatedEvent | Incidente actualizado |
| `incident.resolved` | IncidentResolvedEvent | Incidente resuelto |
| `incident.closed` | IncidentClosedEvent | Incidente cerrado |
| `incident.urgent.alert` | UrgentIncidentAlertEvent | Alerta de incidente CRITICAL |

### Eventos Escuchados

> **Este microservicio NO escucha eventos externos.** Solo publica eventos.

---

## 🌐 Endpoints

### Complaints `/api/v1/complaints`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/complaints` | Crear reclamo |
| GET | `/complaints/{id}` | Obtener por ID |
| GET | `/complaints/{id}/detail` | Obtener con respuestas |
| GET | `/complaints` | Listar activos (filtros: status, userId, categoryId, priority, dateFrom) |
| GET | `/complaints/all` | Listar todos |
| PUT | `/complaints/{id}` | Actualizar |
| DELETE | `/complaints/{id}` | Eliminar (soft delete) |
| PATCH | `/complaints/{id}/restore` | Restaurar |
| POST | `/complaints/{id}/responses` | Agregar respuesta |
| PATCH | `/complaints/{id}/close` | Cerrar reclamo |

### Complaint Categories `/api/v1/complaint-categories`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/complaint-categories` | Crear categoría |
| GET | `/complaint-categories/{id}` | Obtener por ID |
| GET | `/complaint-categories` | Listar activas |
| GET | `/complaint-categories/all` | Listar todas |
| PUT | `/complaint-categories/{id}` | Actualizar |
| DELETE | `/complaint-categories/{id}` | Eliminar (soft delete) |
| PATCH | `/complaint-categories/{id}/restore` | Restaurar |

### Incidents `/api/v1/incidents`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/incidents` | Crear incidente |
| GET | `/incidents/{id}` | Obtener por ID |
| GET | `/incidents/{id}/detail` | Obtener con resolución |
| GET | `/incidents` | Listar activos (filtros: status, typeId, severity, assignedTo) |
| GET | `/incidents/all` | Listar todos |
| PUT | `/incidents/{id}` | Actualizar |
| DELETE | `/incidents/{id}` | Eliminar (soft delete) |
| PATCH | `/incidents/{id}/restore` | Restaurar |
| PATCH | `/incidents/{id}/assign` | Asignar técnico |
| PATCH | `/incidents/{id}/resolve` | Resolver incidente |
| PATCH | `/incidents/{id}/close` | Cerrar incidente |

### Incident Types `/api/v1/incident-types`

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/incident-types` | Crear tipo |
| GET | `/incident-types/{id}` | Obtener por ID |
| GET | `/incident-types` | Listar activos |
| GET | `/incident-types/all` | Listar todos |
| PUT | `/incident-types/{id}` | Actualizar |
| DELETE | `/incident-types/{id}` | Eliminar (soft delete) |
| PATCH | `/incident-types/{id}/restore` | Restaurar |

---

## 📚 Documentación por Capas

| Capa | Archivo | Contenido |
|------|---------|-----------|
| Domain | [README_DOMAIN.md](README_DOMAIN.md) | Modelos, Value Objects, Ports, Services, Exceptions |
| Application | [README_APPLICATION.md](README_APPLICATION.md) | Use Cases, DTOs, Mappers, Events |
| Infrastructure | [README_INFRASTRUCTURE.md](README_INFRASTRUCTURE.md) | REST, MongoDB, RabbitMQ, WebClient, Config |

---

## 🎯 Principios SOLID Aplicados

| Principio | Aplicación |
|-----------|-----------|
| **S** - Single Responsibility | Cada use case tiene una única responsabilidad |
| **O** - Open/Closed | Nuevos tipos de incidente/reclamo no modifican el código existente |
| **L** - Liskov Substitution | Interfaces de puertos permiten cambiar implementaciones |
| **I** - Interface Segregation | Puertos separados por entidad y dirección (in/out) |
| **D** - Dependency Inversion | Dominio depende de abstracciones, no de MongoDB, RabbitMQ ni WebClient |

---

## 🐳 Entorno Local (Docker)

### MongoDB

```bash
docker run -d \
  --name mongo_jass \
  -p 27017:27017 \
  -v jass_mongo_data:/data/db \
  mongo:latest
```

### RabbitMQ

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

## 🚀 Ejecución

```bash
# Desarrollo
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Producción
mvn spring-boot:run -Dspring-boot.run.profiles=prod

# Docker
docker build -t vg-ms-claims-incidents .
docker run -p 8088:8088 vg-ms-claims-incidents
```

---

## 📊 Resumen

| Capa | Clases | Descripción |
|------|--------|-------------|
| **Domain** | ~35 | 6 modelos, 8 value objects, 10 ports out, 25 ports in, 11 exceptions, 1 domain service |
| **Application** | ~55 | 25 use cases, 14 DTOs request, 8 DTOs response, 3 DTOs common, 2 mappers, 10 events |
| **Infrastructure** | ~30 | 3 REST controllers, 2 repository impls, 1 event publisher, 6 documents, 6 mongo repos, 2 external clients, 6 configs |
| **TOTAL** | **~120** | Microservicio completo |
