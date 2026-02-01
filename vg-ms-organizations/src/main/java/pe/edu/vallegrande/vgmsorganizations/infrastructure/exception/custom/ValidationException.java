package pe.edu.vallegrande.vgmsorganizations.infrastructure.exception.custom;

import pe.edu.vallegrande.vgmsorganizations.infrastructure.dto.common.ValidationError;

import java.util.ArrayList;
import java.util.List;

/**
 * Excepción lanzada cuando hay errores de validación
 */
public class ValidationException extends RuntimeException {
    
    private final List<ValidationError> errors;
    
    public ValidationException(String message) {
        super(message);
        this.errors = new ArrayList<>();
    }
    
    public ValidationException(String message, List<ValidationError> errors) {
        super(message);
        this.errors = errors != null ? errors : new ArrayList<>();
    }
    
    public ValidationException(String field, String message, Object rejectedValue) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(ValidationError.builder()
                .field(field)
                .message(message)
                .rejectedValue(rejectedValue)
                .build());
    }
    
    public List<ValidationError> getErrors() {
        return errors;
    }
}
