package pe.edu.vallegrande.msdistribution.application.services.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pe.edu.vallegrande.msdistribution.application.services.DistributionScheduleService;
import pe.edu.vallegrande.msdistribution.domain.models.DistributionSchedule;
import pe.edu.vallegrande.msdistribution.domain.enums.Constants;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.request.DistributionScheduleCreateRequest;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.response.DistributionScheduleResponse;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.custom.CustomException;
import pe.edu.vallegrande.msdistribution.infrastructure.repository.DistributionScheduleRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.time.Instant;

/**
 * Servicio de implementación para la gestión de Horarios de Distribución.
 * 
 * @version 1.0
 */
@Service
@Slf4j
public class DistributionScheduleServiceImpl implements DistributionScheduleService {

    private final DistributionScheduleRepository repository;

    /**
     * Constructor con inyección de dependencias.
     * 
     * @param repository Repositorio de horarios.
     */
    public DistributionScheduleServiceImpl(DistributionScheduleRepository repository) {
        this.repository = repository;
    }

    @Override
    public Flux<DistributionSchedule> getAll() {
        return repository.findAll();
    }

    @Override
    public Flux<DistributionSchedule> getAllActive() {
        return repository.findAllByStatus(Constants.ACTIVE.name());
    }

    @Override
    public Flux<DistributionSchedule> getAllInactive() {
        return repository.findAllByStatus(Constants.INACTIVE.name());
    }

    @Override
    public Mono<DistributionSchedule> getById(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)));
    }

    @Override
    public Mono<DistributionScheduleResponse> save(DistributionScheduleCreateRequest request) {
        return generateNextScheduleCode()
                .flatMap(generatedCode -> {
                    // Usar el código generado si no se proporciona uno
                    String scheduleCode = (request.getScheduleCode() != null && !request.getScheduleCode().isEmpty())
                            ? request.getScheduleCode()
                            : generatedCode;

                    DistributionSchedule schedule = DistributionSchedule.builder()
                            .organizationId(request.getOrganizationId())
                            .scheduleCode(scheduleCode)
                            .zoneId(request.getZoneId())
                            .streetId(request.getStreetId())
                            .scheduleName(request.getScheduleName())
                            .daysOfWeek(request.getDaysOfWeek())
                            .startTime(request.getStartTime())
                            .endTime(request.getEndTime())
                            .durationHours(request.getDurationHours())
                            .status(Constants.ACTIVE.name())
                            .createdAt(Instant.now())
                            .build();

                    return repository.save(schedule)
                            .map(this::toResponse);
                });
    }

    /**
     * Genera código de horario autoincremental (SCH###).
     * 
     * @return Mono con el código.
     */
    private Mono<String> generateNextScheduleCode() {
        return repository.findAll()
                .map(DistributionSchedule::getScheduleCode)
                .filter(code -> code != null && code.startsWith("SCH"))
                .map(code -> {
                    try {
                        return Integer.parseInt(code.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .reduce(0, Integer::max)
                .map(maxNumber -> String.format("SCH%03d", maxNumber + 1))
                .defaultIfEmpty("SCH001");
    }

    @Override
    public Mono<DistributionSchedule> update(String id, DistributionSchedule schedule) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)))
                .flatMap(existing -> {
                    existing.setOrganizationId(schedule.getOrganizationId());
                    existing.setScheduleCode(schedule.getScheduleCode());
                    existing.setZoneId(schedule.getZoneId());
                    existing.setStreetId(schedule.getStreetId());
                    existing.setScheduleName(schedule.getScheduleName());
                    existing.setDaysOfWeek(schedule.getDaysOfWeek());
                    existing.setStartTime(schedule.getStartTime());
                    existing.setEndTime(schedule.getEndTime());
                    existing.setDurationHours(schedule.getDurationHours());
                    return repository.save(existing);
                });
    }

    @Override
    public Mono<DistributionScheduleResponse> update(String id, DistributionScheduleCreateRequest request) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)))
                .flatMap(existing -> {
                    existing.setOrganizationId(request.getOrganizationId());
                    existing.setScheduleCode(request.getScheduleCode());
                    existing.setZoneId(request.getZoneId());
                    existing.setStreetId(request.getStreetId());
                    existing.setScheduleName(request.getScheduleName());
                    existing.setDaysOfWeek(request.getDaysOfWeek());
                    existing.setStartTime(request.getStartTime());
                    existing.setEndTime(request.getEndTime());
                    existing.setDurationHours(request.getDurationHours());
                    return repository.save(existing);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<Void> delete(String id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<DistributionScheduleResponse> activate(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)))
                .flatMap(schedule -> {
                    schedule.setStatus(Constants.ACTIVE.name());
                    return repository.save(schedule);
                })
                .map(this::toResponse);
    }

    @Override
    public Mono<DistributionScheduleResponse> deactivate(String id) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(CustomException.notFound("DistributionSchedule", id)))
                .flatMap(schedule -> {
                    schedule.setStatus(Constants.INACTIVE.name());
                    return repository.save(schedule);
                })
                .map(this::toResponse);
    }

    /**
     * Mapea entidad a DTO de respuesta.
     * 
     * @param schedule Entidad horario.
     * @return DTO horario.
     */
    private DistributionScheduleResponse toResponse(DistributionSchedule schedule) {
        return DistributionScheduleResponse.builder()
                .id(schedule.getId())
                .organizationId(schedule.getOrganizationId())
                .scheduleCode(schedule.getScheduleCode())
                .zoneId(schedule.getZoneId())
                .streetId(schedule.getStreetId())
                .scheduleName(schedule.getScheduleName())
                .daysOfWeek(schedule.getDaysOfWeek())
                .startTime(schedule.getStartTime())
                .endTime(schedule.getEndTime())
                .durationHours(schedule.getDurationHours())
                .status(schedule.getStatus())
                .createdAt(schedule.getCreatedAt())
                .build();
    }
}
