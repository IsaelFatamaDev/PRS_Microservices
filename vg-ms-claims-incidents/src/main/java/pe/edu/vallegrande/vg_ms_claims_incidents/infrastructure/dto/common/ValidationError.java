package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Representa un error de validación individual.
 * Se utiliza para comunicar errores de validación de campos específicos.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationError {
    
    /**
     * Nombre del campo que falló la validación
     */
    private String field;
    
    /**
     * Valor rechazado
     */
    private Object rejectedValue;
    
    /**
     * Mensaje de error de validación
     */
    private String message;
    
    /**
     * Constructor simplificado sin valor rechazado
     */
    public ValidationError(String field, String message) {
        this.field = field;
        this.message = message;
    }
}
