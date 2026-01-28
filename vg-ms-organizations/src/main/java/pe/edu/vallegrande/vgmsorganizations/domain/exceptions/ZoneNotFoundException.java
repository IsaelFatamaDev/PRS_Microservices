package pe.edu.vallegrande.vgmsorganizations.domain.exceptions;

// Excepción cuando no se encuentra una zona
public class ZoneNotFoundException extends RuntimeException {
    public ZoneNotFoundException(String message) {
        super(message);
    }
}
