package pe.edu.vallegrande.vgmsorganizations.infrastructure.mapper;

import org.springframework.stereotype.Component;
import pe.edu.vallegrande.vgmsorganizations.domain.models.Parameters;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.document.ParameterDocument;

/**
 * Mapper para conversión entre Parameters (Domain) y ParameterDocument (Persistence)
 * Ubicado en infrastructure/mapper según estándar PRS1
 * 
 * Responsabilidad: Conversión entre capa de persistencia (Documents) y dominio
 */
@Component
public class ParameterMapper implements BaseMapper<Parameters, ParameterDocument> {

    @Override
    public Parameters toDomain(ParameterDocument document) {
        if (document == null) {
            return null;
        }
        
        return Parameters.builder()
                .id(document.getId())
                .organizationId(document.getOrganizationId())
                .parameterCode(document.getParameterCode())
                .parameterName(document.getParameterName())
                .parameterValue(document.getParameterValue())
                .parameterDescription(document.getParameterDescription())
                .status(document.getStatus())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }

    @Override
    public ParameterDocument toEntity(Parameters domain) {
        if (domain == null) {
            return null;
        }
        
        ParameterDocument document = ParameterDocument.builder()
                .id(domain.getId())
                .organizationId(domain.getOrganizationId())
                .parameterCode(domain.getParameterCode())
                .parameterName(domain.getParameterName())
                .parameterValue(domain.getParameterValue())
                .parameterDescription(domain.getParameterDescription())
                .status(domain.getStatus())
                .build();
        
        if (domain.getCreatedAt() != null) {
            document.setCreatedAt(domain.getCreatedAt());
        }
        if (domain.getUpdatedAt() != null) {
            document.setUpdatedAt(domain.getUpdatedAt());
        }
        
        return document;
    }
}
