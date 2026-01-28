package pe.edu.vallegrande.msdistribution.infrastructure.rest.admin;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import pe.edu.vallegrande.msdistribution.application.services.DistributionProgramService;
import pe.edu.vallegrande.msdistribution.application.services.DistributionRouteService;
import pe.edu.vallegrande.msdistribution.application.services.DistributionScheduleService;

import pe.edu.vallegrande.msdistribution.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionRoute;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.*;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.*;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.custom.CustomException;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller para endpoints administrativos del microservicio de
 * distribución.
 * Proporciona operaciones CRUD y gestión de estados para Programas, Rutas y
 * Horarios.
 * Protegido con rol ADMIN.
 * 
 * @version 1.0
 */
@RestController
@RequestMapping("/internal")
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Slf4j
public class AdminRest {

        private final DistributionProgramService programService;
        private final DistributionRouteService routeService;
        private final DistributionScheduleService scheduleService;

        /**
         * Constructor con inyección de dependencias.
         * 
         * @param programService  Servicio de programas de distribución.
         * @param routeService    Servicio de rutas.
         * @param scheduleService Servicio de horarios.
         */
        public AdminRest(
                        DistributionProgramService programService,
                        DistributionRouteService routeService,
                        DistributionScheduleService scheduleService) {
                this.programService = programService;
                this.routeService = routeService;
                this.scheduleService = scheduleService;
        }

        // ===============================
        // DASHBOARD & STATISTICS
        // ===============================

        /**
         * Obtiene estadísticas generales del dashboard.
         * 
         * @return Estadísticas como total de programas, rutas activas y horarios
         *         activos.
         */
        @GetMapping("/dashboard/stats")
        public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> getDashboardStats() {
                log.debug("Fetching comprehensive dashboard statistics");

                return Mono.zip(
                                programService.getAll().count(),
                                routeService.getAllActive().count(),
                                scheduleService.getAllActive().count())
                                .flatMap(tuple -> {
                                        Map<String, Object> stats = Map.of(
                                                        "totalPrograms", tuple.getT1(),
                                                        "activeRoutes", tuple.getT2(),
                                                        "activeSchedules", tuple.getT3(),
                                                        "lastUpdated", LocalDateTime.now(),
                                                        "systemStatus", "ACTIVE");
                                        return success("Estadísticas del dashboard obtenidas correctamente", stats);
                                })
                                .onErrorResume(e -> this.<Map<String, Object>>error(e));
        }

        /**
         * Obtiene un resumen detallado del sistema.
         * 
         * @return Resumen con conteos activos/inactivos de programas y configuración.
         */
        @GetMapping("/dashboard/summary")
        public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> getSystemSummary() {
                log.debug("Fetching system summary for admin dashboard");

                return Mono.zip(
                                programService.getAll().count(),
                                programService.countActive(),
                                programService.countInactive(),
                                routeService.getAllActive().count(),
                                scheduleService.getAllActive().count())
                                .flatMap(tuple -> {
                                        Map<String, Object> summary = Map.of(
                                                        "programs", Map.of(
                                                                        "total", tuple.getT1(),
                                                                        "active", tuple.getT2(),
                                                                        "inactive", tuple.getT3()),
                                                        "infrastructure", Map.of(
                                                                        "activeRoutes", tuple.getT4(),
                                                                        "activeSchedules", tuple.getT5()),
                                                        "timestamp", LocalDateTime.now());
                                        return success("Resumen del sistema obtenido correctamente", summary);
                                })
                                .onErrorResume(e -> this.<Map<String, Object>>error(e));
        }

        // ===============================
        // DISTRIBUTION PROGRAM ENDPOINTS
        // ===============================

        // ===============================
        // DISTRIBUTION PROGRAM ENDPOINTS
        // ===============================

        /**
         * Listar todos los programas.
         * 
         * @return Flux de programas envueltos en ResponseEntity.
         */
        @GetMapping("/program")
        public Mono<ResponseEntity<ResponseDto<List<DistributionProgramResponse>>>> getAllPrograms() {
                log.debug("Fetching all distribution programs");
                return programService.getAll()
                                .collectList()
                                .flatMap(data -> success("Programas obtenidos correctamente", data))
                                .onErrorResume(e -> this.<List<DistributionProgramResponse>>error(e));
        }

