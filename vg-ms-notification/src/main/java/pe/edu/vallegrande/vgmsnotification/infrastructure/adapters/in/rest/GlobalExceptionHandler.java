package pe.edu.vallegrande.vgmsnotification.infrastructure.adapters.in.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import pe.edu.vallegrande.vgmsnotification.application.dtos.shared.ApiResponse;
import pe.edu.vallegrande.vgmsnotification.application.dtos.shared.ErrorMessage;
import pe.edu.vallegrande.vgmsnotification.domain.exceptions.InvalidTemplateException;
import pe.edu.vallegrande.vgmsnotification.domain.exceptions.NotificationNotFoundException;
import pe.edu.vallegrande.vgmsnotification.domain.exceptions.SendNotificationException;
import pe.edu.vallegrande.vgmsnotification.domain.exceptions.TemplateNotFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorMessage>> handleNotificationNotFound(
            NotificationNotFoundException ex, ServerWebExchange exchange) {
        
        log.error("Notification not found: {}", ex.getMessage());
        
        ErrorMessage error = new ErrorMessage(
            "NOTIFICATION_NOT_FOUND",
            ex.getMessage(),
            exchange.getRequest().getPath().value(),
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(error.getMessage()));
    }
    
    @ExceptionHandler(TemplateNotFoundException.class)
    public ResponseEntity<ApiResponse<ErrorMessage>> handleTemplateNotFound(
            TemplateNotFoundException ex, ServerWebExchange exchange) {
        
        log.error("Template not found: {}", ex.getMessage());
        
        ErrorMessage error = new ErrorMessage(
            "TEMPLATE_NOT_FOUND",
            ex.getMessage(),
            exchange.getRequest().getPath().value(),
            HttpStatus.NOT_FOUND.value(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(error.getMessage()));
    }
    
    @ExceptionHandler(SendNotificationException.class)
    public ResponseEntity<ApiResponse<ErrorMessage>> handleSendNotificationError(
            SendNotificationException ex, ServerWebExchange exchange) {
        
        log.error("Failed to send notification: {}", ex.getMessage());
        
        ErrorMessage error = new ErrorMessage(
            "SEND_NOTIFICATION_FAILED",
            ex.getMessage(),
            exchange.getRequest().getPath().value(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(error.getMessage()));
    }
    
    @ExceptionHandler(InvalidTemplateException.class)
    public ResponseEntity<ApiResponse<ErrorMessage>> handleInvalidTemplate(
            InvalidTemplateException ex, ServerWebExchange exchange) {
        
        log.error("Invalid template: {}", ex.getMessage());
        
        ErrorMessage error = new ErrorMessage(
            "INVALID_TEMPLATE",
            ex.getMessage(),
            exchange.getRequest().getPath().value(),
            HttpStatus.BAD_REQUEST.value(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(error.getMessage()));
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<ErrorMessage>> handleGeneralException(
            Exception ex, ServerWebExchange exchange) {
        
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        
        ErrorMessage error = new ErrorMessage(
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred: " + ex.getMessage(),
            exchange.getRequest().getPath().value(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            LocalDateTime.now()
        );
        
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(error.getMessage()));
    }
}
