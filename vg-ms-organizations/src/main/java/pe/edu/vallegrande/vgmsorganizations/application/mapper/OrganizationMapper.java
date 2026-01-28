package pe.edu.vallegrande.vgmsorganizations.application.mapper;

import pe.edu.vallegrande.vgmsorganizations.application.dto.organization.CreateOrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.application.dto.organization.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.application.dto.organization.UpdateOrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.domain.model.Organization;
import pe.edu.vallegrande.vgmsorganizations.domain.model.valueobject.OrganizationType;
import pe.edu.vallegrande.vgmsorganizations.domain.model.valueobject.RecordStatus;

/**
 * Mapper entre DTOs de Application Layer y Domain Models
 * Responsabilidad: Conversión DTO ↔ Domain
 */
public class OrganizationMapper {
    
    /**
     * CreateRequest → Domain Model
     */
    public static Organization toDomain(CreateOrganizationRequest request) {
        return Organization.createNew(
            null, // ID will be generated
            request.getName(),
            request.getRuc(),
            request.getAddress(),
            request.getPhone(),
            request.getEmail(),
            request.getRegion(),
            request.getProvince(),
            request.getDistrict(),
            request.getLegalRepresentative(),
            OrganizationType.valueOf(request.getType().toUpperCase()),
            RecordStatus.ACTIVE,
            request.getCreatedBy()
        );
    }
    
    /**
     * UpdateRequest + existing Domain → Updated Domain
     */
    public static Organization toDomainForUpdate(String id, UpdateOrganizationRequest request, Organization existing) {
        return existing.updateWith(
            request.getName(),
            request.getAddress(),
            request.getPhone(),
            request.getEmail(),
            request.getRegion(),
            request.getProvince(),
            request.getDistrict(),
            request.getLegalRepresentative(),
            OrganizationType.valueOf(request.getType().toUpperCase()),
            RecordStatus.valueOf(request.getStatus().toUpperCase())
        );
    }
    
    /**
     * Domain → Response DTO
     */
    public static OrganizationResponse toResponse(Organization organization) {
        return OrganizationResponse.builder()
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
}
