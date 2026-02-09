# 🏗️ INFRASTRUCTURE LAYER - Capa de Infraestructura

> **Conecta el dominio con el mundo exterior: REST, MongoDB, RabbitMQ, WebClient.**

## 📋 Principios

1. **Adaptadores como puentes**: Implementan los puertos del dominio sin contaminar la lógica de negocio
2. **Configuración externalizada**: Todo en YAML con perfiles `dev` y `prod`
3. **Seguridad por headers**: El Gateway envía `X-User-Id`, `X-Organization-Id`, `X-Roles`
4. **Eventos asíncronos**: RabbitMQ con `Mono.fromRunnable` en `Schedulers.boundedElastic`
5. **WebClient reactivo**: Comunicación con `vg-ms-users` y `vg-ms-organizations` con Circuit Breaker

---

## 📂 Estructura

```text
infrastructure/
├── adapters/
│   ├── in/
│   │   └── rest/                                       # 🌐 REST Controllers
│   │       ├── ComplaintRest.java                      → [CLASS] @RestController @RequestMapping("/api/v1/complaints")
│   │       │                                             @Tag(name = "Complaints")
│   │       │                                             7 endpoints + responses sub-endpoint
│   │       ├── ComplaintCategoryRest.java               → [CLASS] @RestController @RequestMapping("/api/v1/complaint-categories")
│   │       │                                             @Tag(name = "Complaint Categories")
│   │       │                                             8 endpoints CRUD + filters
│   │       ├── IncidentRest.java                        → [CLASS] @RestController @RequestMapping("/api/v1/incidents")
│   │       │                                             @Tag(name = "Incidents")
│   │       │                                             10 endpoints + resolution sub-endpoint
│   │       ├── IncidentTypeRest.java                    → [CLASS] @RestController @RequestMapping("/api/v1/incident-types")
│   │       │                                             @Tag(name = "Incident Types")
│   │       │                                             8 endpoints CRUD + filters
│   │       └── GlobalExceptionHandler.java              → [CLASS] @RestControllerAdvice
│   │                                                      Manejo centralizado de errores
│   └── out/
│       ├── persistence/                                 # 💾 Repository Implementations
│       │   ├── ComplaintRepositoryImpl.java             → [CLASS] @Repository implements IComplaintRepository
│       │   ├── ComplaintCategoryRepositoryImpl.java     → [CLASS] @Repository implements IComplaintCategoryRepository
│       │   ├── ComplaintResponseRepositoryImpl.java     → [CLASS] @Repository implements IComplaintResponseRepository
│       │   ├── IncidentRepositoryImpl.java              → [CLASS] @Repository implements IIncidentRepository
│       │   ├── IncidentTypeRepositoryImpl.java          → [CLASS] @Repository implements IIncidentTypeRepository
│       │   └── IncidentResolutionRepositoryImpl.java    → [CLASS] @Repository implements IIncidentResolutionRepository
│       ├── messaging/                                   # 📨 Event Publishers
│       │   └── ClaimsEventPublisherImpl.java            → [CLASS] @Component implements IClaimsEventPublisher
│       │                                                  10 métodos de publicación
│       └── clients/                                     # 🌐 External Service Clients
│           ├── UserServiceClientImpl.java               → [CLASS] @Component implements IUserServiceClient
│           │                                              WebClient → vg-ms-users:8081
│           └── InfrastructureClientImpl.java             → [CLASS] @Component implements IInfrastructureClient
│                                                          WebClient → vg-ms-organizations:8083
│
├── persistence/
│   ├── documents/                                       # 📄 MongoDB Documents
│   │   ├── ComplaintDocument.java                       → [CLASS] @Document(collection = "complaints")
│   │   ├── ComplaintCategoryDocument.java               → [CLASS] @Document(collection = "complaint_categories")
│   │   ├── ComplaintResponseDocument.java               → [CLASS] @Document(collection = "complaint_responses")
│   │   ├── IncidentDocument.java                        → [CLASS] @Document(collection = "incidents")
│   │   ├── IncidentTypeDocument.java                    → [CLASS] @Document(collection = "incident_types")
│   │   └── IncidentResolutionDocument.java              → [CLASS] @Document(collection = "incident_resolutions")
│   └── repositories/                                    # 🗃️ Reactive Mongo Repositories
│       ├── ComplaintMongoRepository.java                → [INTERFACE] extends ReactiveMongoRepository
│       ├── ComplaintCategoryMongoRepository.java        → [INTERFACE] extends ReactiveMongoRepository
│       ├── ComplaintResponseMongoRepository.java        → [INTERFACE] extends ReactiveMongoRepository
│       ├── IncidentMongoRepository.java                 → [INTERFACE] extends ReactiveMongoRepository
│       ├── IncidentTypeMongoRepository.java             → [INTERFACE] extends ReactiveMongoRepository
│       └── IncidentResolutionMongoRepository.java       → [INTERFACE] extends ReactiveMongoRepository
│
├── security/                                            # 🔒 Security (Gateway Headers)
│   ├── AuthenticatedUser.java                           → [CLASS] DTO del usuario autenticado
│   ├── GatewayHeadersExtractor.java                     → [CLASS] @Component extrae headers HTTP
│   ├── GatewayHeadersFilter.java                        → [CLASS] @Component WebFilter
│   └── SecurityContextAdapter.java                       → [CLASS] @Component implements ISecurityContext
│
└── config/                                              # ⚙️ Configuration
    ├── MongoConfig.java                                 → [CLASS] @Configuration @EnableReactiveMongoRepositories
    ├── RabbitMQConfig.java                              → [CLASS] @Configuration (Exchange, Queues, Bindings)
    ├── SecurityConfig.java                              → [CLASS] @Configuration @EnableWebFluxSecurity
    ├── WebClientConfig.java                             → [CLASS] @Configuration (WebClient.Builder con timeouts)
    └── RequestContextFilter.java                        → [CLASS] @Component WebFilter (correlationId + userId)
```

---

## 1️⃣ ADAPTERS IN REST - Controladores

### 📄 ComplaintRest.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsclaims.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsclaims.application.dto.request.*;
import pe.edu.vallegrande.vgmsclaims.application.dto.response.ComplaintResponseDTO;
import pe.edu.vallegrande.vgmsclaims.application.dto.response.ComplaintResponseItemDTO;
import pe.edu.vallegrande.vgmsclaims.application.mappers.ComplaintMapper;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaint.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
@Tag(name = "Complaints", description = "Gestión de reclamos")
public class ComplaintRest {

