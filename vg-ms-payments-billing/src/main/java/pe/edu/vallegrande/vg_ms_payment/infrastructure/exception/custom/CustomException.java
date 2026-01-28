package pe.edu.vallegrande.vg_ms_payment.infrastructure.exception.custom;

import lombok.Getter;
import pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.common.ErrorMessage;

@Getter
public class CustomException extends RuntimeException {
    private final ErrorMessage errorMessage;

    public CustomException(int errorCode, String message, String details) {
        super(message);
        this.errorMessage = new ErrorMessage(errorCode, message, details);
    }

    public CustomException(ErrorMessage errorMessage) {
        super(errorMessage.getMessage());
        this.errorMessage = errorMessage;
    }

    public static CustomException notFound(String entity, String id) {
        return new CustomException(
                404,
                entity + " not found",
                "No se encontró " + entity + " con id: " + id
        );
    }

    public static CustomException badRequest(String message, String details) {
        return new CustomException(
                400,
                message,
                details
        );
    }

    public static CustomException internalServerError(String message, String details) {
        return new CustomException(
                500,
                message,
                details
        );
    }
}