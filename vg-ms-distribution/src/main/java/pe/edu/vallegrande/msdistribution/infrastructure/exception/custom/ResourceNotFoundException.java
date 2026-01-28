package pe.edu.vallegrande.msdistribution.infrastructure.exception.custom;

/**
 * Excepción estándar para recursos no encontrados (404).
 * 
 * @version 1.0
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * Constructor con mensaje simple.
     * 
     * @param message Mensaje.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor detallado.
     * 
     * @param resource Nombre del recurso.
     * @param id       ID buscado.
     */
    public ResourceNotFoundException(String resource, String id) {
        super(String.format("%s not found with id: %s", resource, id));
    }

    /**
     * Constructor con causa.
     * 
     * @param message Mensaje.
     * @param cause   Causa raíz.
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
