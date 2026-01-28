package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.request.CreateInventoryMovementDto;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.InventoryMovementDto;
import pe.edu.vallegrande.vgmsinventorypurchases.domain.models.InventoryMovement;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.entities.InventoryMovementEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
public class InventoryMovementMapper {

    /**
     * Convertir entidad a DTO
     */
    public InventoryMovementDto toDto(InventoryMovementEntity entity) {
        if (entity == null) {
            return null;
        }

        return InventoryMovementDto.builder()
                .movementId(entity.getMovementId())
                .organizationId(entity.getOrganizationId())
                .productId(entity.getProductId())
                .movementType(entity.getMovementType())
                .movementReason(entity.getMovementReason())
                .quantity(entity.getQuantity())
                .unitCost(entity.getUnitCost())
                .referenceDocument(entity.getReferenceDocument())
                .referenceId(entity.getReferenceId())
                .previousStock(entity.getPreviousStock())
                .newStock(entity.getNewStock())
                .movementDate(entity.getMovementDate())
                .userId(entity.getUserId())
                .observations(entity.getObservations())
                .createdAt(entity.getCreatedAt())
                .totalValue(entity.getUnitCost() != null && entity.getQuantity() != null
                        ? entity.getUnitCost().multiply(java.math.BigDecimal.valueOf(entity.getQuantity()))
                        : null)
                .build();
    }

    /**
     * Convertir DTO a entidad (Domain)
     */
    public InventoryMovement toEntity(InventoryMovementDto dto) {
        if (dto == null) {
            return null;
        }

        return InventoryMovement.builder()
                .movementId(dto.getMovementId())
                .organizationId(dto.getOrganizationId())
                .productId(dto.getProductId())
                .movementType(dto.getMovementType())
                .movementReason(dto.getMovementReason())
                .quantity(dto.getQuantity())
                .unitCost(dto.getUnitCost())
                .referenceDocument(dto.getReferenceDocument())
                .referenceId(dto.getReferenceId())
                .previousStock(dto.getPreviousStock())
                .newStock(dto.getNewStock())
                .movementDate(dto.getMovementDate())
                .userId(dto.getUserId())
                .observations(dto.getObservations())
                .createdAt(dto.getCreatedAt())
                .build();
    }

