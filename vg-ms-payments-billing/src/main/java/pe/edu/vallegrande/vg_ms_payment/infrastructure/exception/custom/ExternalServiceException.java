package pe.edu.vallegrande.vg_ms_payment.infrastructure.exception.custom;

/**
 * Excepción para errores en servicios externos
 */
public class ExternalServiceException extends RuntimeException {
    
    private final String serviceName;
    
    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error in external service '%s': %s", serviceName, message));
        this.serviceName = serviceName;
    }
    
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Error in external service '%s': %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}