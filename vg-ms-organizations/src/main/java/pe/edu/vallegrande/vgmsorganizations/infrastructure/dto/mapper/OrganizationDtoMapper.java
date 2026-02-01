package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Organization;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.OrganizationRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.OrganizationResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;
import java.util.Collections;

/**
 * Mapper entre Organization (Domain) y DTOs (Request/Response)
 * Ubicado en infrastructure/dto/mapper según estándar PRS1
 * 
 * Responsabilidad: Conversión entre capa de presentación (DTOs) y dominio
 */
@Component
public class OrganizationDtoMapper {
    
    /**
     * Convierte de Request DTO a Domain Model
     * @param request DTO de entrada desde la API
     * @return Modelo de dominio
     */
    public Organization fromRequest(OrganizationRequest request) {
        if (request == null) {
            return null;
        }
        
        return Organization.builder()
                .organizationName(request.getOrganizationName())
                .legalRepresentative(request.getLegalRepresentative())
                .address(request.getAddress())
                .phone(request.getPhone())
                .logo(request.getLogo())
                .status(Constants.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    
    /**
     * Convierte de Domain Model a Response DTO
     * @param domain Modelo de dominio
     * @return DTO de respuesta para la API
     */
    public OrganizationResponse toResponse(Organization domain) {
        if (domain == null) {
            return null;
        }
        
        return OrganizationResponse.builder()
                .organizationId(domain.getOrganizationId())
                .organizationCode(domain.getOrganizationCode())
                .organizationName(domain.getOrganizationName())
                .legalRepresentative(domain.getLegalRepresentative())
                .address(domain.getAddress())
                .phone(domain.getPhone())
                .logo(domain.getLogo())
                .status(domain.getStatus())
                .parameters(Collections.emptyList())
                .zones(Collections.emptyList())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Actualiza un Domain Model existente con datos del Request
     * @param domain Modelo de dominio a actualizar
     * @param request DTO con los nuevos datos
     */
    public void updateFromRequest(Organization domain, OrganizationRequest request) {
        if (domain == null || request == null) {
            return;
        }
        
        domain.setOrganizationName(request.getOrganizationName());
        domain.setLegalRepresentative(request.getLegalRepresentative());
        domain.setAddress(request.getAddress());
        domain.setPhone(request.getPhone());
        
        if (request.getLogo() != null && !request.getLogo().trim().isEmpty()) {
            domain.setLogo(request.getLogo());
        }
        
        domain.setUpdatedAt(Instant.now());
    }
}
