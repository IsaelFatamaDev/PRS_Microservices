# 🔌 INFRASTRUCTURE LAYER - Capa de Infraestructura

> **Adaptadores que conectan el dominio con tecnologías concretas: MongoDB, RabbitMQ, REST.**

## 📋 Principios

1. **Adaptadores implementan puertos**: Cada interfaz del dominio tiene su implementación concreta
2. **MongoDB Documents**: Entidades separadas del modelo de dominio
3. **Eventos asíncronos**: RabbitMQ con `Mono.fromRunnable` en `Schedulers.boundedElastic`
4. **Sin WebClient/Resilience4j**: Este microservicio NO llama a otros servicios

---

## 📂 Estructura (Arquitectura Hexagonal Estándar)

```text
infrastructure/
│
├── adapters/                                       # 🔄 ADAPTADORES (Hexagonal)
│   ├── in/                                         #    └─ Adaptadores de ENTRADA
│   │   └── rest/                                   #       └─ Controladores REST
│   │       ├── OrganizationRest.java               #          └─ @RestController
│   │       ├── ZoneRest.java                       #          └─ @RestController
│   │       ├── StreetRest.java                     #          └─ @RestController
│   │       ├── FareRest.java                       #          └─ @RestController
│   │       ├── ParameterRest.java                  #          └─ @RestController
│   │       └── GlobalExceptionHandler.java         #          └─ @RestControllerAdvice
│   │
│   └── out/                                        #    └─ Adaptadores de SALIDA
│       ├── persistence/                            #       └─ Implementaciones de Repositorio
│       │   ├── OrganizationRepositoryImpl.java     #          └─ @Repository (impl IOrganizationRepository)
│       │   ├── ZoneRepositoryImpl.java             #          └─ @Repository (impl IZoneRepository)
│       │   ├── StreetRepositoryImpl.java           #          └─ @Repository (impl IStreetRepository)
│       │   ├── FareRepositoryImpl.java             #          └─ @Repository (impl IFareRepository)
│       │   └── ParameterRepositoryImpl.java        #          └─ @Repository (impl IParameterRepository)
│       │
│       └── messaging/                              #       └─ Implementaciones de EventPublisher
│           ├── OrganizationEventPublisherImpl.java #          └─ @Component (impl IOrganizationEventPublisher)
│           ├── ZoneEventPublisherImpl.java         #          └─ @Component (impl IZoneEventPublisher)
│           ├── StreetEventPublisherImpl.java       #          └─ @Component (impl IStreetEventPublisher)
│           ├── FareEventPublisherImpl.java         #          └─ @Component (impl IFareEventPublisher)
│           └── ParameterEventPublisherImpl.java    #          └─ @Component (impl IParameterEventPublisher)
│
├── messaging/                                      # 📬 MESSAGING (Eventos externos)
│   └── listeners/                                  #    └─ Listeners de eventos EXTERNOS
│       └── (vacío - organizations no escucha eventos externos)
│
├── persistence/                                    # 💾 PERSISTENCIA MongoDB
│   ├── documents/                                  #    └─ Documentos MongoDB (@Document)
│   │   ├── OrganizationDocument.java               #       └─ @Document(collection="organizations")
│   │   ├── ZoneDocument.java                       #       └─ @Document(collection="zones")
│   │   ├── StreetDocument.java                     #       └─ @Document(collection="streets")
│   │   ├── FareDocument.java                       #       └─ @Document(collection="fares")
│   │   └── ParameterDocument.java                  #       └─ @Document(collection="parameters")
│   │
│   └── repositories/                               #    └─ Repositorios Reactivos (Spring Data)
│       ├── OrganizationMongoRepository.java        #       └─ Interface extends ReactiveMongoRepository
│       ├── ZoneMongoRepository.java                #       └─ Interface extends ReactiveMongoRepository
│       ├── StreetMongoRepository.java              #       └─ Interface extends ReactiveMongoRepository
│       ├── FareMongoRepository.java                #       └─ Interface extends ReactiveMongoRepository
│       └── ParameterMongoRepository.java           #       └─ Interface extends ReactiveMongoRepository
│
├── security/                                       # 🔐 SEGURIDAD (Headers del Gateway)
│   ├── AuthenticatedUser.java                      #    └─ DTO del usuario autenticado
│   │                                               #       userId, organizationId, roles, email
│   │                                               #       Métodos: isSuperAdmin(), isAdmin(), belongsToOrganization()
│   ├── GatewayHeadersExtractor.java                #    └─ @Component
│   │                                               #       Extrae X-User-Id, X-Organization-Id, X-Roles de headers
│   ├── GatewayHeadersFilter.java                   #    └─ @Component WebFilter
│   │                                               #       Almacena AuthenticatedUser en Reactor Context
│   └── SecurityContextAdapter.java                 #    └─ @Component (impl ISecurityContext)
│                                                   #       Implementa ISecurityContext del dominio
│
└── config/                                         # ⚙️ CONFIGURACIONES
    ├── MongoConfig.java                            #    └─ @Configuration (MongoDB Reactive)
    ├── RabbitMQConfig.java                         #    └─ @Configuration (Exchange, Queues, Bindings)
    ├── SecurityConfig.java                         #    └─ @Configuration (WebFlux Security - sin OAuth2)
    └── RequestContextFilter.java                   #    └─ @Component WebFilter (MDC para logging)
```

