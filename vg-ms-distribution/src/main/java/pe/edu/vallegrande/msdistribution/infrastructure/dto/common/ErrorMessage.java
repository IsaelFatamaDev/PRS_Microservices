package pe.edu.vallegrande.msdistribution.infrastructure.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para mensajes de error estandarizados.
 * Utilizado en respuestas de excepción.
 * 
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage {

    /** Código de error específico de la aplicación o HTTP. */
    private int errorCode;

    /** Mensaje de error legible para el usuario. */
    private String message;

    /** Detalles técnicos o adicionales del error. */
    private String details;
}