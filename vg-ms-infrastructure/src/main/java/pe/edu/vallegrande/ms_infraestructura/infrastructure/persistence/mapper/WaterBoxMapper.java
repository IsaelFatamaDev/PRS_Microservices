package pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.mapper;

import pe.edu.vallegrande.ms_infraestructura.domain.models.WaterBox;
import pe.edu.vallegrande.ms_infraestructura.infrastructure.persistence.entity.WaterBoxEntity;

public class WaterBoxMapper {

    private WaterBoxMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static WaterBox toDomain(WaterBoxEntity entity) {
        if (entity == null) {
            return null;
        }
        return WaterBox.builder()
            .id(entity.getId())
            .organizationId(entity.getOrganizationId())
            .boxCode(entity.getBoxCode())
            .boxType(entity.getBoxType())
            .installationDate(entity.getInstallationDate())
            .currentAssignmentId(entity.getCurrentAssignmentId())
            .status(entity.getStatus())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    public static WaterBoxEntity toEntity(WaterBox domain) {
        if (domain == null) {
            return null;
        }
        return WaterBoxEntity.builder()
            .id(domain.getId())
            .organizationId(domain.getOrganizationId())
            .boxCode(domain.getBoxCode())
            .boxType(domain.getBoxType())
            .installationDate(domain.getInstallationDate())
            .currentAssignmentId(domain.getCurrentAssignmentId())
            .status(domain.getStatus())
            .createdAt(domain.getCreatedAt())
            .build();
    }
}
