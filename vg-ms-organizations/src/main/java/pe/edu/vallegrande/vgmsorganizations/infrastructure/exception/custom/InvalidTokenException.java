package pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom;

/**
 * Excepción lanzada cuando un token JWE es inválido
 */
public class InvalidTokenException extends RuntimeException {
    
    public InvalidTokenException(String message) {
        super(message);
    }
    
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
