# 06 - CATÁLOGO COMPLETO DE ENDPOINTS API

Este documento contiene **TODOS** los endpoints REST para **TODAS** las entidades de los 11 microservicios.

---

## 🎯 OPERACIONES ESTÁNDAR

Todas las entidades siguen el mismo patrón CRUD + Soft Delete + Restore:

| Operación | Método | Endpoint | Descripción |
|-----------|--------|----------|-------------|
| **Create** | POST | `/{resource}` | Crear nuevo registro |
| **Get by ID** | GET | `/{resource}/{id}` | Obtener por ID |
| **Get All** | GET | `/{resource}` | Listar con paginación y filtros |
| **Update** | PUT | `/{resource}/{id}` | Actualizar registro |
| **Soft Delete** | DELETE | `/{resource}/{id}` | Eliminación lógica (`record_status=INACTIVE`) |
| **Restore** | PATCH | `/{resource}/{id}/restore` | Restaurar registro eliminado |

---

## 📋 SOFT DELETE (ELIMINACIÓN LÓGICA)

**NO se eliminan registros de la base de datos**, solo se marcan como inactivos:

```java
@DeleteMapping("/{id}")
public Mono<ResponseEntity<ApiResponse<Void>>> softDelete(
    @RequestHeader("X-Organization-ID") String organizationId,
    @PathVariable String id
) {
    return deleteUseCase.softDelete(id, organizationId)
        .then(Mono.just(
            ResponseEntity.ok(ApiResponse.success(null, "Usuario eliminado exitosamente"))
        ));
}
```

**Implementación del UseCase:**
```java
public Mono<Void> softDelete(String id, String organizationId) {
    return repository.findById(id)
        .filter(entity -> entity.getOrganizationId().equals(organizationId))
        .switchIfEmpty(Mono.error(new NotFoundException("Registro no encontrado")))
        .flatMap(entity -> {
            entity.setRecordStatus("INACTIVE");
            entity.setUpdatedAt(LocalDateTime.now());
            return repository.save(entity);
        })
        .then();
}
```

---

## 📋 RESTORE (RESTAURAR REGISTRO)

```java
@PatchMapping("/{id}/restore")
public Mono<ResponseEntity<ApiResponse<UserResponse>>> restore(
    @RequestHeader("X-Organization-ID") String organizationId,
    @PathVariable String id
) {
    return restoreUseCase.restore(id, organizationId)
        .map(user -> ResponseEntity.ok(
            ApiResponse.success(user, "Usuario restaurado exitosamente")
        ));
}
```

**Implementación:**
```java
public Mono<User> restore(String id, String organizationId) {
    return repository.findById(id)
        .filter(entity -> entity.getOrganizationId().equals(organizationId))
        .filter(entity -> "INACTIVE".equals(entity.getRecordStatus()))
        .switchIfEmpty(Mono.error(new BadRequestException("Registro no está inactivo")))
        .flatMap(entity -> {
            entity.setRecordStatus("ACTIVE");
            entity.setUpdatedAt(LocalDateTime.now());
            return repository.save(entity);
        });
}
```

---

# 1️⃣ VG-MS-USERS

## Users

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/users` | Crear usuario |
| GET | `/users/{id}` | Obtener usuario por ID |
| GET | `/users` | Listar usuarios (paginado) |
| PUT | `/users/{id}` | Actualizar usuario |
| DELETE | `/users/{id}` | Eliminar usuario (soft) |
| PATCH | `/users/{id}/restore` | Restaurar usuario |

**Filtros disponibles:**
- `?status=ACTIVE` - Filtrar por estado
- `?role=CLIENT` - Filtrar por rol
- `?zoneId=xxx` - Filtrar por zona
- `?search=juan` - Búsqueda por nombre

**Ejemplo Request (Create):**
```json
POST /users
{
  "firstName": "Juan",
  "lastName": "Pérez",
  "documentType": "DNI",
  "documentNumber": "12345678",
  "email": "juan@example.com",
  "phone": "987654321",
  "address": "Av. Principal 123",
  "zoneId": "550e8400-e29b-41d4-a716-446655440000",
  "streetId": "660f9511-f39c-52e5-b827-557766551111",
  "role": "CLIENT"
}
```

---

# 2️⃣ VG-MS-AUTHENTICATION

## Endpoints Especiales (No CRUD estándar)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/auth/login` | Login con email/password |
| POST | `/auth/logout` | Cerrar sesión |
| POST | `/auth/refresh` | Refresh token |
| POST | `/auth/validate` | Validar token |
| POST | `/auth/reset-password` | Solicitar reset de password |
| POST | `/auth/change-password` | Cambiar password |

