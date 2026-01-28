# vg-ms-organizations

Microservicio de Organizaciones JASS con MongoDB Reactive y Domain Events.

## Arquitectura

- **Patrón**: Hexagonal + DDD + SOLID + Domain Events
- **Stack**: Spring Boot 3.2 WebFlux + MongoDB Reactive + RabbitMQ
- **Puerto**: 8082

## Estructura

```
vg-ms-organizations/
├── domain/
│   ├── model/              # Aggregates: Organization, Zone, Street
│   ├── valueobject/        # Value Objects
│   ├── event/              # Domain Events (7 eventos)
│   ├── exception/          # Domain Exceptions
│   └── ports/
│       ├── in/             # Use Case Interfaces
│       └── out/            # Repository Interfaces + IDomainEventPublisher
├── application/
│   ├── usecases/           # Use Case Implementations
│   ├── dto/                # Request/Response DTOs
│   └── mapper/             # DTO ↔ Domain Mappers
└── infrastructure/
    ├── adapter/
    │   ├── persistence/    # Repository Implementations
    │   ├── messaging/      # DomainEventPublisherImpl (RabbitMQ)
    │   └── client/         # UserServiceClientImpl
    ├── rest/               # REST Controllers
    ├── config/             # Configurations
    └── exception/          # GlobalExceptionHandler
```

## Eventos de Dominio

1. **OrganizationCreated** → `organization.created`
2. **OrganizationUpdated** → `organization.updated`
3. **OrganizationDeleted** → `organization.deleted`
4. **ZoneCreated** → `zone.created`
5. **ZoneUpdated** → `zone.updated`
6. **ZoneFeeChanged** → `zone.fee.changed`
7. **StreetCreated** → `street.created`

## Endpoints

### Organizaciones

- `POST /api/organizations` - Crear organización
- `GET /api/organizations` - Listar todas
- `GET /api/organizations/{id}` - Buscar por ID
- `GET /api/organizations/ruc/{ruc}` - Buscar por RUC
- `GET /api/organizations/status/{status}` - Filtrar por status
- `GET /api/organizations/region/{region}` - Filtrar por región
- `PUT /api/organizations/{id}` - Actualizar
- `DELETE /api/organizations/{id}` - Soft delete
- `PATCH /api/organizations/{id}/restore` - Restaurar

### Zonas

- `POST /api/zones` - Crear zona
- `GET /api/zones` - Listar todas
- `GET /api/zones/{id}` - Buscar por ID
- `GET /api/zones/organization/{orgId}` - Zonas por organización
- `PATCH /api/zones/{id}/update-fee` - Actualizar tarifa

### Calles

- `POST /api/streets` - Crear calle
- `GET /api/streets` - Listar todas
- `GET /api/streets/{id}` - Buscar por ID
- `GET /api/streets/zone/{zoneId}` - Calles por zona

## Configuración

### MongoDB

```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/JASS_DIGITAL
```

### RabbitMQ

```yaml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### Dependencias Externas

- **vg-ms-users**: `http://localhost:8081`

## Ejecutar

```bash
# Dev
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Build
mvn clean package

# Docker
docker-compose up
```

## Colecciones MongoDB

- `organizations` - Organizaciones JASS
- `zones` - Zonas de distribución
- `streets` - Calles por zona
- `zone_fare_history` - Historial de cambios de tarifa

## Principios SOLID Aplicados

- **SRP**: Mappers separados (DTO↔Domain, Domain↔Document)
- **OCP**: Use Cases extensibles vía interfaces
- **LSP**: Implementaciones intercambiables
- **ISP**: Interfaces específicas por caso de uso
- **DIP**: Dependencias invertidas (ports/adapters)