---

## 1️⃣ REST CONTROLLERS

### 📄 OrganizationRest.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateOrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateOrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.OrganizationMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/organizations")
@RequiredArgsConstructor
@Tag(name = "Organizations", description = "Gestión de organizaciones JASS")
public class OrganizationRest {

    private final ICreateOrganizationUseCase createUseCase;
    private final IGetOrganizationUseCase getUseCase;
    private final IUpdateOrganizationUseCase updateUseCase;
    private final IDeleteOrganizationUseCase deleteUseCase;
    private final IRestoreOrganizationUseCase restoreUseCase;
    private final OrganizationMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear organización")
    public Mono<ApiResponse<OrganizationResponse>> create(
            @Valid @RequestBody CreateOrganizationRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(org -> ApiResponse.success(mapper.toResponse(org), "Organization created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener organización por ID")
    public Mono<ApiResponse<OrganizationResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
            .map(org -> ApiResponse.success(mapper.toResponse(org), "Organization found"));
    }

    @GetMapping
    @Operation(summary = "Listar organizaciones activas")
    public Mono<ApiResponse<List<OrganizationResponse>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active organizations retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas las organizaciones")
    public Mono<ApiResponse<List<OrganizationResponse>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "All organizations retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar organización")
    public Mono<ApiResponse<OrganizationResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrganizationRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(org -> ApiResponse.success(mapper.toResponse(org), "Organization updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar organización (soft delete)")
    public Mono<ApiResponse<OrganizationResponse>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(org -> ApiResponse.success(mapper.toResponse(org), "Organization deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar organización")
    public Mono<ApiResponse<OrganizationResponse>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(org -> ApiResponse.success(mapper.toResponse(org), "Organization restored successfully"));
    }
}
```

---

### 📄 ZoneRest.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.ZoneResponse;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.ZoneMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/zones")
@RequiredArgsConstructor
@Tag(name = "Zones", description = "Gestión de zonas")
public class ZoneRest {

    private final ICreateZoneUseCase createUseCase;
    private final IGetZoneUseCase getUseCase;
    private final IUpdateZoneUseCase updateUseCase;
    private final IDeleteZoneUseCase deleteUseCase;
    private final IRestoreZoneUseCase restoreUseCase;
    private final ZoneMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear zona")
    public Mono<ApiResponse<ZoneResponse>> create(
            @Valid @RequestBody CreateZoneRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(z -> ApiResponse.success(mapper.toResponse(z), "Zone created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener zona por ID")
    public Mono<ApiResponse<ZoneResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
            .map(z -> ApiResponse.success(mapper.toResponse(z), "Zone found"));
    }

    @GetMapping
    @Operation(summary = "Listar zonas activas")
    public Mono<ApiResponse<List<ZoneResponse>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active zones retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas las zonas")
    public Mono<ApiResponse<List<ZoneResponse>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "All zones retrieved"));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar zonas por organización")
    public Mono<ApiResponse<List<ZoneResponse>>> findByOrganizationId(@PathVariable String organizationId) {
        return getUseCase.findByOrganizationId(organizationId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Zones by organization retrieved"));
    }

    @GetMapping("/organization/{organizationId}/active")
    @Operation(summary = "Listar zonas activas por organización")
    public Mono<ApiResponse<List<ZoneResponse>>> findActiveByOrganizationId(@PathVariable String organizationId) {
        return getUseCase.findActiveByOrganizationId(organizationId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active zones by organization retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar zona")
    public Mono<ApiResponse<ZoneResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateZoneRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(z -> ApiResponse.success(mapper.toResponse(z), "Zone updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar zona (soft delete)")
    public Mono<ApiResponse<ZoneResponse>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(z -> ApiResponse.success(mapper.toResponse(z), "Zone deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar zona")
    public Mono<ApiResponse<ZoneResponse>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(z -> ApiResponse.success(mapper.toResponse(z), "Zone restored successfully"));
    }
}
```

---

### 📄 StreetRest.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateStreetRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateStreetRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.StreetResponse;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.StreetMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/streets")
@RequiredArgsConstructor
@Tag(name = "Streets", description = "Gestión de calles")
public class StreetRest {

