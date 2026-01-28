package pe.edu.vallegrande.msdistribution.infrastructure.exception.custom;

/**
 * Excepción para errores al llamar servicios externos.
 * 
 * @version 1.0
 */
public class ExternalServiceException extends RuntimeException {

    /** Nombre del servicio externo fallido. */
    private final String serviceName;

    /**
     * Constructor básico.
     * 
     * @param serviceName Nombre del servicio.
     * @param message     Mensaje de error.
     */
    public ExternalServiceException(String serviceName, String message) {
        super(String.format("Error calling external service '%s': %s", serviceName, message));
        this.serviceName = serviceName;
    }

    /**
     * Constructor con causa raíz.
     * 
     * @param serviceName Nombre del servicio.
     * @param message     Mensaje.
     * @param cause       Excepción original.
     */
    public ExternalServiceException(String serviceName, String message, Throwable cause) {
        super(String.format("Error calling external service '%s': %s", serviceName, message), cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
