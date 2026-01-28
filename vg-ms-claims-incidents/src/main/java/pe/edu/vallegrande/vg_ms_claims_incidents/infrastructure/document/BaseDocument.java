package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.document;

import org.springframework.data.mongodb.core.mapping.Field;
import lombok.Data;

import java.time.Instant;

/**
 * Clase base para todos los documentos MongoDB.
 * Proporciona campos comunes de auditoría y estado de registro.
 */
@Data
public abstract class BaseDocument {
    
    @Field("created_at")
    private Instant createdAt;
    
    @Field("updated_at")
    private Instant updatedAt;
    
    @Field("record_status")
    private String recordStatus = "ACTIVE";
    
    /**
     * Método de callback antes de persistir el documento.
     * Establece la fecha de creación si es null.
     */
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        this.updatedAt = Instant.now();
    }
    
    /**
     * Método de callback antes de actualizar el documento.
     * Actualiza la fecha de modificación.
     */
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
