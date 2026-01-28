package pe.edu.vallegrande.msdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import pe.edu.vallegrande.msdistribution.application.services.DistributionProgramService;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionProgram;
import pe.edu.vallegrande.msdistribution.infrastructure.document.DistributionProgramDocument;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionProgramResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.mapper.DistributionProgramMapper;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionProgramRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;

/**
 * Implementación del servicio de gestión de Programas de Distribución.
 * Utiliza programación reactiva con Project Reactor (Flux/Mono).
 * 
 * @version 1.0
 */
@Service
@Slf4j
public class DistributionProgramServiceImpl implements DistributionProgramService {

    private final DistributionProgramRepository repository;
    private final DistributionProgramMapper mapper;
    private static final String PROGRAM_PREFIX = "PRG";

    /**
     * Constructor para inyección de dependencias.
     * 
     * @param repository Repositorio reactivo de MongoDB.
     * @param mapper     Mapper para conversión entre Entidad y DTO.
     */
    public DistributionProgramServiceImpl(DistributionProgramRepository repository,
            DistributionProgramMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Flux<DistributionProgramResponse> getAll() {
        return repository.findAll()
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Flux<DistributionProgramResponse> getByOrganizationId(String organizationId) {
        return repository.findByOrganizationId(organizationId)
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Flux<DistributionProgramResponse> getAllActive() {
        return repository.findAllByStatus("ACTIVE")
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Flux<DistributionProgramResponse> getAllInactive() {
        return repository.findAllByStatus("INACTIVE")
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<Long> countActive() {
        return repository.countByStatus("ACTIVE");
    }

    @Override
    public Mono<Long> countInactive() {
        return repository.countByStatus("INACTIVE");
    }

    @Override
    public Mono<DistributionProgramResponse> getById(String id) {
        return repository.findById(id)
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> save(DistributionProgramCreateRequest request) {
        // Genera código autoincremental antes de guardar
        return generateNextProgramCode()
                .flatMap(generatedCode -> {
                    DistributionProgram program = DistributionProgram.builder()
                            .organizationId(request.getOrganizationId())
                            .programCode(generatedCode)
                            .scheduleId(request.getScheduleId())
                            .routeId(request.getRouteId())
                            .zoneId(request.getZoneId())
                            .streetId(request.getStreetId())
                            .programDate(request.getProgramDate())
                            .plannedStartTime(request.getPlannedStartTime())
                            .plannedEndTime(request.getPlannedEndTime())
                            .actualStartTime(request.getActualStartTime())
                            .actualEndTime(request.getActualEndTime())
                            .status("ACTIVE") // Estado inicial por defecto
                            .responsibleUserId(request.getResponsibleUserId())
                            .observations(request.getObservations())
                            .createdAt(Instant.now())
                            .updatedAt(Instant.now())
                            .build();

                    DistributionProgramDocument document = mapper.toDocument(program);
                    return repository.save(document)
                            .map(mapper::toDomain)
                            .map(this::toResponse);
                });
    }

    @Override
    public Mono<DistributionProgramResponse> update(String id, DistributionProgramCreateRequest request) {
        return repository.findById(id)
                .flatMap(existingDoc -> {
                    // Actualización completa de campos permitidos
                    existingDoc.setOrganizationId(request.getOrganizationId());
                    existingDoc.setScheduleId(request.getScheduleId());
                    existingDoc.setRouteId(request.getRouteId());
                    existingDoc.setZoneId(request.getZoneId());
                    existingDoc.setStreetId(request.getStreetId());
                    existingDoc.setProgramDate(request.getProgramDate());
                    existingDoc.setPlannedStartTime(request.getPlannedStartTime());
                    existingDoc.setPlannedEndTime(request.getPlannedEndTime());
                    existingDoc.setResponsibleUserId(request.getResponsibleUserId());
                    existingDoc.setObservations(request.getObservations());
                    existingDoc.setUpdatedAt(Instant.now());

                    return repository.save(existingDoc);
                })
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> updatePartial(String id,
            pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionProgramUpdateRequest request) {
        return repository.findById(id)
                .flatMap(existingDoc -> {
                    // Verificación nulidad campo por campo para actualización parcial
                    if (request.getOrganizationId() != null) {
                        existingDoc.setOrganizationId(request.getOrganizationId());
                    }
                    if (request.getScheduleId() != null) {
                        existingDoc.setScheduleId(request.getScheduleId());
                    }
                    if (request.getRouteId() != null) {
                        existingDoc.setRouteId(request.getRouteId());
                    }
                    if (request.getZoneId() != null) {
                        existingDoc.setZoneId(request.getZoneId());
                    }
                    if (request.getStreetId() != null) {
                        existingDoc.setStreetId(request.getStreetId());
                    }
                    if (request.getProgramDate() != null) {
                        existingDoc.setProgramDate(request.getProgramDate());
                    }
                    if (request.getPlannedStartTime() != null) {
                        existingDoc.setPlannedStartTime(request.getPlannedStartTime());
                    }
                    if (request.getPlannedEndTime() != null) {
                        existingDoc.setPlannedEndTime(request.getPlannedEndTime());
                    }
                    if (request.getActualStartTime() != null) {
                        existingDoc.setActualStartTime(request.getActualStartTime());
                    }
                    if (request.getActualEndTime() != null) {
                        existingDoc.setActualEndTime(request.getActualEndTime());
                    }
                    if (request.getResponsibleUserId() != null) {
                        existingDoc.setResponsibleUserId(request.getResponsibleUserId());
                    }
                    if (request.getObservations() != null) {
                        existingDoc.setObservations(request.getObservations());
                    }
                    existingDoc.setUpdatedAt(Instant.now());

                    return repository.save(existingDoc);
                })
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        // Eliminación lógica: marca como INACTIVE en lugar de borrar el registro
        return repository.findById(id)
                .flatMap(document -> {
                    document.setStatus("INACTIVE");
                    document.setUpdatedAt(Instant.now());
                    return repository.save(document);
                })
                .then();
    }

    @Override
    public Mono<DistributionProgramResponse> activate(String id) {
        return repository.findById(id)
                .flatMap(document -> {
                    document.setStatus("ACTIVE");
                    document.setUpdatedAt(Instant.now());
                    return repository.save(document);
                })
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> desactivate(String id) {
        return repository.findById(id)
                .flatMap(document -> {
                    document.setStatus("INACTIVE");
                    document.setUpdatedAt(Instant.now());
                    return repository.save(document);
                })
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> physicalDelete(String id) {
        // Eliminación física directa en base de datos
        return repository.deleteById(id);
    }

    @Override
    public Mono<DistributionProgramResponse> updateWaterDeliveryStatus(String id, String status,
            String observations, String confirmedBy) {
        return repository.findById(id)
                .flatMap(document -> {
                    document.setStatus(status);
                    if (observations != null && !observations.isEmpty()) {
                        document.setObservations(observations);
                    }
                    // TODO: Podría agregarse lógica para registrar quién confirmó (confirmedBy) si
                    // el modelo lo soportara
                    document.setUpdatedAt(Instant.now());
                    return repository.save(document);
                })
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Flux<DistributionProgramResponse> getByWaterDeliveryStatus(String status) {
        return repository.findAllByStatus(status)
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<Long> countByWaterDeliveryStatus(String status) {
        return repository.countByStatus(status);
    }

    @Override
    public Flux<DistributionProgramResponse> getProgramsWithWater() {
        return repository.findAllByStatus("ACTIVE")
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Flux<DistributionProgramResponse> getProgramsWithoutWater() {
        return repository.findAllByStatus("INACTIVE")
                .map(mapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionProgramResponse> markWithWater(String id, String observations, String confirmedBy) {
        // Reutiliza lógica genérica estableciendo estado activo
        return updateWaterDeliveryStatus(id, "ACTIVE", observations, confirmedBy);
    }

    @Override
    public Mono<DistributionProgramResponse> markWithoutWater(String id, String observations, String confirmedBy) {
        // Reutiliza lógica genérica estableciendo estado inactivo
        return updateWaterDeliveryStatus(id, "INACTIVE", observations, confirmedBy);
    }

    /**
     * Convierte una entidad de dominio a un DTO de respuesta.
     * 
     * @param program Entidad de dominio.
     * @return DTO de respuesta.
     */
    private DistributionProgramResponse toResponse(DistributionProgram program) {
        return DistributionProgramResponse.builder()
                .id(program.getId())
                .organizationId(program.getOrganizationId())
                .programCode(program.getProgramCode())
                .scheduleId(program.getScheduleId())
                .routeId(program.getRouteId())
                .zoneId(program.getZoneId())
                .streetId(program.getStreetId())
                .programDate(program.getProgramDate())
                .plannedStartTime(program.getPlannedStartTime())
                .plannedEndTime(program.getPlannedEndTime())
                .actualStartTime(program.getActualStartTime())
                .actualEndTime(program.getActualEndTime())
                .status(program.getStatus())
                .responsibleUserId(program.getResponsibleUserId())
                .observations(program.getObservations())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .build();
    }

    /**
     * Genera el siguiente código de programa (ej. PRG001, PRG002).
     * Recupera el último código de la BD, extrae el número e incrementa.
     * 
     * @return Mono con el nuevo código generado.
     */
    private Mono<String> generateNextProgramCode() {
        return repository.findFirstByOrderByProgramCodeDesc()
                .map(last -> {
                    String lastCode = last.getProgramCode();
                    int number = 0;
                    try {
                        number = Integer.parseInt(lastCode.replace(PROGRAM_PREFIX, ""));
                    } catch (NumberFormatException ignored) {
                        // Si falla el parseo, reinicia a 0
                    }
                    return String.format(PROGRAM_PREFIX + "%03d", number + 1);
                })
                .defaultIfEmpty(PROGRAM_PREFIX + "001"); // Valor inicial si no hay registros
    }
}