---

# 3️⃣ VG-MS-ORGANIZATIONS

## Organizations

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/organizations` | Crear organización |
| GET | `/organizations/{id}` | Obtener por ID |
| GET | `/organizations` | Listar (paginado) |
| PUT | `/organizations/{id}` | Actualizar |
| DELETE | `/organizations/{id}` | Eliminar (soft) |
| PATCH | `/organizations/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?department=Lima`
- `?province=Lima`
- `?district=Miraflores`

## Zones

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/zones` | Crear zona |
| GET | `/zones/{id}` | Obtener por ID |
| GET | `/zones` | Listar (paginado) |
| PUT | `/zones/{id}` | Actualizar |
| DELETE | `/zones/{id}` | Eliminar (soft) |
| PATCH | `/zones/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?search=zona`

## Streets

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/streets` | Crear calle |
| GET | `/streets/{id}` | Obtener por ID |
| GET | `/streets` | Listar (paginado) |
| PUT | `/streets/{id}` | Actualizar |
| DELETE | `/streets/{id}` | Eliminar (soft) |
| PATCH | `/streets/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?zoneId=xxx`
- `?streetType=AV`
- `?search=principal`

## Fares

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/fares` | Crear tarifa |
| GET | `/fares/{id}` | Obtener por ID |
| GET | `/fares` | Listar (paginado) |
| PUT | `/fares/{id}` | Actualizar |
| DELETE | `/fares/{id}` | Eliminar (soft) |
| PATCH | `/fares/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?fareType=MONTHLY_FEE`

## Parameters

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/parameters` | Crear parámetro |
| GET | `/parameters/{id}` | Obtener por ID |
| GET | `/parameters` | Listar (paginado) |
| PUT | `/parameters/{id}` | Actualizar |
| DELETE | `/parameters/{id}` | Eliminar (soft) |
| PATCH | `/parameters/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?parameterType=BILLING_DAY`

---

# 4️⃣ VG-MS-COMMERCIAL-OPERATIONS

## Receipts

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/receipts` | Crear recibo |
| GET | `/receipts/{id}` | Obtener por ID |
| GET | `/receipts` | Listar (paginado) |
| PUT | `/receipts/{id}` | Actualizar |
| DELETE | `/receipts/{id}` | Eliminar (soft) |
| PATCH | `/receipts/{id}/restore` | Restaurar |

**Filtros:**
- `?status=PENDING`
- `?userId=xxx`
- `?issueMonth=2024-01`
- `?search=N° 000001`

**Endpoints especiales:**
```
POST   /receipts/generate-monthly      # Generar recibos del mes
GET    /receipts/user/{userId}          # Recibos de un usuario
```

## Payments

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/payments` | Registrar pago |
| GET | `/payments/{id}` | Obtener por ID |
| GET | `/payments` | Listar (paginado) |
| PUT | `/payments/{id}` | Actualizar |
| DELETE | `/payments/{id}` | Eliminar (soft) |
| PATCH | `/payments/{id}/restore` | Restaurar |

**Filtros:**
- `?status=COMPLETED`
- `?userId=xxx`
- `?paymentMethod=CASH`
- `?dateFrom=2024-01-01`
- `?dateTo=2024-01-31`

## Debts

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/debts` | Crear deuda |
| GET | `/debts/{id}` | Obtener por ID |
| GET | `/debts` | Listar (paginado) |
| PUT | `/debts/{id}` | Actualizar |
| DELETE | `/debts/{id}` | Eliminar (soft) |
| PATCH | `/debts/{id}/restore` | Restaurar |

**Filtros:**
- `?status=PENDING`
- `?userId=xxx`

