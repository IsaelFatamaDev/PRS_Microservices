package pe.edu.vallegrande.ms_water_quality.infrastructure.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

/**
 * MongoDB Document - DailyRecord
 * Modelo de persistencia para registros diarios de calidad de agua.
 * Contiene anotaciones específicas de MongoDB.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "daily_records")
public class DailyRecordDocument {

    @Id
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
