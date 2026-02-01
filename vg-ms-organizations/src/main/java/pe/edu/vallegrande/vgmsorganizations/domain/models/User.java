package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

import java.time.Instant;

/**
 * Modelo de dominio puro para Usuario
 * Sin anotaciones de infraestructura (Arquitectura Hexagonal)
 * Nota: Este es solo una referencia, el modelo completo está en MS-Users
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    
    private String userId;
    private String organizationId;
    private String name;
    private String email;
    private String password;
    
    @Builder.Default
    private String status = Constants.ACTIVE;
    
    private Instant createdAt;
    private Instant updatedAt;
}
