package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO para crear administradores en el servicio MS-USERS
 * Estructura completa que coincide con la API de MS-USERS
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateAdminRequest {
  
     private String firstName;

     private String lastName;

     private String documentType;

     private String documentNumber;

     private String email;

     private String phone;

     private String address;

     private String organizationId;
     
     // Nuevos campos requeridos por MS-USERS
     private String streetId;
     
     private String zoneId;
     
     private List<String> roles;
}