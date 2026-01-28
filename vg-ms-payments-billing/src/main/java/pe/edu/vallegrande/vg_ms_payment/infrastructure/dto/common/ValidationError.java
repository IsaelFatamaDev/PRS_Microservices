package pe.edu.vallegrande.vg_ms_payment.infrastructure.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para errores de validación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    
    private String field;
    private String message;
    private Object rejectedValue;
    
    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }
}