## Service Cuts

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/service-cuts` | Programar corte |
| GET | `/service-cuts/{id}` | Obtener por ID |
| GET | `/service-cuts` | Listar (paginado) |
| PUT | `/service-cuts/{id}` | Actualizar |
| DELETE | `/service-cuts/{id}` | Eliminar (soft) |
| PATCH | `/service-cuts/{id}/restore` | Restaurar |

**Filtros:**
- `?status=PENDING`
- `?userId=xxx`
- `?cutReason=NON_PAYMENT`

**Endpoints especiales:**
```
PATCH  /service-cuts/{id}/execute      # Ejecutar corte
PATCH  /service-cuts/{id}/reconnect    # Reconectar servicio
```

## Petty Cash

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/petty-cash` | Crear caja chica |
| GET | `/petty-cash/{id}` | Obtener por ID |
| GET | `/petty-cash` | Listar (paginado) |
| PUT | `/petty-cash/{id}` | Actualizar |
| DELETE | `/petty-cash/{id}` | Eliminar (soft) |
| PATCH | `/petty-cash/{id}/restore` | Restaurar |

## Petty Cash Movements

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/petty-cash-movements` | Registrar movimiento |
| GET | `/petty-cash-movements/{id}` | Obtener por ID |
| GET | `/petty-cash-movements` | Listar (paginado) |
| DELETE | `/petty-cash-movements/{id}` | Eliminar (soft) |
| PATCH | `/petty-cash-movements/{id}/restore` | Restaurar |

**Filtros:**
- `?pettyCashId=xxx`
- `?movementType=IN`
- `?dateFrom=2024-01-01`

---

# 5️⃣ VG-MS-WATER-QUALITY

## Testing Points

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/testing-points` | Crear punto de prueba |
| GET | `/testing-points/{id}` | Obtener por ID |
| GET | `/testing-points` | Listar (paginado) |
| PUT | `/testing-points/{id}` | Actualizar |
| DELETE | `/testing-points/{id}` | Eliminar (soft) |
| PATCH | `/testing-points/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?pointType=SOURCE`
- `?zoneId=xxx`

## Quality Tests

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/quality-tests` | Registrar prueba |
| GET | `/quality-tests/{id}` | Obtener por ID |
| GET | `/quality-tests` | Listar (paginado) |
| PUT | `/quality-tests/{id}` | Actualizar |
| DELETE | `/quality-tests/{id}` | Eliminar (soft) |
| PATCH | `/quality-tests/{id}/restore` | Restaurar |

**Filtros:**
- `?pointId=xxx`
- `?testResult=APPROVED`
- `?dateFrom=2024-01-01`
- `?dateTo=2024-01-31`

---

# 6️⃣ VG-MS-DISTRIBUTION

## Distribution Programs

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/distribution-programs` | Crear programa |
| GET | `/distribution-programs/{id}` | Obtener por ID |
| GET | `/distribution-programs` | Listar (paginado) |
| PUT | `/distribution-programs/{id}` | Actualizar |
| DELETE | `/distribution-programs/{id}` | Eliminar (soft) |
| PATCH | `/distribution-programs/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?search=programa`

## Distribution Routes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/distribution-routes` | Crear ruta |
| GET | `/distribution-routes/{id}` | Obtener por ID |
| GET | `/distribution-routes` | Listar (paginado) |
| PUT | `/distribution-routes/{id}` | Actualizar |
| DELETE | `/distribution-routes/{id}` | Eliminar (soft) |
| PATCH | `/distribution-routes/{id}/restore` | Restaurar |

**Filtros:**
- `?programId=xxx`
- `?status=ACTIVE`

## Distribution Schedules

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/distribution-schedules` | Crear horario |
| GET | `/distribution-schedules/{id}` | Obtener por ID |
| GET | `/distribution-schedules` | Listar (paginado) |
| PUT | `/distribution-schedules/{id}` | Actualizar |
| DELETE | `/distribution-schedules/{id}` | Eliminar (soft) |
| PATCH | `/distribution-schedules/{id}/restore` | Restaurar |

**Filtros:**
- `?routeId=xxx`
- `?dayOfWeek=MONDAY`

---

# 7️⃣ VG-MS-INVENTORY-PURCHASES

## Suppliers

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/suppliers` | Crear proveedor |
| GET | `/suppliers/{id}` | Obtener por ID |
| GET | `/suppliers` | Listar (paginado) |
| PUT | `/suppliers/{id}` | Actualizar |
| DELETE | `/suppliers/{id}` | Eliminar (soft) |
| PATCH | `/suppliers/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?search=proveedor`

