# Flujos de Negocio del Microservicio

Este documento explica los flujos completos de los casos de uso principales del microservicio de distribución.

---

## 1. Flujo: Crear Programa de Distribución

### Descripción
Un administrador crea un nuevo programa de distribución para una organización (JASS).

### Actores
- **Administrador**: Usuario con rol ADMIN
- **Sistema**: Microservicio de distribución
- **Servicio Externo**: ms-organization

### Precondiciones
- Usuario autenticado con token JWT válido
- Usuario tiene rol ADMIN
- Organización existe y está activa

### Flujo Principal

```
┌─────────────┐
│   Cliente   │
│  (Frontend) │
└──────┬──────┘
       │ 1. POST /internal/programs
       │    Authorization: Bearer <JWT>
       │    Body: {name, organizationId, description}
       ▼
┌────────────────────────────────────────────┐
│          AdminRest.createProgram()         │
│  - Valida request con @Valid               │
│  - Extrae token del header                 │
└──────┬─────────────────────────────────────┘
       │ 2. Delega a servicio
       ▼
┌────────────────────────────────────────────┐
│  DistributionProgramServiceImpl.create()   │
│  ┌──────────────────────────────────────┐  │
│  │ 3. Validar organización              │  │
│  │    ExternalServiceClient             │  │
│  │    .getOrganization(orgId, token)    │  │
│  └──────────┬───────────────────────────┘  │
│             │ 4. Respuesta externa         │
│             ▼                                │
│  ┌──────────────────────────────────────┐  │
│  │ 5. Validar que esté activa           │  │
│  │    ExternalClientValidator           │  │
│  └──────────┬───────────────────────────┘  │
│             │ 6. Validación OK             │
│             ▼                                │
│  ┌──────────────────────────────────────┐  │
│  │ 7. Crear entidad de dominio          │  │
│  │    DistributionProgram.builder()     │  │
│  │    .name(request.getName())          │  │
│  │    .organizationId(...)              │  │
│  │    .status("A")                      │  │
│  │    .build()                          │  │
│  └──────────┬───────────────────────────┘  │
│             │ 8. Convertir a documento     │
│             ▼                                │
│  ┌──────────────────────────────────────┐  │
│  │ 9. Mapper.toDocument(program)        │  │
│  └──────────┬───────────────────────────┘  │
│             │ 10. Documento MongoDB        │
│             ▼                                │
│  ┌──────────────────────────────────────┐  │
│  │ 11. Persistir en MongoDB             │  │
│  │     repository.save(document)        │  │
│  └──────────┬───────────────────────────┘  │
│             │ 12. Documento guardado       │
│             ▼                                │
│  ┌──────────────────────────────────────┐  │
│  │ 13. Convertir a Response DTO         │  │
│  │     Mapper.toResponse(document)      │  │
│  └──────────┬───────────────────────────┘  │
│             │ 14. Response DTO             │
│             ▼                                │
│  ┌──────────────────────────────────────┐  │
│  │ 15. Envolver en ResponseDto          │  │
│  │     ResponseDto.success(response)    │  │
│  └──────────┬───────────────────────────┘  │
└─────────────┼────────────────────────────────┘
              │ 16. Retornar Mono<ResponseDto>
              ▼
┌────────────────────────────────────────────┐
│         AdminRest retorna HTTP 201         │
│  {                                         │
│    "status": "success",                    │
│    "message": "Programa creado",           │
│    "data": {                               │
│      "id": "abc123",                       │
│      "name": "Programa 2025",              │
│      "organizationId": "ORG001",           │
│      "status": "A"                         │
│    },                                      │
│    "timestamp": "2025-12-03T20:00:00Z"     │
│  }                                         │
└────────────────────────────────────────────┘
```

### Código Simplificado

