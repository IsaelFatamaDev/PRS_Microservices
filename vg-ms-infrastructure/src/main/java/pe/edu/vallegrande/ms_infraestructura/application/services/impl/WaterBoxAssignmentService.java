package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxAssignmentService;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxAssignment;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxAssignmentRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxAssignmentResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.BadRequestException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.NotFoundException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxAssignmentEntity;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxEntity;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper.WaterBoxAssignmentMapper;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper.WaterBoxMapper;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxAssignmentRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@Service
public class WaterBoxAssignmentService implements IWaterBoxAssignmentService {

    private final WaterBoxAssignmentRepository waterBoxAssignmentRepository;
    private final WaterBoxRepository waterBoxRepository;

    public WaterBoxAssignmentService(WaterBoxAssignmentRepository waterBoxAssignmentRepository,
                                    WaterBoxRepository waterBoxRepository) {
        this.waterBoxAssignmentRepository = waterBoxAssignmentRepository;
        this.waterBoxRepository = waterBoxRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxAssignmentResponse> getAllActive() {
        return Flux.fromIterable(waterBoxAssignmentRepository.findByStatus(Status.ACTIVE))
                .map(WaterBoxAssignmentMapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxAssignmentResponse> getAllInactive() {
        return Flux.fromIterable(waterBoxAssignmentRepository.findByStatus(Status.INACTIVE))
                .map(WaterBoxAssignmentMapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<WaterBoxAssignmentResponse> getById(Long id) {
        return Mono.justOrEmpty(waterBoxAssignmentRepository.findById(id))
                .map(WaterBoxAssignmentMapper::toDomain)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public Mono<WaterBoxAssignmentResponse> save(WaterBoxAssignmentRequest request) {
        return Mono.justOrEmpty(waterBoxRepository.findById(request.getWaterBoxId()))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada.")))
                .map(WaterBoxMapper::toDomain)
                .flatMap(waterBox -> {
                    if (waterBox.getStatus().equals(Status.INACTIVE)) {
                        return Mono.error(new BadRequestException("No se puede asignar a una WaterBox inactiva."));
                    }
                    WaterBoxAssignment assignment = toEntity(request);
                    assignment.setId(null);
                    assignment.setStatus(Status.ACTIVE);
                    assignment.setCreatedAt(LocalDateTime.now());

                    WaterBoxAssignmentEntity entity = WaterBoxAssignmentMapper.toEntity(assignment);
                    return Mono.just(waterBoxAssignmentRepository.saveAndFlush(entity))
                            .map(WaterBoxAssignmentMapper::toDomain)
                            .map(this::toResponse);
                });
    }

    @Override
    @Transactional
    public Mono<Void> delete(Long id) {
        return Mono.justOrEmpty(waterBoxAssignmentRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para eliminación.")))
                .map(WaterBoxAssignmentMapper::toDomain)
                .flatMap(assignment -> {
                    if (assignment.getStatus().equals(Status.INACTIVE)) {
                        return Mono.error(new BadRequestException("WaterBoxAssignment con ID " + id + " ya está inactiva."));
                    }
                    assignment.setStatus(Status.INACTIVE);
                    assignment.setEndDate(LocalDateTime.now());
                    WaterBoxAssignmentEntity updatedEntity = WaterBoxAssignmentMapper.toEntity(assignment);
                    waterBoxAssignmentRepository.save(updatedEntity);
                    return Mono.empty();
                });
    }

    @Override
    @Transactional
    public Mono<WaterBoxAssignmentResponse> update(Long id, WaterBoxAssignmentRequest request) {
        return Mono.justOrEmpty(waterBoxAssignmentRepository.findById(id))
                .switchIfEmpty(Mono.error(new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para actualizar.")))
                .flatMap(existingEntity -> Mono.justOrEmpty(waterBoxRepository.findById(request.getWaterBoxId()))
                        .switchIfEmpty(Mono.error(new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada.")))
                        .map(waterBox -> {
                            WaterBoxAssignment existingAssignment = WaterBoxAssignmentMapper.toDomain(existingEntity);
                            existingAssignment.setWaterBoxId(request.getWaterBoxId());
                            existingAssignment.setUserId(request.getUserId());
                            existingAssignment.setStartDate(request.getStartDate().toLocalDateTime());
                            existingAssignment.setMonthlyFee(request.getMonthlyFee());

                            WaterBoxAssignmentEntity updatedEntity = WaterBoxAssignmentMapper.toEntity(existingAssignment);
                            return waterBoxAssignmentRepository.save(updatedEntity);
                        })
                        .map(WaterBoxAssignmentMapper::toDomain)
                        .map(this::toResponse));
    }

    @Override
    @Transactional
    public Mono<WaterBoxAssignmentResponse> restore(Long id) {
        WaterBoxAssignmentEntity entity = waterBoxAssignmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBoxAssignment con ID " + id + " no encontrada para restauración."));

        WaterBoxAssignment assignment = WaterBoxAssignmentMapper.toDomain(entity);
        if (assignment.getStatus().equals(Status.ACTIVE)) {
            throw new BadRequestException("WaterBoxAssignment con ID " + id + " ya está activa.");
        }

        assignment.setStatus(Status.ACTIVE);
        assignment.setEndDate(null);
        WaterBoxAssignmentEntity restoredEntity = WaterBoxAssignmentMapper.toEntity(assignment);
        WaterBoxAssignmentEntity savedEntity = waterBoxAssignmentRepository.save(restoredEntity);
        WaterBoxAssignment restoredAssignment = WaterBoxAssignmentMapper.toDomain(savedEntity);

        waterBoxRepository.findById(restoredAssignment.getWaterBoxId()).ifPresent(waterBoxEntity -> {
            WaterBox waterBox = WaterBoxMapper.toDomain(waterBoxEntity);
            if (waterBox.getCurrentAssignmentId() == null || waterBoxAssignmentRepository.findById(waterBox.getCurrentAssignmentId())
                    .map(a -> WaterBoxAssignmentMapper.toDomain(a).getStatus().equals(Status.INACTIVE)).orElse(true)) {
                waterBox.setCurrentAssignmentId(restoredAssignment.getId());
                WaterBoxEntity updatedEntity = WaterBoxMapper.toEntity(waterBox);
                waterBoxRepository.save(updatedEntity);
            }
        });

        return Mono.just(toResponse(restoredAssignment));
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<WaterBoxAssignmentResponse> getActiveAssignmentByUserId(String userId) {
        WaterBoxAssignmentResponse response = waterBoxAssignmentRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, Status.ACTIVE)
                .stream()
                .findFirst()
                .map(WaterBoxAssignmentMapper::toDomain)
                .map(this::toResponseWithWaterBoxInfo)
                .orElseThrow(() -> new NotFoundException("No se encontró una asignación activa para el usuario con ID: " + userId));
        return Mono.just(response);
    }

    private WaterBoxAssignment toEntity(WaterBoxAssignmentRequest request) {
        return WaterBoxAssignment.builder()
            .waterBoxId(request.getWaterBoxId())
            .userId(request.getUserId())
            .startDate(request.getStartDate().toLocalDateTime())
            .monthlyFee(request.getMonthlyFee())
            .build();
    }

    private WaterBoxAssignmentResponse toResponse(WaterBoxAssignment assignment) {
        return WaterBoxAssignmentResponse.builder()
                .id(assignment.getId())
                .waterBoxId(assignment.getWaterBoxId())
                .userId(assignment.getUserId())
                .startDate(assignment.getStartDate())
                .endDate(assignment.getEndDate())
                .monthlyFee(assignment.getMonthlyFee())
                .status(assignment.getStatus())
                .createdAt(assignment.getCreatedAt())
                .transferId(assignment.getTransferId())
                .build();
    }

    private WaterBoxAssignmentResponse toResponseWithWaterBoxInfo(WaterBoxAssignment assignment) {
        WaterBoxEntity waterBoxEntity = waterBoxRepository.findById(assignment.getWaterBoxId())
            .orElseThrow(() -> new NotFoundException(
                "WaterBox con ID " + assignment.getWaterBoxId() + " no encontrada."));

        WaterBox waterBox = WaterBoxMapper.toDomain(waterBoxEntity);

        return WaterBoxAssignmentResponse.builder()
            .id(assignment.getId())
            .waterBoxId(assignment.getWaterBoxId())
            .userId(assignment.getUserId())
            .startDate(assignment.getStartDate())
            .endDate(assignment.getEndDate())
            .monthlyFee(assignment.getMonthlyFee())
            .status(assignment.getStatus())
            .createdAt(assignment.getCreatedAt())
            .transferId(assignment.getTransferId())
            .boxCode(waterBox.getBoxCode())
            .boxType(waterBox.getBoxType())
            .build();
    }
}