## Product Categories

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/product-categories` | Crear categoría |
| GET | `/product-categories/{id}` | Obtener por ID |
| GET | `/product-categories` | Listar (paginado) |
| PUT | `/product-categories/{id}` | Actualizar |
| DELETE | `/product-categories/{id}` | Eliminar (soft) |
| PATCH | `/product-categories/{id}/restore` | Restaurar |

## Materials

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/materials` | Crear material |
| GET | `/materials/{id}` | Obtener por ID |
| GET | `/materials` | Listar (paginado) |
| PUT | `/materials/{id}` | Actualizar |
| DELETE | `/materials/{id}` | Eliminar (soft) |
| PATCH | `/materials/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?categoryId=xxx`
- `?search=tubo`

## Purchases

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/purchases` | Crear compra |
| GET | `/purchases/{id}` | Obtener por ID |
| GET | `/purchases` | Listar (paginado) |
| PUT | `/purchases/{id}` | Actualizar |
| DELETE | `/purchases/{id}` | Eliminar (soft) |
| PATCH | `/purchases/{id}/restore` | Restaurar |

**Filtros:**
- `?supplierId=xxx`
- `?status=PENDING`
- `?dateFrom=2024-01-01`

## Inventory Movements

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/inventory-movements` | Registrar movimiento |
| GET | `/inventory-movements/{id}` | Obtener por ID |
| GET | `/inventory-movements` | Listar (paginado) |
| DELETE | `/inventory-movements/{id}` | Eliminar (soft) |
| PATCH | `/inventory-movements/{id}/restore` | Restaurar |

**Filtros:**
- `?materialId=xxx`
- `?movementType=IN`
- `?dateFrom=2024-01-01`

---

# 8️⃣ VG-MS-CLAIMS-INCIDENTS

## Complaints

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/complaints` | Crear reclamo |
| GET | `/complaints/{id}` | Obtener por ID |
| GET | `/complaints` | Listar (paginado) |
| PUT | `/complaints/{id}` | Actualizar |
| DELETE | `/complaints/{id}` | Eliminar (soft) |
| PATCH | `/complaints/{id}/restore` | Restaurar |

**Filtros:**
- `?status=PENDING`
- `?userId=xxx`
- `?categoryId=xxx`
- `?priority=HIGH`
- `?dateFrom=2024-01-01`

## Complaint Categories

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/complaint-categories` | Crear categoría |
| GET | `/complaint-categories/{id}` | Obtener por ID |
| GET | `/complaint-categories` | Listar (paginado) |
| PUT | `/complaint-categories/{id}` | Actualizar |
| DELETE | `/complaint-categories/{id}` | Eliminar (soft) |
| PATCH | `/complaint-categories/{id}/restore` | Restaurar |

## Complaint Responses

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/complaint-responses` | Registrar respuesta |
| GET | `/complaint-responses/{id}` | Obtener por ID |
| GET | `/complaint-responses` | Listar (paginado) |
| DELETE | `/complaint-responses/{id}` | Eliminar (soft) |
| PATCH | `/complaint-responses/{id}/restore` | Restaurar |

**Filtros:**
- `?complaintId=xxx`

## Incidents

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/incidents` | Crear incidente |
| GET | `/incidents/{id}` | Obtener por ID |
| GET | `/incidents` | Listar (paginado) |
| PUT | `/incidents/{id}` | Actualizar |
| DELETE | `/incidents/{id}` | Eliminar (soft) |
| PATCH | `/incidents/{id}/restore` | Restaurar |

**Filtros:**
- `?status=IN_PROGRESS`
- `?typeId=xxx`
- `?priority=HIGH`
- `?assignedTo=xxx`

