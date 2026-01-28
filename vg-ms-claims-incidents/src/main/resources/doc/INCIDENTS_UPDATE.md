# Actualización del Sistema de Incidencias - Versión Mejorada

## Cambios Realizados

Se ha actualizado completamente el sistema de incidencias para usar el formato de datos mejorado especificado.

### Nuevo Modelo de Datos Mejorado

El modelo `Incident` ahora incluye los siguientes campos:

```json
{
  "_id": ObjectId("665f123abc456def78901234"),
  "incident_code": "INC001",
  "organization_id": ObjectId("64abc123def4567890123456"),
  "incident_type": "FUGA_TUBERIA",
  "incident_type_group": "INFRAESTRUCTURA",
  "incident_date": ISODate("2023-06-05T08:30:00Z"),
  "reported_at": ISODate("2023-06-05T09:00:00Z"),
  "description": "Rotura de tubería principal en Calle Los Pinos",
  "severity": "HIGH",
  "zone_id": ObjectId("64def123abc4567890123456"),
  "affected_count": 25,
  "estimated_resolution_time": "24h",
  "resolution_date": ISODate("2023-06-05T10:15:00Z"),
  "reported_by_user_id": ObjectId("64abc789def1234567890123"),
  "resolved_by_user_id": ObjectId("64abc789def1234567890456"),
  "resolved": true,
  "status": "ACTIVE",
  "source_collection": "distribution_incidents",
  "linked_record_id": ObjectId("665e987def1234567890abcd")
}
```

### Campos del Nuevo Modelo Mejorado

| Campo                       | Tipo    | Descripción                                             |
|-----------------------------|---------|---------------------------------------------------------|
| `id`                        | String  | ID único del incidente                                  |
| `incident_code`             | String  | Código del incidente (ej: "INC001")                     |
| `organization_id`           | String  | ID de la organización                                   |
| `incident_type`             | String  | Tipo de incidente (FUGA_TUBERIA, CLORO_BAJO, etc.)      |
| `incident_type_group`       | String  | Grupo del tipo (INFRAESTRUCTURA, CALIDAD, DISTRIBUCION) |
| `incident_date`             | Instant | Fecha cuando ocurrió el incidente                       |
| `reported_at`               | Instant | Fecha cuando fue reportado                              |
| `description`               | String  | Descripción del incidente                               |
| `severity`                  | String  | Severidad (LOW, MEDIUM, HIGH, CRITICAL)                 |
| `zone_id`                   | String  | ID de la zona afectada                                  |
| `affected_count`            | Integer | Número de afectados                                     |
| `estimated_resolution_time` | String  | Tiempo estimado de resolución (24h, 48h, 72h...)        |
| `resolution_date`           | Instant | Fecha de resolución                                     |
| `reported_by_user_id`       | String  | ID del usuario que reportó                              |
| `resolved_by_user_id`       | String  | ID del usuario que resolvió                             |
| `resolved`                  | Boolean | Estado de resolución                                    |
| `status`                    | String  | Estado activo/inactivo (ACTIVE, INACTIVE)               |
| `source_collection`         | String  | Colección de origen                                     |
| `linked_record_id`          | String  | ID del registro original                                |

### Enums Creados

Se han creado los siguientes enums para validación:

- **IncidentType**: FUGA_TUBERIA, CLORO_BAJO, CORTE_SUMINISTRO, etc.
- **IncidentTypeGroup**: INFRAESTRUCTURA, CALIDAD, DISTRIBUCION
- **Severity**: LOW, MEDIUM, HIGH, CRITICAL
- **IncidentStatus**: ACTIVE, INACTIVE

### Nuevos Endpoints

Se han agregado los siguientes endpoints al controlador:

- `GET /api/v1/incidents/organization/{organizationId}` - Buscar por organización
- `GET /api/v1/incidents/type/{incidentType}` - Buscar por tipo de incidente
- `GET /api/v1/incidents/severity/{severity}` - Buscar por severidad
- `GET /api/v1/incidents/resolved/{resolved}` - Buscar por estado de resolución
- `GET /api/v1/incidents/status/{status}` - Buscar por estado activo/inactivo
- `PATCH /api/v1/incidents/{id}/resolve` - Resolver un incidente

