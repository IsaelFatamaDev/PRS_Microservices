package pe.edu.vallegrande.vg_ms_claims_incidents.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * DTO para transferencia de datos de Incidentes
 * Contiene validaciones y documentación OpenAPI
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Datos del incidente")
public class IncidentDTO {
    
    @Schema(description = "ID único del incidente", example = "507f1f77bcf86cd799439011")
    private String id;
    
    @NotBlank(message = "El ID de la organización es obligatorio")
    @Schema(description = "ID de la organización", example = "6896b2ecf3e398570ffd99d3", required = true)
    private String organizationId;
    
    @NotBlank(message = "El código del incidente es obligatorio")
    @Size(min = 3, max = 50, message = "El código debe tener entre 3 y 50 caracteres")
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "El código solo puede contener letras mayúsculas, números y guiones")
    @Schema(description = "Código único del incidente", example = "INC-2025-001", required = true)
    private String incidentCode;
    
    @NotBlank(message = "El tipo de incidente es obligatorio")
    @Schema(description = "ID del tipo de incidente", example = "507f1f77bcf86cd799439011", required = true)
    private String incidentTypeId;
    
    @Schema(description = "Categoría del incidente", example = "WATER_SUPPLY")
    private String incidentCategory;
    
    @Schema(description = "ID de la zona afectada", example = "507f1f77bcf86cd799439012")
    private String zoneId;
    
    @NotNull(message = "La fecha del incidente es obligatoria")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    @Schema(description = "Fecha y hora del incidente", example = "2025-01-15T10:30:00.000Z", required = true)
    private Date incidentDate;
    
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 200, message = "El título debe tener entre 5 y 200 caracteres")
    @Schema(description = "Título descriptivo del incidente", example = "Fuga de agua en tubería principal", required = true)
    private String title;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 10, max = 2000, message = "La descripción debe tener entre 10 y 2000 caracteres")
    @Schema(description = "Descripción detallada del incidente", example = "Se ha detectado una fuga importante...", required = true)
    private String description;
    
    @NotBlank(message = "La severidad es obligatoria")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", message = "La severidad debe ser: LOW, MEDIUM, HIGH o CRITICAL")
    @Schema(description = "Nivel de severidad", example = "HIGH", required = true, allowableValues = {"LOW", "MEDIUM", "HIGH", "CRITICAL"})
    private String severity;
    
    @NotBlank(message = "El estado es obligatorio")
    @Pattern(regexp = "^(REPORTED|ASSIGNED|IN_PROGRESS|RESOLVED|CLOSED)$", 
             message = "El estado debe ser: REPORTED, ASSIGNED, IN_PROGRESS, RESOLVED o CLOSED")
    @Schema(description = "Estado actual del incidente", example = "REPORTED", required = true, 
            allowableValues = {"REPORTED", "ASSIGNED", "IN_PROGRESS", "RESOLVED", "CLOSED"})
    private String status;
    
    @Min(value = 0, message = "El número de cajas afectadas no puede ser negativo")
    @Schema(description = "Número de cajas de agua afectadas", example = "15")
    private Integer affectedBoxesCount;
    
    @NotBlank(message = "El ID del usuario que reporta es obligatorio")
    @Schema(description = "ID del usuario que reportó el incidente", example = "507f1f77bcf86cd799439013", required = true)
    private String reportedByUserId;
    
    @Schema(description = "ID del usuario asignado para resolver", example = "507f1f77bcf86cd799439014")
    private String assignedToUserId;
    
    @Schema(description = "ID del usuario que resolvió el incidente", example = "507f1f77bcf86cd799439015")
    private String resolvedByUserId;
    
    @Schema(description = "Indica si el incidente está resuelto", example = "false")
    @Builder.Default
    private Boolean resolved = false;
    
    @Size(max = 1000, message = "Las notas de resolución no pueden exceder 1000 caracteres")
    @Schema(description = "Notas sobre la resolución del incidente", example = "Se reparó la tubería...")
    private String resolutionNotes;
    
    @Pattern(regexp = "^(ACTIVE|INACTIVE|DELETED)$", message = "El estado del registro debe ser: ACTIVE, INACTIVE o DELETED")
    @Schema(description = "Estado del registro", example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE", "DELETED"})
    @Builder.Default
    private String recordStatus = "ACTIVE";
}