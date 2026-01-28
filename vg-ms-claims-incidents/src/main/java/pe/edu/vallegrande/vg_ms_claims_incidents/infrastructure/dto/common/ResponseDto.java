package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Wrapper estándar para todas las respuestas de la API.
 * Proporciona una estructura consistente para las respuestas.
 * 
 * @param <T> Tipo de datos que se retorna
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Respuesta estándar del API")
public class ResponseDto<T> {
    
    @Schema(description = "Indica si la operación fue exitosa", example = "true")
    private boolean success;
    
    @Schema(description = "Mensaje descriptivo de la operación", example = "Operación exitosa")
    private String message;
    
    @Schema(description = "Datos de la respuesta")
    private T data;
    
    @Schema(description = "Timestamp de la respuesta")
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    @Schema(description = "Código de estado HTTP", example = "200")
    private int statusCode;
    
    @Schema(description = "Ruta del endpoint", example = "/api/v1/incidents")
    private String path;
    
    @Schema(description = "Detalles de errores de validación")
    private Object errors;
    
    /**
     * Constructor para respuestas exitosas
     */
    public static <T> ResponseDto<T> success(T data, String message) {
        return ResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .statusCode(200)
                .build();
    }
    
    /**
     * Constructor para respuestas exitosas sin mensaje
     */
    public static <T> ResponseDto<T> success(T data) {
        return success(data, "Operación exitosa");
    }
    
    /**
     * Constructor para respuestas de creación exitosa (201)
     */
    public static <T> ResponseDto<T> created(T data, String message) {
        return ResponseDto.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(Instant.now())
                .statusCode(201)
                .build();
    }
    
    /**
     * Constructor para respuestas de error
     */
    public static <T> ResponseDto<T> error(String message, int statusCode) {
        return ResponseDto.<T>builder()
                .success(false)
                .message(message)
                .timestamp(Instant.now())
                .statusCode(statusCode)
                .build();
    }
    
    /**
     * Constructor para respuestas de error con código 400
     */
    public static <T> ResponseDto<T> error(String message) {
        return error(message, 400);
    }
    
    /**
     * Constructor para respuestas de error con validaciones
     */
    public static <T> ResponseDto<T> validationError(String message, Object errors) {
        return ResponseDto.<T>builder()
                .success(false)
                .message(message)
                .errors(errors)
                .timestamp(Instant.now())
                .statusCode(400)
                .build();
    }
    
    /**
     * Constructor para errores 401 - No autorizado
     */
    public static <T> ResponseDto<T> unauthorized(String message) {
        return error(message, 401);
    }
    
    /**
     * Constructor para errores 403 - Prohibido
     */
    public static <T> ResponseDto<T> forbidden(String message) {
        return error(message, 403);
    }
    
    /**
     * Constructor para errores 404 - No encontrado
     */
    public static <T> ResponseDto<T> notFound(String message) {
        return error(message, 404);
    }
    
    /**
     * Constructor para errores 409 - Conflicto
     */
    public static <T> ResponseDto<T> conflict(String message) {
        return error(message, 409);
    }
    
    /**
     * Constructor para errores 500 - Error interno
     */
    public static <T> ResponseDto<T> serverError(String message) {
        return error(message, 500);
    }
}

