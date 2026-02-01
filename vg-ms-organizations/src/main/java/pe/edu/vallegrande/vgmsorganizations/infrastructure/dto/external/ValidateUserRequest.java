package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para validar existencia de usuarios en el servicio MS-USERS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidateUserRequest {
    
    private String email;
    
    private String userId;
}