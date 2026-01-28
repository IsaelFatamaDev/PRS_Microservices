package pe.edu.vallegrande.msdistribution.infrastructure.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO para representar errores de validación de campos.
 * Utilizado cuando falla la validación de @Valid o @Validated.
 * 
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationError {

    /** Nombre del campo que falló la validación. */
    private String field;

    /** Mensaje de error de validación. */
    private String message;

    /** Valor que causó el rechazo (opcional). */
    private Object rejectedValue;

    /** Mapa de errores adicionales si es complejo. */
    private Map<String, String> errors;
}
