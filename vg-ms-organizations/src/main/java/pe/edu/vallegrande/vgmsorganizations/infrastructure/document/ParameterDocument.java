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
 * Documento MongoDB para Parámetros
 * Modelo de persistencia separado del dominio (Arquitectura Hexagonal)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Document(collection = "parameters")
@CompoundIndex(def = "{'organizationId': 1, 'parameterCode': 1}", unique = true)
public class ParameterDocument extends BaseDocument {
    
    @Id
    private String id;
    
    @Indexed
    private String organizationId;
    
    private String parameterCode;
    private String parameterName;
    private String parameterValue;
    private String parameterDescription;
    
    @Indexed
    @Builder.Default
    private String status = "ACTIVE";
}