    private final ICreateComplaintUseCase createUseCase;
    private final IGetComplaintUseCase getUseCase;
    private final IUpdateComplaintUseCase updateUseCase;
    private final IDeleteComplaintUseCase deleteUseCase;
    private final IRestoreComplaintUseCase restoreUseCase;
    private final IAddResponseUseCase addResponseUseCase;
    private final ICloseComplaintUseCase closeUseCase;
    private final ComplaintMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear reclamo")
    public Mono<ApiResponse<ComplaintResponseDTO>> create(
            @Valid @RequestBody CreateComplaintRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(c -> ApiResponse.success(mapper.toResponseDTO(c), "Complaint created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reclamo por ID")
    public Mono<ApiResponse<ComplaintResponseDTO>> findById(
            @Parameter(description = "ID del reclamo") @PathVariable String id) {
        return getUseCase.findById(id)
            .map(c -> ApiResponse.success(mapper.toResponseDTO(c), "Complaint found"));
    }

    @GetMapping
    @Operation(summary = "Listar reclamos activos")
    public Mono<ApiResponse<List<ComplaintResponseDTO>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active complaints retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los reclamos")
    public Mono<ApiResponse<List<ComplaintResponseDTO>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "All complaints retrieved"));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar reclamos por estado de reclamo")
    public Mono<ApiResponse<List<ComplaintResponseDTO>>> findByStatus(
            @PathVariable String status) {
        return getUseCase.findByStatus(
                pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus.valueOf(status.toUpperCase()))
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Complaints by status retrieved"));
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Listar reclamos por usuario")
    public Mono<ApiResponse<List<ComplaintResponseDTO>>> findByUserId(
            @PathVariable String userId) {
        return getUseCase.findByUserId(userId)
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Complaints by user retrieved"));
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Listar reclamos por categoría")
    public Mono<ApiResponse<List<ComplaintResponseDTO>>> findByCategoryId(
            @PathVariable String categoryId) {
        return getUseCase.findByCategoryId(categoryId)
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Complaints by category retrieved"));
    }

    @GetMapping("/priority/{priority}")
    @Operation(summary = "Listar reclamos por prioridad")
    public Mono<ApiResponse<List<ComplaintResponseDTO>>> findByPriority(
            @PathVariable String priority) {
        return getUseCase.findByPriority(
                pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintPriority.valueOf(priority.toUpperCase()))
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Complaints by priority retrieved"));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar reclamos por organización")
    public Mono<ApiResponse<List<ComplaintResponseDTO>>> findByOrganizationId(
            @PathVariable String organizationId) {
        return getUseCase.findByOrganizationId(organizationId)
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Complaints by organization retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar reclamo")
    public Mono<ApiResponse<ComplaintResponseDTO>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateComplaintRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(c -> ApiResponse.success(mapper.toResponseDTO(c), "Complaint updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reclamo (soft delete)")
    public Mono<ApiResponse<ComplaintResponseDTO>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(c -> ApiResponse.success(mapper.toResponseDTO(c), "Complaint deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar reclamo")
    public Mono<ApiResponse<ComplaintResponseDTO>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(c -> ApiResponse.success(mapper.toResponseDTO(c), "Complaint restored successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // SUB-ENDPOINTS: Respuestas a reclamos
    // ═══════════════════════════════════════════════════════════════

    @PostMapping("/{complaintId}/responses")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Agregar respuesta a un reclamo")
    public Mono<ApiResponse<ComplaintResponseItemDTO>> addResponse(
            @PathVariable String complaintId,
            @Valid @RequestBody AddComplaintResponseRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return addResponseUseCase.execute(complaintId, mapper.toResponseModel(request), userId)
            .map(r -> ApiResponse.success(mapper.toResponseItemDTO(r), "Response added successfully"));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Cerrar reclamo")
    public Mono<ApiResponse<ComplaintResponseDTO>> close(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return closeUseCase.execute(id, userId)
            .map(c -> ApiResponse.success(mapper.toResponseDTO(c), "Complaint closed successfully"));
    }
}
```

---

### 📄 ComplaintCategoryRest.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsclaims.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsclaims.application.dto.request.CreateComplaintCategoryRequest;
import pe.edu.vallegrande.vgmsclaims.application.dto.request.UpdateComplaintCategoryRequest;
import pe.edu.vallegrande.vgmsclaims.application.dto.response.ComplaintCategoryResponse;
import pe.edu.vallegrande.vgmsclaims.application.mappers.ComplaintCategoryMapper;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.complaintcategory.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/complaint-categories")
@RequiredArgsConstructor
@Tag(name = "Complaint Categories", description = "Gestión de categorías de reclamos")
public class ComplaintCategoryRest {

    private final ICreateComplaintCategoryUseCase createUseCase;
    private final IGetComplaintCategoryUseCase getUseCase;
    private final IUpdateComplaintCategoryUseCase updateUseCase;
    private final IDeleteComplaintCategoryUseCase deleteUseCase;
    private final IRestoreComplaintCategoryUseCase restoreUseCase;
    private final ComplaintCategoryMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear categoría de reclamo")
    public Mono<ApiResponse<ComplaintCategoryResponse>> create(
            @Valid @RequestBody CreateComplaintCategoryRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(c -> ApiResponse.success(mapper.toResponse(c), "Complaint category created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener categoría por ID")
    public Mono<ApiResponse<ComplaintCategoryResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
            .map(c -> ApiResponse.success(mapper.toResponse(c), "Complaint category found"));
    }

    @GetMapping
    @Operation(summary = "Listar categorías activas")
    public Mono<ApiResponse<List<ComplaintCategoryResponse>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active complaint categories retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas las categorías")
    public Mono<ApiResponse<List<ComplaintCategoryResponse>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "All complaint categories retrieved"));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar categorías por organización")
    public Mono<ApiResponse<List<ComplaintCategoryResponse>>> findByOrganizationId(
            @PathVariable String organizationId) {
        return getUseCase.findByOrganizationId(organizationId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Complaint categories by organization retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar categoría de reclamo")
    public Mono<ApiResponse<ComplaintCategoryResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateComplaintCategoryRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(c -> ApiResponse.success(mapper.toResponse(c), "Complaint category updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar categoría (soft delete)")
    public Mono<ApiResponse<ComplaintCategoryResponse>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(c -> ApiResponse.success(mapper.toResponse(c), "Complaint category deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar categoría")
    public Mono<ApiResponse<ComplaintCategoryResponse>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(c -> ApiResponse.success(mapper.toResponse(c), "Complaint category restored successfully"));
    }
}
```

---

### 📄 IncidentRest.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsclaims.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsclaims.application.dto.request.*;
import pe.edu.vallegrande.vgmsclaims.application.dto.response.IncidentResponseDTO;
import pe.edu.vallegrande.vgmsclaims.application.dto.response.IncidentResolutionResponseDTO;
import pe.edu.vallegrande.vgmsclaims.application.mappers.IncidentMapper;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incident.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Tag(name = "Incidents", description = "Gestión de incidencias")
public class IncidentRest {

    private final ICreateIncidentUseCase createUseCase;
    private final IGetIncidentUseCase getUseCase;
    private final IUpdateIncidentUseCase updateUseCase;
    private final IDeleteIncidentUseCase deleteUseCase;
    private final IRestoreIncidentUseCase restoreUseCase;
    private final IAssignIncidentUseCase assignUseCase;
    private final IResolveIncidentUseCase resolveUseCase;
    private final ICloseIncidentUseCase closeUseCase;
    private final IncidentMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear incidencia")
    public Mono<ApiResponse<IncidentResponseDTO>> create(
            @Valid @RequestBody CreateIncidentRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(i -> ApiResponse.success(mapper.toResponseDTO(i), "Incident created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener incidencia por ID")
    public Mono<ApiResponse<IncidentResponseDTO>> findById(
            @Parameter(description = "ID de la incidencia") @PathVariable String id) {
        return getUseCase.findById(id)
            .map(i -> ApiResponse.success(mapper.toResponseDTO(i), "Incident found"));
    }

    @GetMapping
    @Operation(summary = "Listar incidencias activas")
    public Mono<ApiResponse<List<IncidentResponseDTO>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active incidents retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas las incidencias")
    public Mono<ApiResponse<List<IncidentResponseDTO>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "All incidents retrieved"));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Listar incidencias por estado")
    public Mono<ApiResponse<List<IncidentResponseDTO>>> findByStatus(
            @PathVariable String status) {
        return getUseCase.findByStatus(
                pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentStatus.valueOf(status.toUpperCase()))
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Incidents by status retrieved"));
    }

    @GetMapping("/type/{typeId}")
    @Operation(summary = "Listar incidencias por tipo")
    public Mono<ApiResponse<List<IncidentResponseDTO>>> findByTypeId(
            @PathVariable String typeId) {
        return getUseCase.findByTypeId(typeId)
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Incidents by type retrieved"));
    }

    @GetMapping("/severity/{severity}")
    @Operation(summary = "Listar incidencias por severidad")
    public Mono<ApiResponse<List<IncidentResponseDTO>>> findBySeverity(
            @PathVariable String severity) {
        return getUseCase.findBySeverity(
                pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentSeverity.valueOf(severity.toUpperCase()))
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Incidents by severity retrieved"));
    }

    @GetMapping("/assigned/{userId}")
    @Operation(summary = "Listar incidencias asignadas a un usuario")
    public Mono<ApiResponse<List<IncidentResponseDTO>>> findByAssignedUser(
            @PathVariable String userId) {
        return getUseCase.findByAssignedUser(userId)
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Incidents assigned to user retrieved"));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar incidencias por organización")
    public Mono<ApiResponse<List<IncidentResponseDTO>>> findByOrganizationId(
            @PathVariable String organizationId) {
        return getUseCase.findByOrganizationId(organizationId)
            .map(mapper::toResponseDTO)
            .collectList()
            .map(list -> ApiResponse.success(list, "Incidents by organization retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar incidencia")
    public Mono<ApiResponse<IncidentResponseDTO>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateIncidentRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(i -> ApiResponse.success(mapper.toResponseDTO(i), "Incident updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar incidencia (soft delete)")
    public Mono<ApiResponse<IncidentResponseDTO>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(i -> ApiResponse.success(mapper.toResponseDTO(i), "Incident deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar incidencia")
    public Mono<ApiResponse<IncidentResponseDTO>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(i -> ApiResponse.success(mapper.toResponseDTO(i), "Incident restored successfully"));
    }

    // ═══════════════════════════════════════════════════════════════
    // SUB-ENDPOINTS: Asignación, Resolución, Cierre
    // ═══════════════════════════════════════════════════════════════

    @PatchMapping("/{id}/assign")
    @Operation(summary = "Asignar técnico a incidencia")
    public Mono<ApiResponse<IncidentResponseDTO>> assign(
            @PathVariable String id,
            @Valid @RequestBody AssignIncidentRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return assignUseCase.execute(id, request.getTechnicianUserId(), userId)
            .map(i -> ApiResponse.success(mapper.toResponseDTO(i), "Incident assigned successfully"));
    }

    @PostMapping("/{incidentId}/resolve")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Resolver incidencia")
    public Mono<ApiResponse<IncidentResolutionResponseDTO>> resolve(
            @PathVariable String incidentId,
            @Valid @RequestBody ResolveIncidentRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return resolveUseCase.execute(incidentId, mapper.toResolutionModel(request), userId)
            .map(r -> ApiResponse.success(mapper.toResolutionResponseDTO(r), "Incident resolved successfully"));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Cerrar incidencia")
    public Mono<ApiResponse<IncidentResponseDTO>> close(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return closeUseCase.execute(id, userId)
            .map(i -> ApiResponse.success(mapper.toResponseDTO(i), "Incident closed successfully"));
    }
}
```

---

### 📄 IncidentTypeRest.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsclaims.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsclaims.application.dto.request.CreateIncidentTypeRequest;
import pe.edu.vallegrande.vgmsclaims.application.dto.request.UpdateIncidentTypeRequest;
import pe.edu.vallegrande.vgmsclaims.application.dto.response.IncidentTypeResponse;
import pe.edu.vallegrande.vgmsclaims.application.mappers.IncidentTypeMapper;
import pe.edu.vallegrande.vgmsclaims.domain.ports.in.incidenttype.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/incident-types")
@RequiredArgsConstructor
@Tag(name = "Incident Types", description = "Gestión de tipos de incidencia")
public class IncidentTypeRest {

    private final ICreateIncidentTypeUseCase createUseCase;
    private final IGetIncidentTypeUseCase getUseCase;
    private final IUpdateIncidentTypeUseCase updateUseCase;
    private final IDeleteIncidentTypeUseCase deleteUseCase;
    private final IRestoreIncidentTypeUseCase restoreUseCase;
    private final IncidentTypeMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear tipo de incidencia")
    public Mono<ApiResponse<IncidentTypeResponse>> create(
            @Valid @RequestBody CreateIncidentTypeRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(t -> ApiResponse.success(mapper.toResponse(t), "Incident type created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tipo por ID")
    public Mono<ApiResponse<IncidentTypeResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
            .map(t -> ApiResponse.success(mapper.toResponse(t), "Incident type found"));
    }

    @GetMapping
    @Operation(summary = "Listar tipos activos")
    public Mono<ApiResponse<List<IncidentTypeResponse>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active incident types retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los tipos")
    public Mono<ApiResponse<List<IncidentTypeResponse>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "All incident types retrieved"));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar tipos por organización")
    public Mono<ApiResponse<List<IncidentTypeResponse>>> findByOrganizationId(
            @PathVariable String organizationId) {
        return getUseCase.findByOrganizationId(organizationId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Incident types by organization retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tipo de incidencia")
    public Mono<ApiResponse<IncidentTypeResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateIncidentTypeRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(t -> ApiResponse.success(mapper.toResponse(t), "Incident type updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tipo (soft delete)")
    public Mono<ApiResponse<IncidentTypeResponse>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(t -> ApiResponse.success(mapper.toResponse(t), "Incident type deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar tipo de incidencia")
    public Mono<ApiResponse<IncidentTypeResponse>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(t -> ApiResponse.success(mapper.toResponse(t), "Incident type restored successfully"));
    }
}
```

---

### 📄 GlobalExceptionHandler.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.in.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import pe.edu.vallegrande.vgmsclaims.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsclaims.application.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsclaims.domain.exceptions.base.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException ex) {
        log.warn("Not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage(),
                ErrorMessage.of(ex.getMessage(), ex.getErrorCode(), 404)));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ApiResponse<Void>> handleConflictException(ConflictException ex) {
        log.warn("Conflict: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage(),
                ErrorMessage.of(ex.getMessage(), ex.getErrorCode(), 409)));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRuleException(BusinessRuleException ex) {
        log.warn("Business rule violation: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
            .body(ApiResponse.error(ex.getMessage(),
                ErrorMessage.of(ex.getMessage(), ex.getErrorCode(), 422)));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(ValidationException ex) {
        log.warn("Validation error: {} - Field: {}", ex.getMessage(), ex.getField());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(ex.getMessage(),
                ErrorMessage.validation(ex.getField(), ex.getMessage(), ex.getErrorCode())));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResponse<Void>> handleBindException(WebExchangeBindException ex) {
        log.warn("Validation errors: {}", ex.getErrorCount());
        List<ErrorMessage> errors = ex.getFieldErrors().stream()
            .map(fe -> ErrorMessage.validation(
                fe.getField(),
                fe.getDefaultMessage(),
                "VALIDATION_ERROR"
            ))
            .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error("Validation errors", errors));
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiResponse<Void>> handleDomainException(DomainException ex) {
        log.error("Domain error: {}", ex.getMessage());
        HttpStatus status = HttpStatus.valueOf(ex.getHttpStatus());
        return ResponseEntity.status(status)
            .body(ApiResponse.error(ex.getMessage(),
                ErrorMessage.of(ex.getMessage(), ex.getErrorCode(), status.value())));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("Internal server error",
                ErrorMessage.of("An unexpected error has occurred", "INTERNAL_ERROR", 500)));
    }
}
```

---

## 2️⃣ ADAPTERS OUT PERSISTENCE - Implementaciones de Repositorios

### 📄 ComplaintRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.application.mappers.ComplaintMapper;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintPriority;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.ComplaintStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintRepository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories.ComplaintMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ComplaintRepositoryImpl implements IComplaintRepository {

    private final ComplaintMongoRepository mongoRepository;
    private final ComplaintMapper mapper;

    @Override
    public Mono<Complaint> save(Complaint complaint) {
        return mongoRepository.save(mapper.toDocument(complaint))
            .map(mapper::toModel);
    }

    @Override
    public Mono<Complaint> update(Complaint complaint) {
        return mongoRepository.save(mapper.toDocument(complaint))
            .map(mapper::toModel);
    }

    @Override
    public Mono<Complaint> findById(String id) {
        return mongoRepository.findById(id)
            .map(mapper::toModel);
    }

    @Override
    public Flux<Complaint> findAll() {
        return mongoRepository.findAll()
            .map(mapper::toModel);
    }

    @Override
    public Flux<Complaint> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name())
            .map(mapper::toModel);
    }

    @Override
    public Flux<Complaint> findByComplaintStatus(ComplaintStatus status) {
        return mongoRepository.findByComplaintStatus(status.name())
            .map(mapper::toModel);
    }

    @Override
    public Flux<Complaint> findByUserId(String userId) {
        return mongoRepository.findByUserId(userId)
            .map(mapper::toModel);
    }

    @Override
    public Flux<Complaint> findByCategoryId(String categoryId) {
        return mongoRepository.findByCategoryId(categoryId)
            .map(mapper::toModel);
    }

    @Override
    public Flux<Complaint> findByComplaintPriority(ComplaintPriority priority) {
        return mongoRepository.findByComplaintPriority(priority.name())
            .map(mapper::toModel);
    }

    @Override
    public Flux<Complaint> findByOrganizationId(String organizationId) {
        return mongoRepository.findByOrganizationId(organizationId)
            .map(mapper::toModel);
    }

    @Override
    public Mono<Long> countByOrganizationId(String organizationId) {
        return mongoRepository.countByOrganizationId(organizationId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

### 📄 ComplaintCategoryRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.application.mappers.ComplaintCategoryMapper;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintCategory;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintCategoryRepository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories.ComplaintCategoryMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ComplaintCategoryRepositoryImpl implements IComplaintCategoryRepository {

    private final ComplaintCategoryMongoRepository mongoRepository;
    private final ComplaintCategoryMapper mapper;

    @Override
    public Mono<ComplaintCategory> save(ComplaintCategory category) {
        return mongoRepository.save(mapper.toDocument(category)).map(mapper::toModel);
    }

    @Override
    public Mono<ComplaintCategory> update(ComplaintCategory category) {
        return mongoRepository.save(mapper.toDocument(category)).map(mapper::toModel);
    }

    @Override
    public Mono<ComplaintCategory> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Flux<ComplaintCategory> findAll() {
        return mongoRepository.findAll().map(mapper::toModel);
    }

    @Override
    public Flux<ComplaintCategory> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name()).map(mapper::toModel);
    }

    @Override
    public Flux<ComplaintCategory> findByOrganizationId(String organizationId) {
        return mongoRepository.findByOrganizationId(organizationId).map(mapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByCategoryCodeAndOrganizationId(String categoryCode, String organizationId) {
        return mongoRepository.existsByCategoryCodeAndOrganizationId(categoryCode, organizationId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

### 📄 ComplaintResponseRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.application.mappers.ComplaintMapper;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintResponse;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IComplaintResponseRepository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories.ComplaintResponseMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ComplaintResponseRepositoryImpl implements IComplaintResponseRepository {

    private final ComplaintResponseMongoRepository mongoRepository;
    private final ComplaintMapper mapper;

    @Override
    public Mono<ComplaintResponse> save(ComplaintResponse response) {
        return mongoRepository.save(mapper.toResponseDocument(response))
            .map(mapper::toResponseModel);
    }

    @Override
    public Flux<ComplaintResponse> findByComplaintId(String complaintId) {
        return mongoRepository.findByComplaintId(complaintId)
            .map(mapper::toResponseModel);
    }

    @Override
    public Mono<ComplaintResponse> findById(String id) {
        return mongoRepository.findById(id)
            .map(mapper::toResponseModel);
    }
}
```

---

### 📄 IncidentRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.application.mappers.IncidentMapper;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentSeverity;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.IncidentStatus;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentRepository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories.IncidentMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class IncidentRepositoryImpl implements IIncidentRepository {

    private final IncidentMongoRepository mongoRepository;
    private final IncidentMapper mapper;

    @Override
    public Mono<Incident> save(Incident incident) {
        return mongoRepository.save(mapper.toDocument(incident))
            .map(mapper::toModel);
    }

    @Override
    public Mono<Incident> update(Incident incident) {
        return mongoRepository.save(mapper.toDocument(incident))
            .map(mapper::toModel);
    }

    @Override
    public Mono<Incident> findById(String id) {
        return mongoRepository.findById(id)
            .map(mapper::toModel);
    }

    @Override
    public Flux<Incident> findAll() {
        return mongoRepository.findAll()
            .map(mapper::toModel);
    }

    @Override
    public Flux<Incident> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name())
            .map(mapper::toModel);
    }

    @Override
    public Flux<Incident> findByIncidentStatus(IncidentStatus status) {
        return mongoRepository.findByIncidentStatus(status.name())
            .map(mapper::toModel);
    }

    @Override
    public Flux<Incident> findByIncidentTypeId(String typeId) {
        return mongoRepository.findByIncidentTypeId(typeId)
            .map(mapper::toModel);
    }

    @Override
    public Flux<Incident> findByIncidentSeverity(IncidentSeverity severity) {
        return mongoRepository.findByIncidentSeverity(severity.name())
            .map(mapper::toModel);
    }

    @Override
    public Flux<Incident> findByAssignedToUserId(String userId) {
        return mongoRepository.findByAssignedToUserId(userId)
            .map(mapper::toModel);
    }

    @Override
    public Flux<Incident> findByOrganizationId(String organizationId) {
        return mongoRepository.findByOrganizationId(organizationId)
            .map(mapper::toModel);
    }

    @Override
    public Mono<Long> countByOrganizationId(String organizationId) {
        return mongoRepository.countByOrganizationId(organizationId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

### 📄 IncidentTypeRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.application.mappers.IncidentTypeMapper;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentType;
import pe.edu.vallegrande.vgmsclaims.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentTypeRepository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories.IncidentTypeMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class IncidentTypeRepositoryImpl implements IIncidentTypeRepository {

    private final IncidentTypeMongoRepository mongoRepository;
    private final IncidentTypeMapper mapper;

    @Override
    public Mono<IncidentType> save(IncidentType type) {
        return mongoRepository.save(mapper.toDocument(type)).map(mapper::toModel);
    }

    @Override
    public Mono<IncidentType> update(IncidentType type) {
        return mongoRepository.save(mapper.toDocument(type)).map(mapper::toModel);
    }

    @Override
    public Mono<IncidentType> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Flux<IncidentType> findAll() {
        return mongoRepository.findAll().map(mapper::toModel);
    }

    @Override
    public Flux<IncidentType> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name()).map(mapper::toModel);
    }

    @Override
    public Flux<IncidentType> findByOrganizationId(String organizationId) {
        return mongoRepository.findByOrganizationId(organizationId).map(mapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByTypeCodeAndOrganizationId(String typeCode, String organizationId) {
        return mongoRepository.existsByTypeCodeAndOrganizationId(typeCode, organizationId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

### 📄 IncidentResolutionRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.application.mappers.IncidentMapper;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentResolution;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IIncidentResolutionRepository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories.IncidentResolutionMongoRepository;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class IncidentResolutionRepositoryImpl implements IIncidentResolutionRepository {

    private final IncidentResolutionMongoRepository mongoRepository;
    private final IncidentMapper mapper;

    @Override
    public Mono<IncidentResolution> save(IncidentResolution resolution) {
        return mongoRepository.save(mapper.toResolutionDocument(resolution))
            .map(mapper::toResolutionModel);
    }

    @Override
    public Mono<IncidentResolution> findByIncidentId(String incidentId) {
        return mongoRepository.findByIncidentId(incidentId)
            .map(mapper::toResolutionModel);
    }

    @Override
    public Mono<IncidentResolution> findById(String id) {
        return mongoRepository.findById(id)
            .map(mapper::toResolutionModel);
    }
}
```

---

## 3️⃣ ADAPTERS OUT MESSAGING - Event Publisher

### 📄 ClaimsEventPublisherImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsclaims.application.events.*;
import pe.edu.vallegrande.vgmsclaims.domain.models.Complaint;
import pe.edu.vallegrande.vgmsclaims.domain.models.ComplaintResponse;
import pe.edu.vallegrande.vgmsclaims.domain.models.Incident;
import pe.edu.vallegrande.vgmsclaims.domain.models.IncidentResolution;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IClaimsEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClaimsEventPublisherImpl implements IClaimsEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "jass.events";

    // ═══════════════════════════════════════════════════════════════
    // COMPLAINT EVENTS
    // ═══════════════════════════════════════════════════════════════

    @Override
    public Mono<Void> publishComplaintCreated(Complaint complaint, String createdBy) {
        return Mono.fromRunnable(() -> {
            ComplaintCreatedEvent event = ComplaintCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .complaintId(complaint.getId())
                .organizationId(complaint.getOrganizationId())
                .complaintCode(complaint.getComplaintCode())
                .userId(complaint.getUserId())
                .categoryId(complaint.getCategoryId())
                .priority(complaint.getComplaintPriority().name())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "complaint.created", event);
            log.info("Event published: complaint.created - {}", complaint.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishComplaintUpdated(Complaint complaint, Map<String, Object> changedFields, String updatedBy) {
        return Mono.fromRunnable(() -> {
            ComplaintUpdatedEvent event = ComplaintUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .complaintId(complaint.getId())
                .organizationId(complaint.getOrganizationId())
                .changedFields(changedFields)
                .updatedBy(updatedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "complaint.updated", event);
            log.info("Event published: complaint.updated - {}", complaint.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishComplaintClosed(String complaintId, String closedBy) {
        return Mono.fromRunnable(() -> {
            ComplaintClosedEvent event = ComplaintClosedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .complaintId(complaintId)
                .closedBy(closedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "complaint.closed", event);
            log.info("Event published: complaint.closed - {}", complaintId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishComplaintResponseAdded(ComplaintResponse response, String respondedBy) {
        return Mono.fromRunnable(() -> {
            ComplaintResponseAddedEvent event = ComplaintResponseAddedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .complaintId(response.getComplaintId())
                .responseId(response.getId())
                .responseType(response.getResponseType().name())
                .respondedBy(respondedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "complaint.response.added", event);
            log.info("Event published: complaint.response.added - complaint: {}", response.getComplaintId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    // ═══════════════════════════════════════════════════════════════
    // INCIDENT EVENTS
    // ═══════════════════════════════════════════════════════════════

    @Override
    public Mono<Void> publishIncidentCreated(Incident incident, String createdBy) {
        return Mono.fromRunnable(() -> {
            IncidentCreatedEvent event = IncidentCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .incidentId(incident.getId())
                .organizationId(incident.getOrganizationId())
                .incidentCode(incident.getIncidentCode())
                .incidentTypeId(incident.getIncidentTypeId())
                .severity(incident.getIncidentSeverity().name())
                .location(incident.getLocation())
                .zoneId(incident.getZoneId())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "incident.created", event);
            log.info("Event published: incident.created - {}", incident.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishIncidentAssigned(Incident incident, String assignedBy) {
        return Mono.fromRunnable(() -> {
            IncidentAssignedEvent event = IncidentAssignedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .incidentId(incident.getId())
                .organizationId(incident.getOrganizationId())
                .assignedToUserId(incident.getAssignedToUserId())
                .assignedBy(assignedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "incident.assigned", event);
            log.info("Event published: incident.assigned - {}", incident.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishIncidentUpdated(Incident incident, Map<String, Object> changedFields, String updatedBy) {
        return Mono.fromRunnable(() -> {
            IncidentUpdatedEvent event = IncidentUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .incidentId(incident.getId())
                .organizationId(incident.getOrganizationId())
                .changedFields(changedFields)
                .updatedBy(updatedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "incident.updated", event);
            log.info("Event published: incident.updated - {}", incident.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishIncidentResolved(Incident incident, IncidentResolution resolution, String resolvedBy) {
        return Mono.fromRunnable(() -> {
            IncidentResolvedEvent event = IncidentResolvedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .incidentId(incident.getId())
                .organizationId(incident.getOrganizationId())
                .resolutionId(resolution.getId())
                .resolutionType(resolution.getResolutionType().name())
                .totalCost(resolution.getTotalCost())
                .resolvedBy(resolvedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "incident.resolved", event);
            log.info("Event published: incident.resolved - {}", incident.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishIncidentClosed(String incidentId, String closedBy) {
        return Mono.fromRunnable(() -> {
            IncidentClosedEvent event = IncidentClosedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .incidentId(incidentId)
                .closedBy(closedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "incident.closed", event);
            log.info("Event published: incident.closed - {}", incidentId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishUrgentIncidentAlert(Incident incident, String createdBy) {
        return Mono.fromRunnable(() -> {
            UrgentIncidentAlertEvent event = UrgentIncidentAlertEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .incidentId(incident.getId())
                .organizationId(incident.getOrganizationId())
                .incidentCode(incident.getIncidentCode())
                .severity(incident.getIncidentSeverity().name())
                .location(incident.getLocation())
                .description(incident.getDescription())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "incident.urgent.alert", event);
            log.info("🚨 URGENT Event published: incident.urgent.alert - {}", incident.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
```

---

## 4️⃣ ADAPTERS OUT CLIENTS - Clientes de Servicios Externos

### 📄 UserServiceClientImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IUserServiceClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Implementación del cliente para comunicación con vg-ms-users.
 *
 * <p>Uso de WebClient reactivo con Circuit Breaker y Retry.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Component
public class UserServiceClientImpl implements IUserServiceClient {

    private final WebClient webClient;

    public UserServiceClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.users.url:http://localhost:8081}") String usersUrl) {
        this.webClient = webClientBuilder.baseUrl(usersUrl).build();
    }

    @Override
    @CircuitBreaker(name = "usersService", fallbackMethod = "existsUserFallback")
    @Retry(name = "usersService")
    public Mono<Boolean> existsUser(String userId) {
        log.debug("Checking if user exists: {}", userId);
        return webClient.get()
            .uri("/api/v1/users/{id}", userId)
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response -> Mono.empty()
            )
            .bodyToMono(Map.class)
            .map(body -> true)
            .defaultIfEmpty(false)
            .doOnSuccess(exists -> log.debug("User {} exists: {}", userId, exists))
            .doOnError(error -> log.warn("Error checking user {}: {}", userId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "usersService", fallbackMethod = "getUserByIdFallback")
    @Retry(name = "usersService")
    @SuppressWarnings("unchecked")
    public Mono<UserInfo> getUserById(String userId) {
        log.debug("Getting user info: {}", userId);
        return webClient.get()
            .uri("/api/v1/users/{id}", userId)
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response -> Mono.empty()
            )
            .bodyToMono(Map.class)
            .map(body -> {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data == null) data = body;
                return new UserInfo(
                    (String) data.get("id"),
                    (String) data.get("organizationId"),
                    (String) data.get("email"),
                    (String) data.get("firstName"),
                    (String) data.get("lastName"),
                    (String) data.get("role")
                );
            })
            .doOnError(error -> log.warn("Error getting user {}: {}", userId, error.getMessage()));
    }

    // ═══════════════════════════════════════════════════════════════
    // FALLBACKS
    // ═══════════════════════════════════════════════════════════════

    @SuppressWarnings("unused")
    private Mono<Boolean> existsUserFallback(String userId, Throwable t) {
        log.error("Circuit breaker opened for existsUser. UserId: {}, Error: {}", userId, t.getMessage());
        return Mono.just(false);
    }

    @SuppressWarnings("unused")
    private Mono<UserInfo> getUserByIdFallback(String userId, Throwable t) {
        log.error("Circuit breaker opened for getUserById. UserId: {}, Error: {}", userId, t.getMessage());
        return Mono.empty();
    }
}
```

---

### 📄 InfrastructureClientImpl.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.adapters.out.clients;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.IInfrastructureClient;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Implementación del cliente para comunicación con vg-ms-organizations.
 *
 * <p>Valida existencia de zonas y calles mediante WebClient reactivo.</p>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Slf4j
@Component
public class InfrastructureClientImpl implements IInfrastructureClient {

    private final WebClient webClient;

    public InfrastructureClientImpl(
            WebClient.Builder webClientBuilder,
            @Value("${services.organizations.url:http://localhost:8083}") String organizationsUrl) {
        this.webClient = webClientBuilder.baseUrl(organizationsUrl).build();
    }

    @Override
    @CircuitBreaker(name = "organizationsService", fallbackMethod = "existsZoneFallback")
    @Retry(name = "organizationsService")
    public Mono<Boolean> existsZone(String zoneId) {
        log.debug("Checking if zone exists: {}", zoneId);
        return webClient.get()
            .uri("/api/v1/zones/{id}", zoneId)
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response -> Mono.empty()
            )
            .bodyToMono(Map.class)
            .map(body -> true)
            .defaultIfEmpty(false)
            .doOnSuccess(exists -> log.debug("Zone {} exists: {}", zoneId, exists))
            .doOnError(error -> log.warn("Error checking zone {}: {}", zoneId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "organizationsService", fallbackMethod = "existsStreetFallback")
    @Retry(name = "organizationsService")
    public Mono<Boolean> existsStreet(String streetId) {
        log.debug("Checking if street exists: {}", streetId);
        return webClient.get()
            .uri("/api/v1/streets/{id}", streetId)
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response -> Mono.empty()
            )
            .bodyToMono(Map.class)
            .map(body -> true)
            .defaultIfEmpty(false)
            .doOnSuccess(exists -> log.debug("Street {} exists: {}", streetId, exists))
            .doOnError(error -> log.warn("Error checking street {}: {}", streetId, error.getMessage()));
    }

    @Override
    @CircuitBreaker(name = "organizationsService", fallbackMethod = "getZoneByIdFallback")
    @Retry(name = "organizationsService")
    @SuppressWarnings("unchecked")
    public Mono<ZoneInfo> getZoneById(String zoneId) {
        log.debug("Getting zone info: {}", zoneId);
        return webClient.get()
            .uri("/api/v1/zones/{id}", zoneId)
            .retrieve()
            .onStatus(
                status -> status.value() == 404,
                response -> Mono.empty()
            )
            .bodyToMono(Map.class)
            .map(body -> {
                Map<String, Object> data = (Map<String, Object>) body.get("data");
                if (data == null) data = body;
                return new ZoneInfo(
                    (String) data.get("id"),
                    (String) data.get("organizationId"),
                    (String) data.get("zoneName"),
                    (String) data.get("description")
                );
            })
            .doOnError(error -> log.warn("Error getting zone {}: {}", zoneId, error.getMessage()));
    }

    // ═══════════════════════════════════════════════════════════════
    // FALLBACKS
    // ═══════════════════════════════════════════════════════════════

    @SuppressWarnings("unused")
    private Mono<Boolean> existsZoneFallback(String zoneId, Throwable t) {
        log.error("Circuit breaker opened for existsZone. ZoneId: {}, Error: {}", zoneId, t.getMessage());
        return Mono.just(false);
    }

    @SuppressWarnings("unused")
    private Mono<Boolean> existsStreetFallback(String streetId, Throwable t) {
        log.error("Circuit breaker opened for existsStreet. StreetId: {}, Error: {}", streetId, t.getMessage());
        return Mono.just(false);
    }

    @SuppressWarnings("unused")
    private Mono<ZoneInfo> getZoneByIdFallback(String zoneId, Throwable t) {
        log.error("Circuit breaker opened for getZoneById. ZoneId: {}, Error: {}", zoneId, t.getMessage());
        return Mono.empty();
    }
}
```

---

## 5️⃣ PERSISTENCE - MongoDB Documents

### 📄 ComplaintDocument.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "complaints")
public class ComplaintDocument {

    @Id
    private String id;

    @Indexed
    @Field("organization_id")
    private String organizationId;

    @Indexed(unique = true)
    @Field("complaint_code")
    private String complaintCode;

    @Indexed
    @Field("user_id")
    private String userId;

    @Indexed
    @Field("category_id")
    private String categoryId;

    @Field("complaint_date")
    private LocalDateTime complaintDate;

    @Indexed
    @Field("complaint_priority")
    private String complaintPriority;

    @Indexed
    @Field("complaint_status")
    private String complaintStatus;

    @Field("description")
    private String description;

    @Field("assigned_to_user_id")
    private String assignedToUserId;

    @Indexed
    @Field("record_status")
    private String recordStatus;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("updated_by")
    private String updatedBy;
}
```

---

### 📄 ComplaintCategoryDocument.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "complaint_categories")
@CompoundIndex(name = "idx_cat_org_code", def = "{'organization_id': 1, 'category_code': 1}", unique = true)
public class ComplaintCategoryDocument {

    @Id
    private String id;

    @Indexed
    @Field("organization_id")
    private String organizationId;

    @Field("category_code")
    private String categoryCode;

    @Field("category_name")
    private String categoryName;

    @Field("description")
    private String description;

    @Indexed
    @Field("record_status")
    private String recordStatus;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("updated_by")
    private String updatedBy;
}
```

---

### 📄 ComplaintResponseDocument.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "complaint_responses")
public class ComplaintResponseDocument {

    @Id
    private String id;

    @Indexed
    @Field("complaint_id")
    private String complaintId;

    @Field("response_date")
    private LocalDateTime responseDate;

    @Field("response_type")
    private String responseType;

    @Field("response_description")
    private String responseDescription;

    @Field("responded_by_user_id")
    private String respondedByUserId;

    @Field("created_at")
    private LocalDateTime createdAt;
}
```

---

### 📄 IncidentDocument.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "incidents")
public class IncidentDocument {

    @Id
    private String id;

    @Indexed
    @Field("organization_id")
    private String organizationId;

    @Indexed(unique = true)
    @Field("incident_code")
    private String incidentCode;

    @Indexed
    @Field("incident_type_id")
    private String incidentTypeId;

    @Field("incident_date")
    private LocalDateTime incidentDate;

    @Field("location")
    private String location;

    @Indexed
    @Field("zone_id")
    private String zoneId;

    @Field("street_id")
    private String streetId;

    @Indexed
    @Field("incident_severity")
    private String incidentSeverity;

    @Indexed
    @Field("incident_status")
    private String incidentStatus;

    @Field("description")
    private String description;

    @Field("reported_by_user_id")
    private String reportedByUserId;

    @Indexed
    @Field("assigned_to_user_id")
    private String assignedToUserId;

    @Indexed
    @Field("record_status")
    private String recordStatus;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("updated_by")
    private String updatedBy;
}
```

---

### 📄 IncidentTypeDocument.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "incident_types")
@CompoundIndex(name = "idx_type_org_code", def = "{'organization_id': 1, 'type_code': 1}", unique = true)
public class IncidentTypeDocument {

    @Id
    private String id;

    @Indexed
    @Field("organization_id")
    private String organizationId;

    @Field("type_code")
    private String typeCode;

    @Field("type_name")
    private String typeName;

    @Field("description")
    private String description;

    @Indexed
    @Field("record_status")
    private String recordStatus;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("created_by")
    private String createdBy;

    @Field("updated_at")
    private LocalDateTime updatedAt;

    @Field("updated_by")
    private String updatedBy;
}
```

---

### 📄 IncidentResolutionDocument.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "incident_resolutions")
public class IncidentResolutionDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("incident_id")
    private String incidentId;

    @Field("resolution_date")
    private LocalDateTime resolutionDate;

    @Field("resolution_type")
    private String resolutionType;

    @Field("resolution_description")
    private String resolutionDescription;

    @Field("materials_used")
    private List<MaterialUsedEmbedded> materialsUsed;

    @Field("total_cost")
    private Double totalCost;

    @Field("resolved_by_user_id")
    private String resolvedByUserId;

    @Field("created_at")
    private LocalDateTime createdAt;

    /**
     * Subdocumento embebido para materiales utilizados.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MaterialUsedEmbedded {

        @Field("material_id")
        private String materialId;

        @Field("quantity")
        private Double quantity;

        @Field("unit")
        private String unit;

        @Field("unit_cost")
        private Double unitCost;
    }
}
```

---

## 6️⃣ PERSISTENCE - Reactive Mongo Repositories

### 📄 ComplaintMongoRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.ComplaintDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ComplaintMongoRepository extends ReactiveMongoRepository<ComplaintDocument, String> {
    Flux<ComplaintDocument> findByRecordStatus(String recordStatus);
    Flux<ComplaintDocument> findByComplaintStatus(String complaintStatus);
    Flux<ComplaintDocument> findByUserId(String userId);
    Flux<ComplaintDocument> findByCategoryId(String categoryId);
    Flux<ComplaintDocument> findByComplaintPriority(String complaintPriority);
    Flux<ComplaintDocument> findByOrganizationId(String organizationId);
    Mono<Long> countByOrganizationId(String organizationId);
}
```

---

### 📄 ComplaintCategoryMongoRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.ComplaintCategoryDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ComplaintCategoryMongoRepository extends ReactiveMongoRepository<ComplaintCategoryDocument, String> {
    Flux<ComplaintCategoryDocument> findByRecordStatus(String recordStatus);
    Flux<ComplaintCategoryDocument> findByOrganizationId(String organizationId);
    Mono<Boolean> existsByCategoryCodeAndOrganizationId(String categoryCode, String organizationId);
}
```

---

### 📄 ComplaintResponseMongoRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.ComplaintResponseDocument;
import reactor.core.publisher.Flux;

@Repository
public interface ComplaintResponseMongoRepository extends ReactiveMongoRepository<ComplaintResponseDocument, String> {
    Flux<ComplaintResponseDocument> findByComplaintId(String complaintId);
}
```

---

### 📄 IncidentMongoRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.IncidentDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface IncidentMongoRepository extends ReactiveMongoRepository<IncidentDocument, String> {
    Flux<IncidentDocument> findByRecordStatus(String recordStatus);
    Flux<IncidentDocument> findByIncidentStatus(String incidentStatus);
    Flux<IncidentDocument> findByIncidentTypeId(String incidentTypeId);
    Flux<IncidentDocument> findByIncidentSeverity(String incidentSeverity);
    Flux<IncidentDocument> findByAssignedToUserId(String assignedToUserId);
    Flux<IncidentDocument> findByOrganizationId(String organizationId);
    Mono<Long> countByOrganizationId(String organizationId);
}
```

---

### 📄 IncidentTypeMongoRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.IncidentTypeDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface IncidentTypeMongoRepository extends ReactiveMongoRepository<IncidentTypeDocument, String> {
    Flux<IncidentTypeDocument> findByRecordStatus(String recordStatus);
    Flux<IncidentTypeDocument> findByOrganizationId(String organizationId);
    Mono<Boolean> existsByTypeCodeAndOrganizationId(String typeCode, String organizationId);
}
```

---

### 📄 IncidentResolutionMongoRepository.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.documents.IncidentResolutionDocument;
import reactor.core.publisher.Mono;

@Repository
public interface IncidentResolutionMongoRepository extends ReactiveMongoRepository<IncidentResolutionDocument, String> {
    Mono<IncidentResolutionDocument> findByIncidentId(String incidentId);
}
```

---

## 7️⃣ SECURITY - Seguridad (Headers del Gateway)

> **⚠️ IMPORTANTE**: Estos componentes extraen la información del usuario autenticado desde los headers HTTP que envía el Gateway (`X-User-Id`, `X-Organization-Id`, `X-Roles`). NO hay OAuth2/JWT directo aquí.

### 📄 AuthenticatedUser.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO que representa al usuario autenticado extraído de los headers del Gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticatedUser {

    private String userId;
    private String organizationId;
    private String email;
    private List<String> roles;

    public boolean isSuperAdmin() {
        return roles != null && roles.contains("SUPER_ADMIN");
    }

    public boolean isAdmin() {
        return roles != null && (roles.contains("ADMIN") || roles.contains("SUPER_ADMIN"));
    }

    public boolean belongsToOrganization(String orgId) {
        if (isSuperAdmin()) return true;
        return organizationId != null && organizationId.equals(orgId);
    }
}
```

---

### 📄 GatewayHeadersExtractor.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Extrae la información del usuario desde los headers HTTP enviados por el Gateway.
 */
@Slf4j
@Component
public class GatewayHeadersExtractor {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_ORGANIZATION_ID = "X-Organization-Id";
    private static final String HEADER_EMAIL = "X-User-Email";
    private static final String HEADER_ROLES = "X-Roles";

    public AuthenticatedUser extract(HttpHeaders headers) {
        String userId = headers.getFirst(HEADER_USER_ID);
        String organizationId = headers.getFirst(HEADER_ORGANIZATION_ID);
        String email = headers.getFirst(HEADER_EMAIL);
        String rolesHeader = headers.getFirst(HEADER_ROLES);

        List<String> roles = Collections.emptyList();
        if (rolesHeader != null && !rolesHeader.isBlank()) {
            roles = Arrays.asList(rolesHeader.split(","));
        }

        log.debug("Headers extracted - userId: {}, orgId: {}, roles: {}", userId, organizationId, roles);

        return AuthenticatedUser.builder()
            .userId(userId)
            .organizationId(organizationId)
            .email(email)
            .roles(roles)
            .build();
    }
}
```

---

### 📄 GatewayHeadersFilter.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

/**
 * WebFilter que extrae los headers del Gateway y almacena el AuthenticatedUser
 * en el contexto reactivo de Reactor para que esté disponible en toda la cadena.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GatewayHeadersFilter implements WebFilter {

    public static final String AUTHENTICATED_USER_KEY = "authenticatedUser";

    private final GatewayHeadersExtractor extractor;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        AuthenticatedUser user = extractor.extract(exchange.getRequest().getHeaders());

        return chain.filter(exchange)
            .contextWrite(Context.of(AUTHENTICATED_USER_KEY, user));
    }
}
```

---

### 📄 SecurityContextAdapter.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.security;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsclaims.domain.ports.out.ISecurityContext;
import reactor.core.publisher.Mono;

/**
 * Adaptador que implementa ISecurityContext del dominio.
 * Obtiene el usuario autenticado desde el contexto reactivo.
 */
@Component
public class SecurityContextAdapter implements ISecurityContext {

    @Override
    public Mono<String> getCurrentUserId() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(GatewayHeadersFilter.AUTHENTICATED_USER_KEY)) {
                AuthenticatedUser user = ctx.get(GatewayHeadersFilter.AUTHENTICATED_USER_KEY);
                return Mono.justOrEmpty(user.getUserId());
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<String> getCurrentOrganizationId() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(GatewayHeadersFilter.AUTHENTICATED_USER_KEY)) {
                AuthenticatedUser user = ctx.get(GatewayHeadersFilter.AUTHENTICATED_USER_KEY);
                return Mono.justOrEmpty(user.getOrganizationId());
            }
            return Mono.empty();
        });
    }

    @Override
    public Mono<Boolean> isSuperAdmin() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(GatewayHeadersFilter.AUTHENTICATED_USER_KEY)) {
                AuthenticatedUser user = ctx.get(GatewayHeadersFilter.AUTHENTICATED_USER_KEY);
                return Mono.just(user.isSuperAdmin());
            }
            return Mono.just(false);
        });
    }

