package pe.edu.vallegrande.vgmsusers.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.DocumentType;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.RecordStatus;
import pe.edu.vallegrande.vgmsusers.domain.models.valueobjects.Role;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {
     @Id
     @Column("user_id")
     private UUID userId;

     @Column("username")
     private String username;

     @Column("first_name")
     private String firstName;

     @Column("last_name")
     private String lastName;

     @Column("document_type")
     private DocumentType documentType;

     @Column("document_number")
     private String documentNumber;

     @Column("email")
     private String email;

     @Column("phone")
     private String phone;

     @Column("address")
     private String address;

     @Column("role")
     private Role role;

     @Column("status")
     private RecordStatus status;

     @Column("organization_id")
     private String organizationId;

     @Column("zone_id")
     private String zoneId;

     @Column("street_id")
     private String streetId;

     @Column("created_at")
     private LocalDateTime createdAt;

     @Column("updated_at")
     private LocalDateTime updatedAt;

     @Column("created_by")
     private UUID createdBy;

     @Column("updated_by")
     private UUID updatedBy;
}
