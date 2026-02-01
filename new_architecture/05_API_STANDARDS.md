# 05 - ESTÁNDARES DE API REST

Este documento define los **ESTÁNDARES OBLIGATORIOS** para TODAS las APIs del sistema JASS Digital.

---

## 📋 PRINCIPIOS GENERALES

1. **RESTful** - Seguir principios REST
2. **JSON** - Formato exclusivo para request/response
3. **Consistencia** - Misma estructura en todos los servicios
4. **Versionamiento** - Preparado para evolución
5. **Reactivo** - Todos los endpoints son reactivos (`Mono<>` / `Flux<>`)

---

## 🎯 CONVENCIONES DE NAMING

### Endpoints (URLs)

```
✅ CORRECTO:
GET    /users
GET    /users/{id}
POST   /users
PUT    /users/{id}
DELETE /users/{id}
GET    /users/{id}/receipts

❌ INCORRECTO:
GET    /getUsers
GET    /user_list
POST   /createUser
GET    /users/get/{id}
```

**Reglas:**
- ✅ Usar **sustantivos en plural** (`users`, `receipts`, `payments`)
- ✅ Usar **kebab-case** para URLs compuestas (`water-quality`, `petty-cash`)
- ✅ Rutas anidadas para relaciones (`/users/{id}/receipts`)
- ❌ NO usar verbos en la URL (`/createUser`, `/getPayment`)
- ❌ NO usar snake_case (`user_list`)
- ❌ NO usar camelCase en URLs (`userList`)

### JSON Fields (Propiedades)

```json
✅ CORRECTO:
{
  "userId": "123",
  "firstName": "Juan",
  "createdAt": "2024-01-15T10:30:00Z"
}

❌ INCORRECTO:
{
  "user_id": "123",
  "FirstName": "Juan",
  "created_at": "2024-01-15T10:30:00Z"
}
```

**Reglas:**
- ✅ Usar **camelCase** para propiedades JSON
- ✅ Fechas en formato **ISO 8601** con timezone UTC
- ❌ NO usar snake_case en JSON
- ❌ NO usar PascalCase en JSON

---

## 🎨 ESTRUCTURA DE RESPUESTAS ESTÁNDAR

### ✅ Respuesta Exitosa (1 item)

```java
@GetMapping("/users/{id}")
public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserById(@PathVariable String id) {
    return userService.getUserById(id)
        .map(user -> ResponseEntity.ok(
            ApiResponse.success(user, "Usuario obtenido exitosamente")
        ));
}
```