## Incident Types

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/incident-types` | Crear tipo |
| GET | `/incident-types/{id}` | Obtener por ID |
| GET | `/incident-types` | Listar (paginado) |
| PUT | `/incident-types/{id}` | Actualizar |
| DELETE | `/incident-types/{id}` | Eliminar (soft) |
| PATCH | `/incident-types/{id}/restore` | Restaurar |

## Incident Resolutions

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/incident-resolutions` | Registrar resolución |
| GET | `/incident-resolutions/{id}` | Obtener por ID |
| GET | `/incident-resolutions` | Listar (paginado) |
| DELETE | `/incident-resolutions/{id}` | Eliminar (soft) |
| PATCH | `/incident-resolutions/{id}/restore` | Restaurar |

---

# 9️⃣ VG-MS-INFRASTRUCTURE

## Water Boxes

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/water-boxes` | Crear caja de agua |
| GET | `/water-boxes/{id}` | Obtener por ID |
| GET | `/water-boxes` | Listar (paginado) |
| PUT | `/water-boxes/{id}` | Actualizar |
| DELETE | `/water-boxes/{id}` | Eliminar (soft) |
| PATCH | `/water-boxes/{id}/restore` | Restaurar |

**Filtros:**
- `?status=ACTIVE`
- `?boxType=RESIDENTIAL`
- `?zoneId=xxx`
- `?streetId=xxx`

## Water Box Assignments

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/water-box-assignments` | Asignar caja a usuario |
| GET | `/water-box-assignments/{id}` | Obtener por ID |
| GET | `/water-box-assignments` | Listar (paginado) |
| PUT | `/water-box-assignments/{id}` | Actualizar |
| DELETE | `/water-box-assignments/{id}` | Eliminar (soft) |
| PATCH | `/water-box-assignments/{id}/restore` | Restaurar |

**Filtros:**
- `?userId=xxx`
- `?waterBoxId=xxx`
- `?status=ACTIVE`

## Water Box Transfers

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/water-box-transfers` | Crear transferencia |
| GET | `/water-box-transfers/{id}` | Obtener por ID |
| GET | `/water-box-transfers` | Listar (paginado) |
| DELETE | `/water-box-transfers/{id}` | Eliminar (soft) |
| PATCH | `/water-box-transfers/{id}/restore` | Restaurar |

**Filtros:**
- `?waterBoxId=xxx`
- `?fromUserId=xxx`
- `?toUserId=xxx`

---

# 🔟 VG-MS-NOTIFICATION

## Endpoints Especiales (No CRUD estándar)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| POST | `/notifications/whatsapp` | Enviar WhatsApp |
| POST | `/notifications/email` | Enviar Email |
| GET | `/notifications/history` | Historial de notificaciones |

---

## ✅ RESUMEN TOTAL

### Cantidad de Endpoints por Microservicio:

| Microservicio | Entidades | Endpoints (aprox) |
|---------------|-----------|-------------------|
| users | 1 | 6 |
| authentication | - | 6 (especiales) |
| organizations | 5 | 30 |
| commercial-operations | 5 | 30 + especiales |
| water-quality | 2 | 12 |
| distribution | 3 | 18 |
| inventory-purchases | 5 | 30 |
| claims-incidents | 6 | 36 |
| infrastructure | 3 | 18 |
| notification | - | 3 (especiales) |
| **TOTAL** | **30+ entidades** | **~189 endpoints** |

---

## 🎯 PATRÓN ESTÁNDAR DE IMPLEMENTACIÓN

```java
@RestController
@RequestMapping("/users")
@Tag(name = "Users")
@RequiredArgsConstructor
public class UserController {
    
    // CREATE
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> create(...) {}
    
    // READ BY ID
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getById(...) {}
    
    // READ ALL (Paginated)
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PageResponse<UserResponse>>>> getAll(...) {}
    
    // UPDATE
    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> update(...) {}
    
    // SOFT DELETE
    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> softDelete(...) {}
    
    // RESTORE
    @PatchMapping("/{id}/restore")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> restore(...) {}
}
```

**Todos los endpoints siguen el mismo patrón estándar** ✅
