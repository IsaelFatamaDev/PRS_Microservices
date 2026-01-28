package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

// Excepción cuando ya existe una organización con el mismo RUC
public class DuplicateOrganizationException extends RuntimeException {
    public DuplicateOrganizationException(String message) {
        super(message);
    }
}
