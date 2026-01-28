package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

// Excepción cuando no se encuentra una calle
public class StreetNotFoundException extends RuntimeException {
    public StreetNotFoundException(String message) {
        super(message);
    }
}
