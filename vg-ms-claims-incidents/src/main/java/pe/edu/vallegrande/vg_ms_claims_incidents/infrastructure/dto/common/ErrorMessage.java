package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Representa un mensaje de error estructurado.
 * Se utiliza para comunicar errores de forma consistente.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorMessage {
    
    /**
     * Timestamp del error
     */
    private Instant timestamp;
    
    /**
     * Código de estado HTTP
     */
    private int status;
    
    /**
     * Tipo de error (por ejemplo: "VALIDATION_ERROR", "NOT_FOUND", etc.)
     */
    private String error;
    
    /**
     * Mensaje descriptivo del error
     */
    private String message;
    
    /**
     * Ruta de la petición que causó el error
     */
    private String path;
    
    /**
     * Constructor simplificado
     */
    public ErrorMessage(int status, String error, String message, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }
}
