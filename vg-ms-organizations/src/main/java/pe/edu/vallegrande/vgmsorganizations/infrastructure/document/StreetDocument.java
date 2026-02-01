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
 * Documento MongoDB para Calle
 * Modelo de persistencia separado del dominio (Arquitectura Hexagonal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "streets")
@CompoundIndex(def = "{'zoneId': 1, 'streetCode': 1}", unique = true)
public class StreetDocument extends BaseDocument {
    
    @Id
    private String streetId;
    
    @Indexed
    private String zoneId;
    
    private String streetCode;
    private String streetName;
    private String streetType;
    
    @Indexed
    @Builder.Default
    private String status = "ACTIVE";
}
