package pe.edu.vallegrande.vgmsusers.infrastructure.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.edu.vallegrande.vgmsusers.domain.enums.RolesUsers;
import pe.edu.vallegrande.vgmsusers.domain.enums.UserStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.Contact;
import pe.edu.vallegrande.vgmsusers.domain.models.PersonalInfo;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users_demo")
@Builder
public class UserDocument {

    @Id
    private String id;
    private String userCode;
    private String username;
    private String organizationId;
    private PersonalInfo personalInfo;
    private Contact contact;
    private Set<RolesUsers> roles;
    private UserStatus status;
    private LocalDateTime registrationDate;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime deletedAt;
    private String deletedBy;
}
