package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;

/**
 * Modelo de dominio puro para Parámetros
 * Sin anotaciones de infraestructura (Arquitectura Hexagonal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parameters {
    
    private String id;
    private String organizationId;
    private String parameterCode;
    private String parameterName;
    private String parameterValue;
    private String parameterDescription;
    
    @Builder.Default
    private String status = Constants.ACTIVE;
    
    private Instant createdAt;
    private Instant updatedAt;
}