        /**
         * Obtener programa por ID.
         * 
         * @param id ID del programa.
         * @return Programa encontrado o 404.
         */
        @GetMapping("/program/{id}")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> getProgramById(@PathVariable String id) {
                return programService.getById(id)
                                .flatMap(data -> success("Programa obtenido correctamente", data))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionProgram", id)))
                                .onErrorResume(e -> this.<DistributionProgramResponse>error(e));
        }

        /**
         * Crear nuevo programa.
         * 
         * @param request Datos de creación.
         * @return Programa creado con status 201.
         */
        @PostMapping("/program")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> createProgram(
                        @Valid @RequestBody DistributionProgramCreateRequest request) {
                log.debug("Creating program for organization: {}", request.getOrganizationId());
                return programService.save(request)
                                .flatMap(data -> success("Programa creado correctamente", data, HttpStatus.CREATED))
                                .onErrorResume(e -> this.<DistributionProgramResponse>error(e));
        }

        /**
         * Actualizar programa existente parcialmente.
         * 
         * @param id      ID del programa.
         * @param request Datos a actualizar.
         * @return Programa actualizado.
         */
        @PutMapping("/program/{id}")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> updateProgram(@PathVariable String id,
                        @RequestBody pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramUpdateRequest request) {
                log.debug("Updating program {}", id);
                return programService.updatePartial(id, request)
                                .flatMap(data -> success("Programa actualizado correctamente", data))
                                .onErrorResume(e -> this.<DistributionProgramResponse>error(e));
        }

        /**
         * Eliminar programa (físico o lógico según implementación servicio).
         * 
         * @param id ID del programa.
         * @return Void.
         */
        @DeleteMapping("/program/{id}")
        public Mono<ResponseEntity<ResponseDto<Void>>> deleteProgram(@PathVariable String id) {
                return programService.delete(id)
                                .then(this.<Void>success("Programa eliminado correctamente", null))
                                .onErrorResume(e -> this.<Void>error(e));
        }

        // ===============================
        // ROUTE ENDPOINTS
        // ===============================

        // ===============================
        // ROUTE ENDPOINTS
        // ===============================

        /**
         * Listar todas las rutas.
         * 
         * @return Lista de rutas.
         */
        @GetMapping("/route")
        public Mono<ResponseEntity<ResponseDto<List<DistributionRoute>>>> getAllRoutes() {
                log.debug("Fetching all routes");
                return routeService.getAll()
                                .collectList()
                                .flatMap(data -> success("Rutas obtenidas correctamente", data))
                                .onErrorResume(e -> this.<List<DistributionRoute>>error(e));
        }

        /**
         * Obtener ruta por ID.
         * 
         * @param id Identificador de la ruta.
         * @return Detalle de la ruta.
         */
        @GetMapping("/route/{id}")
        public Mono<ResponseEntity<ResponseDto<DistributionRoute>>> getRouteById(@PathVariable String id) {
                return routeService.getById(id)
                                .flatMap(data -> success("Ruta obtenida correctamente", data))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionRoute", id)))
                                .onErrorResume(e -> this.<DistributionRoute>error(e));
        }

        /**
         * Crear nueva ruta.
         * 
         * @param request Datos de la ruta.
         * @return Ruta creada.
         */
        @PostMapping("/route")
        public Mono<ResponseEntity<ResponseDto<DistributionRouteResponse>>> createRoute(
                        @Valid @RequestBody DistributionRouteCreateRequest request) {
                log.debug("Creating route for organization: {}", request.getOrganizationId());
                return routeService.save(request)
                                .flatMap(data -> success("Ruta creada correctamente", data, HttpStatus.CREATED))
                                .onErrorResume(e -> this.<DistributionRouteResponse>error(e));
        }

        /**
         * Actualizar ruta parcialmente.
         * 
         * @param id      Identificador de ruta.
         * @param request Datos a modificar.
         * @return Ruta actualizada.
         */
        @PutMapping("/route/{id}")
        public Mono<ResponseEntity<ResponseDto<DistributionRouteResponse>>> updateRoute(@PathVariable String id,
                        @RequestBody pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionRouteUpdateRequest request) {
                log.debug("Updating route {}", id);
                return routeService.updatePartial(id, request)
                                .flatMap(data -> success("Ruta actualizada correctamente", data))
                                .onErrorResume(e -> this.<DistributionRouteResponse>error(e));
        }

        /**
         * Eliminar ruta.
         * 
         * @param id Identificador de ruta.
         * @return Void.
         */
        @DeleteMapping("/route/{id}")
        public Mono<ResponseEntity<ResponseDto<Void>>> deleteRoute(@PathVariable String id) {
                return routeService.delete(id)
                                .then(this.<Void>success("Ruta eliminada correctamente", null))
                                .onErrorResume(e -> this.<Void>error(e));
        }

