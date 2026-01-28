package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

// Excepción cuando no se encuentra una organización
public class OrganizationNotFoundException extends RuntimeException {
    public OrganizationNotFoundException(String message) {
        super(message);
    }
}
