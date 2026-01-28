package pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper;

import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBoxTransfer;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxTransferEntity;

public class WaterBoxTransferMapper {

    private WaterBoxTransferMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static WaterBoxTransfer toDomain(WaterBoxTransferEntity entity) {
        if (entity == null) {
            return null;
        }
        return WaterBoxTransfer.builder()
            .id(entity.getId())
            .waterBoxId(entity.getWaterBoxId())
            .oldAssignmentId(entity.getOldAssignmentId())
            .newAssignmentId(entity.getNewAssignmentId())
            .transferReason(entity.getTransferReason())
            .documents(entity.getDocuments())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    public static WaterBoxTransferEntity toEntity(WaterBoxTransfer domain) {
        if (domain == null) {
            return null;
        }
        return WaterBoxTransferEntity.builder()
            .id(domain.getId())
            .waterBoxId(domain.getWaterBoxId())
            .oldAssignmentId(domain.getOldAssignmentId())
            .newAssignmentId(domain.getNewAssignmentId())
            .transferReason(domain.getTransferReason())
            .documents(domain.getDocuments())
            .createdAt(domain.getCreatedAt())
            .build();
    }
}