```java
// 1. Controller
@PostMapping("/programs")
public Mono<ResponseDto<DistributionProgramResponse>> create(
    @Valid @RequestBody DistributionProgramCreateRequest request,
    @RequestHeader("Authorization") String authHeader) {
    
    String token = authHeader.replace("Bearer ", "");
    return service.createProgram(request, token);
}

// 2. Service
public Mono<ResponseDto<DistributionProgramResponse>> createProgram(
    DistributionProgramCreateRequest request, String token) {
    
    return externalClient.getOrganization(request.getOrganizationId(), token)
        .flatMap(org -> validator.validateOrganization(org))
        .map(org -> buildProgram(request))
        .map(program -> mapper.toDocument(program))
        .flatMap(document -> repository.save(document))
        .map(saved -> mapper.toResponse(saved))
        .map(response -> ResponseDto.success("Programa creado", response));
}
```

### Flujos Alternativos

#### A1: Organización no existe
```
3. getOrganization() → 404 Not Found
   ↓
4. ExternalServiceException
   ↓
5. GlobalExceptionHandler.handleExternalServiceException()
   ↓
6. HTTP 400 Bad Request
   {
     "status": "error",
     "message": "Organización no encontrada",
     "errors": [...]
   }
```

#### A2: Organización inactiva
```
5. validateOrganization() → status = "I"
   ↓
6. CustomException("Organización inactiva")
   ↓
7. GlobalExceptionHandler.handleCustomException()
   ↓
8. HTTP 400 Bad Request
```

#### A3: Validación de request falla
```
1. @Valid detecta errores
   ↓
2. MethodArgumentNotValidException
   ↓
3. GlobalExceptionHandler.handleValidationException()
   ↓
4. HTTP 400 Bad Request
   {
     "status": "error",
     "message": "Errores de validación",
     "errors": [
       {"field": "name", "message": "El nombre es obligatorio"}
     ]
   }
```

---

## 2. Flujo: Crear Ruta de Distribución

### Descripción
Crear una ruta de distribución asociada a un programa y una zona.

### Precondiciones
- Programa de distribución existe
- Zona existe en ms-organization
- Usuario autenticado con rol ADMIN

### Flujo Principal

```
1. Cliente → POST /internal/routes
   Body: {
     programId,
     zoneId,
     name,
     description
   }

2. AdminRest.createRoute()
   ↓
3. DistributionRouteServiceImpl.create()
   ↓
4. Validar programa existe
   repository.findById(programId)
   ↓
5. Validar zona existe
   externalClient.getZone(zoneId, token)
   ↓
6. Crear entidad DistributionRoute
   ↓
7. Convertir a documento
   ↓
8. Persistir en MongoDB
   ↓
9. Retornar ResponseDto
```

### Validaciones Específicas

- **Programa existe**: `repository.findById(programId).switchIfEmpty(error)`
- **Programa activo**: `program.getStatus() == "A"`
- **Zona existe**: `externalClient.getZone(zoneId, token)`
- **Zona activa**: `zone.getStatus() == "A"`
- **Zona pertenece a organización del programa**: `zone.getOrganizationId() == program.getOrganizationId()`

---

## 3. Flujo: Crear Horario de Distribución

### Descripción
Crear un horario de distribución para una ruta y calle específica.

### Precondiciones
- Ruta de distribución existe
- Calle existe en ms-organization
- Calle pertenece a la zona de la ruta

### Flujo Principal

```
1. Cliente → POST /internal/schedules
   Body: {
     routeId,
     streetId,
     dayOfWeek,
     startTime,
     endTime
   }

2. AdminRest.createSchedule()
   ↓
3. DistributionScheduleServiceImpl.create()
   ↓
4. Validar ruta existe
   routeRepository.findById(routeId)
   ↓
5. Validar calle existe
   externalClient.getStreet(streetId, token)
   ↓
6. Validar calle pertenece a zona de la ruta
   street.getZoneId() == route.getZoneId()
   ↓
7. Validar horarios no se solapan
   checkTimeOverlap(routeId, dayOfWeek, startTime, endTime)
   ↓
8. Crear entidad DistributionSchedule
   ↓
9. Persistir en MongoDB
   ↓
10. Retornar ResponseDto
```

### Validaciones Específicas

