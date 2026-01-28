package pe.edu.vallegrande.msdistribution.infrastructure.document;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Documento base con campos de auditoría para MongoDB.
 * Todos los documentos persistentes deben extender de esta clase para heredar
 * ID y auditoría.
 * 
 * @version 1.0
 */
@Data
public abstract class BaseDocument {

    /** Identificador único del documento. */
    @Id
    private String id;

    /** Fecha y hora de creación (automático). */
    @CreatedDate
    private Instant createdAt;

    /** Fecha y hora de última modificación (automático). */
    @LastModifiedDate
    private Instant updatedAt;

    /** Usuario que creó el registro. */
    private String createdBy;

    /** Usuario que actualizó el registro. */
    private String updatedBy;

    /** Estado del registro (ACTIVE, INACTIVE, etc.). */
    private String status;
}