    @Override
    public Mono<Boolean> isAdmin() {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(GatewayHeadersFilter.AUTHENTICATED_USER_KEY)) {
                AuthenticatedUser user = ctx.get(GatewayHeadersFilter.AUTHENTICATED_USER_KEY);
                return Mono.just(user.isAdmin());
            }
            return Mono.just(false);
        });
    }

    @Override
    public Mono<Boolean> belongsToOrganization(String organizationId) {
        return Mono.deferContextual(ctx -> {
            if (ctx.hasKey(GatewayHeadersFilter.AUTHENTICATED_USER_KEY)) {
                AuthenticatedUser user = ctx.get(GatewayHeadersFilter.AUTHENTICATED_USER_KEY);
                return Mono.just(user.belongsToOrganization(organizationId));
            }
            return Mono.just(false);
        });
    }
}
```

---

## 8️⃣ CONFIG - Configuraciones

### 📄 MongoConfig.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = "pe.edu.vallegrande.vgmsclaims.infrastructure.persistence.repositories"
)
public class MongoConfig {
}
```

---

### 📄 RabbitMQConfig.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "jass.events";

    // ═══════════════════════════════════════════════════════════════
    // EXCHANGE
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public TopicExchange jassEventsExchange() {
        return ExchangeBuilder
            .topicExchange(EXCHANGE_NAME)
            .durable(true)
            .build();
    }

    // ═══════════════════════════════════════════════════════════════
    // QUEUES - Complaint Events
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Queue complaintEventsQueue() {
        return QueueBuilder.durable("complaint.events.queue").build();
    }

    @Bean
    public Queue complaintResponseEventsQueue() {
        return QueueBuilder.durable("complaint.response.events.queue").build();
    }

    // ═══════════════════════════════════════════════════════════════
    // QUEUES - Incident Events
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Queue incidentEventsQueue() {
        return QueueBuilder.durable("incident.events.queue").build();
    }

    @Bean
    public Queue incidentUrgentQueue() {
        return QueueBuilder.durable("incident.urgent.queue").build();
    }

    // ═══════════════════════════════════════════════════════════════
    // BINDINGS - Complaint
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Binding complaintBinding(Queue complaintEventsQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(complaintEventsQueue).to(jassEventsExchange).with("complaint.*");
    }

    @Bean
    public Binding complaintResponseBinding(Queue complaintResponseEventsQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(complaintResponseEventsQueue).to(jassEventsExchange).with("complaint.response.*");
    }

    // ═══════════════════════════════════════════════════════════════
    // BINDINGS - Incident
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public Binding incidentBinding(Queue incidentEventsQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(incidentEventsQueue).to(jassEventsExchange).with("incident.*");
    }

    @Bean
    public Binding incidentUrgentBinding(Queue incidentUrgentQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(incidentUrgentQueue).to(jassEventsExchange).with("incident.urgent.*");
    }

    // ═══════════════════════════════════════════════════════════════
    // SERIALIZATION
    // ═══════════════════════════════════════════════════════════════

    @Bean
    public MessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jackson2JsonMessageConverter());
        return template;
    }
}
```

---

### 📄 SecurityConfig.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)
            .authorizeExchange(exchanges -> exchanges
                .pathMatchers("/actuator/**").permitAll()
                .pathMatchers("/v3/api-docs/**").permitAll()
                .pathMatchers("/swagger-ui/**").permitAll()
                .pathMatchers("/swagger-ui.html").permitAll()
                .pathMatchers("/webjars/**").permitAll()
                .pathMatchers("/api/**").permitAll()
                .anyExchange().authenticated()
            )
            .build();
    }
}
```

