package pe.edu.vallegrande.vgmsusers.application.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.DocumentType;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
     private UUID userId;
     private String username;
     private String firstName;
     private String lastName;
     private String fullName;
     private DocumentType documentType;
     private String documentNumber;
     private String email;
     private String phone;
     private String address;
     private Role role;
     private RecordStatus status;
     private String organizationId;
     private String zoneId;
     private String streetId;
     private LocalDateTime createdAt;
     private LocalDateTime updatedAt;
}
