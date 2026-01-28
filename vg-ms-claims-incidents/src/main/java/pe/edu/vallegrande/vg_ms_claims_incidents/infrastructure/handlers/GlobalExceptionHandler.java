package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.handlers;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.common.ResponseDto;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.exception.custom.DatosInvalidosException;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.exception.custom.ErrorServidorException;
import pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.exception.custom.RecursoNoEncontradoException;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Manejador global de excepciones para el microservicio
 * Proporciona respuestas estandarizadas y códigos HTTP apropiados
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Extrae la ruta de la petición del exchange
     */
    private String getRequestPath(ServerWebExchange exchange) {
        return exchange != null ? exchange.getRequest().getPath().value() : "/unknown";
    }

    /**
     * Maneja RecursoNoEncontradoException - 404 Not Found
     */
    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Mono<ResponseEntity<ResponseDto<Object>>> handleRecursoNoEncontrado(
            RecursoNoEncontradoException ex, 
            ServerWebExchange exchange) {
        
        log.warn("Recurso no encontrado: {} - Path: {}", ex.getMessage(), getRequestPath(exchange));
        
        ResponseDto<Object> response = ResponseDto.<Object>builder()
                .success(false)
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message(ex.getMessage())
                .path(getRequestPath(exchange))
                .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(response));
    }

    /**
     * Maneja DatosInvalidosException - 400 Bad Request
     */
    @ExceptionHandler(DatosInvalidosException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<ResponseDto<Object>>> handleDatosInvalidos(
            DatosInvalidosException ex, 
            ServerWebExchange exchange) {
        
        log.warn("Datos inválidos: {} - Path: {}", ex.getMessage(), getRequestPath(exchange));
        
        ResponseDto<Object> response = ResponseDto.<Object>builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Datos inválidos")
                .errors(ex.getMessage())
                .path(getRequestPath(exchange))
                .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    /**
     * Maneja ErrorServidorException - 500 Internal Server Error
     */
    @ExceptionHandler(ErrorServidorException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseEntity<ResponseDto<Object>>> handleErrorServidor(
            ErrorServidorException ex, 
            ServerWebExchange exchange) {
        
        log.error("Error interno del servidor: {} - Path: {}", ex.getMessage(), getRequestPath(exchange), ex);
        
        ResponseDto<Object> response = ResponseDto.<Object>builder()
                .success(false)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Error interno del servidor")
                .errors(ex.getMessage())
                .path(getRequestPath(exchange))
                .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }

    /**
     * Maneja WebExchangeBindException - Errores de validación de Bean Validation
     * 400 Bad Request
     */
    @ExceptionHandler(WebExchangeBindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<ResponseDto<Object>>> handleValidationExceptions(
            WebExchangeBindException ex, 
            ServerWebExchange exchange) {
        
        log.warn("Error de validación - Path: {}", getRequestPath(exchange));
        
        Map<String, String> validationErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                    FieldError::getField,
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Error de validación",
                    (existing, replacement) -> existing
                ));

        ResponseDto<Object> response = ResponseDto.<Object>builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Error de validación en los datos proporcionados")
                .errors(validationErrors)
                .path(getRequestPath(exchange))
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    /**
     * Maneja HttpMessageNotReadableException - JSON mal formado
     * 400 Bad Request
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<ResponseDto<Object>>> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, 
            ServerWebExchange exchange) {
        
        log.warn("Error al leer mensaje HTTP - Path: {} - Error: {}", 
                 getRequestPath(exchange), ex.getMessage());

        String errorMessage = "El formato del JSON enviado no es válido";
        if (ex.getCause() != null) {
            errorMessage += ": " + ex.getCause().getMessage();
        }

        ResponseDto<Object> response = ResponseDto.<Object>builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Error de formato en el cuerpo de la petición")
                .errors(errorMessage)
                .path(getRequestPath(exchange))
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    /**
     * Maneja IllegalArgumentException - Argumentos inválidos
     * 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ResponseEntity<ResponseDto<Object>>> handleIllegalArgument(
            IllegalArgumentException ex, 
            ServerWebExchange exchange) {
        
        log.warn("Argumento ilegal: {} - Path: {}", ex.getMessage(), getRequestPath(exchange));
        
        ResponseDto<Object> response = ResponseDto.<Object>builder()
                .success(false)
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Argumento inválido")
                .errors(ex.getMessage())
                .path(getRequestPath(exchange))
                .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response));
    }

    /**
     * Maneja IllegalStateException - Estado inválido
     * 409 Conflict
     */
    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public Mono<ResponseEntity<ResponseDto<Object>>> handleIllegalState(
            IllegalStateException ex, 
            ServerWebExchange exchange) {
        
        log.warn("Estado ilegal: {} - Path: {}", ex.getMessage(), getRequestPath(exchange));
        
        ResponseDto<Object> response = ResponseDto.<Object>builder()
                .success(false)
                .statusCode(HttpStatus.CONFLICT.value())
                .message("Conflicto en el estado de la operación")
                .errors(ex.getMessage())
                .path(getRequestPath(exchange))
                .build();
        
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(response));
    }

    /**
     * Maneja excepciones genéricas no controladas
     * 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ResponseEntity<ResponseDto<Object>>> handleGeneralException(
            Exception ex, 
            ServerWebExchange exchange) {
        
        log.error("Error inesperado: {} - Path: {}", ex.getMessage(), getRequestPath(exchange), ex);
        
        ResponseDto<Object> response = ResponseDto.<Object>builder()
                .success(false)
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("Ha ocurrido un error inesperado en el servidor")
                .errors(ex.getMessage())
                .path(getRequestPath(exchange))
                .build();

        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
    }
}