package pe.edu.vallegrande.vgmsusers.infrastructure.persistence.mappers;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities.UserEntity;

// SRP: Responsabilidad única - Conversión Domain ↔ Entity (Capa Persistencia)
@Component
public class UserDomainMapper {

    // Domain → Entity (para guardar en BD)
    public UserEntity toEntity(User user) {
        return UserEntity.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .documentType(user.getDocumentType())
                .documentNumber(user.getDocumentNumber())
                .email(user.getEmail())
                .phone(user.getPhone())
                .address(user.getAddress())
                .role(user.getRole())
                .status(user.getStatus())
                .organizationId(user.getOrganizationId())
                .zoneId(user.getZoneId())
                .streetId(user.getStreetId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .createdBy(user.getCreatedBy())
                .updatedBy(user.getUpdatedBy())
                .build();
    }

    // Entity → Domain (leer de BD)
    public User toDomain(UserEntity entity) {
        return User.builder()
                .userId(entity.getUserId())
                .username(entity.getUsername())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .documentType(entity.getDocumentType())
                .documentNumber(entity.getDocumentNumber())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .role(entity.getRole())
                .status(entity.getStatus())
                .organizationId(entity.getOrganizationId())
                .zoneId(entity.getZoneId())
                .streetId(entity.getStreetId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .createdBy(entity.getCreatedBy())
                .updatedBy(entity.getUpdatedBy())
                .build();
    }
}
