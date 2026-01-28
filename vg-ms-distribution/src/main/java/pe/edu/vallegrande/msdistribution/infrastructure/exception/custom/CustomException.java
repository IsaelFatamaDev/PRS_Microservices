package pe.edu.vallegrande.msdistribution.infrastructure.exception.custom;

import lombok.Getter;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.common.ErrorMessage;

/**
 * Excepción personalizada base para la aplicación.
 * Contiene un objeto ErrorMessage con detalles del error.
 * 
 * @version 1.0
 */
@Getter
public class CustomException extends RuntimeException {

    /** Detalle del error. */
    private final ErrorMessage errorMessage;

    /**
     * Constructor con parámetros individuales.
     * 
     * @param errorCode Código de error.
     * @param message   Mensaje corto.
     * @param details   Detalles adicionales.
     */
    public CustomException(int errorCode, String message, String details) {
        super(message);
        this.errorMessage = new ErrorMessage(errorCode, message, details);
    }

    /**
     * Constructor usando ErrorMessage.
     * 
     * @param errorMessage Objeto de error.
     */
    public CustomException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    /**
     * Crea excepción 404 Not Found.
     * 
     * @param entity Nombre de la entidad.
     * @param id     ID no encontrado.
     * @return CustomException configurada.
     */
    public static CustomException notFound(String entity, String id) {
        return new CustomException(
                404,
                entity + " not found",
                "No se encontró " + entity + " con id: " + id);
    }

    /**
     * Crea excepción 400 Bad Request.
     * 
     * @param message Mensaje.
     * @param details Detalles.
     * @return CustomException configurada.
     */
    public static CustomException badRequest(String message, String details) {
        return new CustomException(
                400,
                message,
                details);
    }

    /**
     * Crea excepción 500 Internal Server Error.
     * 
     * @param message Mensaje.
     * @param details Detalles.
     * @return CustomException configurada.
     */
    public static CustomException internalServerError(String message, String details) {
        return new CustomException(
                500,
                message,
                details);
    }

    /**
     * Crea excepción 409 Conflict.
     * 
     * @param message Mensaje del conflicto.
     * @return CustomException configurada.
     */
    public static CustomException conflict(String message) {
        return new CustomException(
                409,
                "Conflicto",
                message);
    }
}