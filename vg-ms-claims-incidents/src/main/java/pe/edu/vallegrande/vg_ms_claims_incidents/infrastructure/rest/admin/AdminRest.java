package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.rest.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.IncidentResolutionService;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.IncidentService;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.IncidentTypeService;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.UserEnrichmentService;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.client.UserApiClient;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentCreateDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentEnrichedDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentResolutionDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentTypeDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.UserDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.common.ResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para operaciones de administración de incidentes
 * Requiere rol ADMIN para todos los endpoints
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "API de administración para gestión completa de incidentes y resoluciones")
public class AdminRest {

    private final IncidentService incidentService;
    private final IncidentTypeService incidentTypeService;
    private final UserEnrichmentService userEnrichmentService;
    private final UserApiClient userApiClient;
    private final IncidentResolutionService incidentResolutionService;

    public AdminRest(IncidentService incidentService,
                     IncidentTypeService incidentTypeService,
                     UserEnrichmentService userEnrichmentService,
                     UserApiClient userApiClient,
                     IncidentResolutionService incidentResolutionService) {
        this.incidentService = incidentService;
        this.incidentTypeService = incidentTypeService;
        this.userEnrichmentService = userEnrichmentService;
        this.userApiClient = userApiClient;
        this.incidentResolutionService = incidentResolutionService;
    }

    // ================================
    // ENDPOINTS DE GESTIÓN DE INCIDENTES
    // ================================

