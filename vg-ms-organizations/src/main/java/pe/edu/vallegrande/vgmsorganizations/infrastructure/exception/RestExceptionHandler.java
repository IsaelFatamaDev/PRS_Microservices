package pe.edu.vallegrande.vgmsorganizations.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ValidationError;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom.ExternalServiceException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom.InvalidTokenException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom.ResourceNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom.ValidationException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Manejador de excepciones REST específico
 */
@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ResponseDto<Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.error("Resource not found: {}", ex.getMessage());
        
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.NOT_FOUND.value(),
                "Resource not found",
                ex.getMessage()
        );
        
        ResponseDto<Object> response = new ResponseDto<>(false, errorMessage);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(ExternalServiceException.class)
    public ResponseEntity<ResponseDto<Object>> handleExternalService(ExternalServiceException ex) {
        log.error("External service error: {}", ex.getMessage());
        
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                "External service error",
                ex.getMessage()
        );
        
        ResponseDto<Object> response = new ResponseDto<>(false, errorMessage);
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ResponseDto<Object>> handleInvalidToken(InvalidTokenException ex) {
        log.error("Invalid token: {}", ex.getMessage());
        
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.UNAUTHORIZED.value(),
                "Invalid token",
                ex.getMessage()
        );
        
        ResponseDto<Object> response = new ResponseDto<>(false, errorMessage);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ResponseDto<Object>> handleValidation(ValidationException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                ex.getMessage()
        );
        
        ResponseDto<Object> response = new ResponseDto<>(false, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ResponseDto<Object>> handleWebExchangeBindException(WebExchangeBindException ex) {
        log.error("Validation error: {}", ex.getMessage());
        
        List<ValidationError> validationErrors = ex.getFieldErrors().stream()
                .map(error -> ValidationError.builder()
                        .field(error.getField())
                        .message(error.getDefaultMessage())
                        .rejectedValue(error.getRejectedValue())
                        .build())
                .collect(Collectors.toList());
        
        ErrorMessage errorMessage = new ErrorMessage(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                "Invalid request data"
        );
        
        ResponseDto<Object> response = new ResponseDto<>(false, errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