### Métodos del Repositorio

Se han agregado los siguientes métodos al repositorio:

- `findByOrganizationId(String organizationId)`
- `findByIncidentType(String incidentType)`
- `findBySeverity(String severity)`
- `findByResolved(Boolean resolved)`
- `findByStatus(String status)`

### Valores por Defecto

El sistema establece automáticamente los siguientes valores por defecto al crear un incidente:

- `resolved`: false
- `status`: "ACTIVE"
- `reported_at`: Fecha y hora actual
- `estimated_resolution_time`: "24h"

### Datos de Prueba Mejorados

Se han incluido 3 incidentes de prueba en el `DataInitializer` con el nuevo formato:

1. **INC001**: Fuga de tubería (resuelto, 24h estimado)
2. **INC002**: Cloro bajo (pendiente, 48h estimado)
3. **INC003**: Corte de suministro (pendiente, 72h estimado)

### Configuración

Para habilitar la carga de datos de prueba, agregar en `application.yml`:

```yaml
app:
  data:
    initialize: true
```

## Archivos Modificados

1. `domain/models/Incident.java` - Modelo actualizado con nuevos campos
2. `infrastructure/dto/IncidentDTO.java` - DTO actualizado
3. `infrastructure/repository/IncidentRepository.java` - Nuevos métodos de búsqueda
4. `application/services/IncidentService.java` - Interfaz actualizada
5. `infrastructure/service/impl/IncidentServiceImpl.java` - Implementación actualizada
6. `infrastructure/rest/IncidentController.java` - Nuevos endpoints
7. `application/config/DataInitializer.java` - Datos de prueba mejorados
8. `domain/enums/IncidentType.java` - Enum de tipos
9. `domain/enums/IncidentTypeGroup.java` - Enum de grupos
10. `domain/enums/Severity.java` - Enum de severidad
11. `domain/enums/IncidentStatus.java` - Nuevo enum de estado

## Uso

### Crear un Incidente

```bash
POST /api/v1/incidents
Content-Type: application/json

{
  "incident_code": "INC004",
  "organization_id": "64abc123def4567890123456",
  "incident_type": "FUGA_TUBERIA",
  "incident_type_group": "INFRAESTRUCTURA",
  "incident_date": "2023-06-08T10:00:00Z",
  "description": "Nueva fuga detectada",
  "severity": "HIGH",
  "zone_id": "64def123abc4567890123456",
  "affected_count": 30,
  "estimated_resolution_time": "48h",
  "reported_by_user_id": "64abc789def1234567890123",
  "resolved": false,
  "status": "ACTIVE",
  "source_collection": "distribution_incidents"
}
```

### Resolver un Incidente

```bash
PATCH /api/v1/incidents/{id}/resolve
Content-Type: application/json

{
  "resolved_by_user_id": "64abc789def1234567890456",
  "description": "Incidente resuelto - tubería reparada"
}
```

### Buscar Incidentes

```bash
# Por organización
GET /api/v1/incidents/organization/64abc123def4567890123456

# Por tipo
GET /api/v1/incidents/type/FUGA_TUBERIA

# Por severidad
GET /api/v1/incidents/severity/HIGH

# Por estado de resolución
GET /api/v1/incidents/resolved/false

# Por estado activo/inactivo
GET /api/v1/incidents/status/ACTIVE
```

## Mejoras Implementadas

1. **Separación de fechas**: `incident_date` (cuándo ocurrió) vs `reported_at` (cuándo se reportó)
2. **Tiempo estimado**: Campo `estimated_resolution_time` para planificación
3. **Estado dual**: `resolved` (resuelto/pendiente) y `status` (activo/inactivo)
4. **Valores por defecto**: Configuración automática de campos obligatorios
5. **Búsquedas mejoradas**: Nuevos endpoints para filtrar por estado
6. **Datos de prueba**: Ejemplos realistas con todos los campos 