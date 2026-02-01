package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Street;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.StreetRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.StreetResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;

/**
 * Mapper entre Street (Domain) y DTOs (Request/Response)
 * Ubicado en infrastructure/dto/mapper según estándar PRS1
 * 
 * Responsabilidad: Conversión entre capa de presentación (DTOs) y dominio
 */
@Component
public class StreetDtoMapper {
    
    /**
     * Convierte de Request DTO a Domain Model
     */
    public Street fromRequest(StreetRequest request) {
        if (request == null) {
            return null;
        }
        
        return Street.builder()
                .zoneId(request.getZoneId())
                .streetName(request.getStreetName())
                .streetType(request.getStreetType())
                .status(Constants.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    
    /**
     * Convierte de Domain Model a Response DTO
     */
    public StreetResponse toResponse(Street domain) {
        if (domain == null) {
            return null;
        }
        
        return StreetResponse.builder()
                .streetId(domain.getStreetId())
                .zoneId(domain.getZoneId())
                .streetCode(domain.getStreetCode())
                .streetName(domain.getStreetName())
                .streetType(domain.getStreetType())
                .status(domain.getStatus())
                .createdAt(domain.getCreatedAt())
                .build();
    }
    
    /**
     * Actualiza un Domain Model existente con datos del Request
     */
    public void updateFromRequest(Street domain, StreetRequest request) {
        if (domain == null || request == null) {
            return;
        }
        
        domain.setStreetName(request.getStreetName());
        domain.setStreetType(request.getStreetType());
        domain.setUpdatedAt(Instant.now());
    }
}
