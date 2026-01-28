package pe.edu.vallegrande.ms_water_quality.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Domain Model - DailyRecord
 * Representa un registro diario de calidad de agua en el dominio del negocio.
 * NO contiene anotaciones de persistencia (arquitectura hexagonal).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyRecord {

    private String id;

    private String organizationId;
    private String recordCode;
    private List<String> testingPointIds;
    private LocalDateTime recordDate;
    private Double level;
    private boolean acceptable;
    private boolean actionRequired;
    private String recordedByUserId;
    private String observations;
    private Double amount;
    private String recordType; // "CLORO" o "SULFATO"
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
}

    // private LocalDateTime nextChlorinationDate;

