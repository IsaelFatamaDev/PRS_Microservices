package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.rest.client;

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
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.IncidentService;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.IncidentTypeService;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.UserEnrichmentService;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.IncidentResolutionService;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.client.UserApiClient;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentCreateDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentTypeDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentResolutionDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.UserDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.common.ResponseDto;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Validated
@RestController
@RequestMapping("/api/client")
@Tag(name = "Client Incidents API", description = "Endpoints para clientes - Gestión de incidentes")
public class ClientRest {

    private final IncidentService incidentService;
    private final IncidentTypeService incidentTypeService;
    private final UserEnrichmentService userEnrichmentService;
    @SuppressWarnings("unused") // Reservado para uso futuro
    private final UserApiClient userApiClient;
    private final IncidentResolutionService incidentResolutionService;

    public ClientRest(IncidentService incidentService,
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

    @Operation(
            summary = "Crear nuevo incidente",
            description = "Permite a un cliente crear un nuevo incidente en el sistema. El incidente se crea con estado REPORTED."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Incidente creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/incidents/create")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> createIncident(@Valid @RequestBody IncidentCreateDTO incidentCreateDTO) {
        log.info("Client - Creando nuevo incidente: {}", incidentCreateDTO.getIncidentCode());
        
        // Validación específica para clientes
        validateClientCreateDTO(incidentCreateDTO);
        IncidentDTO incidentDTO = convertToIncidentDTO(incidentCreateDTO);
        
        // Para clientes, establecer estado inicial como REPORTED
        incidentDTO.setStatus("REPORTED");
        incidentDTO.setResolved(false);
        incidentDTO.setRecordStatus("ACTIVE");
        
        return incidentService.save(incidentDTO)
                .map(saved -> ResponseEntity.status(HttpStatus.CREATED).body(
                        ResponseDto.created(saved, "Incidente creado exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Incidente creado exitosamente: {}", incidentDTO.getIncidentCode()))
                .doOnError(error -> log.error("Client - Error al crear incidente: {}", error.getMessage()));
    }

    @Operation(
            summary = "Obtener mis incidentes",
            description = "Obtiene todos los incidentes reportados por el usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/my-incidents")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getMyIncidents(@RequestParam String userId) {
        log.info("Client - Obteniendo incidentes del usuario: {}", userId);
        return incidentService.findByOrganizationId(userId)
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes del usuario obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Incidentes del usuario {} obtenidos", userId));
    }

    @Operation(
            summary = "Rastrear incidente",
            description = "Obtiene información detallada de seguimiento de un incidente específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Información de seguimiento obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/track/{id}")
    public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> trackIncident(@PathVariable String id) {
        log.info("Client - Seguimiento de incidente: {}", id);
        
        return incidentService.findById(id)
                .map(incident -> {
                    Map<String, Object> trackingInfo = new HashMap<>();
                    trackingInfo.put("incidentId", incident.getId());
                    trackingInfo.put("incidentCode", incident.getIncidentCode());
                    trackingInfo.put("title", incident.getTitle());
                    trackingInfo.put("status", incident.getStatus());
                    trackingInfo.put("severity", incident.getSeverity());
                    trackingInfo.put("resolved", incident.getResolved());
                    trackingInfo.put("incidentDate", incident.getIncidentDate());
                    trackingInfo.put("assignedToUserId", incident.getAssignedToUserId());
                    trackingInfo.put("resolutionNotes", incident.getResolutionNotes());
                    trackingInfo.put("lastUpdate", Instant.now().toString());
                    
                    // Información de progreso
                    Map<String, Object> progress = new HashMap<>();
                    switch (incident.getStatus()) {
                        case "REPORTED":
                            progress.put("step", 1);
                            progress.put("stepName", "Reportado");
                            progress.put("description", "Su incidente ha sido recibido y está siendo revisado");
                            break;
                        case "ASSIGNED":
                            progress.put("step", 2);
                            progress.put("stepName", "Asignado");
                            progress.put("description", "Su incidente ha sido asignado a un técnico");
                            break;
                        case "IN_PROGRESS":
                            progress.put("step", 3);
                            progress.put("stepName", "En Progreso");
                            progress.put("description", "El técnico está trabajando en resolver su incidente");
                            break;
                        case "RESOLVED":
                            progress.put("step", 4);
                            progress.put("stepName", "Resuelto");
                            progress.put("description", "Su incidente ha sido resuelto");
                            break;
                        default:
                            progress.put("step", 1);
                            progress.put("stepName", "En Revisión");
                            progress.put("description", "Su incidente está siendo procesado");
                    }
                    trackingInfo.put("progress", progress);
                    
                    return trackingInfo;
                })
                .map(trackingInfo -> ResponseEntity.ok(
                        ResponseDto.success(trackingInfo, "Información de seguimiento obtenida exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Seguimiento de incidente {} obtenido", id));
    }

    @Operation(
            summary = "Obtener incidente por ID",
            description = "Obtiene un incidente específico. Solo puede ver incidentes propios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidente encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Sin permisos para ver este incidente"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/{id}")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> getIncidentById(
            @PathVariable String id, 
            @RequestParam(required = false) String userId) {
        log.info("Client - Obteniendo incidente con ID: {} para usuario: {}", id, userId);
        
        return incidentService.findById(id)
                .filter(incident -> userId == null || userId.equals(incident.getReportedByUserId()))
                .map(incident -> ResponseEntity.ok(
                        ResponseDto.success(incident, "Incidente obtenido exitosamente")
                ))
                .switchIfEmpty(Mono.error(new RuntimeException("Incidente no encontrado o sin permisos")))
                .doOnSuccess(result -> log.info("Client - Incidente {} obtenido", id));
    }

    @Operation(
            summary = "Obtener incidentes por zona",
            description = "Obtiene todos los incidentes activos de una zona específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/zone/{zoneId}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByZone(@PathVariable String zoneId) {
        log.info("Client - Obteniendo incidentes públicos de la zona: {}", zoneId);
        return incidentService.findByZoneId(zoneId)
                .filter(incident -> "ACTIVE".equals(incident.getRecordStatus()))
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes de la zona obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Incidentes de zona {} obtenidos", zoneId));
    }

    @Operation(
            summary = "Obtener incidentes por tipo",
            description = "Obtiene todos los incidentes activos de un tipo específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/type/{incidentTypeId}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByType(@PathVariable String incidentTypeId) {
        log.info("Client - Obteniendo incidentes por tipo: {}", incidentTypeId);
        return incidentService.findByIncidentTypeId(incidentTypeId)
                .filter(incident -> "ACTIVE".equals(incident.getRecordStatus()))
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes por tipo obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Incidentes de tipo {} obtenidos", incidentTypeId));
    }

    @Operation(
            summary = "Obtener incidentes por categoría",
            description = "Obtiene todos los incidentes activos de una categoría específica."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de incidentes obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/category/{category}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByCategory(@PathVariable String category) {
        log.info("Client - Obteniendo incidentes por categoría: {}", category);
        return incidentService.findByIncidentCategory(category)
                .filter(incident -> "ACTIVE".equals(incident.getRecordStatus()))
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Incidentes por categoría obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Incidentes de categoría {} obtenidos", category));
    }

    @Operation(
            summary = "Actualizar incidente (limitado)",
            description = "Permite a un cliente actualizar ciertos campos de su incidente (título y descripción únicamente)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidente actualizado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Sin permisos para editar este incidente"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PatchMapping("/incidents/{id}/update")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> updateIncidentLimited(
            @PathVariable String id, 
            @RequestBody Map<String, Object> updates,
            @RequestParam String userId) {
        log.info("Client - Actualizando incidente {} por usuario {}", id, userId);
        
        return incidentService.findById(id)
                .filter(incident -> userId.equals(incident.getReportedByUserId()))
                .flatMap(incident -> {
                    // Solo permitir actualizar ciertos campos para clientes
                    if (updates.containsKey("description")) {
                        incident.setDescription((String) updates.get("description"));
                    }
                    if (updates.containsKey("title")) {
                        incident.setTitle((String) updates.get("title"));
                    }
                    // No permitir cambiar estado, severidad, etc.
                    
                    return incidentService.update(id, incident);
                })
                .map(updated -> ResponseEntity.ok(
                        ResponseDto.success(updated, "Incidente actualizado exitosamente")
                ))
                .switchIfEmpty(Mono.error(new RuntimeException("Incidente no encontrado o sin permisos para editar")))
                .doOnSuccess(result -> log.info("Client - Incidente {} actualizado", id));
    }

    @Operation(
            summary = "Buscar incidentes",
            description = "Busca incidentes por texto en título, descripción o código. Opcionalmente filtrado por usuario."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/search")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> searchIncidents(
            @RequestParam String query,
            @RequestParam(required = false) String userId) {
        log.info("Client - Buscando incidentes con query: {} para usuario: {}", query, userId);
        
        return incidentService.findAll()
                .filter(incident -> {
                    // Filtrar solo incidentes activos y del usuario si se especifica
                    if (!"ACTIVE".equals(incident.getRecordStatus())) {
                        return false;
                    }
                    if (userId != null && !userId.equals(incident.getReportedByUserId())) {
                        return false;
                    }
                    
                    // Buscar en título, descripción o código
                    String searchText = query.toLowerCase();
                    return incident.getTitle().toLowerCase().contains(searchText) ||
                           incident.getDescription().toLowerCase().contains(searchText) ||
                           incident.getIncidentCode().toLowerCase().contains(searchText);
                })
                .collectList()
                .map(incidents -> ResponseEntity.ok(
                        ResponseDto.success(incidents, "Búsqueda completada exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Búsqueda de incidentes completada"));
    }

    @Operation(
            summary = "Obtener estadísticas de usuario",
            description = "Obtiene estadísticas de incidentes para un usuario específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estadísticas obtenidas exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/stats/user/{userId}")
    public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> getUserIncidentStats(@PathVariable String userId) {
        log.info("Client - Obteniendo estadísticas de incidentes para usuario: {}", userId);
        
        return incidentService.findByOrganizationId(userId)
                .collectList()
                .map(incidentsList -> {
                    Map<String, Object> stats = new HashMap<>();
                    
                    long totalIncidents = incidentsList.size();
                    long resolvedIncidents = incidentsList.stream().filter(IncidentDTO::getResolved).count();
                    long pendingIncidents = totalIncidents - resolvedIncidents;
                    
                    stats.put("totalIncidents", totalIncidents);
                    stats.put("resolvedIncidents", resolvedIncidents);
                    stats.put("pendingIncidents", pendingIncidents);
                    stats.put("resolutionRate", totalIncidents > 0 ? 
                        String.format("%.2f%%", (resolvedIncidents * 100.0) / totalIncidents) : "0%");
                    
                    return ResponseEntity.ok(
                            ResponseDto.success(stats, "Estadísticas de incidentes obtenidas exitosamente")
                    );
                })
                .doOnSuccess(result -> log.info("Estadísticas de usuario {} obtenidas", userId));
    }

    @Operation(
            summary = "Endpoint de prueba Cliente",
            description = "Endpoint de prueba para verificar que el servicio Cliente funciona correctamente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio funcionando correctamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/test")
    public Mono<ResponseEntity<ResponseDto<Map<String, String>>>> testClientEndpoint() {
        log.info("Client - Endpoint de prueba llamado");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Client Incidents API funcionando correctamente");
        response.put("status", "OK");
        response.put("role", "CLIENT");
        return Mono.just(ResponseEntity.ok(
                ResponseDto.success(response, "Test exitoso")
        ));
    }

    @Operation(
            summary = "Ping - Verificar conectividad",
            description = "Endpoint simple para verificar que el servicio está activo"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Servicio activo"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/ping")
    public Mono<ResponseEntity<ResponseDto<Map<String, String>>>> ping() {
        log.info("Client - Endpoint ping llamado");
        Map<String, String> response = new HashMap<>();
        response.put("message", "Cliente - Backend funcionando correctamente");
        response.put("timestamp", Instant.now().toString());
        response.put("status", "OK");
        return Mono.just(ResponseEntity.ok(
                ResponseDto.success(response, "Pong")
        ));
    }

    // ================================
    // ENDPOINTS DE TIPOS DE INCIDENCIAS (CLIENT - SOLO LECTURA)
    // ================================

    @Operation(
            summary = "Obtener tipos de incidencias disponibles",
            description = "Obtiene todos los tipos de incidencias activos disponibles para reportar."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-types")
    public Mono<ResponseEntity<ResponseDto<List<IncidentTypeDTO>>>> getAvailableIncidentTypes() {
        log.info("Client - Obteniendo tipos de incidencias disponibles");
        return incidentTypeService.findAll()
                .filter(type -> "ACTIVE".equals(type.getStatus()))
                .collectList()
                .map(types -> ResponseEntity.ok(
                        ResponseDto.success(types, "Tipos de incidencias obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Tipos de incidencias obtenidos"));
    }

    @Operation(
            summary = "Obtener tipo de incidencia por ID",
            description = "Obtiene información de un tipo de incidencia específico (solo activos)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tipo de incidencia encontrado"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "404", description = "Tipo de incidencia no encontrado o no disponible"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-types/{id}")
    public Mono<ResponseEntity<ResponseDto<IncidentTypeDTO>>> getIncidentTypeById(@PathVariable String id) {
        log.info("Client - Obteniendo tipo de incidencia con ID: {}", id);
        return incidentTypeService.findById(id)
                .filter(type -> "ACTIVE".equals(type.getStatus()))
                .map(type -> ResponseEntity.ok(
                        ResponseDto.success(type, "Tipo de incidencia obtenido exitosamente")
                ))
                .switchIfEmpty(Mono.error(new RuntimeException("Tipo de incidencia no disponible")))
                .doOnSuccess(result -> log.info("Client - Tipo de incidencia {} obtenido", id));
    }


    @Operation(
            summary = "Obtener tipos de incidencias por prioridad",
            description = "Obtiene todos los tipos de incidencias activos con un nivel de prioridad específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de tipos obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-types/priority/{priorityLevel}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentTypeDTO>>>> getIncidentTypesByPriority(@PathVariable String priorityLevel) {
        log.info("Client - Obteniendo tipos de incidencias con prioridad: {}", priorityLevel);
        return incidentTypeService.findAll()
                .filter(type -> "ACTIVE".equals(type.getStatus()) && 
                               priorityLevel.equals(type.getPriorityLevel()))
                .collectList()
                .map(types -> ResponseEntity.ok(
                        ResponseDto.success(types, "Tipos de incidencias por prioridad obtenidos exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Tipos con prioridad {} obtenidos", priorityLevel));
    }

    @Operation(
            summary = "Buscar tipos de incidencias",
            description = "Busca tipos de incidencias activos por nombre o descripción."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda completada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incident-types/search")
    public Mono<ResponseEntity<ResponseDto<List<IncidentTypeDTO>>>> searchIncidentTypes(@RequestParam String query) {
        log.info("Client - Buscando tipos de incidencias con query: {}", query);
        return incidentTypeService.findAll()
                .filter(type -> "ACTIVE".equals(type.getStatus()))
                .filter(type -> {
                    String searchText = query.toLowerCase();
                    return type.getTypeName().toLowerCase().contains(searchText) ||
                           (type.getDescription() != null && type.getDescription().toLowerCase().contains(searchText));
                })
                .collectList()
                .map(types -> ResponseEntity.ok(
                        ResponseDto.success(types, "Búsqueda de tipos completada exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Búsqueda de tipos completada"));
    }

    // ================================
    // ENDPOINTS DE USUARIOS (CLIENT - LIMITADO)
    // ================================

    @Operation(
            summary = "Obtener perfil de usuario",
            description = "Obtiene la información del perfil del usuario autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Perfil obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Se requiere rol CLIENT o USER"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/user/profile/{userId}")
    public Mono<ResponseEntity<ResponseDto<UserDTO>>> getUserProfile(
            @PathVariable String userId,
            @Parameter(description = "ID de la organización") 
            @RequestParam(required = true) String organizationId) {
        log.info("Client - Obteniendo perfil de usuario: {} de org: {}", userId, organizationId);
        return userEnrichmentService.getUserById(userId, organizationId)
                .map(user -> ResponseEntity.ok(
                        ResponseDto.success(user, "Perfil obtenido exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Perfil de usuario {} obtenido", userId));
    }

    @Operation(
            summary = "Verificar estado del sistema",
            description = "Verifica la disponibilidad del sistema y sus servicios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estado del sistema obtenido"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/system/status")
    public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> getSystemStatus() {
        log.info("Client - Verificando estado del sistema");
        
        return userEnrichmentService.isUserServiceAvailable()
                .map(userServiceUp -> {
                    Map<String, Object> status = new HashMap<>();
                    status.put("status", "AVAILABLE");
                    status.put("timestamp", Instant.now().toString());
                    status.put("userServiceAvailable", userServiceUp);
                    status.put("incidentServiceAvailable", true);
                    
                    return status;
                })
                .map(status -> ResponseEntity.ok(
                        ResponseDto.success(status, "Estado del sistema verificado")
                ))
                .doOnSuccess(result -> log.info("Client - Estado del sistema verificado"));
    }

    // ================================
    // ENDPOINTS DE RESOLUCIONES (CLIENT - SOLO LECTURA)
    // ================================

    @Operation(
            summary = "Obtener resolución de incidente",
            description = "Obtiene la resolución de un incidente específico (solo incidentes propios)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resolución encontrada"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Sin permisos para ver este incidente"),
            @ApiResponse(responseCode = "404", description = "Incidente o resolución no encontrada"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/{id}/resolution")
    public Mono<ResponseEntity<ResponseDto<IncidentResolutionDTO>>> getIncidentResolution(
            @PathVariable String id,
            @RequestParam String userId) {
        log.info("Client - Obteniendo resolución para incidente: {} por usuario: {}", id, userId);
        
        // Primero verificar que el incidente pertenece al usuario
        return incidentService.findById(id)
                .filter(incident -> userId.equals(incident.getReportedByUserId()))
                .switchIfEmpty(Mono.error(new RuntimeException("Incidente no encontrado o sin permisos")))
                .flatMap(incident -> incidentResolutionService.findByIncidentId(id)
                        .next() // Obtener la primera (y probablemente única) resolución
                        .switchIfEmpty(Mono.error(new RuntimeException("No hay resolución disponible para este incidente")))
                )
                .map(resolution -> ResponseEntity.ok(
                        ResponseDto.success(resolution, "Resolución obtenida exitosamente")
                ))
                .doOnSuccess(result -> log.info("Client - Resolución del incidente {} obtenida", id));
    }

    @Operation(
            summary = "Verificar si un incidente tiene resolución",
            description = "Verifica si un incidente específico tiene una resolución asociada (solo incidentes propios)."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Verificación completada exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Sin permisos para ver este incidente"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/incidents/{id}/has-resolution")
    public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> checkIncidentResolution(
            @PathVariable String id,
            @RequestParam String userId) {
        log.info("Client - Verificando resolución para incidente: {} por usuario: {}", id, userId);
        
        return incidentService.findById(id)
                .filter(incident -> userId.equals(incident.getReportedByUserId()))
                .switchIfEmpty(Mono.error(new RuntimeException("Incidente no encontrado o sin permisos")))
                .flatMap(incident -> 
                    incidentResolutionService.findByIncidentId(id)
                            .hasElements()
                            .map(hasResolution -> {
                                Map<String, Object> result = new HashMap<>();
                                result.put("incidentCode", id);
                                result.put("hasResolution", hasResolution);
                                result.put("incidentStatus", incident.getStatus());
                                result.put("isResolved", incident.getResolved());
                                return result;
                            })
                )
                .map(result -> ResponseEntity.ok(
                        ResponseDto.success(result, "Verificación completada exitosamente")
                ))
                .doOnSuccess(res -> log.info("Client - Verificación de resolución completada para incidente {}", id));
    }

    // Métodos auxiliares
    private void validateClientCreateDTO(IncidentCreateDTO createDTO) {
        if (createDTO.getIncidentCode() == null || createDTO.getIncidentCode().trim().isEmpty()) {
            throw new IllegalArgumentException("El código del incidente es obligatorio");
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
        if (createDTO.getIncidentTypeId() == null || createDTO.getIncidentTypeId().trim().isEmpty()) {
            throw new IllegalArgumentException("El tipo de incidente es obligatorio");
        }
        
        // Validaciones adicionales para clientes
        if (createDTO.getSeverity() == null) {
            createDTO.setSeverity("MEDIUM"); // Valor por defecto
        }
        if (createDTO.getIncidentCategory() == null) {
            createDTO.setIncidentCategory("GENERAL"); // Valor por defecto
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
        incidentDTO.setResolved(createDTO.getResolved() != null ? createDTO.getResolved() : false);
        incidentDTO.setResolutionNotes(createDTO.getResolutionNotes());
        incidentDTO.setRecordStatus(createDTO.getRecordStatus() != null ? createDTO.getRecordStatus() : "ACTIVE");
        return incidentDTO;
    }
}
