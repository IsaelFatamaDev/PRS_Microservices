package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO wrapper estándar para respuestas de la API
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {
    private boolean status;
    private String message;
    private T data;
    private ErrorMessage error;
    private java.time.LocalDateTime timestamp;

    public ResponseDto(boolean status, T data) {
        this.status = status;
        this.data = data;
        this.error = null;
        this.timestamp = java.time.LocalDateTime.now();
    }

    public ResponseDto(boolean status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
        this.error = null;
        this.timestamp = java.time.LocalDateTime.now();
    }

    public ResponseDto(boolean status, ErrorMessage error) {
        this.status = status;
        this.data = null;
        this.error = error;
        this.timestamp = java.time.LocalDateTime.now();
    }
}
