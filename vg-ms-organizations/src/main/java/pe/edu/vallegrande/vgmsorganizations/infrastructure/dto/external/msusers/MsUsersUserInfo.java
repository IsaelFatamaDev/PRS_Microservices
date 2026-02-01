package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external.msusers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para la información de usuario dentro de UserCreationResponse de MS-USERS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MsUsersUserInfo {
    private String id; // Renombrado de userId a id para coincidir con el JSON de MS-USERS
    private String userCode;
    private String username;
    private String firstName;
    private String lastName;
    private String email;
    private String name; // Añadido para mapear el nombre completo si viene
    private String status; // Añadido para mapear el estado del usuario
    
    // Campos adicionales que vienen del servicio MS-USERS
    private String documentType;
    private String documentNumber;
    private String phone;
    private String address;
    private String role;
    private String organizationId;
}