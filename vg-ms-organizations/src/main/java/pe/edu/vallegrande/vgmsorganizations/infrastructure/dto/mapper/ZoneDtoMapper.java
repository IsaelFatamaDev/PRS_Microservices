package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Zone;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.request.ZoneRequest;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response.ZoneResponse;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;
import java.util.Collections;

/**
 * Mapper entre Zone (Domain) y DTOs (Request/Response)
 * Ubicado en infrastructure/dto/mapper según estándar PRS1
 * 
 * Responsabilidad: Conversión entre capa de presentación (DTOs) y dominio
 */
@Component
public class ZoneDtoMapper {
    
    /**
     * Convierte de Request DTO a Domain Model
     */
    public Zone fromRequest(ZoneRequest request) {
        if (request == null) {
            return null;
        }
        
        return Zone.builder()
                .organizationId(request.getOrganizationId())
                .zoneName(request.getZoneName())
                .description(request.getDescription())
                .status(Constants.ACTIVE)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }
    
    /**
     * Convierte de Domain Model a Response DTO
     */
    public ZoneResponse toResponse(Zone domain) {
        if (domain == null) {
            return null;
        }
        
        return ZoneResponse.builder()
                .zoneId(domain.getZoneId())
                .organizationId(domain.getOrganizationId())
                .zoneCode(domain.getZoneCode())
                .zoneName(domain.getZoneName())
                .description(domain.getDescription())
                .status(domain.getStatus())
                .streets(Collections.emptyList())
                .build();
    }
    
    /**
     * Actualiza un Domain Model existente con datos del Request
     */
    public void updateFromRequest(Zone domain, ZoneRequest request) {
        if (domain == null || request == null) {
            return;
        }
        
        domain.setZoneName(request.getZoneName());
        domain.setDescription(request.getDescription());
        domain.setUpdatedAt(Instant.now());
    }
}
