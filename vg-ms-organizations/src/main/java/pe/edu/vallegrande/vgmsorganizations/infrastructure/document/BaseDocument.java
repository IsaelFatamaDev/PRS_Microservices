package pe.edu.vallegrande.vgmsorganizations.infrastructure.document;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Documento base con auditoría para MongoDB
 * Todos los documentos deben extender de esta clase
 */
@Data
public abstract class BaseDocument {
    
    @CreatedDate
    private Instant createdAt;
    
    @LastModifiedDate
    private Instant updatedAt;
}
