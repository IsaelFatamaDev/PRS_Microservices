package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameters;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.ParameterRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ParameterResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;

/**
 * Mapper entre Parameters (Domain) y DTOs (Request/Response)
 * Ubicado en infrastructure/dto/mapper según estándar PRS1
 * 
 * Responsabilidad: Conversión entre capa de presentación (DTOs) y dominio
 */
@Component
public class ParameterDtoMapper {
    
    /**
     * Convierte de Request DTO a Domain Model
     */
    public Parameters fromRequest(ParameterRequest request) {
        if (request == null) {
            return null;
        }
        
        return Parameters.builder()
                .organizationId(request.getOrganizationId())
                .parameterName(request.getParameterName())
                .parameterValue(request.getParameterValue())
                .parameterDescription(request.getParameterDescription())
                .status(request.getStatus() != null ? request.getStatus() : Constants.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    
    /**
     * Convierte de Domain Model a Response DTO
     */
    public ParameterResponse toResponse(Parameters domain) {
        if (domain == null) {
            return null;
        }
        
        return ParameterResponse.builder()
                .id(domain.getId())
                .organizationId(domain.getOrganizationId())
                .parameterCode(domain.getParameterCode())
                .parameterName(domain.getParameterName())
                .parameterValue(domain.getParameterValue())
                .parameterDescription(domain.getParameterDescription())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }
    
    /**
     * Actualiza un Domain Model existente con datos del Request
     */
    public void updateFromRequest(Parameters domain, ParameterRequest request) {
        if (domain == null || request == null) {
            return;
        }
        
        domain.setParameterName(request.getParameterName());
        domain.setParameterValue(request.getParameterValue());
        domain.setParameterDescription(request.getParameterDescription());
        
        if (request.getStatus() != null) {
            domain.setStatus(request.getStatus());
        }
        
        domain.setUpdatedAt(Instant.now());
    }
}