---

### 📄 WebClientConfig.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.config;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Configuración de WebClient para comunicación HTTP reactiva con otros microservicios.
 *
 * <p>Servicios consumidos:</p>
 * <ul>
 *   <li>vg-ms-users (puerto 8081) - Validación de usuarios</li>
 *   <li>vg-ms-organizations (puerto 8083) - Validación de zonas/calles</li>
 * </ul>
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Configuration
public class WebClientConfig {

    @Value("${webclient.timeout.connect:5000}")
    private int connectTimeout;

    @Value("${webclient.timeout.read:10000}")
    private int readTimeout;

    @Value("${webclient.timeout.write:10000}")
    private int writeTimeout;

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
            .responseTimeout(Duration.ofMillis(readTimeout))
            .doOnConnected(conn -> conn
                .addHandlerLast(new ReadTimeoutHandler(readTimeout, TimeUnit.MILLISECONDS))
                .addHandlerLast(new WriteTimeoutHandler(writeTimeout, TimeUnit.MILLISECONDS))
            );

        return WebClient.builder()
            .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}
```

---

### 📄 Resilience4jConfig.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuración de Resilience4j para Circuit Breaker y Retry
 * en las llamadas a microservicios externos.
 *
 * @author Valle Grande
 * @since 1.0.0
 */
@Configuration
public class Resilience4jConfig {

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .permittedNumberOfCallsInHalfOpenState(3)
            .build();

        return CircuitBreakerRegistry.of(config);
    }

    @Bean
    public RetryRegistry retryRegistry() {
        RetryConfig config = RetryConfig.custom()
            .maxAttempts(3)
            .waitDuration(Duration.ofMillis(500))
            .build();

        return RetryRegistry.of(config);
    }
}
```

