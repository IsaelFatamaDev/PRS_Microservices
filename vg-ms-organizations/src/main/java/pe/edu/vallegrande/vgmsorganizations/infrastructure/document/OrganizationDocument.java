package pe.edu.vallegrande.vgmsorganizations.infrastructure.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import pe.edu.vallegrande.vgmsorganizations.domain.enums.Constants;

/**
 * Documento MongoDB para Organización
 * Modelo de persistencia separado del dominio (Arquitectura Hexagonal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "organizations")
public class OrganizationDocument extends BaseDocument {
    
    @Id
    private String organizationId;
    
    @Indexed(unique = true)
    private String organizationCode;
    
    @Indexed
    private String organizationName;
    
    private String legalRepresentative;
    private String address;
    private String phone;
    private String logo;
    
    @Indexed
    @Builder.Default
    private String status = Constants.ACTIVE;
}
