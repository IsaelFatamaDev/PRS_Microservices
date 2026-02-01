package pe.edu.vallegrande.vgmsusers.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsusers.domain.models.User;
import pe.edu.vallegrande.vgmsusers.infrastructure.document.UserDocument;

@Component
public class UserMapper {

    public User toDomain(UserDocument document) {
        if (document == null) {
            return null;
        }
        return User.builder()
                .id(document.getId())
                .userCode(document.getUserCode())
                .username(document.getUsername())
                .organizationId(document.getOrganizationId())
                .personalInfo(document.getPersonalInfo())
                .contact(document.getContact())
                .roles(document.getRoles())
                .status(document.getStatus())
                .registrationDate(document.getRegistrationDate())
                .lastLogin(document.getLastLogin())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .createdBy(document.getCreatedBy())
                .updatedBy(document.getUpdatedBy())
                .deletedAt(document.getDeletedAt())
                .deletedBy(document.getDeletedBy())
                .build();
    }

    public UserDocument toDocument(User domain) {
        if (domain == null) {
            return null;
        }
        return UserDocument.builder()
                .id(domain.getId())
                .userCode(domain.getUserCode())
                .username(domain.getUsername())
                .organizationId(domain.getOrganizationId())
                .personalInfo(domain.getPersonalInfo())
                .contact(domain.getContact())
                .roles(domain.getRoles())
                .status(domain.getStatus())
                .registrationDate(domain.getRegistrationDate())
                .lastLogin(domain.getLastLogin())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .createdBy(domain.getCreatedBy())
                .updatedBy(domain.getUpdatedBy())
                .deletedAt(domain.getDeletedAt())
                .deletedBy(domain.getDeletedBy())
                .build();
    }
}
