package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;

/**
 * Modelo de dominio puro para Organización (Aggregate Root)
 * Sin anotaciones de infraestructura (Arquitectura Hexagonal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Organization {

    private String organizationId;
    private String organizationCode;
    private String organizationName;
    private String legalRepresentative;
    private String address;
    private String phone;
    private String logo;
    
    @Builder.Default
    private String status = Constants.ACTIVE;
    
    private Instant createdAt;
    private Instant updatedAt;
}