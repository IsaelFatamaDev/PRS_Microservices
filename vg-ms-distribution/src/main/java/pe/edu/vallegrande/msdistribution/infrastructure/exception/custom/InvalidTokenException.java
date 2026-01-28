package pe.edu.vallegrande.msdistribution.infrastructure.exception.custom;

/**
 * Excepción lanzada cuando el token de seguridad es inválido o ha expirado.
 * 
 * @version 1.0
 */
public class InvalidTokenException extends RuntimeException {

    /**
     * Constructor con mensaje.
     * 
     * @param message Detalle del error.
     */
    public InvalidTokenException(String message) {
        super(message);
    }

    /**
     * Constructor con mensaje y causa.
     * 
     * @param message Detalle.
     * @param cause   Causa raíz.
     */
    public InvalidTokenException(String message, Throwable cause) {
        super(message, cause);
    }
}
