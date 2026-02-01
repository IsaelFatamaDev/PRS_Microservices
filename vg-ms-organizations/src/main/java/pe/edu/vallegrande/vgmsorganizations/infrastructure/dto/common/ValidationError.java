package pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para errores de validación
 * Contiene detalles de campos que fallaron la validación
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    
    private String field;
    private String message;
    private Object rejectedValue;
    
    /**
     * Crea un error de validación simple
     */
    public static ValidationError of(String field, String message) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .build();
    }
    
    /**
     * Crea un error de validación con valor rechazado
     */
    public static ValidationError of(String field, String message, Object rejectedValue) {
        return ValidationError.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build();
    }
}
