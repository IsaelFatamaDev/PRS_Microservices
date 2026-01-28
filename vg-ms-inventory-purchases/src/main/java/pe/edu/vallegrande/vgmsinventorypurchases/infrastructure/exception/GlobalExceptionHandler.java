package pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.ErrorMessage;
import pe.edu.vallegrande.vgmsinventorypurchases.infrastructure.dto.common.ResponseDto;
import reactor.core.publisher.Mono;

import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(CustomException.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleCustomException(CustomException ex) {
                log.error("Custom exception: {}", ex.getMessage(), ex);

                ErrorMessage errorMessage = new ErrorMessage(
                                ex.getMessage(),
                                ex.getErrorCode(),
                                ex.getHttpStatus());

                return Mono.just(ResponseEntity
                                .status(ex.getHttpStatus())
                                .body(new ResponseDto<>(false, errorMessage)));
        }

        @ExceptionHandler(WebExchangeBindException.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleValidationException(WebExchangeBindException ex) {
                log.error("Validation exception: {}", ex.getMessage(), ex);

                String details = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(FieldError::getDefaultMessage)
                                .collect(Collectors.joining(", "));

                ErrorMessage errorMessage = new ErrorMessage(
                                "Errores de validación: " + details,
                                "VALIDATION_ERROR",
                                HttpStatus.BAD_REQUEST.value());

                return Mono.just(ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ResponseDto<>(false, errorMessage)));
        }

        @ExceptionHandler(IllegalArgumentException.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleIllegalArgumentException(IllegalArgumentException ex) {
                log.error("Illegal argument exception: {}", ex.getMessage(), ex);

                ErrorMessage errorMessage = new ErrorMessage(
                                ex.getMessage(),
                                "INVALID_ARGUMENT",
                                HttpStatus.BAD_REQUEST.value());

                return Mono.just(ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(new ResponseDto<>(false, errorMessage)));
        }

        @ExceptionHandler(WebClientResponseException.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleWebClientException(WebClientResponseException ex) {
                log.error("Error al comunicarse con microservicio externo: {} - {}", ex.getStatusCode(),
                                ex.getResponseBodyAsString());

                String message = "Error de comunicación con servicio externo";
                String errorCode = "EXTERNAL_SERVICE_ERROR";

                if (ex.getStatusCode().is4xxClientError()) {
                        message = "No se pudo obtener información del usuario o la organización";
                        errorCode = "USER_INFO_NOT_FOUND";
                } else if (ex.getStatusCode().is5xxServerError()) {
                        message = "Servicio externo temporalmente no disponible";
                        errorCode = "EXTERNAL_SERVICE_UNAVAILABLE";
                }

                ErrorMessage errorMessage = new ErrorMessage(
                                message,
                                errorCode,
                                HttpStatus.INTERNAL_SERVER_ERROR.value());

                return Mono.just(ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ResponseDto<>(false, errorMessage)));
        }

        @ExceptionHandler(UserOrganizationMismatchException.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleUserOrganizationMismatch(
                        UserOrganizationMismatchException ex) {
                log.warn("Usuario no autorizado para organización: {}", ex.getMessage());

                ErrorMessage errorMessage = new ErrorMessage(
                                ex.getMessage(),
                                "UNAUTHORIZED_ORGANIZATION_ACCESS",
                                HttpStatus.FORBIDDEN.value());

                return Mono.just(ResponseEntity
                                .status(HttpStatus.FORBIDDEN)
                                .body(new ResponseDto<>(false, errorMessage)));
        }

        @ExceptionHandler(UserServiceUnavailableException.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleUserServiceUnavailable(
                        UserServiceUnavailableException ex) {
                log.error("Servicio de usuarios no disponible: {}", ex.getMessage());

                ErrorMessage errorMessage = new ErrorMessage(
                                "Servicio de usuarios temporalmente no disponible",
                                "USER_SERVICE_UNAVAILABLE",
                                HttpStatus.SERVICE_UNAVAILABLE.value());

                return Mono.just(ResponseEntity
                                .status(HttpStatus.SERVICE_UNAVAILABLE)
                                .body(new ResponseDto<>(false, errorMessage)));
        }

        @ExceptionHandler(NoResourceFoundException.class)
        public Mono<ResponseEntity<String>> handleResourceNotFound(NoResourceFoundException ex) {
                // No logueamos estos errores para evitar spam en los logs
                // Típicamente ocurre con favicon.ico, robots.txt, etc.
                return Mono.just(ResponseEntity.notFound().build());
        }

        @ExceptionHandler(AccessDeniedException.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleAccessDenied(AccessDeniedException ex) {
                log.warn("Acceso denegado: {}", ex.getMessage());

                ErrorMessage errorMessage = new ErrorMessage(
                                "Acceso no autorizado. Se requiere rol ADMIN para acceder a este recurso",
                                "UNAUTHORIZED_ACCESS",
                                HttpStatus.UNAUTHORIZED.value());

                return Mono.just(ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(new ResponseDto<>(false, errorMessage)));
        }

        @ExceptionHandler(AuthenticationException.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleAuthentication(AuthenticationException ex) {
                log.warn("Error de autenticación: {}", ex.getMessage());

                ErrorMessage errorMessage = new ErrorMessage(
                                "Token de autenticación inválido o expirado",
                                "INVALID_TOKEN",
                                HttpStatus.UNAUTHORIZED.value());

                return Mono.just(ResponseEntity
                                .status(HttpStatus.UNAUTHORIZED)
                                .body(new ResponseDto<>(false, errorMessage)));
        }

        @ExceptionHandler(Exception.class)
        public Mono<ResponseEntity<ResponseDto<Object>>> handleGlobalException(Exception ex) {
                log.error("Unexpected exception: {}", ex.getMessage(), ex);

                ErrorMessage errorMessage = new ErrorMessage(
                                "Error interno del servidor",
                                "INTERNAL_SERVER_ERROR",
                                HttpStatus.INTERNAL_SERVER_ERROR.value());

                return Mono.just(ResponseEntity
                                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(new ResponseDto<>(false, errorMessage)));
        }
}
