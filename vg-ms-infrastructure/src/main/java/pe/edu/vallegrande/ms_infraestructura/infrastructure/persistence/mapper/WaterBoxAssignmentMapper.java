package pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper;

import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxAssignment;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxAssignmentEntity;

public class WaterBoxAssignmentMapper {

    private WaterBoxAssignmentMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static WaterBoxAssignment toDomain(WaterBoxAssignmentEntity entity) {
        if (entity == null) {
            return null;
        }
        return WaterBoxAssignment.builder()
            .id(entity.getId())
            .waterBoxId(entity.getWaterBoxId())
            .userId(entity.getUserId())
            .startDate(entity.getStartDate())
            .endDate(entity.getEndDate())
            .monthlyFee(entity.getMonthlyFee())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .transferId(entity.getTransferId())
            .build();
    }

    public static WaterBoxAssignmentEntity toEntity(WaterBoxAssignment domain) {
        if (domain == null) {
            return null;
        }
        return WaterBoxAssignmentEntity.builder()
            .id(domain.getId())
            .waterBoxId(domain.getWaterBoxId())
            .userId(domain.getUserId())
            .startDate(domain.getStartDate())
            .endDate(domain.getEndDate())
            .monthlyFee(domain.getMonthlyFee())
            .status(domain.getStatus())
            .createdAt(domain.getCreatedAt())
            .transferId(domain.getTransferId())
            .build();
    }
}
