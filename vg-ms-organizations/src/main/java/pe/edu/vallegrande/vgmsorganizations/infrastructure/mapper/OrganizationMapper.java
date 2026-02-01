package pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.OrganizationDocument;

/**
 * Mapper para conversión entre Organization (Domain) y OrganizationDocument (Persistence)
 * Ubicado en infrastructure/mapper según estándar PRS1
 * 
 * Responsabilidad: Conversión entre capa de persistencia (Documents) y dominio
 * Implementa el patrón de separación de capas de Arquitectura Hexagonal
 */
@Component
public class OrganizationMapper implements BaseMapper<Organization, OrganizationDocument> {

    @Override
    public Organization toDomain(OrganizationDocument document) {
        if (document == null) {
            return null;
        }
        
        return Organization.builder()
                .organizationId(document.getOrganizationId())
                .organizationCode(document.getOrganizationCode())
                .organizationName(document.getOrganizationName())
                .legalRepresentative(document.getLegalRepresentative())
                .address(document.getAddress())
                .phone(document.getPhone())
                .logo(document.getLogo())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    @Override
    public OrganizationDocument toEntity(Organization domain) {
        if (domain == null) {
            return null;
        }
        
        OrganizationDocument document = OrganizationDocument.builder()
                .organizationId(domain.getOrganizationId())
                .organizationCode(domain.getOrganizationCode())
                .organizationName(domain.getOrganizationName())
                .legalRepresentative(domain.getLegalRepresentative())
                .address(domain.getAddress())
                .phone(domain.getPhone())
                .logo(domain.getLogo())
                .status(domain.getStatus())
                .build();
        
        // Establecer timestamps si existen
        if (domain.getCreatedAt() != null) {
            document.setCreatedAt(domain.getCreatedAt());
        }
        if (domain.getUpdatedAt() != null) {
            document.setUpdatedAt(domain.getUpdatedAt());
        }
        
        return document;
    }
}
