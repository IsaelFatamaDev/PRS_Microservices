package pe.edu.vallegrande.vgmsorganizations.infrastructure.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Documento MongoDB para Zona
 * Modelo de persistencia separado del dominio (Arquitectura Hexagonal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "zones")
@CompoundIndex(def = "{'organizationId': 1, 'zoneCode': 1}", unique = true)
public class ZoneDocument extends BaseDocument {
    
    @Id
    private String zoneId;
    
    @Indexed
    private String organizationId;
    
    private String zoneCode;
    private String zoneName;
    private String description;
    
    @Indexed
    @Builder.Default
    private String status = "ACTIVE";
}
