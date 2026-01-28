package pe.edu.vallegrande.vgmsauthentication.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import pe.edu.vallegrande.vgmsauthentication.infrastructure.dto.response.ApiResponse;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * Manejador global de excepciones
 * Centraliza el manejo de errores y retorna respuestas consistentes
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

     /**
      * Maneja errores de validación de campos
      * HTTP 400 Bad Request
      */
     @ExceptionHandler(WebExchangeBindException.class)
     public Mono<ResponseEntity<ApiResponse<Map<String, String>>>> handleValidationExceptions(
               WebExchangeBindException ex) {
          log.warn("Error de validación: {}", ex.getMessage());

          Map<String, String> errors = new HashMap<>();
          ex.getBindingResult().getAllErrors().forEach(error -> {
               String fieldName = ((FieldError) error).getField();
               String errorMessage = error.getDefaultMessage();
               errors.put(fieldName, errorMessage);
          });

          ApiResponse<Map<String, String>> response = ApiResponse.<Map<String, String>>builder()
                    .success(false)
                    .message("Errores de validación en los campos")
                    .data(errors)
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

          return Mono.just(ResponseEntity.badRequest().body(response));
     }

     /**
      * Maneja errores de comunicación con MS-users
      * HTTP 502 Bad Gateway o código original del error
      */
     @ExceptionHandler(WebClientResponseException.class)
     public Mono<ResponseEntity<ApiResponse<Void>>> handleWebClientException(WebClientResponseException ex) {
          log.error("Error comunicándose con servicio externo: {} - Status: {}", ex.getMessage(), ex.getStatusCode());

          HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
          if (status == null) {
               status = HttpStatus.BAD_GATEWAY;
          }

          ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error de comunicación con servicio externo")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

          return Mono.just(ResponseEntity.status(status).body(response));
     }

     /**
      * Maneja IllegalArgumentException
      * HTTP 400 Bad Request
      */
     @ExceptionHandler(IllegalArgumentException.class)
     public Mono<ResponseEntity<ApiResponse<Void>>> handleIllegalArgument(IllegalArgumentException ex) {
          log.warn("Argumento inválido: {}", ex.getMessage());

          ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(false)
                    .message(ex.getMessage())
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

          return Mono.just(ResponseEntity.badRequest().body(response));
     }

     /**
      * Maneja excepciones de seguridad
      * HTTP 401 Unauthorized o 403 Forbidden
      */
     @ExceptionHandler({
               org.springframework.security.access.AccessDeniedException.class,
               org.springframework.security.core.AuthenticationException.class
     })
     public Mono<ResponseEntity<ApiResponse<Void>>> handleSecurityExceptions(Exception ex) {
          log.error("Error de seguridad: {}", ex.getMessage());

          HttpStatus status = ex instanceof org.springframework.security.access.AccessDeniedException
                    ? HttpStatus.FORBIDDEN
                    : HttpStatus.UNAUTHORIZED;

          ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error de autenticación/autorización")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

          return Mono.just(ResponseEntity.status(status).body(response));
     }

     /**
      * Maneja excepciones no controladas
      * HTTP 500 Internal Server Error
      */
     @ExceptionHandler(Exception.class)
     public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericException(Exception ex) {
          log.error("Error no controlado: ", ex);

          ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(false)
                    .message("Error interno del servidor")
                    .timestamp(java.time.LocalDateTime.now())
                    .build();

          return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response));
     }
}
