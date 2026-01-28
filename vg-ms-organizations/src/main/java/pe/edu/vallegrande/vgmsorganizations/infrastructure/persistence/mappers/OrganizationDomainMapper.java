package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.mappers;

import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.OrganizationType;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities.OrganizationDocument;

/**
 * Mapper entre Domain Model y MongoDB Document
 * Responsabilidad: Conversión Domain ↔ Document (Persistence)
 */
public class OrganizationDomainMapper {

    /**
     * Domain → Document (para guardar en MongoDB)
     */
    public static OrganizationDocument toDocument(Organization organization) {
        return OrganizationDocument.builder()
                .id(organization.getId())
                .name(organization.getName())
                .ruc(organization.getRuc())
                .address(organization.getAddress())
                .phone(organization.getPhone())
                .email(organization.getEmail())
                .region(organization.getRegion())
                .province(organization.getProvince())
                .district(organization.getDistrict())
                .legalRepresentative(organization.getLegalRepresentative())
                .type(organization.getType().name())
                .status(organization.getStatus().name())
                .createdAt(organization.getCreatedAt())
                .createdBy(organization.getCreatedBy())
                .updatedAt(organization.getUpdatedAt())
                .updatedBy(organization.getUpdatedBy())
                .build();
    }

    /**
     * Document → Domain (al leer de MongoDB)
     */
    public static Organization toDomain(OrganizationDocument document) {
        return Organization.builder()
                .id(document.getId())
                .name(document.getName())
                .ruc(document.getRuc())
                .address(document.getAddress())
                .phone(document.getPhone())
                .email(document.getEmail())
                .region(document.getRegion())
                .province(document.getProvince())
                .district(document.getDistrict())
                .legalRepresentative(document.getLegalRepresentative())
                .type(OrganizationType.valueOf(document.getType()))
                .status(RecordStatus.valueOf(document.getStatus()))
                .createdAt(document.getCreatedAt())
                .createdBy(document.getCreatedBy())
                .updatedAt(document.getUpdatedAt())
                .updatedBy(document.getUpdatedBy())
                .build();
    }
}
