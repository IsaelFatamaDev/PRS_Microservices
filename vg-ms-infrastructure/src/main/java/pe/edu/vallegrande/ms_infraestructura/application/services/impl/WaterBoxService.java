package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxService;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.BadRequestException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.NotFoundException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxEntity;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper.WaterBoxMapper;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaterBoxService implements IWaterBoxService {

    private final WaterBoxRepository waterBoxRepository;

    public WaterBoxService(WaterBoxRepository waterBoxRepository) {
        this.waterBoxRepository = waterBoxRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxResponse> getAllActive() {
        List<WaterBoxResponse> responses = waterBoxRepository.findByStatus(Status.ACTIVE).stream()
                .map(WaterBoxMapper::toDomain)
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Flux.fromIterable(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxResponse> getAllInactive() {
        List<WaterBoxResponse> responses = waterBoxRepository.findByStatus(Status.INACTIVE).stream()
                .map(WaterBoxMapper::toDomain)
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Flux.fromIterable(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<WaterBoxResponse> getById(Long id) {
        WaterBoxEntity entity = waterBoxRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + id + " no encontrada."));
        WaterBox waterBox = WaterBoxMapper.toDomain(entity);
        return Mono.just(toResponse(waterBox));
    }

    @Override
    @Transactional
    public Mono<WaterBoxResponse> save(WaterBoxRequest request) {
        WaterBox waterBox = toEntity(request);
        waterBox.setStatus(request.getStatus() != null ? request.getStatus() : Status.ACTIVE);
        waterBox.setCreatedAt(LocalDateTime.now());
        
        WaterBoxEntity entity = WaterBoxMapper.toEntity(waterBox);
        WaterBoxEntity savedEntity = waterBoxRepository.save(entity);
        WaterBox savedWaterBox = WaterBoxMapper.toDomain(savedEntity);
        return Mono.just(toResponse(savedWaterBox));
    }

    @Override
    @Transactional
    public Mono<WaterBoxResponse> update(Long id, WaterBoxRequest request) {
        WaterBoxEntity existingEntity = waterBoxRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + id + " no encontrada para actualizar."));

        WaterBox existingWaterBox = WaterBoxMapper.toDomain(existingEntity);
        existingWaterBox.setOrganizationId(request.getOrganizationId());
        existingWaterBox.setBoxCode(request.getBoxCode());
        existingWaterBox.setBoxType(request.getBoxType());
        existingWaterBox.setInstallationDate(request.getInstallationDate());

        WaterBoxEntity updatedEntity = WaterBoxMapper.toEntity(existingWaterBox);
        WaterBoxEntity savedEntity = waterBoxRepository.save(updatedEntity);
        WaterBox updatedWaterBox = WaterBoxMapper.toDomain(savedEntity);
        return Mono.just(toResponse(updatedWaterBox));
    }

    @Override
    @Transactional
    public Mono<Void> delete(Long id) {
        WaterBoxEntity entity = waterBoxRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + id + " no encontrada para eliminación."));

        WaterBox waterBox = WaterBoxMapper.toDomain(entity);
        
        if (waterBox.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("WaterBox con ID " + id + " ya está inactiva.");
        }

        if (waterBox.getCurrentAssignmentId() != null) {
            throw new BadRequestException(
                    "WaterBox con ID " + id + " tiene una asignación activa. Desactive la asignación primero.");
        }

        waterBox.setStatus(Status.INACTIVE);
        WaterBoxEntity updatedEntity = WaterBoxMapper.toEntity(waterBox);
        waterBoxRepository.save(updatedEntity);
        return Mono.empty();
    }

    @Override
    @Transactional
    public Mono<WaterBoxResponse> restore(Long id) {
        WaterBoxEntity entity = waterBoxRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + id + " no encontrada para restauración."));

        WaterBox waterBox = WaterBoxMapper.toDomain(entity);
        
        if (waterBox.getStatus().equals(Status.ACTIVE)) {
            throw new BadRequestException("WaterBox con ID " + id + " ya está activa.");
        }

        waterBox.setStatus(Status.ACTIVE);
        WaterBoxEntity restoredEntity = WaterBoxMapper.toEntity(waterBox);
        WaterBoxEntity savedEntity = waterBoxRepository.save(restoredEntity);
        WaterBox restoredWaterBox = WaterBoxMapper.toDomain(savedEntity);
        return Mono.just(toResponse(restoredWaterBox));
    }

    private WaterBox toEntity(WaterBoxRequest request) {
        return WaterBox.builder()
                .organizationId(request.getOrganizationId())
                .boxCode(request.getBoxCode())
                .boxType(request.getBoxType())
                .installationDate(request.getInstallationDate())
                .currentAssignmentId(request.getCurrentAssignmentId())
                .build();
    }

    private WaterBoxResponse toResponse(WaterBox waterBox) {
        return WaterBoxResponse.builder()
                .id(waterBox.getId())
                .organizationId(waterBox.getOrganizationId())
                .boxCode(waterBox.getBoxCode())
                .boxType(waterBox.getBoxType())
                .installationDate(waterBox.getInstallationDate())
                .currentAssignmentId(waterBox.getCurrentAssignmentId())
                .status(waterBox.getStatus())
                .createdAt(waterBox.getCreatedAt())
                .build();
    }
}
