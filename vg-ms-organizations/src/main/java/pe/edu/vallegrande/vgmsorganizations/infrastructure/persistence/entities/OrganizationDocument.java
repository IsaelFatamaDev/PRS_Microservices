package pe.edu.vallegrande.vgmsorganizations.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "organizations")
public class OrganizationDocument {

    @Id
    private String id;

    private String name;

    @Indexed(unique = true)
    private String ruc;

    private String address;
    private String phone;
    private String email;
    private String region;
    private String province;
    private String district;
    private String legalRepresentative;
    private String type; // JASS, MUNICIPAL, COOPERATIVE, PRIVATE
    private String status; // ACTIVE, INACTIVE, SUSPENDED
    private LocalDateTime createdAt;
    private String createdBy;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
