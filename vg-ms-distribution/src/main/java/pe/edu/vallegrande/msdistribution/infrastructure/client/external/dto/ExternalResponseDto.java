package pe.edu.vallegrande.msdistribution.infrastructure.client.external.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.vallegrande.msdistribution.infrastructure.dto.common.ErrorMessage;

import java.time.Instant;

/**
 * DTO genérico para respuestas de servicios externos.
 * Mantiene compatibilidad con la estructura ResponseDto.
 * 
 * @param <T> Tipo de dato de la respuesta.
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExternalResponseDto<T> {

    /** Indica si la operación fue exitosa. */
    private boolean status;

    /** Datos de la respuesta. */
    private T data;

    /** Objeto de error si existe. */
    private ErrorMessage error;

    /** Marca de tiempo de la respuesta. */
    private Instant timestamp;
}
