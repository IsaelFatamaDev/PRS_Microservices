package pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.CustomException;

/**
 * Excepción lanzada cuando los datos de una organización son inválidos
 */
public class InvalidOrganizationException extends CustomException {
    
    public InvalidOrganizationException(String message) {
        super(new ErrorMessage(
                400,
                "Datos de organización inválidos",
                message
        ));
    }
    
    public InvalidOrganizationException(String field, String reason) {
        super(new ErrorMessage(
                400,
                "Datos de organización inválidos",
                String.format("El campo '%s' es inválido: %s", field, reason)
        ));
    }
}
