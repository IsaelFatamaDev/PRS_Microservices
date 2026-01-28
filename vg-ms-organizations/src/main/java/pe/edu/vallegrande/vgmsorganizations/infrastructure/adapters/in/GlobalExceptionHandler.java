package pe.edu.vallegrande.vgmsorganizations.infrastructure.adapters.in;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import pe.edu.vallegrande.vgmsorganizations.application.dto.shared.ErrorMessage;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.DuplicateOrganizationException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.StreetNotFoundException;
import pe.edu.vallegrande.vgmsorganizations.domain.exceptions.ZoneNotFoundException;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OrganizationNotFoundException.class)
    public Mono<ResponseEntity<ErrorMessage>> handleOrganizationNotFound(
            OrganizationNotFoundException ex, ServerWebExchange exchange) {
        log.error("Organization not found: {}", ex.getMessage());
        ErrorMessage error = ErrorMessage.of(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(ZoneNotFoundException.class)
    public Mono<ResponseEntity<ErrorMessage>> handleZoneNotFound(
            ZoneNotFoundException ex, ServerWebExchange exchange) {
        log.error("Zone not found: {}", ex.getMessage());
        ErrorMessage error = ErrorMessage.of(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(StreetNotFoundException.class)
    public Mono<ResponseEntity<ErrorMessage>> handleStreetNotFound(
            StreetNotFoundException ex, ServerWebExchange exchange) {
        log.error("Street not found: {}", ex.getMessage());
        ErrorMessage error = ErrorMessage.of(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }

    @ExceptionHandler(DuplicateOrganizationException.class)
    public Mono<ResponseEntity<ErrorMessage>> handleDuplicateOrganization(
            DuplicateOrganizationException ex, ServerWebExchange exchange) {
        log.error("Duplicate organization: {}", ex.getMessage());
        ErrorMessage error = ErrorMessage.of(
                HttpStatus.CONFLICT.value(),
                ex.getMessage(),
                exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).body(error));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorMessage>> handleValidationErrors(
            WebExchangeBindException ex, ServerWebExchange exchange) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        log.error("Validation error: {}", message);
        ErrorMessage error = ErrorMessage.of(
                HttpStatus.BAD_REQUEST.value(),
                "Errores de validación: " + message,
                exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<ErrorMessage>> handleIllegalArgument(
            IllegalArgumentException ex, ServerWebExchange exchange) {
        log.error("Illegal argument: {}", ex.getMessage());
        ErrorMessage error = ErrorMessage.of(
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessage(),
                exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorMessage>> handleGenericException(
            Exception ex, ServerWebExchange exchange) {
        log.error("Unexpected error: ", ex);
        ErrorMessage error = ErrorMessage.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor: " + ex.getMessage(),
                exchange.getRequest().getPath().value());
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error));
    }
}
