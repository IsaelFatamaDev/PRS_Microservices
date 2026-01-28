package pe.edu.vallegrande.ms_infraestructura.application.services.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.vallegrande.ms_infraestructura.application.services.IWaterBoxTransferService;
import pe.edu.vallegrande.ms_infraestructura.domain.enums.Status;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxAssignment;
import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxTransfer;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.request.WaterBoxTransferRequest;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.dto.response.WaterBoxTransferResponse;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.BadRequestException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.exceptions.NotFoundException;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxAssignmentEntity;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxEntity;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxTransferEntity;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper.WaterBoxAssignmentMapper;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper.WaterBoxMapper;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper.WaterBoxTransferMapper;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxAssignmentRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxRepository;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.repository.WaterBoxTransferRepository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class WaterBoxTransferService implements IWaterBoxTransferService {

    private final WaterBoxTransferRepository waterBoxTransferRepository;
    private final WaterBoxAssignmentRepository waterBoxAssignmentRepository;
    private final WaterBoxRepository waterBoxRepository;

    public WaterBoxTransferService(WaterBoxTransferRepository waterBoxTransferRepository,
                                  WaterBoxAssignmentRepository waterBoxAssignmentRepository,
                                  WaterBoxRepository waterBoxRepository) {
        this.waterBoxTransferRepository = waterBoxTransferRepository;
        this.waterBoxAssignmentRepository = waterBoxAssignmentRepository;
        this.waterBoxRepository = waterBoxRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Flux<WaterBoxTransferResponse> getAll() {
        List<WaterBoxTransferResponse> responses = waterBoxTransferRepository.findAll().stream()
                .map(WaterBoxTransferMapper::toDomain)
                .map(this::toResponse)
                .collect(Collectors.toList());
        return Flux.fromIterable(responses);
    }

    @Override
    @Transactional(readOnly = true)
    public Mono<WaterBoxTransferResponse> getById(Long id) {
        WaterBoxTransferEntity entity = waterBoxTransferRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("WaterBoxTransfer con ID " + id + " no encontrada."));
        WaterBoxTransfer transfer = WaterBoxTransferMapper.toDomain(entity);
        return Mono.just(toResponse(transfer));
    }

    @Override
    @Transactional
    public Mono<WaterBoxTransferResponse> save(WaterBoxTransferRequest request) {
        WaterBoxEntity waterBoxEntity = waterBoxRepository.findById(request.getWaterBoxId())
                .orElseThrow(() -> new NotFoundException("WaterBox con ID " + request.getWaterBoxId() + " no encontrada."));

        WaterBox waterBox = WaterBoxMapper.toDomain(waterBoxEntity);
        if (waterBox.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("No se puede transferir una WaterBox inactiva.");
        }

        WaterBoxAssignmentEntity oldAssignmentEntity = waterBoxAssignmentRepository.findById(request.getOldAssignmentId())
                .orElseThrow(() -> new NotFoundException("Asignación antigua con ID " + request.getOldAssignmentId() + " no encontrada."));

        WaterBoxAssignment oldAssignment = WaterBoxAssignmentMapper.toDomain(oldAssignmentEntity);
        if (!oldAssignment.getWaterBoxId().equals(waterBox.getId())) {
            throw new BadRequestException("La asignación antigua no pertenece a la WaterBox especificada.");
        }
        if (oldAssignment.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("La asignación antigua ya está inactiva. No se puede iniciar transferencia desde una asignación inactiva.");
        }
        if (waterBox.getCurrentAssignmentId() == null || !waterBox.getCurrentAssignmentId().equals(oldAssignment.getId())) {
            throw new BadRequestException("La asignación antigua proporcionada no es la asignación actual activa para esta WaterBox.");
        }

        WaterBoxAssignmentEntity newAssignmentEntity = waterBoxAssignmentRepository.findById(request.getNewAssignmentId())
                .orElseThrow(() -> new NotFoundException("Nueva asignación con ID " + request.getNewAssignmentId() + " no encontrada."));

        WaterBoxAssignment newAssignment = WaterBoxAssignmentMapper.toDomain(newAssignmentEntity);
        if (!newAssignment.getWaterBoxId().equals(waterBox.getId())) {
            throw new BadRequestException("La nueva asignación no pertenece a la WaterBox especificada.");
        }
        if (newAssignment.getStatus().equals(Status.INACTIVE)) {
            throw new BadRequestException("La nueva asignación está inactiva. Debería estar activa para una nueva transferencia.");
        }
        if (newAssignment.getId().equals(oldAssignment.getId())) {
            throw new BadRequestException("La asignación antigua y la nueva no pueden ser la misma.");
        }

        WaterBoxTransfer transfer = toEntity(request);
        transfer.setCreatedAt(LocalDateTime.now());
        WaterBoxTransferEntity transferEntity = WaterBoxTransferMapper.toEntity(transfer);
        WaterBoxTransferEntity savedTransferEntity = waterBoxTransferRepository.save(transferEntity);
        WaterBoxTransfer savedTransfer = WaterBoxTransferMapper.toDomain(savedTransferEntity);

        oldAssignment.setStatus(Status.INACTIVE);
        oldAssignment.setEndDate(LocalDateTime.now());
        oldAssignment.setTransferId(savedTransfer.getId());
        WaterBoxAssignmentEntity updatedOldEntity = WaterBoxAssignmentMapper.toEntity(oldAssignment);
        waterBoxAssignmentRepository.save(updatedOldEntity);

        waterBox.setCurrentAssignmentId(newAssignment.getId());
        WaterBoxEntity updatedWaterBoxEntity = WaterBoxMapper.toEntity(waterBox);
        waterBoxRepository.save(updatedWaterBoxEntity);

        return Mono.just(toResponse(savedTransfer));
    }

    private WaterBoxTransfer toEntity(WaterBoxTransferRequest request) {
        return WaterBoxTransfer.builder()
                .waterBoxId(request.getWaterBoxId())
                .oldAssignmentId(request.getOldAssignmentId())
                .newAssignmentId(request.getNewAssignmentId())
                .transferReason(request.getTransferReason())
                .documents(request.getDocuments())
                .build();
    }

    private WaterBoxTransferResponse toResponse(WaterBoxTransfer transfer) {
        return WaterBoxTransferResponse.builder()
                .id(transfer.getId())
                .waterBoxId(transfer.getWaterBoxId())
                .oldAssignmentId(transfer.getOldAssignmentId())
                .newAssignmentId(transfer.getNewAssignmentId())
                .transferReason(transfer.getTransferReason())
                .documents(transfer.getDocuments())
                .createdAt(transfer.getCreatedAt())
                .build();
    }
}