---

### 📄 RequestContextFilter.java

```java
package pe.edu.vallegrande.vgmsclaims.infrastructure.config;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
public class RequestContextFilter implements WebFilter {

    private static final String CORRELATION_ID = "correlationId";
    private static final String USER_ID = "userId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest().getHeaders().getFirst("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
        }
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        MDC.put(CORRELATION_ID, correlationId);
        if (userId != null) {
            MDC.put(USER_ID, userId);
        }

        exchange.getResponse().getHeaders().add("X-Correlation-Id", correlationId);

        final String finalCorrelationId = correlationId;
        final String finalUserId = userId;

        return chain.filter(exchange)
            .contextWrite(ctx -> {
                Context context = ctx.put(CORRELATION_ID, finalCorrelationId);
                if (finalUserId != null) {
                    context = context.put(USER_ID, finalUserId);
                }
                return context;
            })
            .doFinally(signalType -> MDC.clear());
    }
}
```

---

## 9️⃣ YAML - Configuraciones de Aplicación

### 📄 application.yml

```yaml
spring:
  application:
    name: vg-ms-claims-incidents
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: UTC
  main:
    allow-bean-definition-overriding: true

server:
  port: 8085

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  health:
    mongo:
      enabled: true
    rabbit:
      enabled: true

springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    path: /swagger-ui.html
    operations-sorter: alpha
    tags-sorter: alpha

# WebClient timeouts (defaults)
webclient:
  timeout:
    connect: 5000
    read: 10000
    write: 10000
```

