package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdminUserResponse {
    private String userId;

    private String firstName;
    
    private String lastName;
    
    private String documentType;
    
    private String documentNumber;
    
    private String email;
    
    private String phone;
    
    private String address;
    
}
    