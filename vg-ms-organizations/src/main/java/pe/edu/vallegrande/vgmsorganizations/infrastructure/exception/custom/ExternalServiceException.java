package pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom;

/**
 * Excepción lanzada cuando hay un error al comunicarse con un servicio externo
 */
public class ExternalServiceException extends RuntimeException {
    
    public ExternalServiceException(String message) {
        super(message);
    }
    
    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public ExternalServiceException(String service, String operation, Throwable cause) {
        super(String.format("Error calling %s service for operation: %s", service, operation), cause);
    }
}