---

### 📄 application-dev.yml

```yaml
spring:
  data:
    mongodb:
      # Conexión local (Docker: mongo_jass)
      uri: mongodb://localhost:27017/db_jass_claims
      auto-index-creation: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

# URLs de microservicios en desarrollo
services:
  users:
    url: http://localhost:8081
  organizations:
    url: http://localhost:8083

logging:
  level:
    root: INFO
    pe.edu.vallegrande.vgmsclaims: DEBUG
    org.springframework.data.mongodb: DEBUG
```

---

### 📄 application-prod.yml

```yaml
spring:
  data:
    mongodb:
      uri: ${MONGODB_URI}

  rabbitmq:
    host: ${RABBITMQ_HOST}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME}
    password: ${RABBITMQ_PASSWORD}
    virtual-host: ${RABBITMQ_VHOST:/}

# URLs de microservicios en producción
services:
  users:
    url: ${USERS_SERVICE_URL:http://vg-ms-users:8081}
  organizations:
    url: ${ORGANIZATIONS_SERVICE_URL:http://vg-ms-organizations:8083}

logging:
  level:
    root: INFO
    pe.edu.vallegrande.vgmsclaims: INFO
```

---

## 🔟 DOCKER CONFIGURATION

### 📄 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 📄 docker-compose.yml

