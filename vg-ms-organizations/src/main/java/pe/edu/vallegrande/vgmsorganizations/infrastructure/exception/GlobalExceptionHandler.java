package pe.edu.vallegrande.vgmsorganizations.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.CustomException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        // Extrae el mensaje de error personalizado
        ErrorMessage errorMessage = ex.getErrorMessage();

        // Retorna la respuesta con el código de error
        return ResponseEntity.status(errorMessage.getErrorCode())
                .body(ApiResponse.error(errorMessage.getMessage(), errorMessage));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access Denied: {}", ex.getMessage());

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                "No tienes permisos para acceder a este recurso. Verifica que tu token tenga los roles necesarios.");

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("Acceso denegado", errorMessage));
    }

    @ExceptionHandler(org.springframework.web.bind.support.WebExchangeBindException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(
            org.springframework.web.bind.support.WebExchangeBindException ex) {
        log.warn("Validation Error: {}", ex.getMessage());

        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .reduce("", (acc, curr) -> acc + (acc.isEmpty() ? "" : "; ") + curr);

        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                details);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Error de validación", errorMessage));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        log.error("Error no manejado: ", ex);

        int statusCode = (ex instanceof RuntimeException) ? 500 : 400;

        // Crea un mensaje de error genérico
        ErrorMessage errorMessage = new ErrorMessage(
                statusCode,
                "Error interno del servidor",
                ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());

        // Retorna la respuesta
        return ResponseEntity.status(statusCode)
                .body(ApiResponse.error("Error interno del servidor", errorMessage));
    }
}