    /**
     * Convertir CreateDTO a entidad (Domain)
     */
    public InventoryMovement toEntity(CreateInventoryMovementDto createDto, Integer currentStock) {
        if (createDto == null) {
            return null;
        }

        Integer newStock = calculateNewStock(
                currentStock,
                createDto.getQuantity(),
                createDto.getMovementType());

        return InventoryMovement.builder()
                .movementId(UUID.randomUUID().toString())
                .organizationId(createDto.getOrganizationId())
                .productId(createDto.getProductId())
                .movementType(createDto.getMovementType())
                .movementReason(createDto.getMovementReason())
                .quantity(createDto.getQuantity())
                .unitCost(createDto.getUnitCost())
                .referenceDocument(createDto.getReferenceDocument())
                .referenceId(createDto.getReferenceId())
                .previousStock(currentStock)
                .newStock(newStock)
                .movementDate(createDto.getMovementDate() != null ? createDto.getMovementDate() : LocalDateTime.now())
                .userId(createDto.getUserId())
                .observations(createDto.getObservations())
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * Convertir Entity (Persistence) a Domain
     */
    public InventoryMovement toDomain(InventoryMovementEntity entity) {
        if (entity == null) {
            return null;
        }
        return InventoryMovement.builder()
                .movementId(entity.getMovementId())
                .organizationId(entity.getOrganizationId())
                .productId(entity.getProductId())
                .movementType(entity.getMovementType())
                .movementReason(entity.getMovementReason())
                .quantity(entity.getQuantity())
                .unitCost(entity.getUnitCost())
                .referenceDocument(entity.getReferenceDocument())
                .referenceId(entity.getReferenceId())
                .previousStock(entity.getPreviousStock())
                .newStock(entity.getNewStock())
                .movementDate(entity.getMovementDate())
                .userId(entity.getUserId())
                .observations(entity.getObservations())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    /**
     * Convertir Domain a Entity (Persistence)
     */
    public InventoryMovementEntity toPersistence(InventoryMovement domain) {
        if (domain == null) {
            return null;
        }
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setMovementId(domain.getMovementId());
        entity.setOrganizationId(domain.getOrganizationId());
        entity.setProductId(domain.getProductId());
        entity.setMovementType(domain.getMovementType());
        entity.setMovementReason(domain.getMovementReason());
        entity.setQuantity(domain.getQuantity());
        entity.setUnitCost(domain.getUnitCost());
        entity.setReferenceDocument(domain.getReferenceDocument());
        entity.setReferenceId(domain.getReferenceId());
        entity.setPreviousStock(domain.getPreviousStock());
        entity.setNewStock(domain.getNewStock());
        entity.setMovementDate(domain.getMovementDate());
        entity.setUserId(domain.getUserId());
        entity.setObservations(domain.getObservations());
        entity.setCreatedAt(domain.getCreatedAt());
        return entity;
    }

    /**
     * Convertir DTO a Entity (Persistence)
     */
    public InventoryMovementEntity toPersistence(InventoryMovementDto dto) {
        if (dto == null) {
            return null;
        }
        InventoryMovementEntity entity = new InventoryMovementEntity();
        entity.setMovementId(dto.getMovementId());
        entity.setOrganizationId(dto.getOrganizationId());
        entity.setProductId(dto.getProductId());
        entity.setMovementType(dto.getMovementType());
        entity.setMovementReason(dto.getMovementReason());
        entity.setQuantity(dto.getQuantity());
        entity.setUnitCost(dto.getUnitCost());
        entity.setReferenceDocument(dto.getReferenceDocument());
        entity.setReferenceId(dto.getReferenceId());
        entity.setPreviousStock(dto.getPreviousStock());
        entity.setNewStock(dto.getNewStock());
        entity.setMovementDate(dto.getMovementDate());
        entity.setUserId(dto.getUserId());
        entity.setObservations(dto.getObservations());
        entity.setCreatedAt(dto.getCreatedAt());
        return entity;
    }

    /**
     * Calcular el nuevo stock basado en el tipo de movimiento
     */
    private Integer calculateNewStock(Integer currentStock, Integer quantity,
            pe.edu.vallegrande.vgmsinventorypurchases.domain.enums.MovementType movementType) {
        if (currentStock == null)
            currentStock = 0;
        if (quantity == null)
            quantity = 0;

        return switch (movementType) {
            case ENTRADA -> currentStock + quantity;
            case SALIDA -> Math.max(0, currentStock - quantity); // No permitir stock negativo
            case AJUSTE -> quantity; // En ajustes, el quantity es el nuevo stock total
        };
    }

    /**
     * Convertir Domain a DTO
     */
    public InventoryMovementDto toDto(InventoryMovement domain) {
        if (domain == null) {
            return null;
        }

        return InventoryMovementDto.builder()
                .movementId(domain.getMovementId())
                .organizationId(domain.getOrganizationId())
                .productId(domain.getProductId())
                .movementType(domain.getMovementType())
                .movementReason(domain.getMovementReason())
                .quantity(domain.getQuantity())
                .unitCost(domain.getUnitCost())
                .referenceDocument(domain.getReferenceDocument())
                .referenceId(domain.getReferenceId())
                .previousStock(domain.getPreviousStock())
                .newStock(domain.getNewStock())
                .movementDate(domain.getMovementDate())
                .userId(domain.getUserId())
                .observations(domain.getObservations())
                .createdAt(domain.getCreatedAt())
                .totalValue(domain.getUnitCost() != null && domain.getQuantity() != null
                        ? domain.getUnitCost().multiply(java.math.BigDecimal.valueOf(domain.getQuantity()))
                        : null)
                .build();
    }
}