```yaml
version: '3.8'

services:
  vg-ms-claims-incidents:
    build: .
    container_name: vg-ms-claims-incidents
    ports:
      - "8085:8085"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MONGODB_URI=mongodb://mongo-db:27017/db_jass_claims
      - RABBITMQ_HOST=rabbitmq
      - RABBITMQ_PORT=5672
      - RABBITMQ_USERNAME=guest
      - RABBITMQ_PASSWORD=guest
      - USERS_SERVICE_URL=http://vg-ms-users:8081
      - ORGANIZATIONS_SERVICE_URL=http://vg-ms-organizations:8083
    depends_on:
      - mongo-db
      - rabbitmq

  mongo-db:
    image: mongo:latest
    container_name: mongo_jass
    ports:
      - "27017:27017"
    volumes:
      - jass_mongo_data:/data/db

  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbit_jass
    ports:
      - "5672:5672"
      - "15672:15672"
    environment:
      - RABBITMQ_DEFAULT_USER=guest
      - RABBITMQ_DEFAULT_PASS=guest
    volumes:
      - jass_rabbit_data:/var/lib/rabbitmq

volumes:
  jass_mongo_data:
  jass_rabbit_data:
```

---

## 1️⃣1️⃣ MAVEN CONFIGURATION - pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.5.10</version>
        <relativePath/>
    </parent>

    <groupId>pe.edu.vallegrande</groupId>
    <artifactId>vg-ms-claims-incidents</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>vg-ms-claims-incidents</name>
    <description>Microservicio de Reclamos e Incidencias - Sistema JASS Digital</description>

    <properties>
        <java.version>21</java.version>
        <springdoc.version>2.3.0</springdoc.version>
        <resilience4j.version>2.2.0</resilience4j.version>
    </properties>

    <dependencies>
        <!-- Spring WebFlux -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webflux</artifactId>
        </dependency>

        <!-- MongoDB Reactive -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-mongodb-reactive</artifactId>
        </dependency>

        <!-- RabbitMQ -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-amqp</artifactId>
        </dependency>

        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>

        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>

        <!-- Actuator -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>

        <!-- SpringDoc OpenAPI -->
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
            <version>${springdoc.version}</version>
        </dependency>

        <!-- Resilience4j - Circuit Breaker & Retry -->
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-spring-boot3</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>
        <dependency>
            <groupId>io.github.resilience4j</groupId>
            <artifactId>resilience4j-reactor</artifactId>
            <version>${resilience4j.version}</version>
        </dependency>

        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>

        <!-- Test -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>io.projectreactor</groupId>
            <artifactId>reactor-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