    private final ICreateStreetUseCase createUseCase;
    private final IGetStreetUseCase getUseCase;
    private final IUpdateStreetUseCase updateUseCase;
    private final IDeleteStreetUseCase deleteUseCase;
    private final IRestoreStreetUseCase restoreUseCase;
    private final StreetMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear calle")
    public Mono<ApiResponse<StreetResponse>> create(
            @Valid @RequestBody CreateStreetRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(s -> ApiResponse.success(mapper.toResponse(s), "Street created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener calle por ID")
    public Mono<ApiResponse<StreetResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
            .map(s -> ApiResponse.success(mapper.toResponse(s), "Street found"));
    }

    @GetMapping
    @Operation(summary = "Listar calles activas")
    public Mono<ApiResponse<List<StreetResponse>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active streets retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas las calles")
    public Mono<ApiResponse<List<StreetResponse>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "All streets retrieved"));
    }

    @GetMapping("/zone/{zoneId}")
    @Operation(summary = "Listar calles por zona")
    public Mono<ApiResponse<List<StreetResponse>>> findByZoneId(@PathVariable String zoneId) {
        return getUseCase.findByZoneId(zoneId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Streets by zone retrieved"));
    }

    @GetMapping("/zone/{zoneId}/active")
    @Operation(summary = "Listar calles activas por zona")
    public Mono<ApiResponse<List<StreetResponse>>> findActiveByZoneId(@PathVariable String zoneId) {
        return getUseCase.findActiveByZoneId(zoneId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active streets by zone retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar calle")
    public Mono<ApiResponse<StreetResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateStreetRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(s -> ApiResponse.success(mapper.toResponse(s), "Street updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar calle (soft delete)")
    public Mono<ApiResponse<StreetResponse>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(s -> ApiResponse.success(mapper.toResponse(s), "Street deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar calle")
    public Mono<ApiResponse<StreetResponse>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(s -> ApiResponse.success(mapper.toResponse(s), "Street restored successfully"));
    }
}
```

---

### 📄 FareRest.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateFareRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateFareRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.FareResponse;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.FareMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/fares")
@RequiredArgsConstructor
@Tag(name = "Fares", description = "Gestión de tarifas")
public class FareRest {

    private final ICreateFareUseCase createUseCase;
    private final IGetFareUseCase getUseCase;
    private final IUpdateFareUseCase updateUseCase;
    private final IDeleteFareUseCase deleteUseCase;
    private final IRestoreFareUseCase restoreUseCase;
    private final FareMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear tarifa")
    public Mono<ApiResponse<FareResponse>> create(
            @Valid @RequestBody CreateFareRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(f -> ApiResponse.success(mapper.toResponse(f), "Fare created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener tarifa por ID")
    public Mono<ApiResponse<FareResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
            .map(f -> ApiResponse.success(mapper.toResponse(f), "Fare found"));
    }

    @GetMapping
    @Operation(summary = "Listar tarifas activas")
    public Mono<ApiResponse<List<FareResponse>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active fares retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todas las tarifas")
    public Mono<ApiResponse<List<FareResponse>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "All fares retrieved"));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar tarifas por organización")
    public Mono<ApiResponse<List<FareResponse>>> findByOrganizationId(@PathVariable String organizationId) {
        return getUseCase.findByOrganizationId(organizationId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Fares by organization retrieved"));
    }

    @GetMapping("/organization/{organizationId}/active")
    @Operation(summary = "Listar tarifas activas por organización")
    public Mono<ApiResponse<List<FareResponse>>> findActiveByOrganizationId(@PathVariable String organizationId) {
        return getUseCase.findActiveByOrganizationId(organizationId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active fares by organization retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar tarifa")
    public Mono<ApiResponse<FareResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateFareRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(f -> ApiResponse.success(mapper.toResponse(f), "Fare updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar tarifa (soft delete)")
    public Mono<ApiResponse<FareResponse>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(f -> ApiResponse.success(mapper.toResponse(f), "Fare deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar tarifa")
    public Mono<ApiResponse<FareResponse>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(f -> ApiResponse.success(mapper.toResponse(f), "Fare restored successfully"));
    }
}
```

---

### 📄 ParameterRest.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vgmsorganizations.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.CreateParameterRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.request.UpdateParameterRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.response.ParameterResponse;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.ParameterMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.in.*;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/parameters")
@RequiredArgsConstructor
@Tag(name = "Parameters", description = "Gestión de parámetros de configuración")
public class ParameterRest {

    private final ICreateParameterUseCase createUseCase;
    private final IGetParameterUseCase getUseCase;
    private final IUpdateParameterUseCase updateUseCase;
    private final IDeleteParameterUseCase deleteUseCase;
    private final IRestoreParameterUseCase restoreUseCase;
    private final ParameterMapper mapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crear parámetro")
    public Mono<ApiResponse<ParameterResponse>> create(
            @Valid @RequestBody CreateParameterRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return createUseCase.execute(mapper.toModel(request), userId)
            .map(p -> ApiResponse.success(mapper.toResponse(p), "Parameter created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener parámetro por ID")
    public Mono<ApiResponse<ParameterResponse>> findById(@PathVariable String id) {
        return getUseCase.findById(id)
            .map(p -> ApiResponse.success(mapper.toResponse(p), "Parameter found"));
    }

    @GetMapping
    @Operation(summary = "Listar parámetros activos")
    public Mono<ApiResponse<List<ParameterResponse>>> findAllActive() {
        return getUseCase.findAllActive()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active parameters retrieved"));
    }

    @GetMapping("/all")
    @Operation(summary = "Listar todos los parámetros")
    public Mono<ApiResponse<List<ParameterResponse>>> findAll() {
        return getUseCase.findAll()
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "All parameters retrieved"));
    }

    @GetMapping("/organization/{organizationId}")
    @Operation(summary = "Listar parámetros por organización")
    public Mono<ApiResponse<List<ParameterResponse>>> findByOrganizationId(@PathVariable String organizationId) {
        return getUseCase.findByOrganizationId(organizationId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Parameters by organization retrieved"));
    }

    @GetMapping("/organization/{organizationId}/active")
    @Operation(summary = "Listar parámetros activos por organización")
    public Mono<ApiResponse<List<ParameterResponse>>> findActiveByOrganizationId(@PathVariable String organizationId) {
        return getUseCase.findActiveByOrganizationId(organizationId)
            .map(mapper::toResponse)
            .collectList()
            .map(list -> ApiResponse.success(list, "Active parameters by organization retrieved"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar parámetro")
    public Mono<ApiResponse<ParameterResponse>> update(
            @PathVariable String id,
            @Valid @RequestBody UpdateParameterRequest request,
            @RequestHeader("X-User-Id") String userId) {
        return updateUseCase.execute(id, mapper.toModel(request), userId)
            .map(p -> ApiResponse.success(mapper.toResponse(p), "Parameter updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar parámetro (soft delete)")
    public Mono<ApiResponse<ParameterResponse>> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestBody(required = false) Map<String, String> body) {
        String reason = body != null ? body.getOrDefault("reason", "No reason provided") : "No reason provided";
        return deleteUseCase.execute(id, userId, reason)
            .map(p -> ApiResponse.success(mapper.toResponse(p), "Parameter deleted successfully"));
    }

    @PatchMapping("/{id}/restore")
    @Operation(summary = "Restaurar parámetro")
    public Mono<ApiResponse<ParameterResponse>> restore(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        return restoreUseCase.execute(id, userId)
            .map(p -> ApiResponse.success(mapper.toResponse(p), "Parameter restored successfully"));
    }
}
```

---

### 📄 GlobalExceptionHandler.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import pe.edu.vallegrande.vgmsorganizations.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.*;

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
        HttpStatus status = ex.getHttpStatus() != null
            ? HttpStatus.valueOf(ex.getHttpStatus())
            : HttpStatus.INTERNAL_SERVER_ERROR;
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

### 📄 OrganizationRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.OrganizationMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.OrganizationMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrganizationRepositoryImpl implements IOrganizationRepository {

    private final OrganizationMongoRepository mongoRepository;
    private final OrganizationMapper mapper;

    @Override
    public Mono<Organization> save(Organization organization) {
        return mongoRepository.save(mapper.toDocument(organization))
            .map(mapper::toModel);
    }

    @Override
    public Mono<Organization> update(Organization organization) {
        return mongoRepository.save(mapper.toDocument(organization))
            .map(mapper::toModel);
    }

    @Override
    public Mono<Organization> findById(String id) {
        return mongoRepository.findById(id)
            .map(mapper::toModel);
    }

    @Override
    public Flux<Organization> findAll() {
        return mongoRepository.findAll()
            .map(mapper::toModel);
    }

    @Override
    public Flux<Organization> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name())
            .map(mapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByOrganizationName(String name) {
        return mongoRepository.existsByOrganizationName(name);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

### 📄 ZoneRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.ZoneMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.ZoneMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ZoneRepositoryImpl implements IZoneRepository {

    private final ZoneMongoRepository mongoRepository;
    private final ZoneMapper mapper;

    @Override
    public Mono<Zone> save(Zone zone) {
        return mongoRepository.save(mapper.toDocument(zone)).map(mapper::toModel);
    }

    @Override
    public Mono<Zone> update(Zone zone) {
        return mongoRepository.save(mapper.toDocument(zone)).map(mapper::toModel);
    }

    @Override
    public Mono<Zone> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Flux<Zone> findAll() {
        return mongoRepository.findAll().map(mapper::toModel);
    }

    @Override
    public Flux<Zone> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name()).map(mapper::toModel);
    }

    @Override
    public Flux<Zone> findByOrganizationId(String organizationId) {
        return mongoRepository.findByOrganizationId(organizationId).map(mapper::toModel);
    }

    @Override
    public Flux<Zone> findByOrganizationIdAndRecordStatus(String organizationId, RecordStatus status) {
        return mongoRepository.findByOrganizationIdAndRecordStatus(organizationId, status.name()).map(mapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByZoneNameAndOrganizationId(String zoneName, String organizationId) {
        return mongoRepository.existsByZoneNameAndOrganizationId(zoneName, organizationId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

### 📄 StreetRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.StreetMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.StreetMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class StreetRepositoryImpl implements IStreetRepository {

    private final StreetMongoRepository mongoRepository;
    private final StreetMapper mapper;

    @Override
    public Mono<Street> save(Street street) {
        return mongoRepository.save(mapper.toDocument(street)).map(mapper::toModel);
    }

    @Override
    public Mono<Street> update(Street street) {
        return mongoRepository.save(mapper.toDocument(street)).map(mapper::toModel);
    }

    @Override
    public Mono<Street> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Flux<Street> findAll() {
        return mongoRepository.findAll().map(mapper::toModel);
    }

    @Override
    public Flux<Street> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name()).map(mapper::toModel);
    }

    @Override
    public Flux<Street> findByZoneId(String zoneId) {
        return mongoRepository.findByZoneId(zoneId).map(mapper::toModel);
    }

    @Override
    public Flux<Street> findByZoneIdAndRecordStatus(String zoneId, RecordStatus status) {
        return mongoRepository.findByZoneIdAndRecordStatus(zoneId, status.name()).map(mapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByStreetNameAndZoneId(String streetName, String zoneId) {
        return mongoRepository.existsByStreetNameAndZoneId(streetName, zoneId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

### 📄 FareRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.FareMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.FareMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class FareRepositoryImpl implements IFareRepository {

    private final FareMongoRepository mongoRepository;
    private final FareMapper mapper;

    @Override
    public Mono<Fare> save(Fare fare) {
        return mongoRepository.save(mapper.toDocument(fare)).map(mapper::toModel);
    }

    @Override
    public Mono<Fare> update(Fare fare) {
        return mongoRepository.save(mapper.toDocument(fare)).map(mapper::toModel);
    }

    @Override
    public Mono<Fare> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Flux<Fare> findAll() {
        return mongoRepository.findAll().map(mapper::toModel);
    }

    @Override
    public Flux<Fare> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name()).map(mapper::toModel);
    }

    @Override
    public Flux<Fare> findByOrganizationId(String organizationId) {
        return mongoRepository.findByOrganizationId(organizationId).map(mapper::toModel);
    }

    @Override
    public Flux<Fare> findByOrganizationIdAndRecordStatus(String organizationId, RecordStatus status) {
        return mongoRepository.findByOrganizationIdAndRecordStatus(organizationId, status.name()).map(mapper::toModel);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

### 📄 ParameterRepositoryImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.application.mappers.ParameterMapper;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterRepository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories.ParameterMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
@RequiredArgsConstructor
public class ParameterRepositoryImpl implements IParameterRepository {

    private final ParameterMongoRepository mongoRepository;
    private final ParameterMapper mapper;

    @Override
    public Mono<Parameter> save(Parameter parameter) {
        return mongoRepository.save(mapper.toDocument(parameter)).map(mapper::toModel);
    }

    @Override
    public Mono<Parameter> update(Parameter parameter) {
        return mongoRepository.save(mapper.toDocument(parameter)).map(mapper::toModel);
    }

    @Override
    public Mono<Parameter> findById(String id) {
        return mongoRepository.findById(id).map(mapper::toModel);
    }

    @Override
    public Flux<Parameter> findAll() {
        return mongoRepository.findAll().map(mapper::toModel);
    }

    @Override
    public Flux<Parameter> findByRecordStatus(RecordStatus status) {
        return mongoRepository.findByRecordStatus(status.name()).map(mapper::toModel);
    }

    @Override
    public Flux<Parameter> findByOrganizationId(String organizationId) {
        return mongoRepository.findByOrganizationId(organizationId).map(mapper::toModel);
    }

    @Override
    public Flux<Parameter> findByOrganizationIdAndRecordStatus(String organizationId, RecordStatus status) {
        return mongoRepository.findByOrganizationIdAndRecordStatus(organizationId, status.name()).map(mapper::toModel);
    }

    @Override
    public Mono<Boolean> existsByParameterTypeAndOrganizationId(String parameterType, String organizationId) {
        return mongoRepository.existsByParameterTypeAndOrganizationId(parameterType, organizationId);
    }

    @Override
    public Mono<Void> deleteById(String id) {
        return mongoRepository.deleteById(id);
    }
}
```

---

## 3️⃣ ADAPTERS OUT MESSAGING - Event Publishers

### 📄 OrganizationEventPublisherImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.events.*;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IOrganizationEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrganizationEventPublisherImpl implements IOrganizationEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "jass.events";

    @Override
    public Mono<Void> publishOrganizationCreated(Organization org, String createdBy) {
        return Mono.fromRunnable(() -> {
            OrganizationCreatedEvent event = OrganizationCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .organizationId(org.getId())
                .organizationName(org.getOrganizationName())
                .district(org.getDistrict())
                .province(org.getProvince())
                .department(org.getDepartment())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "organization.created", event);
            log.info("Event published: organization.created - {}", org.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishOrganizationUpdated(Organization org, Map<String, Object> changedFields, String updatedBy) {
        return Mono.fromRunnable(() -> {
            OrganizationUpdatedEvent event = OrganizationUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .organizationId(org.getId())
                .changedFields(changedFields)
                .updatedBy(updatedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "organization.updated", event);
            log.info("Event published: organization.updated - {}", org.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishOrganizationDeleted(String organizationId, String reason, String deletedBy) {
        return Mono.fromRunnable(() -> {
            OrganizationDeletedEvent event = OrganizationDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .organizationId(organizationId)
                .previousStatus("ACTIVE")
                .reason(reason)
                .deletedBy(deletedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "organization.deleted", event);
            log.info("Event published: organization.deleted - {}", organizationId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishOrganizationRestored(String organizationId, String restoredBy) {
        return Mono.fromRunnable(() -> {
            OrganizationRestoredEvent event = OrganizationRestoredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .organizationId(organizationId)
                .previousStatus("INACTIVE")
                .restoredBy(restoredBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "organization.restored", event);
            log.info("Event published: organization.restored - {}", organizationId);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
```

---

### 📄 ZoneEventPublisherImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.events.*;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IZoneEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ZoneEventPublisherImpl implements IZoneEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "jass.events";

    @Override
    public Mono<Void> publishZoneCreated(Zone zone, String createdBy) {
        return Mono.fromRunnable(() -> {
            ZoneCreatedEvent event = ZoneCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .zoneId(zone.getId())
                .organizationId(zone.getOrganizationId())
                .zoneName(zone.getZoneName())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "zone.created", event);
            log.info("Event published: zone.created - {}", zone.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishZoneUpdated(Zone zone, Map<String, Object> changedFields, String updatedBy) {
        return Mono.fromRunnable(() -> {
            ZoneUpdatedEvent event = ZoneUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .zoneId(zone.getId())
                .organizationId(zone.getOrganizationId())
                .changedFields(changedFields)
                .updatedBy(updatedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "zone.updated", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishZoneDeleted(String zoneId, String organizationId, String reason, String deletedBy) {
        return Mono.fromRunnable(() -> {
            ZoneDeletedEvent event = ZoneDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .zoneId(zoneId)
                .organizationId(organizationId)
                .reason(reason)
                .deletedBy(deletedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "zone.deleted", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishZoneRestored(String zoneId, String organizationId, String restoredBy) {
        return Mono.fromRunnable(() -> {
            ZoneRestoredEvent event = ZoneRestoredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .zoneId(zoneId)
                .organizationId(organizationId)
                .restoredBy(restoredBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "zone.restored", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
```

---

### 📄 StreetEventPublisherImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.events.*;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IStreetEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreetEventPublisherImpl implements IStreetEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "jass.events";

    @Override
    public Mono<Void> publishStreetCreated(Street street, String createdBy) {
        return Mono.fromRunnable(() -> {
            StreetCreatedEvent event = StreetCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .streetId(street.getId())
                .zoneId(street.getZoneId())
                .organizationId(street.getOrganizationId())
                .streetType(street.getStreetType().name())
                .streetName(street.getStreetName())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "street.created", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishStreetUpdated(Street street, Map<String, Object> changedFields, String updatedBy) {
        return Mono.fromRunnable(() -> {
            StreetUpdatedEvent event = StreetUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .streetId(street.getId())
                .zoneId(street.getZoneId())
                .changedFields(changedFields)
                .updatedBy(updatedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "street.updated", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishStreetDeleted(String streetId, String zoneId, String reason, String deletedBy) {
        return Mono.fromRunnable(() -> {
            StreetDeletedEvent event = StreetDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .streetId(streetId)
                .zoneId(zoneId)
                .reason(reason)
                .deletedBy(deletedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "street.deleted", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishStreetRestored(String streetId, String zoneId, String restoredBy) {
        return Mono.fromRunnable(() -> {
            StreetRestoredEvent event = StreetRestoredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .streetId(streetId)
                .zoneId(zoneId)
                .restoredBy(restoredBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "street.restored", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
```

---

### 📄 FareEventPublisherImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.events.*;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Fare;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IFareEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class FareEventPublisherImpl implements IFareEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "jass.events";

    @Override
    public Mono<Void> publishFareCreated(Fare fare, String createdBy) {
        return Mono.fromRunnable(() -> {
            FareCreatedEvent event = FareCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .fareId(fare.getId())
                .organizationId(fare.getOrganizationId())
                .fareType(fare.getFareType().name())
                .amount(fare.getAmount())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "fare.created", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishFareUpdated(Fare fare, Map<String, Object> changedFields, String updatedBy) {
        return Mono.fromRunnable(() -> {
            FareUpdatedEvent event = FareUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .fareId(fare.getId())
                .organizationId(fare.getOrganizationId())
                .changedFields(changedFields)
                .updatedBy(updatedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "fare.updated", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishFareDeleted(String fareId, String organizationId, String reason, String deletedBy) {
        return Mono.fromRunnable(() -> {
            FareDeletedEvent event = FareDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .fareId(fareId)
                .organizationId(organizationId)
                .reason(reason)
                .deletedBy(deletedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "fare.deleted", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishFareRestored(String fareId, String organizationId, String restoredBy) {
        return Mono.fromRunnable(() -> {
            FareRestoredEvent event = FareRestoredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .fareId(fareId)
                .organizationId(organizationId)
                .restoredBy(restoredBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "fare.restored", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
```

---

### 📄 ParameterEventPublisherImpl.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.out.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.application.events.*;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameter;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.IParameterEventPublisher;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParameterEventPublisherImpl implements IParameterEventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private static final String EXCHANGE = "jass.events";

    @Override
    public Mono<Void> publishParameterCreated(Parameter param, String createdBy) {
        return Mono.fromRunnable(() -> {
            ParameterCreatedEvent event = ParameterCreatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .parameterId(param.getId())
                .organizationId(param.getOrganizationId())
                .parameterType(param.getParameterType().name())
                .parameterValue(param.getParameterValue())
                .createdBy(createdBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "parameter.created", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishParameterUpdated(Parameter param, Map<String, Object> changedFields, String updatedBy) {
        return Mono.fromRunnable(() -> {
            ParameterUpdatedEvent event = ParameterUpdatedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .parameterId(param.getId())
                .organizationId(param.getOrganizationId())
                .changedFields(changedFields)
                .updatedBy(updatedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "parameter.updated", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishParameterDeleted(String parameterId, String organizationId, String reason, String deletedBy) {
        return Mono.fromRunnable(() -> {
            ParameterDeletedEvent event = ParameterDeletedEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .parameterId(parameterId)
                .organizationId(organizationId)
                .reason(reason)
                .deletedBy(deletedBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "parameter.deleted", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<Void> publishParameterRestored(String parameterId, String organizationId, String restoredBy) {
        return Mono.fromRunnable(() -> {
            ParameterRestoredEvent event = ParameterRestoredEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now())
                .parameterId(parameterId)
                .organizationId(organizationId)
                .restoredBy(restoredBy)
                .correlationId(MDC.get("correlationId"))
                .build();
            rabbitTemplate.convertAndSend(EXCHANGE, "parameter.restored", event);
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
```

---

## 4️⃣ PERSISTENCE - MongoDB Documents

### 📄 OrganizationDocument.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents;

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
@Document(collection = "organizations")
public class OrganizationDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    @Field("organization_name")
    private String organizationName;

    @Field("district")
    private String district;

    @Field("province")
    private String province;

    @Field("department")
    private String department;

    @Field("address")
    private String address;

    @Field("phone")
    private String phone;

    @Field("email")
    private String email;

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

### 📄 ZoneDocument.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents;

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
@Document(collection = "zones")
@CompoundIndex(name = "idx_zone_org_name", def = "{'organization_id': 1, 'zone_name': 1}", unique = true)
public class ZoneDocument {

    @Id
    private String id;

    @Indexed
    @Field("organization_id")
    private String organizationId;

    @Field("zone_name")
    private String zoneName;

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

### 📄 StreetDocument.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents;

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
@Document(collection = "streets")
@CompoundIndex(name = "idx_street_zone_name", def = "{'zone_id': 1, 'street_name': 1}", unique = true)
public class StreetDocument {

    @Id
    private String id;

    @Indexed
    @Field("organization_id")
    private String organizationId;

    @Indexed
    @Field("zone_id")
    private String zoneId;

    @Field("street_type")
    private String streetType;

    @Field("street_name")
    private String streetName;

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

### 📄 FareDocument.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents;

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
@Document(collection = "fares")
public class FareDocument {

    @Id
    private String id;

    @Indexed
    @Field("organization_id")
    private String organizationId;

    @Field("fare_type")
    private String fareType;

    @Field("amount")
    private Double amount;

    @Field("description")
    private String description;

    @Field("valid_from")
    private LocalDateTime validFrom;

    @Field("valid_to")
    private LocalDateTime validTo;

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

### 📄 ParameterDocument.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents;

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
@Document(collection = "parameters")
@CompoundIndex(name = "idx_param_org_type", def = "{'organization_id': 1, 'parameter_type': 1}", unique = true)
public class ParameterDocument {

    @Id
    private String id;

    @Indexed
    @Field("organization_id")
    private String organizationId;

    @Field("parameter_type")
    private String parameterType;

    @Field("parameter_value")
    private String parameterValue;

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

## 5️⃣ PERSISTENCE - Reactive Mongo Repositories

### 📄 OrganizationMongoRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.OrganizationDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrganizationMongoRepository extends ReactiveMongoRepository<OrganizationDocument, String> {
    Flux<OrganizationDocument> findByRecordStatus(String recordStatus);
    Mono<Boolean> existsByOrganizationName(String organizationName);
}
```

---

### 📄 ZoneMongoRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.ZoneDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ZoneMongoRepository extends ReactiveMongoRepository<ZoneDocument, String> {
    Flux<ZoneDocument> findByRecordStatus(String recordStatus);
    Flux<ZoneDocument> findByOrganizationId(String organizationId);
    Flux<ZoneDocument> findByOrganizationIdAndRecordStatus(String organizationId, String recordStatus);
    Mono<Boolean> existsByZoneNameAndOrganizationId(String zoneName, String organizationId);
}
```

---

### 📄 StreetMongoRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.StreetDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface StreetMongoRepository extends ReactiveMongoRepository<StreetDocument, String> {
    Flux<StreetDocument> findByRecordStatus(String recordStatus);
    Flux<StreetDocument> findByZoneId(String zoneId);
    Flux<StreetDocument> findByZoneIdAndRecordStatus(String zoneId, String recordStatus);
    Mono<Boolean> existsByStreetNameAndZoneId(String streetName, String zoneId);
}
```

---

### 📄 FareMongoRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.FareDocument;
import reactor.core.publisher.Flux;

@Repository
public interface FareMongoRepository extends ReactiveMongoRepository<FareDocument, String> {
    Flux<FareDocument> findByRecordStatus(String recordStatus);
    Flux<FareDocument> findByOrganizationId(String organizationId);
    Flux<FareDocument> findByOrganizationIdAndRecordStatus(String organizationId, String recordStatus);
    Flux<FareDocument> findByOrganizationIdAndFareTypeAndRecordStatus(String organizationId, String fareType, String recordStatus);
}
```

---

### 📄 ParameterMongoRepository.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.documents.ParameterDocument;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface ParameterMongoRepository extends ReactiveMongoRepository<ParameterDocument, String> {
    Flux<ParameterDocument> findByRecordStatus(String recordStatus);
    Flux<ParameterDocument> findByOrganizationId(String organizationId);
    Flux<ParameterDocument> findByOrganizationIdAndRecordStatus(String organizationId, String recordStatus);
    Mono<Boolean> existsByParameterTypeAndOrganizationId(String parameterType, String organizationId);
}
```

---

## 6️⃣ SECURITY - Seguridad (Headers del Gateway)

> **⚠️ IMPORTANTE**: Estos componentes extraen la información del usuario autenticado desde los headers HTTP que envía el Gateway (X-User-Id, X-Organization-Id, X-Roles). NO hay OAuth2/JWT directo aquí.

### 📄 AuthenticatedUser.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.security;

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

    public boolean canCreateRole(String role) {
        if (isSuperAdmin()) return true;
        if (isAdmin()) {
            return !"SUPER_ADMIN".equals(role) && !"ADMIN".equals(role);
        }
        return false;
    }
}
```

---

### 📄 GatewayHeadersExtractor.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.security;

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
package pe.edu.vallegrande.vgmsorganizations.infrastructure.security;

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
package pe.edu.vallegrande.vgmsorganizations.infrastructure.security;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.ports.out.ISecurityContext;
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

## 7️⃣ CONFIG - Configuraciones

### 📄 MongoConfig.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;

@Configuration
@EnableReactiveMongoRepositories(
    basePackages = "pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.repositories"
)
public class MongoConfig {
}
```

---

### 📄 RabbitMQConfig.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.config;

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

    @Bean
    public TopicExchange jassEventsExchange() {
        return ExchangeBuilder
            .topicExchange(EXCHANGE_NAME)
            .durable(true)
            .build();
    }

    @Bean
    public Queue organizationEventsQueue() {
        return QueueBuilder.durable("organization.events.queue").build();
    }

    @Bean
    public Queue zoneEventsQueue() {
        return QueueBuilder.durable("zone.events.queue").build();
    }

    @Bean
    public Queue streetEventsQueue() {
        return QueueBuilder.durable("street.events.queue").build();
    }

    @Bean
    public Queue fareEventsQueue() {
        return QueueBuilder.durable("fare.events.queue").build();
    }

    @Bean
    public Queue parameterEventsQueue() {
        return QueueBuilder.durable("parameter.events.queue").build();
    }

    @Bean
    public Binding organizationBinding(Queue organizationEventsQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(organizationEventsQueue).to(jassEventsExchange).with("organization.*");
    }

    @Bean
    public Binding zoneBinding(Queue zoneEventsQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(zoneEventsQueue).to(jassEventsExchange).with("zone.*");
    }

    @Bean
    public Binding streetBinding(Queue streetEventsQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(streetEventsQueue).to(jassEventsExchange).with("street.*");
    }

    @Bean
    public Binding fareBinding(Queue fareEventsQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(fareEventsQueue).to(jassEventsExchange).with("fare.*");
    }

    @Bean
    public Binding parameterBinding(Queue parameterEventsQueue, TopicExchange jassEventsExchange) {
        return BindingBuilder.bind(parameterEventsQueue).to(jassEventsExchange).with("parameter.*");
    }

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
package pe.edu.vallegrande.vgmsorganizations.infrastructure.config;

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

### 📄 RequestContextFilter.java

```java
package pe.edu.vallegrande.vgmsorganizations.infrastructure.config;

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

## 8️⃣ YAML - Configuraciones de Aplicación

### 📄 application.yml

```yaml
spring:
  application:
    name: vg-ms-organizations
  profiles:
    active: dev
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
    time-zone: UTC
  main:
    allow-bean-definition-overriding: true

server:
  port: 8083

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
```

---

### 📄 application-dev.yml

```yaml
spring:
  data:
    mongodb:
      # Conexión local (Docker: mongo_jass)
      uri: mongodb://localhost:27017/db_jass_organizations
      auto-index-creation: true

  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest

logging:
  level:
    root: INFO
    pe.edu.vallegrande.vgmsorganizations: DEBUG
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

logging:
  level:
    root: INFO
    pe.edu.vallegrande.vgmsorganizations: INFO
```

---

## 9️⃣ DOCKER CONFIGURATION

### 📄 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY . .
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8083
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

### 📄 docker-compose.yml

```yaml
version: '3.8'

services:
  vg-ms-organizations:
    build: .
    container_name: vg-ms-organizations
    ports:
      - "8083:8083"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - MONGODB_URI=mongodb://mongo-db:27017/db_jass_organizations
      - RABBITMQ_HOST=rabbitmq
      - RABBITMQ_PORT=5672
      - RABBITMQ_USERNAME=guest
      - RABBITMQ_PASSWORD=guest
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

## 🔟 MAVEN CONFIGURATION - pom.xml

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
    <artifactId>vg-ms-organizations</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>vg-ms-organizations</name>
    <description>Microservicio de Organizaciones - Sistema JASS Digital</description>

    <properties>
        <java.version>21</java.version>
        <springdoc.version>2.3.0</springdoc.version>
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
| REST Controllers | 5 | Uno por entidad con CRUD + restore |
| GlobalExceptionHandler | 1 | Manejo centralizado de errores |
| Repository Impls | 5 | Adaptadores MongoDB por entidad |
| Event Publisher Impls | 5 | Publicadores RabbitMQ por entidad |
| MongoDB Documents | 5 | Entidades de persistencia con @Document |
| Mongo Repositories | 5 | ReactiveMongoRepository por entidad |
| Configuraciones | 4 | MongoConfig, RabbitMQConfig, SecurityConfig, RequestContextFilter |
| YAMLs | 3 | application.yml, application-dev.yml, application-prod.yml |
| **TOTAL** | **~33 clases** | |

---

> **Nota**: Este microservicio **NO** incluye WebClientConfig ni Resilience4jConfig porque no realiza llamadas a otros microservicios. Solo publica eventos vía RabbitMQ.