- **Ruta existe y activa**
- **Calle existe y activa**
- **Calle pertenece a zona de la ruta**
- **Horarios no se solapan**: No puede haber dos horarios para la misma ruta y día que se crucen
- **Horario válido**: `startTime < endTime`
- **Día válido**: `dayOfWeek` entre 1 (Lunes) y 7 (Domingo)

---

## 4. Flujo: Actualizar Estado de Entrega

### Descripción
Actualizar el estado de entrega de agua para un horario específico.

### Estados Posibles
- `PENDING` - Pendiente
- `IN_PROGRESS` - En progreso
- `COMPLETED` - Completado
- `CANCELLED` - Cancelado

### Flujo Principal

```
1. Cliente → PUT /internal/schedules/{id}/status
   Body: {
     status: "COMPLETED"
   }

2. AdminRest.updateDeliveryStatus()
   ↓
3. DistributionScheduleServiceImpl.updateStatus()
   ↓
4. Buscar horario
   repository.findById(id)
   ↓
5. Validar transición de estado válida
   validateStatusTransition(currentStatus, newStatus)
   ↓
6. Actualizar estado
   schedule.setWaterDeliveryStatus(newStatus)
   schedule.setUpdatedAt(now())
   ↓
7. Persistir cambios
   repository.save(schedule)
   ↓
8. Retornar ResponseDto
```

### Transiciones de Estado Válidas

```
PENDING → IN_PROGRESS
PENDING → CANCELLED

IN_PROGRESS → COMPLETED
IN_PROGRESS → CANCELLED

COMPLETED → (no puede cambiar)
CANCELLED → (no puede cambiar)
```

---

## 5. Flujo: Listar Programas por Organización

### Descripción
Obtener todos los programas de distribución de una organización.

### Flujo Principal

```
1. Cliente → GET /internal/programs?organizationId=ORG001

2. AdminRest.findByOrganization()
   ↓
3. DistributionProgramServiceImpl.findByOrganization()
   ↓
4. Buscar en repositorio
   repository.findByOrganizationId(orgId)
   ↓
5. Convertir a Response DTOs
   Flux.map(doc → mapper.toResponse(doc))
   ↓
6. Retornar Flux<DistributionProgramResponse>
```

### Código Reactivo

```java
public Flux<DistributionProgramResponse> findByOrganization(String orgId) {
    return repository.findByOrganizationId(orgId)
        .map(document -> mapper.toResponse(document))
        .switchIfEmpty(Flux.empty());
}
```

---

## 6. Flujo: Activar/Desactivar Programa

### Descripción
Cambiar el estado de un programa entre activo (A) e inactivo (I).

### Flujo Principal

```
1. Cliente → PUT /internal/programs/{id}/status
   Body: { status: "I" }

2. AdminRest.updateStatus()
   ↓
3. DistributionProgramServiceImpl.updateStatus()
   ↓
4. Buscar programa
   repository.findById(id)
   ↓
5. Validar nuevo estado
   validateStatus(newStatus) // Solo "A" o "I"
   ↓
6. Actualizar estado
   program.setStatus(newStatus)
   program.setUpdatedAt(now())
   ↓
7. Si se desactiva programa, desactivar rutas asociadas
   if (newStatus == "I") {
     routeRepository.findByProgramId(id)
       .flatMap(route → {
         route.setStatus("I");
         return routeRepository.save(route);
       })
   }
   ↓
8. Persistir cambios
   repository.save(program)
   ↓
9. Retornar ResponseDto
```

### Efecto en Cascada

Cuando se desactiva un programa:
1. El programa pasa a estado "I"
2. Todas las rutas del programa pasan a "I"
3. Todos los horarios de esas rutas pasan a "I"

---

## 7. Flujo: Integración con Servicio Externo

### Descripción
Cómo el microservicio consume ms-organization.

### Componentes Involucrados
- `ExternalServiceClient` - Cliente WebClient
- `ExternalClientValidator` - Validador de respuestas
- `WebClientConfig` - Configuración

