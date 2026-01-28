package pe.edu.vallegrande.msdistribution.infrastructure.exception;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.msdistribution.infrastructure.exception.custom.CustomException;

/**
 * Manejador global de excepciones para toda la aplicación.
 * Intercepta excepciones y retorna respuestas estandarizadas usando ResponseDto
 * y ErrorMessage.
 * 
 * @version 1.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja excepciones personalizadas de negocio.
     * 
     * @param ex Excepción personalizada.
     * @return Respuesta con el código de error específico.
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ResponseDto<Object>> handleCustomException(CustomException ex) {
        log.error("Custom exception occurred: {}", ex.getMessage());
        ErrorMessage error = ex.getErrorMessage();
        // Usamos el mensaje del error como mensaje principal y el objeto ErrorMessage
        // como data
        ResponseDto<Object> response = new ResponseDto<>(false, ex.getMessage(), error);
        return ResponseEntity.status(error.getErrorCode()).body(response);
    }

    /**
     * Maneja excepciones de validación genéricas.
     * 
     * @param ex Excepción de validación.
     * @return Respuesta HTTP 400.
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ResponseDto<Object>> handleValidationException(ValidationException ex) {
        log.error("Validation exception occurred: {}", ex.getMessage());

        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Error de validación",
                "Los datos proporcionados no cumplen con las reglas de validación");
        ResponseDto<Object> response = new ResponseDto<>(false, "Error de validación", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores de binding en WebFlux (datos de formularios/query params).
     * 
     * @param ex Excepción de binding.
     * @return Respuesta HTTP 400.
     */
    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ResponseDto<Object>> handleWebExchangeBindException(WebExchangeBindException ex) {
        log.error("Web exchange bind exception occurred: {}", ex.getMessage());

        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Errores de validación",
                "Los datos proporcionados no cumplen con las reglas de validación");
        ResponseDto<Object> response = new ResponseDto<>(false, "Errores de validación", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja errores de validación en argumentos de métodos (Annotations @Valid).
     * 
     * @param ex Excepción de argumentos inválidos.
     * @return Respuesta HTTP 400.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseDto<Object>> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.error("Method argument validation exception occurred: {}", ex.getMessage());

        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Errores de validación en los argumentos",
                "Los datos proporcionados no cumplen con las reglas de validación");
        ResponseDto<Object> response = new ResponseDto<>(false, "Errores de validación", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja fallos de autenticación (login fallido, token inválido).
     * 
     * @param ex Excepción de autenticación.
     * @return Respuesta HTTP 401.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseDto<Object>> handleAuthenticationException(AuthenticationException ex) {
        log.error("Authentication exception occurred: {}", ex.getMessage());

        ErrorMessage error = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                "Error de autenticación",
                "Credenciales inválidas o token expirado");
        ResponseDto<Object> response = new ResponseDto<>(false, "Error de autenticación", error);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Maneja acceso denegado (falta de roles/permisos).
     * 
     * @param ex Excepción de acceso denegado.
     * @return Respuesta HTTP 403.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseDto<Object>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("Access denied exception occurred: {}", ex.getMessage());

        ErrorMessage error = new ErrorMessage(
                HttpStatus.FORBIDDEN.value(),
                "Acceso denegado",
                "No tiene permisos para acceder a este recurso");
        ResponseDto<Object> response = new ResponseDto<>(false, "Acceso denegado", error);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * Maneja errores en la entrada de datos web (JSON mal formado, tipos
     * incorrectos).
     * 
     * @param ex Excepción de input.
     * @return Respuesta HTTP 400.
     */
    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<ResponseDto<Object>> handleServerWebInputException(ServerWebInputException ex) {
        log.error("Server web input exception occurred: {}", ex.getMessage());

        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Error en los datos de entrada",
                ex.getReason() != null ? ex.getReason() : "Formato de datos inválido");
        ResponseDto<Object> response = new ResponseDto<>(false, "Error en los datos de entrada", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja argumentos ilegales o no permitidos.
     * 
     * @param ex Excepción de argumento ilegal.
     * @return Respuesta HTTP 400.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDto<Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.error("Illegal argument exception occurred: {}", ex.getMessage());

        ErrorMessage error = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Argumento inválido",
                ex.getMessage());
        ResponseDto<Object> response = new ResponseDto<>(false, "Argumento inválido", error);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Manejador por defecto para cualquier otra excepción no controlada.
     * 
     * @param ex Excepción genérica.
     * @return Respuesta HTTP 500 (o 400 si es Runtime).
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDto<Object>> handleGenericException(Exception ex) {
        log.error("Unexpected exception occurred: ", ex);

        int statusCode = (ex instanceof RuntimeException) ? 500 : 400;

        ErrorMessage error = new ErrorMessage(
                statusCode,
                "Error interno del servidor",
                "Ha ocurrido un error inesperado. Por favor, contacte al administrador.");
        ResponseDto<Object> response = new ResponseDto<>(false, "Error interno del servidor", error);
        return ResponseEntity.status(statusCode).body(response);
    }
}