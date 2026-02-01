package pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.CustomException;

/**
 * Excepción lanzada cuando se intenta crear una organización duplicada
 */
public class DuplicateOrganizationException extends CustomException {
    
    public DuplicateOrganizationException(String organizationCode) {
        super(new ErrorMessage(
                409,
                "Organización duplicada",
                String.format("Ya existe una organización con el código: %s", organizationCode)
        ));
    }
    
    public DuplicateOrganizationException(String field, String value) {
        super(new ErrorMessage(
                409,
                "Organización duplicada",
                String.format("Ya existe una organización con %s: %s", field, value)
        ));
    }
}