---

## ✅ Resumen de la Capa de Infraestructura

| Componente | Cantidad | Descripción |
| :--- | :--- | :--- |
| REST Controllers | 4 | ComplaintRest, ComplaintCategoryRest, IncidentRest, IncidentTypeRest |
| GlobalExceptionHandler | 1 | Manejo centralizado de errores del dominio |
| Repository Impls | 6 | Adaptadores MongoDB por entidad |
| Event Publisher Impl | 1 | ClaimsEventPublisherImpl (10 métodos) |
| External Clients | 2 | UserServiceClientImpl, InfrastructureClientImpl |
| MongoDB Documents | 6 | Entidades de persistencia con @Document |
| Mongo Repositories | 6 | ReactiveMongoRepository por entidad |
| Security | 4 | AuthenticatedUser, Extractor, Filter, SecurityContextAdapter |
| Configuraciones | 6 | MongoConfig, RabbitMQConfig, SecurityConfig, WebClientConfig, Resilience4jConfig, RequestContextFilter |
| YAMLs | 3 | application.yml, application-dev.yml, application-prod.yml |
| Docker | 2 | Dockerfile, docker-compose.yml |
| **TOTAL** | **~41 clases** | |

---

### Diferencias con vg-ms-organizations

| Aspecto | organizations | claims-incidents |
| :--- | :--- | :--- |
| Puerto | 8083 | 8085 |
| Base de datos | db_jass_organizations | db_jass_claims |
| WebClient | ❌ No necesita | ✅ Llama a users y organizations |
| Resilience4j | ❌ No incluido | ✅ Circuit Breaker + Retry |
| Event Publisher | 5 separados | 1 unificado (10 métodos) |
| Clientes externos | ❌ Ninguno | 2 (UserServiceClient, InfrastructureClient) |
| Collections MongoDB | 5 | 6 (incluye complaint_responses e incident_resolutions) |
| Endpoints especiales | ❌ Solo CRUD | ✅ assign, resolve, close, addResponse |

---

> **Siguiente paso**: Con las 3 capas documentadas, el equipo tiene la guía completa para implementar `vg-ms-claims-incidents` siguiendo la arquitectura hexagonal estándar del Sistema JASS Digital.