        // ===============================
        // SCHEDULE ENDPOINTS
        // ===============================

        // ===============================
        // SCHEDULE ENDPOINTS
        // ===============================

        /**
         * Listar todos los horarios.
         * 
         * @return Lista de horarios.
         */
        @GetMapping("/schedule")
        public Mono<ResponseEntity<ResponseDto<List<DistributionSchedule>>>> getAllSchedules() {
                log.debug("Fetching all schedules");
                return scheduleService.getAll()
                                .collectList()
                                .flatMap(data -> success("Horarios obtenidos correctamente", data))
                                .onErrorResume(e -> this.<List<DistributionSchedule>>error(e));
        }

        /**
         * Obtener horario por ID.
         * 
         * @param id Identificador de horario.
         * @return Detalle del horario.
         */
        @GetMapping("/schedule/{id}")
        public Mono<ResponseEntity<ResponseDto<DistributionSchedule>>> getScheduleById(@PathVariable String id) {
                return scheduleService.getById(id)
                                .flatMap(data -> success("Horario obtenido correctamente", data))
                                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)))
                                .onErrorResume(e -> this.<DistributionSchedule>error(e));
        }

        /**
         * Crear nuevo horario.
         * 
         * @param request Datos del horario.
         * @return Horario creado.
         */
        @PostMapping("/schedule")
        public Mono<ResponseEntity<ResponseDto<DistributionScheduleResponse>>> createSchedule(
                        @Valid @RequestBody DistributionScheduleCreateRequest request) {
                log.debug("Creating schedule for organization: {}", request.getOrganizationId());
                return scheduleService.save(request)
                                .flatMap(data -> success("Horario creado correctamente", data, HttpStatus.CREATED))
                                .onErrorResume(e -> this.<DistributionScheduleResponse>error(e));
        }

        /**
         * Actualizar horario.
         * 
         * @param id      Identificador.
         * @param request Datos a modificar.
         * @return Horario actualizado.
         */
        @PutMapping("/schedule/{id}")
        public Mono<ResponseEntity<ResponseDto<DistributionScheduleResponse>>> updateSchedule(@PathVariable String id,
                        @Valid @RequestBody DistributionScheduleCreateRequest request) {
                log.debug("Updating schedule {} for organization: {}", id, request.getOrganizationId());
                return scheduleService.update(id, request)
                                .flatMap(data -> success("Horario actualizado correctamente", data))
                                .onErrorResume(e -> this.<DistributionScheduleResponse>error(e));
        }

        /**
         * Eliminar horario.
         * 
         * @param id Identificador.
         * @return Void.
         */
        @DeleteMapping("/schedule/{id}")
        public Mono<ResponseEntity<ResponseDto<Void>>> deleteSchedule(@PathVariable String id) {
                return scheduleService.delete(id)
                                .then(this.<Void>success("Horario eliminado correctamente", null))
                                .onErrorResume(e -> this.<Void>error(e));
        }

        // ===============================
        // ACTIVATE/DEACTIVATE ENDPOINTS
        // ===============================

        // ===============================
        // ACTIVATE/DEACTIVATE ENDPOINTS
        // ===============================

        /**
         * Activar programa.
         * 
         * @param id ID programa.
         * @return Programa activado.
         */
        @PutMapping("/program/{id}/activate")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> activateProgram(@PathVariable String id) {
                log.debug("Activating program {}", id);
                return programService.activate(id)
                                .flatMap(data -> success("Programa activado correctamente", data))
                                .onErrorResume(e -> this.<DistributionProgramResponse>error(e));
        }

        /**
         * Desactivar programa.
         * 
         * @param id ID programa.
         * @return Programa desactivado.
         */
        @PutMapping("/program/{id}/deactivate")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> deactivateProgram(
                        @PathVariable String id) {
                log.debug("Deactivating program {}", id);
                return programService.desactivate(id)
                                .flatMap(data -> success("Programa desactivado correctamente", data))
                                .onErrorResume(e -> this.<DistributionProgramResponse>error(e));
        }

        /**
         * Activar ruta.
         * 
         * @param id ID ruta.
         * @return Ruta activada.
         */
        @PutMapping("/route/{id}/activate")
        public Mono<ResponseEntity<ResponseDto<DistributionRouteResponse>>> activateRoute(@PathVariable String id) {
                log.debug("Activating route {}", id);
                return routeService.activate(id)
                                .flatMap(data -> success("Ruta activada correctamente", data))
                                .onErrorResume(e -> this.<DistributionRouteResponse>error(e));
        }

        /**
         * Desactivar ruta.
         * 
         * @param id ID ruta.
         * @return Ruta desactivada.
         */
        @PutMapping("/route/{id}/deactivate")
        public Mono<ResponseEntity<ResponseDto<DistributionRouteResponse>>> deactivateRoute(@PathVariable String id) {
                log.debug("Deactivating route {}", id);
                return routeService.deactivate(id)
                                .flatMap(data -> success("Ruta desactivada correctamente", data))
                                .onErrorResume(e -> this.<DistributionRouteResponse>error(e));
        }

        /**
         * Activar horario.
         * 
         * @param id ID horario.
         * @return Horario activado.
         */
        @PutMapping("/schedule/{id}/activate")
        public Mono<ResponseEntity<ResponseDto<DistributionScheduleResponse>>> activateSchedule(
                        @PathVariable String id) {
                log.debug("Activating schedule {}", id);
                return scheduleService.activate(id)
                                .flatMap(data -> success("Horario activado correctamente", data))
                                .onErrorResume(e -> this.<DistributionScheduleResponse>error(e));
        }

        /**
         * Desactivar horario.
         * 
         * @param id ID horario.
         * @return Horario desactivado.
         */
        @PutMapping("/schedule/{id}/deactivate")
        public Mono<ResponseEntity<ResponseDto<DistributionScheduleResponse>>> deactivateSchedule(
                        @PathVariable String id) {
                log.debug("Deactivating schedule {}", id);
                return scheduleService.deactivate(id)
                                .flatMap(data -> success("Horario desactivado correctamente", data))
                                .onErrorResume(e -> this.<DistributionScheduleResponse>error(e));
        }

        // ===============================
        // WATER DELIVERY STATUS ENDPOINTS
        // ===============================

        // ===============================
        // WATER DELIVERY STATUS ENDPOINTS
        // ===============================

        /**
         * Actualizar estado de entrega de agua de un programa.
         * 
         * @param id      ID programa.
         * @param request Datos de estado y observaciones.
         * @return Programa actualizado.
         */
        @PutMapping("/program/{id}/water-status")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> updateWaterDeliveryStatus(
                        @PathVariable String id,
                        @Valid @RequestBody WaterDeliveryStatusUpdateRequest request) {
                log.debug("Updating water delivery status for program: {}", id);
                return programService.updateWaterDeliveryStatus(
                                id,
                                request.getWaterDeliveryStatus(),
                                request.getObservations(),
                                request.getConfirmedBy())
                                .flatMap(data -> success("Estado de entrega de agua actualizado correctamente", data))
                                .onErrorResume(e -> this.<DistributionProgramResponse>error(e));
        }

        /**
         * Marcar programa como CON AGUA.
         * 
         * @param id   ID programa.
         * @param body Observaciones y confirmador (opcional).
         * @return Programa actualizado.
         */
        @PutMapping("/program/{id}/mark-with-water")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> markWithWater(
                        @PathVariable String id,
                        @RequestBody(required = false) Map<String, String> body) {
                log.debug("Marking program {} as WITH water", id);
                String observations = body != null ? body.get("observations") : null;
                String confirmedBy = body != null ? body.get("confirmedBy") : null;
                return programService.markWithWater(id, observations, confirmedBy)
                                .flatMap(data -> success("Programa marcado CON AGUA correctamente", data))
                                .onErrorResume(e -> this.<DistributionProgramResponse>error(e));
        }

        /**
         * Marcar programa como SIN AGUA.
         * 
         * @param id   ID programa.
         * @param body Observaciones y confirmador (opcional).
         * @return Programa actualizado.
         */
        @PutMapping("/program/{id}/mark-without-water")
        public Mono<ResponseEntity<ResponseDto<DistributionProgramResponse>>> markWithoutWater(
                        @PathVariable String id,
                        @RequestBody(required = false) Map<String, String> body) {
                log.debug("Marking program {} as WITHOUT water", id);
                String observations = body != null ? body.get("observations") : null;
                String confirmedBy = body != null ? body.get("confirmedBy") : null;
                return programService.markWithoutWater(id, observations, confirmedBy)
                                .flatMap(data -> success("Programa marcado SIN AGUA correctamente", data))
                                .onErrorResume(e -> this.<DistributionProgramResponse>error(e));
        }

        /**
         * Obtener programas por estado de agua.
         * 
         * @param status Estado (CON_AGUA, SIN_AGUA).
         * @return Lista de programas.
         */
        @GetMapping("/program/water-status/{status}")
        public Mono<ResponseEntity<ResponseDto<List<DistributionProgramResponse>>>> getProgramsByWaterStatus(
                        @PathVariable String status) {
                log.debug("Fetching programs with water delivery status: {}", status);
                return programService.getByWaterDeliveryStatus(status)
                                .collectList()
                                .flatMap(data -> success(
                                                "Programas filtrados por estado de agua obtenidos correctamente", data))
                                .onErrorResume(e -> this.<List<DistributionProgramResponse>>error(e));
        }

        /**
         * Obtener programas CON agua.
         * 
         * @return Lista de programas.
         */
        @GetMapping("/program/with-water")
        public Mono<ResponseEntity<ResponseDto<List<DistributionProgramResponse>>>> getProgramsWithWater() {
                log.debug("Fetching programs with water delivered");
                return programService.getProgramsWithWater()
                                .collectList()
                                .flatMap(data -> success("Programas con agua obtenidos correctamente", data))
                                .onErrorResume(e -> this.<List<DistributionProgramResponse>>error(e));
        }

        /**
         * Obtener programas SIN agua.
         * 
         * @return Lista de programas.
         */
        @GetMapping("/program/without-water")
        public Mono<ResponseEntity<ResponseDto<List<DistributionProgramResponse>>>> getProgramsWithoutWater() {
                log.debug("Fetching programs without water delivered");
                return programService.getProgramsWithoutWater()
                                .collectList()
                                .flatMap(data -> success("Programas sin agua obtenidos correctamente", data))
                                .onErrorResume(e -> this.<List<DistributionProgramResponse>>error(e));
        }

        /**
         * Obtener estadísticas de entrega de agua.
         * 
         * @return Estadísticas.
         */
        @GetMapping("/program/water-stats")
        public Mono<ResponseEntity<ResponseDto<Map<String, Object>>>> getWaterDeliveryStats() {
                log.debug("Fetching water delivery statistics");

                return Mono.zip(
                                programService.countByWaterDeliveryStatus("ACTIVE"),
                                programService.countByWaterDeliveryStatus("INACTIVE"),
                                programService.getAll().count())
                                .flatMap(tuple -> {
                                        long active = tuple.getT1();
                                        long inactive = tuple.getT2();
                                        long total = tuple.getT3();

                                        double deliveryRate = total > 0 ? (active * 100.0 / total) : 0.0;

                                        Map<String, Object> stats = Map.of(
                                                        "active", active,
                                                        "inactive", inactive,
                                                        "total", total,
                                                        "deliveryRate", String.format("%.2f%%", deliveryRate),
                                                        "timestamp", LocalDateTime.now());
                                        return success("Estadísticas de entrega de agua obtenidas correctamente",
                                                        stats);
                                })
                                .onErrorResume(e -> this.<Map<String, Object>>error(e));
        }

        // ===============================
        // HELPER METHODS
        // ===============================

        // ===============================
        // HELPER METHODS
        // ===============================

        /**
         * Respuesta de éxito estándar.
         * 
         * @param message Mensaje.
         * @param data    Datos.
         * @return ResponseEntity OK.
         */
        private <T> Mono<ResponseEntity<ResponseDto<T>>> success(String message, T data) {
                return Mono.just(ResponseEntity.ok(new ResponseDto<T>(true, message, data)));
        }

        /**
         * Respuesta de éxito con status personalizado.
         * 
         * @param message Mensaje.
         * @param data    Datos.
         * @param status  HTTP Status.
         * @return ResponseEntity.
         */
        private <T> Mono<ResponseEntity<ResponseDto<T>>> success(String message, T data, HttpStatus status) {
                return Mono.just(ResponseEntity.status(status)
                                .body(new ResponseDto<T>(true, message, data)));
        }

        /**
         * Manejo de errores.
         * 
         * @param e Excepción.
         * @return ResponseEntity con error.
         */
        private <T> Mono<ResponseEntity<ResponseDto<T>>> error(Throwable e) {
                log.error("Error in admin endpoint: {}", e.getMessage());
                HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
                if (e instanceof CustomException) {
                        // Here we could map CustomException status to HttpStatus if available
                        // For now default to 500 or 404 depending on exception type if we could inspect
                        // it
                }
                return Mono.just(ResponseEntity.status(status)
                                .body(new ResponseDto<T>(false, e.getMessage(), null)));
        }
}
