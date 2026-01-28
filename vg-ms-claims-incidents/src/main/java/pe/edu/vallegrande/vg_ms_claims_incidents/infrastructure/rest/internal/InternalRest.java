package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.rest.internal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.vg_ms_claims_incidents.application.services.IncidentService;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.IncidentDTO;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.exception.custom.RecursoNoEncontradoException;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * REST Controller INTERNO para comunicación entre microservicios
 * - Solo accesible desde otros microservicios en la red Docker
 * - Requiere token JWE para validar la comunicación MS-to-MS
 * - Endpoints optimizados para uso interno del ecosistema JASS
 */
@Slf4j
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Tag(name = "Internal API", description = "API interna para comunicación entre microservicios (MS-to-MS)")
public class InternalRest {

    private final IncidentService incidentService;

    @Operation(
            summary = "Obtener incidentes por organización (MS-to-MS)",
            description = "Endpoint interno para obtener todos los incidentes de una organización específica. Solo accesible desde otros microservicios."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidentes obtenidos exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWE inválido o ausente"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado - Requiere rol SERVICE"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @GetMapping("/organizations/{organizationId}/incidents")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByOrganization(
            @PathVariable String organizationId,
            @RequestHeader(value = "X-Internal-Token", required = false) String jweToken
    ) {
        log.info("[INTERNAL-MS] 🔍 Petición desde otro MS - Incidentes de organización: {}", organizationId);
        
        return incidentService.findByOrganizationId(organizationId)
                .collectList()
                .map(incidents -> {
                    log.info("[INTERNAL-MS] ✅ {} incidentes obtenidos de org: {}", incidents.size(), organizationId);
                    return ResponseEntity.ok(
                            ResponseDto.success(incidents, 
                                String.format("%d incidentes encontrados para la organización", incidents.size()))
                    );
                });
    }

    @Operation(
            summary = "Obtener incidente por ID (MS-to-MS)",
            description = "Endpoint interno para obtener información detallada de un incidente específico."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Incidente obtenido exitosamente"),
            @ApiResponse(responseCode = "401", description = "Token JWE inválido"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado"),
            @ApiResponse(responseCode = "404", description = "Incidente no encontrado"),
            @ApiResponse(responseCode = "500", description = "Error interno")
    })
    @GetMapping("/incidents/{incidentId}")
    public Mono<ResponseEntity<ResponseDto<IncidentDTO>>> getIncidentById(
            @PathVariable String incidentId,
            @RequestHeader(value = "X-Internal-Token", required = false) String jweToken
    ) {
        log.info("[INTERNAL-MS] 🔍 Petición MS-to-MS - Incidente ID: {}", incidentId);

        return incidentService.findById(incidentId)
                .switchIfEmpty(Mono.error(new RecursoNoEncontradoException("Incidente no encontrado: " + incidentId)))
                .map(incident -> {
                    log.info("[INTERNAL-MS] ✅ Incidente {} obtenido", incident.getIncidentCode());
                    return ResponseEntity.ok(
                            ResponseDto.success(incident, "Incidente obtenido exitosamente")
                    );
                });
    }

    @Operation(
            summary = "Obtener incidentes por estado (MS-to-MS)",
            description = "Endpoint interno para filtrar incidentes por estado."
    )
    @GetMapping("/incidents/status/{status}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByStatus(
            @PathVariable String status,
            @RequestHeader(value = "X-Internal-Token", required = false) String jweToken
    ) {
        log.info("[INTERNAL-MS] 🔍 Petición MS-to-MS - Incidentes por estado: {}", status);

        return incidentService.findByStatus(status)
                .collectList()
                .map(incidents -> {
                    log.info("[INTERNAL-MS] ✅ {} incidentes con estado: {}", incidents.size(), status);
                    return ResponseEntity.ok(
                            ResponseDto.success(incidents, 
                                String.format("%d incidentes con estado %s", incidents.size(), status))
                    );
                });
    }

    @Operation(
            summary = "Obtener incidentes por tipo (MS-to-MS)",
            description = "Endpoint interno para filtrar incidentes por tipo de incidente."
    )
    @GetMapping("/incidents/type/{typeId}")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsByType(
            @PathVariable String typeId,
            @RequestHeader(value = "X-Internal-Token", required = false) String jweToken
    ) {
        log.info("[INTERNAL-MS] 🔍 Petición MS-to-MS - Incidentes por tipo: {}", typeId);

        return incidentService.findByIncidentTypeId(typeId)
                .collectList()
                .map(incidents -> {
                    log.info("[INTERNAL-MS] ✅ {} incidentes de tipo: {}", incidents.size(), typeId);
                    return ResponseEntity.ok(
                            ResponseDto.success(incidents, 
                                String.format("%d incidentes del tipo especificado", incidents.size()))
                    );
                });
    }

    @Operation(
            summary = "Obtener incidentes asignados a usuario (MS-to-MS)",
            description = "Endpoint interno para obtener todos los incidentes asignados a un usuario específico."
    )
    @GetMapping("/users/{userId}/assigned-incidents")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsAssignedToUser(
            @PathVariable String userId,
            @RequestHeader(value = "X-Internal-Token", required = false) String jweToken
    ) {
        log.info("[INTERNAL-MS] 🔍 Petición MS-to-MS - Incidentes asignados a: {}", userId);

        return incidentService.findByAssignedToUserId(userId)
                .collectList()
                .map(incidents -> {
                    log.info("[INTERNAL-MS] ✅ {} incidentes asignados al usuario: {}", incidents.size(), userId);
                    return ResponseEntity.ok(
                            ResponseDto.success(incidents, 
                                String.format("%d incidentes asignados al usuario", incidents.size()))
                    );
                });
    }

    @Operation(
            summary = "Obtener incidentes resueltos por usuario (MS-to-MS)",
            description = "Endpoint interno para obtener todos los incidentes resueltos por un usuario específico."
    )
    @GetMapping("/users/{userId}/resolved-incidents")
    public Mono<ResponseEntity<ResponseDto<List<IncidentDTO>>>> getIncidentsResolvedByUser(
            @PathVariable String userId,
            @RequestHeader(value = "X-Internal-Token", required = false) String jweToken
    ) {
        log.info("[INTERNAL-MS] 🔍 Petición MS-to-MS - Incidentes resueltos por: {}", userId);

        return incidentService.findByResolvedByUserId(userId)
                .collectList()
                .map(incidents -> {
                    log.info("[INTERNAL-MS] ✅ {} incidentes resueltos por: {}", incidents.size(), userId);
                    return ResponseEntity.ok(
                            ResponseDto.success(incidents, 
                                String.format("%d incidentes resueltos por el usuario", incidents.size()))
                    );
                });
    }

    @Operation(
            summary = "Obtener estadísticas de incidentes (MS-to-MS)",
            description = "Endpoint interno para obtener estadísticas agregadas de incidentes."
    )
    @GetMapping("/incidents/statistics")
    public Mono<ResponseEntity<ResponseDto<java.util.Map<String, Object>>>> getIncidentStatistics(
            @RequestHeader(value = "X-Internal-Token", required = false) String jweToken
    ) {
        log.info("[INTERNAL-MS] 🔍 Petición MS-to-MS - Estadísticas de incidentes");

        return incidentService.findAll()
                .collectList()
                .map(incidents -> {
                    java.util.Map<String, Object> stats = new java.util.HashMap<>();
                    stats.put("totalIncidents", incidents.size());
                    stats.put("activeIncidents", incidents.stream().filter(i -> "ACTIVE".equals(i.getStatus())).count());
                    stats.put("resolvedIncidents", incidents.stream().filter(IncidentDTO::getResolved).count());
                    stats.put("pendingIncidents", incidents.stream().filter(i -> !i.getResolved()).count());
                    
                    log.info("[INTERNAL-MS] ✅ Estadísticas generadas: {} total incidentes", incidents.size());
                    return ResponseEntity.ok(
                            ResponseDto.success(stats, "Estadísticas de incidentes obtenidas")
                    );
                });
    }

    @Operation(
            summary = "Health check interno (MS-to-MS)",
            description = "Endpoint de health check para verificar disponibilidad del servicio desde otros microservicios."
    )
    @GetMapping("/health")
    public Mono<ResponseEntity<ResponseDto<String>>> internalHealthCheck() {
        log.debug("[INTERNAL-MS] 🏥 Health check desde otro microservicio");
        return Mono.just(ResponseEntity.ok(
                ResponseDto.success("UP", "Servicio de incidentes disponible")
        ));
    }
}
