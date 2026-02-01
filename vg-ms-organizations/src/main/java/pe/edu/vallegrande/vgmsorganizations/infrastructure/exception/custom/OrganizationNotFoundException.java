package pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.CustomException;

/**
 * Excepción lanzada cuando no se encuentra una organización
 */
public class OrganizationNotFoundException extends CustomException {
    
    public OrganizationNotFoundException(String organizationId) {
        super(new ErrorMessage(
                404,
                "Organización no encontrada",
                String.format("No se encontró la organización con ID: %s", organizationId)
        ));
    }
    
    public OrganizationNotFoundException(String field, String value) {
        super(new ErrorMessage(
                404,
                "Organización no encontrada",
                String.format("No se encontró la organización con %s: %s", field, value)
        ));
    }
}
