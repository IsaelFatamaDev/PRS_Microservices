package pe.edu.vallegrande.vgmsorganizations.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.vgmsorganizations.domain.models.valueobjects.ParameterType;

import java.time.LocalDateTime;

// Parámetros de configuración del sistema
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Parameter {

     private String id;
     private String organizationId;
     private String paramKey;
     private String paramValue;
     private ParameterType paramType;
     private String description;
     private Boolean isEditable;
     private String category;
     private LocalDateTime lastModified;
     private String updatedBy;
}