    /**
     * Gestionar incidentes con filtros
     */
    @Operation(summary = "Gestionar incidentes", 
               description = "Obtiene incidentes filtrados por zona, organización, severidad o estado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Incidentes obtenidos exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos de administrador"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/manage")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> manageIncidents(
            @Parameter(description = "ID de la zona") @RequestParam(required = false) String zoneId,
            @Parameter(description = "ID de la organización") @RequestParam(required = false) String organizationId,
            @Parameter(description = "Nivel de severidad") @RequestParam(required = false) String severity,
            @Parameter(description = "Estado del incidente") @RequestParam(required = false) String status) {
        
        log.info("Admin - Gestionando incidentes con filtros: zoneId={}, organizationId={}, severity={}, status={}", 
                 zoneId, organizationId, severity, status);
        
        Flux<IncidentDTO> incidents;
        
        if (zoneId != null) {
            incidents = incidentService.findByZoneId(zoneId);
        } else if (organizationId != null) {
            incidents = incidentService.findByOrganizationId(organizationId);
        } else if (severity != null) {
            incidents = incidentService.findBySeverity(severity);
        } else if (status != null) {
            incidents = incidentService.findByStatus(status);
        } else {
            incidents = incidentService.findAll();
        }
        
        return incidents.collectList()
                .map(list -> ResponseEntity.ok(
                    ResponseDto.success(list, "Incidentes obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Se obtuvieron {} incidentes", 
                    result.getBody().getData().size()));
    }

    /**
     * Asignar responsable a incidente
     */
    @Operation(summary = "Asignar responsable", 
               description = "Asigna un técnico responsable a un incidente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Responsable asignado exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping("/incidents/assign")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> assignResponsible(
            @RequestBody Map<String, String> assignmentData) {
        
        log.info("Admin - Asignando responsable a incidente");
        
        String incidentId = assignmentData.get("incidentId");
        String assignedToUserId = assignmentData.get("assignedToUserId");
        
        if (incidentId == null || assignedToUserId == null) {
            throw new IllegalArgumentException("incidentId y assignedToUserId son requeridos");
        }
        
        log.info("Asignando usuario {} al incidente {}", assignedToUserId, incidentId);
        
        return incidentService.findById(incidentId)
                .flatMap(incident -> {
                    incident.setAssignedToUserId(assignedToUserId);
                    incident.setStatus("ASSIGNED");
                    return incidentService.update(incidentId, incident);
                })
                .map(updated -> ResponseEntity.ok(
                    ResponseDto.success(updated, "Responsable asignado exitosamente")
                ))
                .doOnSuccess(result -> log.info("Incidente {} asignado a usuario {}", 
                    incidentId, assignedToUserId));
    }

    /**
     * Resolver incidente
     */
    @Operation(summary = "Resolver incidente", 
               description = "Marca un incidente como resuelto con notas de resolución")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Incidente resuelto exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
        @ApiResponse(responseCode = "409", description = "El incidente no puede ser resuelto en su estado actual"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping("/incidents/resolve")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> resolveIncident(
            @RequestBody Map<String, Object> resolutionData) {
        
        log.info("Admin - Resolviendo incidente");
        
        String incidentId = (String) resolutionData.get("incidentId");
        String resolvedByUserId = (String) resolutionData.get("resolvedByUserId");
        String resolutionNotes = (String) resolutionData.get("resolutionNotes");
        
        if (incidentId == null || resolvedByUserId == null) {
            throw new IllegalArgumentException("incidentId y resolvedByUserId son requeridos");
        }
        
        log.info("Resolviendo incidente {} por usuario {}", incidentId, resolvedByUserId);
        
        return incidentService.findById(incidentId)
                .flatMap(incident -> {
                    incident.setResolvedByUserId(resolvedByUserId);
                    incident.setResolutionNotes(resolutionNotes);
                    incident.setResolved(true);
                    incident.setStatus("RESOLVED");
                    return incidentService.update(incidentId, incident);
                })
                .map(resolved -> ResponseEntity.ok(
                    ResponseDto.success(resolved, "Incidente resuelto exitosamente")
                ))
                .doOnSuccess(result -> log.info("Incidente {} resuelto por {}", 
                    incidentId, resolvedByUserId));
    }

    /**
     * Obtener todos los incidentes
     */
    @Operation(summary = "Obtener todos los incidentes", 
               description = "Retorna la lista completa de incidentes del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Incidentes obtenidos exitosamente"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "403", description = "Sin permisos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getAllIncidents() {
        log.info("Admin - Obteniendo todos los incidentes");
        
        return incidentService.findAll()
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                    ResponseDto.success(incidents, "Incidentes obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Se obtuvieron {} incidentes", 
                    result.getBody().getData().size()))
                .doOnError(error -> log.error("Error obteniendo incidentes: {}", error.getMessage()));
    }

    /**
     * Obtener incidente por ID
     */
    @Operation(summary = "Obtener incidente por ID", 
               description = "Retorna los detalles de un incidente específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Incidente encontrado"),
        @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
        @ApiResponse(responseCode = "401", description = "No autorizado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/{id}")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> getIncidentById(
            @Parameter(description = "ID del incidente", required = true) @PathVariable String id) {
        
        log.info("Admin - Obteniendo incidente con ID: {}", id);
        
        return incidentService.findById(id)
                .map(incident -> ResponseEntity.ok(
                    ResponseDto.success(incident, "Incidente obtenido exitosamente")
                ))
                .doOnSuccess(result -> log.info("Incidente encontrado: {}", id));
    }

    @Operation(
            summary = "Crear nuevo incidente",
            description = "Crea un nuevo incidente en el sistema. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Incidente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "Conflicto - El código de incidente ya existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/incidents")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> createIncident(
            @Valid @RequestBody IncidentCreateDTO incidentCreateDTO) {
        
        log.info("Admin - Creando nuevo incidente: {}", incidentCreateDTO.getIncidentCode());
        
        validateCreateDTO(incidentCreateDTO);
        IncidentDTO incidentDTO = convertToIncidentDTO(incidentCreateDTO);
        
        return incidentService.save(incidentDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(
                        ResponseDto.created(saved, "Incidente creado exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Incidente creado exitosamente: {}", incidentDTO.getIncidentCode()))
                .doOnError(error -> log.error("Admin - Error al crear incidente: {}", error.getMessage()));
    }

    @Operation(
            summary = "Actualizar incidente",
            description = "Actualiza los datos de un incidente existente. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidente actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/incidents/{id}")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> updateIncident(
            @PathVariable String id, 
            @Valid @RequestBody IncidentDTO incidentDTO) {
        log.info("Admin - Actualizando incidente con ID: {}", id);
        return incidentService.update(id, incidentDTO)
                .map(updated -> ResponseEntity.ok(
                        ResponseDto.success(updated, "Incidente actualizado exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Incidente actualizado: {}", id));
    }

    @Operation(
            summary = "Eliminar incidente",
            description = "Realiza un borrado lógico de un incidente (soft delete). Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Incidente eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/incidents/{id}")
    public Mono<ResponseEntity<Void>> deleteIncident(@PathVariable String id) {
        log.info("Admin - Eliminando incidente con ID: {}", id);
        return incidentService.deleteById(id)
                .then(Mono.fromCallable(() -> {
                    log.info("Admin - Incidente eliminado: {}", id);
                    return ResponseEntity.noContent().<Void>build();
                }));
    }

    @Operation(
            summary = "Restaurar incidente eliminado",
            description = "Restaura un incidente previamente eliminado (soft delete). Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidente restaurado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping("/incidents/{id}/restore")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> restoreIncident(@PathVariable String id) {
        log.info("Admin - Restaurando incidente con ID: {}", id);
        return incidentService.restoreById(id)
                .map(restored -> ResponseEntity.ok(
                        ResponseDto.success(restored, "Incidente restaurado exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Incidente restaurado: {}", id));
    }

    @Operation(
            summary = "Obtener incidentes por zona",
            description = "Obtiene todos los incidentes asociados a una zona específica. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/zone/{zoneId}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByZone(@PathVariable String zoneId) {
        log.info("Admin - Obteniendo incidentes de la zona: {}", zoneId);
        return incidentService.findByZoneId(zoneId)
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes de la zona obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Incidentes de zona {} obtenidos", zoneId));
    }

    @Operation(
            summary = "Obtener incidentes por severidad",
            description = "Obtiene todos los incidentes con una severidad específica. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/severity/{severity}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsBySeverity(@PathVariable String severity) {
        log.info("Admin - Obteniendo incidentes por severidad: {}", severity);
        return incidentService.findBySeverity(severity)
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes por severidad obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Incidentes con severidad {} obtenidos", severity));
    }

    @Operation(
            summary = "Obtener incidentes por estado",
            description = "Obtiene todos los incidentes con un estado específico. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/status/{status}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByStatus(@PathVariable String status) {
        log.info("Admin - Obteniendo incidentes por estado: {}", status);
        return incidentService.findByStatus(status)
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes por estado obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Incidentes con estado {} obtenidos", status));
    }

    @Operation(
            summary = "Obtener incidentes asignados a un usuario",
            description = "Obtiene todos los incidentes asignados a un usuario específico. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/assigned/{userId}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByAssignedUser(@PathVariable String userId) {
        log.info("Admin - Obteniendo incidentes asignados al usuario: {}", userId);
        return incidentService.findByAssignedToUserId(userId)
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes asignados al usuario obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Incidentes del usuario {} obtenidos", userId));
    }

    @Operation(
            summary = "Obtener incidentes por organización",
            description = "Obtiene todos los incidentes de una organización específica. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/organization/{organizationId}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByOrganization(@PathVariable String organizationId) {
        log.info("Admin - Obteniendo incidentes de la organización: {}", organizationId);
        return incidentService.findByOrganizationId(organizationId)
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes de la organización obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Incidentes de organización {} obtenidos", organizationId));
    }

    @Operation(
            summary = "Obtener estadísticas de incidentes",
            description = "Obtiene estadísticas generales de todos los incidentes del sistema. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/stats")
    public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> getIncidentStats() {
        log.info("Admin - Obteniendo estadísticas de incidentes");
        
        return incidentService.findAll()
                .collectList()
                .map(incidents -> {
                    Map<String, Object> stats = new HashMap<>();
                    
                    long totalIncidents = incidents.size();
                    long resolvedIncidents = incidents.stream().filter(IncidentDTO::getResolved).count();
                    long pendingIncidents = totalIncidents - resolvedIncidents;
                    
                    Map<String, Long> severityStats = incidents.stream()
                            .collect(java.util.stream.Collectors.groupingBy(
                                    IncidentDTO::getSeverity,
                                    java.util.stream.Collectors.counting()));
                    
                    Map<String, Long> statusStats = incidents.stream()
                            .collect(java.util.stream.Collectors.groupingBy(
                                    IncidentDTO::getStatus,
                                    java.util.stream.Collectors.counting()));
                    
                    stats.put("totalIncidents", totalIncidents);
                    stats.put("resolvedIncidents", resolvedIncidents);
                    stats.put("pendingIncidents", pendingIncidents);
                    stats.put("severityStats", severityStats);
                    stats.put("statusStats", statusStats);
                    stats.put("timestamp", Instant.now().toString());
                    
                    return stats;
                })
                .map(stats -> ResponseEntity.ok(
                        ResponseDto.success(stats, "Estadísticas obtenidas exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Estadísticas de incidentes obtenidas"));
    }

    @Operation(
            summary = "Endpoint de prueba Admin",
            description = "Endpoint de prueba para verificar que el servicio Admin funciona correctamente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/test")
    public Mono<ResponseEntity<ResponseDto<Map<String, String>>>> testAdminEndpoint() {
        log.info("Admin - Endpoint de prueba llamado");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Admin Incidents API funcionando correctamente");
        response.put("status", "OK");
        response.put("role", "ADMIN");
        return Mono.just(ResponseEntity.ok(
                ResponseDto.success(response, "Test exitoso")
        ));
    }

    // ================================
    // ENDPOINTS DE TIPOS DE INCIDENCIAS
    // ================================

    @Operation(
            summary = "Obtener todos los tipos de incidencias",
            description = "Obtiene la lista completa de tipos de incidencias disponibles. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-types")
    public Mono<ResponseEntity<ResponseDto<List<IncidentTypeDTO>>>> getAllIncidentTypes() {
        log.info("Admin - Obteniendo todos los tipos de incidencias");
        return incidentTypeService.findAll()
                .collectList()
                .map(types -> ResponseEntity.ok(
                        ResponseDto.success(types, "Tipos de incidencias obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Tipos de incidencias obtenidos"));
    }

    @Operation(
            summary = "Obtener tipo de incidencia por ID",
            description = "Obtiene la información de un tipo de incidencia específico. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de incidencia encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Tipo de incidencia no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-types/{id}")
    public Mono<ResponseEntity<ResponseDto<IncidentTypeDTO>>> getIncidentTypeById(@PathVariable String id) {
        log.info("Admin - Obteniendo tipo de incidencia con ID: {}", id);
        return incidentTypeService.findById(id)
                .map(type -> ResponseEntity.ok(
                        ResponseDto.success(type, "Tipo de incidencia obtenido exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Tipo de incidencia {} obtenido", id));
    }

    @Operation(
            summary = "Crear nuevo tipo de incidencia",
            description = "Crea un nuevo tipo de incidencia en el sistema. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Tipo de incidencia creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "Conflicto - El tipo de incidencia ya existe"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/incident-types")
    public Mono<ResponseEntity<ResponseDto<IncidentTypeDTO>>> createIncidentType(@Valid @RequestBody IncidentTypeDTO incidentTypeDTO) {
        log.info("Admin - Creando nuevo tipo de incidencia: {}", incidentTypeDTO.toString());
        return incidentTypeService.save(incidentTypeDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(
                        ResponseDto.created(saved, "Tipo de incidencia creado exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Tipo de incidencia creado exitosamente"))
                .doOnError(error -> log.error("Admin - Error al crear tipo de incidencia: {}", error.getMessage()));
    }

    @Operation(
            summary = "Actualizar tipo de incidencia",
            description = "Actualiza la información de un tipo de incidencia existente. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de incidencia actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Tipo de incidencia no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/incident-types/{id}")
    public Mono<ResponseEntity<ResponseDto<IncidentTypeDTO>>> updateIncidentType(
            @PathVariable String id, 
            @Valid @RequestBody IncidentTypeDTO incidentTypeDTO) {
        log.info("Admin - Actualizando tipo de incidencia con ID: {}", id);
        return incidentTypeService.update(id, incidentTypeDTO)
                .map(updated -> ResponseEntity.ok(
                        ResponseDto.success(updated, "Tipo de incidencia actualizado exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Tipo de incidencia {} actualizado", id));
    }

    @Operation(
            summary = "Eliminar tipo de incidencia",
            description = "Realiza un borrado lógico de un tipo de incidencia (soft delete). Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Tipo de incidencia eliminado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Tipo de incidencia no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/incident-types/{id}")
    public Mono<ResponseEntity<Void>> deleteIncidentType(@PathVariable String id) {
        log.info("Admin - Eliminando tipo de incidencia con ID: {}", id);
        return incidentTypeService.deleteById(id)
                .then(Mono.fromCallable(() -> {
                    log.info("Admin - Tipo de incidencia {} eliminado", id);
                    return ResponseEntity.noContent().<Void>build();
                }));
    }

    @Operation(
            summary = "Restaurar tipo de incidencia eliminado",
            description = "Restaura un tipo de incidencia previamente eliminado (soft delete). Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de incidencia restaurado exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Tipo de incidencia no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping("/incident-types/{id}/restore")
    public Mono<ResponseEntity<ResponseDto<IncidentTypeDTO>>> restoreIncidentType(@PathVariable String id) {
        log.info("Admin - Restaurando tipo de incidencia con ID: {}", id);
        return incidentTypeService.restoreById(id)
                .map(restored -> ResponseEntity.ok(
                        ResponseDto.success(restored, "Tipo de incidencia restaurado exitosamente")
                ))
                .doOnSuccess(result -> log.info("Admin - Tipo de incidencia {} restaurado", id));
    }

    // ================================
    // ENDPOINTS DE GESTIÓN DE USUARIOS
    // ================================

    @Operation(
            summary = "Obtener administradores de la organización",
            description = "Obtiene la lista de todos los administradores de la organización. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de administradores obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/users/admins")
    public Mono<ResponseEntity<ResponseDto<List<UserDTO>>>> getOrganizationAdmins(
            @Parameter(description = "ID de la organización") 
            @RequestParam(required = true) String organizationId) {
        log.info("Admin - Obteniendo administradores de la organización: {}", organizationId);
        
        return userApiClient.getOrganizationAdmins(organizationId)
                .map(admins -> ResponseEntity.ok(
                        ResponseDto.success(admins, "Administradores obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Administradores obtenidos exitosamente"))
                .onErrorResume(error -> {
                    log.error("Error al obtener administradores: {}", error.getMessage());
                    return Mono.just(ResponseEntity.ok(
                            ResponseDto.success(List.of(), "No se pudieron obtener administradores")
                    ));
                });
    }

    @Operation(
            summary = "Obtener clientes de la organización",
            description = "Obtiene la lista de todos los clientes de la organización. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de clientes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/users/clients")
    public Mono<ResponseEntity<ResponseDto<List<UserDTO>>>> getOrganizationClients(
            @Parameter(description = "ID de la organización") 
            @RequestParam(required = true) String organizationId) {
        log.info("Admin - Obteniendo clientes de la organización: {}", organizationId);
        
        return userApiClient.getClientsByOrganization(organizationId)
                .map(clients -> ResponseEntity.ok(
                        ResponseDto.success(clients, "Clientes obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Clientes obtenidos exitosamente"))
                .onErrorResume(error -> {
                    log.error("Error al obtener clientes: {}", error.getMessage());
                    return Mono.just(ResponseEntity.ok(
                            ResponseDto.success(List.of(), "No se pudieron obtener clientes")
                    ));
                });
    }

    @Operation(
            summary = "Obtener información de usuario por ID",
            description = "Obtiene la información completa de un usuario específico por su ID. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/users/{userId}")
    public Mono<ResponseEntity<ResponseDto<UserDTO>>> getUserById(
            @PathVariable String userId,
            @Parameter(description = "ID de la organización") 
            @RequestParam(required = true) String organizationId) {
        log.info("Admin - Obteniendo información de usuario: {} de org: {}", userId, organizationId);
        return userEnrichmentService.getUserById(userId, organizationId)
                .map(user -> ResponseEntity.ok(
                        ResponseDto.success(user, "Usuario obtenido exitosamente")
                ))
                .doOnSuccess(result -> log.info("Usuario {} obtenido", userId));
    }

    @Operation(
            summary = "Obtener información de usuario por username",
            description = "Obtiene la información completa de un usuario específico por su username. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuario encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/users/username/{username}")
    public Mono<ResponseEntity<ResponseDto<UserDTO>>> getUserByUsername(
            @PathVariable String username,
            @Parameter(description = "ID de la organización") 
            @RequestParam(required = true) String organizationId) {
        log.info("Admin - Obteniendo información de usuario por username: {} de org: {}", username, organizationId);
        return userEnrichmentService.getUserByUsername(username, organizationId)
                .map(user -> ResponseEntity.ok(
                        ResponseDto.success(user, "Usuario obtenido exitosamente")
                ))
                .doOnSuccess(result -> log.info("Usuario {} obtenido", username));
    }

    @Operation(
            summary = "Obtener incidentes enriquecidos con información de usuarios",
            description = "Obtiene todos los incidentes enriquecidos con información completa de usuarios (reportador, asignado). Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidentes enriquecidos obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/enriched")
    public Mono<ResponseEntity<ResponseDto<List<IncidentEnrichedDTO>>>> getEnrichedIncidents() {
        log.info("Admin - Obteniendo incidentes enriquecidos con información de usuarios");
        
        return incidentService.findAll()
                .doOnNext(incident -> log.debug("Procesando incidente: {}", incident.getIncidentCode()))
                .flatMap(incident -> enrichIncidentWithUserInfo(incident)
                    .doOnSuccess(enriched -> log.debug("Incidente enriquecido exitosamente: {}", incident.getIncidentCode()))
                    .doOnError(error -> log.error("Error enriqueciendo incidente {}: {}", 
                        incident.getIncidentCode(), error.getMessage(), error))
                    .onErrorResume(error -> {
                        log.warn("Fallback: creando incidente enriquecido básico para: {} debido a error: {}", 
                            incident.getIncidentCode(), error.getMessage());
                        return createBasicEnrichedIncident(incident);
                    }))
                .collectList()
                .map(enrichedList -> ResponseEntity.ok(
                        ResponseDto.success(enrichedList, "Incidentes enriquecidos obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Total de incidentes enriquecidos obtenidos: {}", 
                    result.getBody().getData().size()));
    }

    @Operation(
            summary = "Obtener incidente enriquecido por ID",
            description = "Obtiene un incidente específico enriquecido con información completa de usuarios. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidente enriquecido obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/{id}/enriched")
    public Mono<ResponseEntity<ResponseDto<IncidentEnrichedDTO>>> getEnrichedIncidentById(@PathVariable String id) {
        log.info("Admin - Obteniendo incidente enriquecido por ID: {}", id);
        
        return incidentService.findById(id)
                .flatMap(this::enrichIncidentWithUserInfo)
                .map(enriched -> ResponseEntity.ok(
                        ResponseDto.success(enriched, "Incidente enriquecido obtenido exitosamente")
                ))
                .doOnSuccess(result -> log.info("Incidente enriquecido {} obtenido", id));
    }

    @Operation(
            summary = "Verificar salud del sistema",
            description = "Verifica el estado de salud del sistema y sus servicios externos. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado de salud obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/system/health")
    public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> getSystemHealth() {
        log.info("Admin - Verificando salud del sistema");
        
        return userEnrichmentService.isUserServiceAvailable()
                .map(userServiceUp -> {
                    Map<String, Object> health = new HashMap<>();
                    health.put("status", "UP");
                    health.put("timestamp", Instant.now().toString());
                    
                    Map<String, Object> services = new HashMap<>();
                    services.put("userService", userServiceUp ? "UP" : "DOWN");
                    services.put("incidentService", "UP"); // Asumimos que está UP si llegamos aquí
                    
                    health.put("services", services);
                    health.put("description", "Estado de los servicios del sistema");
                    
                    return health;
                })
                .map(health -> ResponseEntity.ok(
                        ResponseDto.success(health, "Salud del sistema verificada")
                ))
                .doOnSuccess(result -> log.info("Salud del sistema verificada"));
    }

    @Operation(
            summary = "Test de integración con servicio de usuarios",
            description = "Prueba la integración con el servicio de usuarios externo. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Integración exitosa"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor o servicio externo no disponible")
    })
    @GetMapping("/test/user-integration/{organizationId}")
    public Mono<ResponseEntity<ResponseDto<List<UserDTO>>>> testUserIntegration(@PathVariable String organizationId) {
        log.info("Admin - Probando integración con usuarios de org: {}", organizationId);
        return userEnrichmentService.testUserIntegration(organizationId)
                .map(users -> ResponseEntity.ok(
                        ResponseDto.success(users, "Integración con servicio de usuarios exitosa")
                ))
                .doOnSuccess(result -> log.info("Test de integración con usuarios exitoso"));
    }

    // Métodos auxiliares
    private void validateCreateDTO(IncidentCreateDTO createDTO) {
        if (createDTO.getIncidentCode() == null || createDTO.getIncidentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del incidente es obligatorio");
        }
        if (createDTO.getOrganizationId() == null || createDTO.getOrganizationId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID de la organización es obligatorio");
        }
        if (createDTO.getIncidentTypeId() == null || createDTO.getIncidentTypeId().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de incidente es obligatorio");
        }
        if (createDTO.getTitle() == null || createDTO.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("El título del incidente es obligatorio");
        }
        if (createDTO.getDescription() == null || createDTO.getDescription().trim().isEmpty()) {
            throw new IllegalArgumentException("La descripción es obligatoria");
        }
        if (createDTO.getReportedByUserId() == null || createDTO.getReportedByUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario que reportó es obligatorio");
        }
    }

    private void validateResolutionDTO(IncidentResolutionDTO resolutionDTO) {
        if (resolutionDTO.getIncidentId() == null || resolutionDTO.getIncidentId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del incidente es obligatorio");
        }
        if (resolutionDTO.getResolutionType() == null || resolutionDTO.getResolutionType().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de resolución es obligatorio");
        }
        if (resolutionDTO.getActionsTaken() == null || resolutionDTO.getActionsTaken().trim().isEmpty()) {
            throw new IllegalArgumentException("Las acciones tomadas son obligatorias");
        }
        if (resolutionDTO.getResolvedByUserId() == null || resolutionDTO.getResolvedByUserId().trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del usuario que resuelve es obligatorio");
        }
        if (resolutionDTO.getResolutionDate() == null) {
            resolutionDTO.setResolutionDate(new Date());
        }
        if (resolutionDTO.getCreatedAt() == null) {
            resolutionDTO.setCreatedAt(new Date());
        }
        // Validaciones de negocio
        if (resolutionDTO.getLaborHours() != null && resolutionDTO.getLaborHours() < 0) {
            throw new IllegalArgumentException("Las horas de trabajo no pueden ser negativas");
        }
        if (resolutionDTO.getTotalCost() != null && resolutionDTO.getTotalCost() < 0) {
            throw new IllegalArgumentException("El costo total no puede ser negativo");
        }
    }

    private IncidentDTO convertToIncidentDTO(IncidentCreateDTO createDTO) {
        IncidentDTO incidentDTO = new IncidentDTO();
        incidentDTO.setOrganizationId(createDTO.getOrganizationId());
        incidentDTO.setIncidentCode(createDTO.getIncidentCode());
        incidentDTO.setIncidentTypeId(createDTO.getIncidentTypeId());
        incidentDTO.setIncidentCategory(createDTO.getIncidentCategory());
        incidentDTO.setZoneId(createDTO.getZoneId());
        incidentDTO.setIncidentDate(createDTO.getIncidentDate());
        incidentDTO.setTitle(createDTO.getTitle());
        incidentDTO.setDescription(createDTO.getDescription());
        incidentDTO.setSeverity(createDTO.getSeverity());
        incidentDTO.setStatus(createDTO.getStatus());
        incidentDTO.setAffectedBoxesCount(createDTO.getAffectedBoxesCount());
        incidentDTO.setReportedByUserId(createDTO.getReportedByUserId());
        incidentDTO.setAssignedToUserId(createDTO.getAssignedToUserId());
        incidentDTO.setResolvedByUserId(createDTO.getResolvedByUserId());
        incidentDTO.setResolved(createDTO.getResolved());
        incidentDTO.setResolutionNotes(createDTO.getResolutionNotes());
        incidentDTO.setRecordStatus(createDTO.getRecordStatus());
        return incidentDTO;
    }

    /**
     * Enriquecer incidente con información completa de usuarios
     */
    private Mono<IncidentEnrichedDTO> enrichIncidentWithUserInfo(IncidentDTO incident) {
        try {
            log.debug("Enriqueciendo incidente: {} con IDs - Reporter: {}, Assigned: {}, Resolver: {}", 
                incident.getIncidentCode(), 
                incident.getReportedByUserId(), 
                incident.getAssignedToUserId(), 
                incident.getResolvedByUserId());
                
            IncidentEnrichedDTO enriched = new IncidentEnrichedDTO();
            
            // Copiar todos los campos del incidente original
            enriched.setId(incident.getId());
            enriched.setOrganizationId(incident.getOrganizationId());
            enriched.setIncidentCode(incident.getIncidentCode());
            enriched.setIncidentTypeId(incident.getIncidentTypeId());
            enriched.setIncidentCategory(incident.getIncidentCategory());
            enriched.setZoneId(incident.getZoneId());
            enriched.setIncidentDate(incident.getIncidentDate());
            enriched.setTitle(incident.getTitle());
            enriched.setDescription(incident.getDescription());
            enriched.setSeverity(incident.getSeverity());
            enriched.setStatus(incident.getStatus());
            enriched.setAffectedBoxesCount(incident.getAffectedBoxesCount());
            enriched.setReportedByUserId(incident.getReportedByUserId());
            enriched.setAssignedToUserId(incident.getAssignedToUserId());
            enriched.setResolvedByUserId(incident.getResolvedByUserId());
            enriched.setResolved(incident.getResolved());
            enriched.setResolutionNotes(incident.getResolutionNotes());
            enriched.setRecordStatus(incident.getRecordStatus());

            // Obtener información del reportante con manejo de errores
            Mono<UserDTO> reporterMono = incident.getReportedByUserId() != null 
                ? userEnrichmentService.getUserWithFallback(incident.getReportedByUserId(), incident.getOrganizationId(), "Reportante_" + incident.getReportedByUserId())
                    .doOnError(error -> log.warn("Error obteniendo reportante {}: {}", incident.getReportedByUserId(), error.getMessage()))
                    .onErrorReturn(createFallbackUserDTO(incident.getReportedByUserId(), "Reportante"))
                : Mono.just(createFallbackUserDTO(null, "Reportante"));

            // Obtener información del asignado con manejo de errores
            Mono<UserDTO> assignedMono = incident.getAssignedToUserId() != null 
                ? userEnrichmentService.getUserWithFallback(incident.getAssignedToUserId(), incident.getOrganizationId(), "Asignado_" + incident.getAssignedToUserId())
                    .doOnError(error -> log.warn("Error obteniendo asignado {}: {}", incident.getAssignedToUserId(), error.getMessage()))
                    .onErrorReturn(createFallbackUserDTO(incident.getAssignedToUserId(), "Asignado"))
                : Mono.just(createFallbackUserDTO(null, "Asignado"));

            // Obtener información del resolvedor con manejo de errores
            Mono<UserDTO> resolverMono = incident.getResolvedByUserId() != null 
                ? userEnrichmentService.getUserWithFallback(incident.getResolvedByUserId(), incident.getOrganizationId(), "Resolvedor_" + incident.getResolvedByUserId())
                    .doOnError(error -> log.warn("Error obteniendo resolvedor {}: {}", incident.getResolvedByUserId(), error.getMessage()))
                    .onErrorReturn(createFallbackUserDTO(incident.getResolvedByUserId(), "Resolvedor"))
                : Mono.just(createFallbackUserDTO(null, "Resolvedor"));

            return Mono.zip(reporterMono, assignedMono, resolverMono)
                .map(tuple -> {
                    UserDTO reporter = tuple.getT1();
                    UserDTO assigned = tuple.getT2();
                    UserDTO resolver = tuple.getT3();
                    
                    // IMPORTANTE: Asignar los usuarios obtenidos a los campos del objeto enriched usando reflection
                    try {
                        java.lang.reflect.Field reporterField = enriched.getClass().getDeclaredField("reporterInfo");
                        reporterField.setAccessible(true);
                        reporterField.set(enriched, reporter);
                        
                        java.lang.reflect.Field assignedField = enriched.getClass().getDeclaredField("assignedUserInfo");
                        assignedField.setAccessible(true);
                        assignedField.set(enriched, assigned);
                        
                        java.lang.reflect.Field resolverField = enriched.getClass().getDeclaredField("resolverInfo");
                        resolverField.setAccessible(true);
                        resolverField.set(enriched, resolver);
                        
                        log.debug("Usuarios enriquecidos asignados para incidente {} - Reporter: {}, Assigned: {}, Resolver: {}", 
                            incident.getIncidentCode(), 
                            reporter != null ? reporter.getFullName() : "null",
                            assigned != null ? assigned.getFullName() : "null", 
                            resolver != null ? resolver.getFullName() : "null");
                            
                    } catch (Exception e) {
                        log.error("Error asignando usuarios enriquecidos usando reflection: {}", e.getMessage());
                    }
                    
                    return enriched;
                })
                .doOnSuccess(result -> log.debug("Incidente {} enriquecido exitosamente", incident.getIncidentCode()))
                .doOnError(error -> log.error("Error final en enriquecimiento de incidente {}: {}", incident.getIncidentCode(), error.getMessage()));
                
        } catch (Exception e) {
            log.error("Error sincrónico en enrichIncidentWithUserInfo para incidente {}: {}", incident.getIncidentCode(), e.getMessage(), e);
            return createBasicEnrichedIncident(incident);
        }
    }

    // ================================
    // ENDPOINTS DE GESTIÓN DE RESOLUCIONES DE INCIDENTES
    // ================================

    /**
     * Obtener todas las resoluciones de incidentes (ADMIN)
     */
    @Operation(summary = "Obtener todas las resoluciones", 
               description = "Permite a los administradores obtener todas las resoluciones de incidentes del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resoluciones obtenidas exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-resolutions")
    @ResponseStatus(HttpStatus.OK)
    public Flux<IncidentResolutionDTO> getAllIncidentResolutions() {
        log.info("Admin - Obteniendo todas las resoluciones de incidentes");
        return incidentResolutionService.findAll()
                .doOnNext(resolution -> log.debug("Resolución encontrada: {}", resolution.getId()))
                .doOnError(error -> log.error("Error al obtener resoluciones: {}", error.getMessage()))
                .doOnComplete(() -> log.info("Completada la obtención de todas las resoluciones"));
    }

    /**
     * Obtener resolución por ID (ADMIN)
     */
    @Operation(summary = "Obtener resolución por ID", 
               description = "Permite obtener una resolución específica por su ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resolución encontrada"),
        @ApiResponse(responseCode = "404", description = "Resolución no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-resolutions/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<IncidentResolutionDTO> getIncidentResolutionById(
            @Parameter(description = "ID de la resolución", required = true) @PathVariable String id) {
        log.info("Admin - Obteniendo resolución con ID: {}", id);
        return incidentResolutionService.findById(id)
                .doOnSuccess(resolution -> log.info("Resolución encontrada: {}", resolution.getId()))
                .doOnError(error -> log.error("Error al obtener resolución {}: {}", id, error.getMessage()));
    }

    /**
     * Crear nueva resolución de incidente (ADMIN)
     */
    @Operation(summary = "Crear nueva resolución", 
               description = "Permite a los administradores crear una nueva resolución para un incidente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Resolución creada exitosamente"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/incident-resolutions")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<IncidentResolutionDTO> createIncidentResolution(
            @RequestBody IncidentResolutionDTO resolutionDTO) {
        log.info("Admin - Creando nueva resolución para incidente: {}", resolutionDTO.getIncidentId());
        
        validateResolutionDTO(resolutionDTO);
        
        return incidentResolutionService.save(resolutionDTO)
                .doOnSuccess(saved -> log.info("Admin - Resolución creada exitosamente: {} para incidente: {}", 
                    saved.getId(), saved.getIncidentId()))
                .doOnError(error -> log.error("Admin - Error al crear resolución: {}", error.getMessage()));
    }

    /**
     * Actualizar resolución existente (ADMIN)
     */
    @Operation(summary = "Actualizar resolución", 
               description = "Permite actualizar una resolución existente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resolución actualizada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Resolución no encontrada"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PutMapping("/incident-resolutions/{id}")
    @ResponseStatus(HttpStatus.OK)
    public Mono<IncidentResolutionDTO> updateIncidentResolution(
            @Parameter(description = "ID de la resolución", required = true) @PathVariable String id,
            @RequestBody IncidentResolutionDTO resolutionDTO) {
        log.info("Admin - Actualizando resolución con ID: {}", id);
        
        validateResolutionDTO(resolutionDTO);
        
        return incidentResolutionService.update(id, resolutionDTO)
                .doOnSuccess(updated -> log.info("Admin - Resolución actualizada exitosamente: {}", updated.getId()))
                .doOnError(error -> log.error("Admin - Error al actualizar resolución {}: {}", id, error.getMessage()));
    }

    /**
     * Eliminar resolución (ADMIN)
     */
    @Operation(summary = "Eliminar resolución", 
               description = "Permite eliminar una resolución del sistema")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Resolución eliminada exitosamente"),
        @ApiResponse(responseCode = "404", description = "Resolución no encontrada"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @DeleteMapping("/incident-resolutions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteIncidentResolution(
            @Parameter(description = "ID de la resolución", required = true) @PathVariable String id) {
        log.info("Admin - Eliminando resolución con ID: {}", id);
        return incidentResolutionService.deleteById(id)
                .doOnSuccess(v -> log.info("Admin - Resolución eliminada exitosamente: {}", id))
                .doOnError(error -> log.error("Admin - Error al eliminar resolución {}: {}", id, error.getMessage()));
    }

    /**
     * Obtener resoluciones por ID de incidente (ADMIN)
     */
    @Operation(summary = "Obtener resoluciones por incidente", 
               description = "Permite obtener todas las resoluciones asociadas a un incidente específico")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Resoluciones obtenidas exitosamente"),
        @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-resolutions/incident/{incidentId}")
    @ResponseStatus(HttpStatus.OK)
    public Flux<IncidentResolutionDTO> getResolutionsByIncidentId(
            @Parameter(description = "ID del incidente", required = true) @PathVariable String incidentId) {
        log.info("Admin - Obteniendo resoluciones para incidente: {}", incidentId);
        return incidentResolutionService.findByIncidentId(incidentId)
                .doOnNext(resolution -> log.debug("Resolución encontrada para incidente {}: {}", 
                    incidentId, resolution.getId()))
                .doOnError(error -> log.error("Error al obtener resoluciones para incidente {}: {}", 
                    incidentId, error.getMessage()))
                .doOnComplete(() -> log.info("Completada obtención de resoluciones para incidente: {}", incidentId));
    }

    /**
     * Resolver incidente con resolución detallada (ADMIN)
     */
    @Operation(summary = "Resolver incidente con detalles", 
               description = "Permite resolver un incidente creando una resolución detallada y actualizando el estado del incidente")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Incidente resuelto exitosamente"),
        @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/incident-resolutions/resolve-incident/{incidentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<IncidentResolutionDTO> resolveIncidentWithDetails(
            @Parameter(description = "ID del incidente a resolver", required = true) @PathVariable String incidentId,
            @RequestBody IncidentResolutionDTO resolutionDTO) {
        log.info("Admin - Resolviendo incidente {} con resolución detallada", incidentId);
        
        // Establecer el ID del incidente en la resolución
        resolutionDTO.setIncidentId(incidentId);
        validateResolutionDTO(resolutionDTO);
        
        // Crear la resolución y actualizar el incidente
        return incidentResolutionService.save(resolutionDTO)
                .flatMap(savedResolution -> {
                    // Actualizar el estado del incidente a resuelto
                    return incidentService.findById(incidentId)
                            .flatMap(incident -> {
                                incident.setResolved(true);
                                incident.setStatus("RESOLVED");
                                incident.setResolvedByUserId(resolutionDTO.getResolvedByUserId());
                                incident.setResolutionNotes(resolutionDTO.getResolutionNotes());
                                return incidentService.update(incidentId, incident);
                            })
                            .then(Mono.just(savedResolution));
                })
                .doOnSuccess(resolution -> log.info("Admin - Incidente {} resuelto exitosamente con resolución: {}", 
                    incidentId, resolution.getId()))
                .doOnError(error -> log.error("Admin - Error al resolver incidente {}: {}", 
                    incidentId, error.getMessage()));
    }

    /**
     * Obtener estadísticas de resoluciones (ADMIN)
     */
    @Operation(summary = "Obtener estadísticas de resoluciones", 
               description = "Proporciona estadísticas detalladas sobre las resoluciones de incidentes")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
        @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-resolutions/stats")
    @ResponseStatus(HttpStatus.OK)
    public Mono<Map<String, Object>> getResolutionStats() {
        log.info("Admin - Obteniendo estadísticas de resoluciones");
        
        return incidentResolutionService.findAll()
                .collectList()
                .map(resolutions -> {
                    Map<String, Object> stats = new HashMap<>();
                    
                    long totalResolutions = resolutions.size();
                    
                    // Estadísticas por tipo de resolución
                    Map<String, Long> resolutionTypeStats = resolutions.stream()
                            .collect(java.util.stream.Collectors.groupingBy(
                                    resolution -> resolution.getResolutionType() != null ? 
                                        resolution.getResolutionType() : "UNKNOWN",
                                    java.util.stream.Collectors.counting()));
                    
                    // Costo total
                    double totalCost = resolutions.stream()
                            .mapToDouble(resolution -> resolution.getTotalCost() != null ? 
                                resolution.getTotalCost() : 0.0)
                            .sum();
                    
                    // Horas de trabajo total
                    int totalLaborHours = resolutions.stream()
                            .mapToInt(resolution -> resolution.getLaborHours() != null ? 
                                resolution.getLaborHours() : 0)
                            .sum();
                    
                    // Resoluciones que requieren seguimiento
                    long followUpRequired = resolutions.stream()
                            .filter(resolution -> Boolean.TRUE.equals(resolution.getFollowUpRequired()))
                            .count();
                    
                    // Resoluciones con control de calidad aprobado
                    long qualityApproved = resolutions.stream()
                            .filter(resolution -> Boolean.TRUE.equals(resolution.getQualityCheck()))
                            .count();
                    
                    stats.put("totalResolutions", totalResolutions);
                    stats.put("resolutionTypeStats", resolutionTypeStats);
                    stats.put("totalCost", totalCost);
                    stats.put("totalLaborHours", totalLaborHours);
                    stats.put("followUpRequired", followUpRequired);
                    stats.put("qualityApproved", qualityApproved);
                    stats.put("averageCostPerResolution", totalResolutions > 0 ? totalCost / totalResolutions : 0);
                    stats.put("averageHoursPerResolution", totalResolutions > 0 ? (double) totalLaborHours / totalResolutions : 0);
                    stats.put("timestamp", Instant.now().toString());
                    
                    return stats;
                })
                .doOnSuccess(stats -> log.info("Admin - Estadísticas de resoluciones generadas exitosamente"))
                .doOnError(error -> log.error("Admin - Error al generar estadísticas de resoluciones: {}", error.getMessage()));
    }

    /**
     * Crear un incidente enriquecido básico cuando falla el enriquecimiento completo
     */
    private Mono<IncidentEnrichedDTO> createBasicEnrichedIncident(IncidentDTO incident) {
        IncidentEnrichedDTO enriched = new IncidentEnrichedDTO();
        
        // Copiar todos los campos del incidente original
        enriched.setId(incident.getId());
        enriched.setOrganizationId(incident.getOrganizationId());
        enriched.setIncidentCode(incident.getIncidentCode());
        enriched.setIncidentTypeId(incident.getIncidentTypeId());
        enriched.setIncidentCategory(incident.getIncidentCategory());
        enriched.setZoneId(incident.getZoneId());
        enriched.setIncidentDate(incident.getIncidentDate());
        enriched.setTitle(incident.getTitle());
        enriched.setDescription(incident.getDescription());
        enriched.setSeverity(incident.getSeverity());
        enriched.setStatus(incident.getStatus());
        enriched.setAffectedBoxesCount(incident.getAffectedBoxesCount());
        enriched.setReportedByUserId(incident.getReportedByUserId());
        enriched.setAssignedToUserId(incident.getAssignedToUserId());
        enriched.setResolvedByUserId(incident.getResolvedByUserId());
        enriched.setResolved(incident.getResolved());
        enriched.setResolutionNotes(incident.getResolutionNotes());
        enriched.setRecordStatus(incident.getRecordStatus());
        
        // Establecer información de usuario básica si los IDs están disponibles
        log.debug("Creando incidente enriquecido básico para: {}", incident.getIncidentCode());
        
        return Mono.just(enriched);
    }

    /**
     * Crear un UserDTO de fallback con datos básicos
     */
    private UserDTO createFallbackUserDTO(String userId, String role) {
        UserDTO fallbackUser = new UserDTO();
        
        try {
            // Usar reflection para establecer campos básicos
            setFieldValue(fallbackUser, "userId", userId != null ? userId : "unknown");
            setFieldValue(fallbackUser, "userCode", "UNKNOWN");
            setFieldValue(fallbackUser, "username", "unknown_user");
            setFieldValue(fallbackUser, "status", "UNKNOWN");
            setFieldValue(fallbackUser, "roles", List.of("UNKNOWN"));
            
            // Crear información personal básica
            UserDTO.PersonalInfo personalInfo = new UserDTO.PersonalInfo();
            if (role.equals("Reportante")) {
                setFieldValue(personalInfo, "firstName", "Usuario");
                setFieldValue(personalInfo, "lastName", "desconocido");
            } else if (role.equals("Asignado")) {
                setFieldValue(personalInfo, "firstName", "No");
                setFieldValue(personalInfo, "lastName", "asignado");
            } else if (role.equals("Resolvedor")) {
                setFieldValue(personalInfo, "firstName", "No");
                setFieldValue(personalInfo, "lastName", "resuelto");
            } else {
                setFieldValue(personalInfo, "firstName", "Usuario");
                setFieldValue(personalInfo, "lastName", "desconocido");
            }
            setFieldValue(personalInfo, "documentType", "N/A");
            setFieldValue(personalInfo, "documentNumber", "N/A");
            setFieldValue(fallbackUser, "personalInfo", personalInfo);
            
            // Crear información de contacto básica
            UserDTO.Contact contact = new UserDTO.Contact();
            setFieldValue(fallbackUser, "contact", contact);
            
            // Crear dirección básica
            UserDTO.Address address = new UserDTO.Address();
            setFieldValue(fallbackUser, "address", address);
            
        } catch (Exception e) {
            log.warn("Error creando usuario de respaldo: {}", e.getMessage());
        }
        
        log.debug("Creando usuario de respaldo: {} - {} con nombre: {}", 
            role, userId, fallbackUser.getFullName());
        
        return fallbackUser;
    }
    
    /**
     * Método auxiliar para establecer valores de campo usando reflection
     */
    private void setFieldValue(Object obj, String fieldName, Object value) {
        try {
            java.lang.reflect.Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            log.debug("No se pudo establecer campo {} en {}: {}", fieldName, obj.getClass().getSimpleName(), e.getMessage());
        }
    }
}