**Response JSON:**
```json
{
  "success": true,
  "message": "Usuario obtenido exitosamente",
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "firstName": "Juan",
    "lastName": "Pérez",
    "email": "juan.perez@example.com",
    "createdAt": "2024-01-15T10:30:00Z"
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### ✅ Respuesta Exitosa (Lista)

```java
@GetMapping("/users")
public Mono<ResponseEntity<ApiResponse<List<UserResponse>>>> getAllUsers() {
    return userService.getAllUsers()
        .collectList()
        .map(users -> ResponseEntity.ok(
            ApiResponse.success(users, "Usuarios obtenidos exitosamente")
        ));
}
```

**Response JSON:**
```json
{
  "success": true,
  "message": "Usuarios obtenidos exitosamente",
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "firstName": "Juan",
      "lastName": "Pérez"
    },
    {
      "id": "660f9511-f39c-52e5-b827-557766551111",
      "firstName": "María",
      "lastName": "García"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### ✅ Respuesta Exitosa con Paginación

```java
@GetMapping("/users")
public Mono<ResponseEntity<ApiResponse<PageResponse<UserResponse>>>> getUsersPaginated(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size
) {
    return userService.getUsersPaginated(page, size)
        .map(pageData -> ResponseEntity.ok(
            ApiResponse.success(pageData, "Usuarios obtenidos exitosamente")
        ));
}
```

**Response JSON:**
```json
{
  "success": true,
  "message": "Usuarios obtenidos exitosamente",
  "data": {
    "content": [
      {"id": "550e8400", "firstName": "Juan"},
      {"id": "660f9511", "firstName": "María"}
    ],
    "page": 0,
    "size": 10,
    "totalElements": 25,
    "totalPages": 3
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### ❌ Respuesta de Error

```java
@ExceptionHandler(UserNotFoundException.class)
public Mono<ResponseEntity<ApiResponse<Void>>> handleUserNotFound(UserNotFoundException ex) {
    return Mono.just(
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage()))
    );
}
```

**Response JSON:**
```json
{
  "success": false,
  "message": "Usuario no encontrado",
  "errors": [
    {
      "field": null,
      "message": "Usuario con ID 550e8400 no existe",
      "code": "USER_NOT_FOUND"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### ❌ Respuesta de Errores de Validación

```json
{
  "success": false,
  "message": "Errores de validación",
  "errors": [
    {
      "field": "email",
      "message": "Email debe ser válido",
      "code": "INVALID_EMAIL"
    },
    {
      "field": "firstName",
      "message": "Nombre es obligatorio",
      "code": "REQUIRED_FIELD"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## 🔢 CÓDIGOS HTTP ESTÁNDAR

### 2xx - Éxito

| Código | Uso | Ejemplo |
|--------|-----|---------|
| **200 OK** | GET exitoso, PUT exitoso | Obtener usuario, Actualizar usuario |
| **201 Created** | POST exitoso | Crear nuevo usuario |
| **204 No Content** | DELETE exitoso | Eliminar usuario |

### 4xx - Errores del Cliente

| Código | Uso | Ejemplo |
|--------|-----|---------|
| **400 Bad Request** | Validación fallida, datos inválidos | Email mal formado |
| **401 Unauthorized** | No autenticado | Token JWT inválido |
| **403 Forbidden** | Autenticado pero sin permisos | Usuario no es ADMIN |
| **404 Not Found** | Recurso no existe | Usuario con ID no encontrado |
| **409 Conflict** | Conflicto de estado | Email ya registrado |
| **422 Unprocessable Entity** | Lógica de negocio falla | Usuario inactivo no puede pagar |

### 5xx - Errores del Servidor

| Código | Uso | Ejemplo |
|--------|-----|---------|
| **500 Internal Server Error** | Error inesperado | NullPointerException |
| **503 Service Unavailable** | Servicio caído | Circuit Breaker abierto |

---

## 📝 VALIDACIONES

### Anotaciones Jakarta Validation

```java
public class CreateUserRequest {
    
    @NotBlank(message = "Nombre es obligatorio")
    @Size(min = 2, max = 100, message = "Nombre debe tener entre 2 y 100 caracteres")
    private String firstName;
    
    @NotBlank(message = "Apellido es obligatorio")
    @Size(min = 2, max = 150, message = "Apellido debe tener entre 2 y 150 caracteres")
    private String lastName;
    
    @Email(message = "Email debe ser válido")
    private String email;
    
    @Pattern(regexp = "^[0-9]{8,20}$", message = "Documento debe contener solo números")
    @NotBlank(message = "Número de documento es obligatorio")
    private String documentNumber;
    
    @NotBlank(message = "Dirección es obligatoria")
    private String address;
    
    @NotNull(message = "Zona es obligatoria")
    private String zoneId;
}
```

### Controller con Validación

```java
@PostMapping("/users")
public Mono<ResponseEntity<ApiResponse<UserResponse>>> createUser(
    @Valid @RequestBody CreateUserRequest request
) {
    return userService.createUser(request)
        .map(user -> ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(user, "Usuario creado exitosamente"))
        );
}
```

---

## 🔐 HEADERS OBLIGATORIOS

### Request Headers

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
X-Organization-ID: 550e8400-e29b-41d4-a716-446655440000
Content-Type: application/json
Accept: application/json
```

**Reglas:**
- ✅ `Authorization`: Obligatorio en endpoints protegidos
- ✅ `X-Organization-ID`: Obligatorio para multi-tenancy
- ✅ `Content-Type: application/json` en POST/PUT
- ✅ `Accept: application/json` siempre

### Response Headers

```http
Content-Type: application/json
X-Request-ID: 7f3c8d92-5a41-4c89-b612-3e9f8a1b2c3d
```

---

## 📄 PAGINACIÓN

### Query Parameters

```http
GET /users?page=0&size=10&sort=createdAt,desc
```

**Parámetros:**
- `page`: Número de página (0-indexed)
- `size`: Tamaño de página (default: 10, max: 100)
- `sort`: Campo y dirección (`field,asc` o `field,desc`)

### Response

```json
{
  "success": true,
  "data": {
    "content": [...],
    "page": 0,
    "size": 10,
    "totalElements": 250,
    "totalPages": 25,
    "first": true,
    "last": false
  }
}
```

---

## 🔍 FILTROS Y BÚSQUEDA

### Query Parameters para Filtros

```http
GET /users?status=ACTIVE&role=CLIENT&search=juan
```

**Reglas:**
- ✅ Usar nombres descriptivos (`status`, `role`, `search`)
- ✅ Valores en UPPER_CASE para enums (`ACTIVE`, `CLIENT`)
- ✅ `search` para búsqueda de texto libre
- ✅ Múltiples filtros se combinan con AND

---

## 🎨 CLASES ESTÁNDAR COMPARTIDAS (OBLIGATORIAS)

Estas **3 clases** deben existir en **TODOS** los microservicios Spring Boot en `application/dto/common/`.

---

### 1️⃣ ApiResponse.java

**Ubicación:** `application/dto/common/ApiResponse.java`

**Propósito:** Wrapper estándar para TODAS las respuestas REST (éxito y error).

```java
package pe.edu.vallegrande.vgmsusers.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * Wrapper estándar para respuestas REST.
 * 
 * @param <T> Tipo de dato en el campo 'data'
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /**
     * Indica si la operación fue exitosa
     */
    private boolean success;
    
    /**
     * Mensaje descriptivo de la operación
     */
    private String message;
    
    /**
     * Datos de respuesta (null si es error)
     */
    private T data;
    
    /**
     * Lista de errores (null si es éxito)
     */
    private List<ErrorMessage> errors;
    
    /**
     * Timestamp de la respuesta (ISO 8601 UTC)
     */
    private Instant timestamp;
    
    // ========== MÉTODOS ESTÁTICOS DE CONSTRUCCIÓN ==========
    
    /**
     * Construye una respuesta exitosa con datos
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        response.setTimestamp(Instant.now());
        return response;
    }
    
    /**
     * Construye una respuesta exitosa sin datos
     */
    public static <T> ApiResponse<T> success(String message) {
        return success(null, message);
    }
    
    /**
     * Construye una respuesta de error simple
     */
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setTimestamp(Instant.now());
        return response;
    }
    
    /**
     * Construye una respuesta de error con detalles
     */
    public static <T> ApiResponse<T> error(String message, List<ErrorMessage> errors) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setErrors(errors);
        response.setTimestamp(Instant.now());
        return response;
    }
    
    /**
     * Construye una respuesta de error con un solo error
     */
    public static <T> ApiResponse<T> error(String message, ErrorMessage error) {
        return error(message, List.of(error));
    }
}
```

**Ejemplo de uso:**
```java
// Éxito con datos
return ResponseEntity.ok(
    ApiResponse.success(userResponse, "Usuario creado exitosamente")
);

// Éxito sin datos
return ResponseEntity.ok(
    ApiResponse.success("Usuario eliminado exitosamente")
);

// Error simple
return ResponseEntity.status(HttpStatus.NOT_FOUND)
    .body(ApiResponse.error("Usuario no encontrado"));

// Error con detalles
return ResponseEntity.badRequest()
    .body(ApiResponse.error("Errores de validación", validationErrors));
```

---

### 2️⃣ PageResponse.java

**Ubicación:** `application/dto/common/PageResponse.java`

**Propósito:** DTO para respuestas paginadas (reemplazo reactivo de Spring `Page<T>`).

```java
package pe.edu.vallegrande.vgmsusers.application.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO para endpoints paginados.
 * Compatible con programación reactiva (WebFlux).
 * 
 * @param <T> Tipo de elementos en la página
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /**
     * Lista de elementos de la página actual
     */
    private List<T> content;
    
    /**
     * Número de página actual (0-indexed)
     */
    @JsonProperty("page")
    private int currentPage;
    
    /**
     * Tamaño de página solicitado
     */
    @JsonProperty("size")
    private int pageSize;
    
    /**
     * Total de elementos en todas las páginas
     */
    private long totalElements;
    
    /**
     * Total de páginas disponibles
     */
    private int totalPages;
    
    /**
     * Indica si es la primera página
     */
    @JsonProperty("isFirst")
    private boolean first;
    
    /**
     * Indica si es la última página
     */
    @JsonProperty("isLast")
    private boolean last;
    
    // ========== MÉTODOS DE UTILIDAD ==========
    
    /**
     * Verifica si hay página siguiente
     */
    public boolean hasNext() {
        return !last;
    }
    
    /**
     * Verifica si hay página anterior
     */
    public boolean hasPrevious() {
        return !first;
    }
    
    /**
     * Obtiene el número de la siguiente página (si existe)
     */
    public Integer getNextPage() {
        return hasNext() ? currentPage + 1 : null;
    }
    
    /**
     * Obtiene el número de la página anterior (si existe)
     */
    public Integer getPreviousPage() {
        return hasPrevious() ? currentPage - 1 : null;
    }
    
    /**
     * Verifica si la página está vacía
     */
    public boolean isEmpty() {
        return content == null || content.isEmpty();
    }
    
    /**
     * Obtiene el número de elementos en esta página
     */
    public int getNumberOfElements() {
        return content != null ? content.size() : 0;
    }
}
```

**Ejemplo de uso:**
```java
// En el Controller
@GetMapping
public Mono<ResponseEntity<ApiResponse<PageResponse<UserResponse>>>> getUsers(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(required = false) String status
) {
    return getUsersUseCase.execute(page, size, status)
        .map(pageData -> ResponseEntity.ok(
            ApiResponse.success(pageData, "Usuarios obtenidos exitosamente")
        ));
}

// En el UseCase
public Mono<PageResponse<UserResponse>> execute(int page, int size, String status) {
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
    
    Flux<User> usersFlux = repository.findByRecordStatus(status, pageable);
    Mono<Long> totalMono = repository.countByRecordStatus(status);
    
    return Mono.zip(
        usersFlux.map(mapper::toResponse).collectList(),
        totalMono
    ).map(tuple -> {
        List<UserResponse> content = tuple.getT1();
        long total = tuple.getT2();
        int totalPages = (int) Math.ceil((double) total / size);
        
        return new PageResponse<>(
            content,
            page,
            size,
            total,
            totalPages,
            page == 0,                    // first
            page >= totalPages - 1        // last
        );
    });
}
```

**JSON Response:**
```json
{
  "success": true,
  "message": "Usuarios obtenidos exitosamente",
  "data": {
    "content": [
      {"id": "1", "firstName": "Juan"},
      {"id": "2", "firstName": "María"}
    ],
    "page": 0,
    "size": 10,
    "totalElements": 250,
    "totalPages": 25,
    "isFirst": true,
    "isLast": false
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

### 3️⃣ ErrorMessage.java

**Ubicación:** `application/dto/common/ErrorMessage.java`

**Propósito:** DTO para detalles de errores (validaciones, excepciones).

```java
package pe.edu.vallegrande.vgmsusers.application.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un error individual en la respuesta.
 * Usado para errores de validación y excepciones detalladas.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {
    
    /**
     * Campo/propiedad que causó el error (null para errores generales)
     */
    private String field;
    
    /**
     * Mensaje descriptivo del error
     */
    private String message;
    
    /**
     * Código de error (para i18n o manejo específico en frontend)
     */
    private String code;
    
    // ========== CONSTRUCTORES DE UTILIDAD ==========
    
    /**
     * Constructor para errores de validación (con campo específico)
     */
    public ErrorMessage(String field, String message) {
        this.field = field;
        this.message = message;
    }
    
    /**
     * Constructor para errores generales (sin campo específico)
     */
    public static ErrorMessage general(String message, String code) {
        return new ErrorMessage(null, message, code);
    }
    
    /**
     * Constructor para errores de validación con código
     */
    public static ErrorMessage validation(String field, String message, String code) {
        return new ErrorMessage(field, message, code);
    }
}
```

**Ejemplo de uso:**
```java
// En ExceptionHandler para errores de validación
@ExceptionHandler(MethodArgumentNotValidException.class)
public Mono<ResponseEntity<ApiResponse<Void>>> handleValidationErrors(
    MethodArgumentNotValidException ex
) {
    List<ErrorMessage> errors = ex.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(error -> ErrorMessage.validation(
            error.getField(),
            error.getDefaultMessage(),
            "VALIDATION_ERROR"
        ))
        .toList();
    
    return Mono.just(
        ResponseEntity.badRequest()
            .body(ApiResponse.error("Errores de validación", errors))
    );
}

// Para una excepción específica
@ExceptionHandler(UserNotFoundException.class)
public Mono<ResponseEntity<ApiResponse<Void>>> handleUserNotFound(
    UserNotFoundException ex
) {
    ErrorMessage error = ErrorMessage.general(
        ex.getMessage(),
        "USER_NOT_FOUND"
    );
    
    return Mono.just(
        ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error("Recurso no encontrado", error))
    );
}
```

**JSON Response (Validación):**
```json
{
  "success": false,
  "message": "Errores de validación",
  "errors": [
    {
      "field": "email",
      "message": "Email debe ser válido",
      "code": "VALIDATION_ERROR"
    },
    {
      "field": "firstName",
      "message": "Nombre es obligatorio",
      "code": "VALIDATION_ERROR"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

**JSON Response (Error general):**
```json
{
  "success": false,
  "message": "Recurso no encontrado",
  "errors": [
    {
      "field": null,
      "message": "Usuario con ID 550e8400 no existe",
      "code": "USER_NOT_FOUND"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z"
}
```

---

## ✅ RESUMEN DE LAS 3 CLASES

| Clase | Propósito | Cuándo usar |
|-------|-----------|-------------|
| **ApiResponse<T>** | Wrapper general | TODAS las respuestas REST |
| **PageResponse<T>** | Paginación | Endpoints GET con lista (usuarios, recibos, etc.) |
| **ErrorMessage** | Detalles de errores | Validaciones, excepciones |

**Ubicación en TODOS los microservicios:**
```
application/dto/common/
├── ApiResponse.java     ← Copiar/pegar
├── PageResponse.java    ← Copiar/pegar
└── ErrorMessage.java    ← Copiar/pegar
```

**¡Estas 3 clases son IGUALES en los 11 microservicios!** Solo cambiar el package name. ✅

---

## 📚 DOCUMENTACIÓN SWAGGER

### Ejemplo de Controller Documentado

```java
@RestController
@RequestMapping("/users")
@Tag(name = "Users", description = "API de gestión de usuarios")
public class UserController {
    
    @Operation(
        summary = "Crear usuario",
        description = "Crea un nuevo usuario en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario creado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "Email ya registrado")
    })
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> createUser(
        @Parameter(description = "Datos del nuevo usuario")
        @Valid @RequestBody CreateUserRequest request
    ) {
        // Implementation
    }
    
    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserById(
        @Parameter(description = "ID del usuario", required = true)
        @PathVariable String id
    ) {
        // Implementation
    }
}
```

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

Para CADA endpoint nuevo, verificar:

- [ ] ✅ URL usa sustantivos plurales y kebab-case
- [ ] ✅ JSON usa camelCase
- [ ] ✅ Retorna `Mono<ResponseEntity<ApiResponse<T>>>`
- [ ] ✅ Usa códigos HTTP correctos (200, 201, 404, etc.)
- [ ] ✅ Incluye validaciones con `@Valid`
- [ ] ✅ Maneja errores con `@ExceptionHandler`
- [ ] ✅ Documentado con `@Operation` y `@ApiResponse`
- [ ] ✅ Lee headers `X-Organization-ID` para multi-tenancy
- [ ] ✅ Paginación usa `page`, `size`, `sort`
- [ ] ✅ Respuestas incluyen `timestamp`

---

## 🎯 EJEMPLO COMPLETO

```java
@RestController
@RequestMapping("/users")
@Tag(name = "Users")
@RequiredArgsConstructor
public class UserController {
    
    private final ICreateUserUseCase createUserUseCase;
    private final IGetUserUseCase getUserUseCase;
    
    @Operation(summary = "Crear usuario")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario creado"),
        @ApiResponse(responseCode = "400", description = "Validación fallida")
    })
    @PostMapping
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> createUser(
        @RequestHeader("X-Organization-ID") String organizationId,
        @Valid @RequestBody CreateUserRequest request
    ) {
        return createUserUseCase.execute(request, organizationId)
            .map(user -> ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "Usuario creado exitosamente"))
            );
    }
    
    @Operation(summary = "Obtener usuario por ID")
    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponse>>> getUserById(
        @RequestHeader("X-Organization-ID") String organizationId,
        @PathVariable String id
    ) {
        return getUserUseCase.execute(id, organizationId)
            .map(user -> ResponseEntity.ok(
                ApiResponse.success(user, "Usuario encontrado")
            ))
            .defaultIfEmpty(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Listar usuarios con paginación")
    @GetMapping
    public Mono<ResponseEntity<ApiResponse<PageResponse<UserResponse>>>> getUsers(
        @RequestHeader("X-Organization-ID") String organizationId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(required = false) String status
    ) {
        return getUserUseCase.getAllPaginated(organizationId, page, size, status)
            .map(pageData -> ResponseEntity.ok(
                ApiResponse.success(pageData, "Usuarios obtenidos")
            ));
    }
}
```

---

## 🚀 RESULTADO ESPERADO

**Con estos estándares, TODAS las APIs tendrán:**

1. ✅ URLs consistentes y predecibles
2. ✅ Respuestas JSON uniformes
3. ✅ Códigos HTTP semánticos
4. ✅ Validaciones automáticas
5. ✅ Documentación Swagger completa
6. ✅ Manejo de errores estandarizado
7. ✅ Paginación uniforme
8. ✅ Multi-tenancy mediante headers

**Esto facilita:**
- Frontend puede esperar siempre el mismo formato
- Testing automatizado más simple
- Documentación auto-generada
- Debugging más rápido
- Onboarding de nuevos desarrolladores más fácil