### Flujo de Llamada Externa

```
1. Service necesita validar organización
   ↓
2. ExternalServiceClient.getOrganization(id, token)
   ↓
3. WebClient construye request
   GET https://lab.vallegrande.edu.pe/jass/ms-organization/api/admin/organizations/{id}
   Header: Authorization: Bearer {token}
   ↓
4. Envía request HTTP
   ↓
5. ms-organization responde
   {
     "status": "success",
     "data": {
       "id": "ORG001",
       "name": "JASS San Juan",
       "status": "A"
     }
   }
   ↓
6. WebClient deserializa JSON
   ↓
7. ExternalClientValidator.validate(organization)
   - Verifica que status = "A"
   - Verifica que id no sea null
   ↓
8. Retorna Mono<ExternalOrganization>
```

### Manejo de Errores

```java
public Mono<ExternalOrganization> getOrganization(String id, String token) {
    return webClient.get()
        .uri("/organizations/{id}", id)
        .header("Authorization", "Bearer " + token)
        .retrieve()
        .onStatus(HttpStatus::is4xxClientError, response → 
            Mono.error(new ExternalServiceException("Organización no encontrada")))
        .onStatus(HttpStatus::is5xxServerError, response → 
            Mono.error(new ExternalServiceException("Error en servicio externo")))
        .bodyToMono(ExternalResponseDto.class)
        .map(response → response.getData())
        .timeout(Duration.ofSeconds(5))
        .retry(2);
}
```

---

## 8. Flujo de Autenticación y Autorización

### Descripción
Cómo se valida la seguridad en cada request.

### Flujo Completo

```
1. Cliente envía request
   GET /internal/programs
   Header: Authorization: Bearer eyJhbGc...
   ↓
2. Spring Security intercepta
   ↓
3. SecurityConfig valida JWT
   - Verifica firma con JWK Set de Keycloak
   - Verifica issuer
   - Verifica expiración
   ↓
4. Extrae roles del token
   JWT claims → realm_access.roles → ["admin", "user"]
   ↓
5. Convierte a GrantedAuthority
   ["admin", "user"] → ["ROLE_ADMIN", "ROLE_USER"]
   ↓
6. Evalúa @PreAuthorize
   @PreAuthorize("hasRole('ADMIN')")
   ↓
7. Si tiene rol, permite acceso
   ↓
8. Controller ejecuta
   ↓
9. Retorna respuesta
```

### Estructura del JWT

```json
{
  "sub": "user123",
  "realm_access": {
    "roles": ["admin", "user"]
  },
  "iss": "https://lab.vallegrande.edu.pe/jass/keycloak/realms/sistema-jass",
  "exp": 1701648000
}
```

---

## 9. Flujo de Manejo de Errores

### Descripción
Cómo se capturan y formatean los errores.

### Flujo de Error

```
1. Ocurre excepción en cualquier capa
   ↓
2. GlobalExceptionHandler captura
   @ExceptionHandler(ResourceNotFoundException.class)
   ↓
3. Formatea error
   ResponseDto.error(message, errors)
   ↓
4. Retorna HTTP con código apropiado
   - 400 Bad Request (validación)
   - 404 Not Found (recurso no existe)
   - 500 Internal Server Error (error inesperado)
```

### Ejemplo de Respuesta de Error

```json
{
  "status": "error",
  "message": "Errores de validación",
  "errors": [
    {
      "field": "name",
      "message": "El nombre es obligatorio",
      "rejectedValue": null
    }
  ],
  "timestamp": "2025-12-03T20:00:00Z"
}
```

---

## Conclusión

Los flujos de negocio del microservicio:
- ✅ **Validan datos** en múltiples niveles
- ✅ **Integran servicios externos** de forma reactiva
- ✅ **Manejan errores** de forma consistente
- ✅ **Aplican seguridad** en todos los endpoints
- ✅ **Mantienen integridad** de datos con validaciones
- ✅ **Usan programación reactiva** (Mono/Flux) para alto rendimiento
