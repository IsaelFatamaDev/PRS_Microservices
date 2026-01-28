package pe.edu.vallegrande.msdistribution.infrastructure.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Wrapper genérico para respuestas de la API.
 * Estandariza el formato de respuesta JSON para éxito o fallo.
 * 
 * @param <T> Tipo de datos contenidos en la respuesta.
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseDto<T> {

    /** Indica si la operación fue exitosa. */
    private boolean success;

    /** Mensaje descriptivo del resultado. */
    private String message;

    /** Datos de la respuesta (payload). */
    private T data;

    /** Marca de tiempo de la respuesta. */
    private Instant timestamp;

    /**
     * Constructor para respuestas simples.
     * Genera automáticamente el timestamp.
     * 
     * @param success Éxito de la operación.
     * @param message Mensaje.
     * @param data    Datos.
     */
    public ResponseDto(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }
}