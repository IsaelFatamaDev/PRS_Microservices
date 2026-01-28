package pe.edu.vallegrande.vgmsusers.infrastructure.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import pe.edu.vallegrande.vgmsusers.application.dto.common.ApiResponse;
import pe.edu.vallegrande.vgmsusers.application.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.DuplicateUserException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.OrganizationNotFoundException;
import pe.edu.vallegrande.vgmsusers.domain.exceptions.UserNotFoundException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

     @ExceptionHandler(UserNotFoundException.class)
     public Mono<ResponseEntity<ApiResponse<Void>>> handleUserNotFoundException(UserNotFoundException ex) {
          return Mono.just(ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, ex.getMessage())));
     }

     @ExceptionHandler(DuplicateUserException.class)
     public Mono<ResponseEntity<ApiResponse<Void>>> handleDuplicateUserException(DuplicateUserException ex) {
          return Mono.just(ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(409, ex.getMessage())));
     }

     @ExceptionHandler(OrganizationNotFoundException.class)
     public Mono<ResponseEntity<ApiResponse<Void>>> handleOrganizationNotFoundException(
               OrganizationNotFoundException ex) {
          return Mono.just(ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(404, ex.getMessage())));
     }

     @ExceptionHandler(WebExchangeBindException.class)
     public Mono<ResponseEntity<ApiResponse<List<ErrorMessage>>>> handleValidationException(
               WebExchangeBindException ex) {
          List<ErrorMessage> errors = ex.getBindingResult()
                    .getFieldErrors()
                    .stream()
                    .map(error -> ErrorMessage.builder()
                              .field(error.getField())
                              .message(error.getDefaultMessage())
                              .build())
                    .collect(Collectors.toList());

          return Mono.just(ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.<List<ErrorMessage>>builder()
                              .status(400)
                              .message("Validation failed")
                              .data(errors)
                              .timestamp(java.time.LocalDateTime.now().toString())
                              .build()));
     }

     @ExceptionHandler(Exception.class)
     public Mono<ResponseEntity<ApiResponse<Void>>> handleGenericException(Exception ex) {
          return Mono.just(ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error(500, "Internal server error: " + ex.getMessage())));
     }
}
