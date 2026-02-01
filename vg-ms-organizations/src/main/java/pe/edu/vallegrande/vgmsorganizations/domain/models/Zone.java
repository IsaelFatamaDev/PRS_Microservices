package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;

/**
 * Modelo de dominio puro para Zona
 * Sin anotaciones de infraestructura (Arquitectura Hexagonal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Zone {
    
    private String zoneId;
    private String organizationId;
    private String zoneCode;
    private String zoneName;
    private String description;
    
    @Builder.Default
    private String status = Constants.ACTIVE;
    
    private Instant createdAt;
    private Instant updatedAt;
}
